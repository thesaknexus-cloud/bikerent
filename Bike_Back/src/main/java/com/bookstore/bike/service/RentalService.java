package com.bookstore.bike.service;

import com.bookstore.bike.entity.*;
import com.bookstore.bike.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RentalService {
    private static final List<Enums.RentalStatus> OPEN = List.of(Enums.RentalStatus.SCHEDULED, Enums.RentalStatus.ACTIVE);
    private final RentalSessionRepository rentals;
    private final BikeRepository bikes;
    private final UserRepository users;
    private final Clock clock;
    private final long configuredFineRate;

    public RentalService(RentalSessionRepository rentals, BikeRepository bikes, UserRepository users, Clock clock,
                         @Value("${campusride.fine-rate-paise-per-minute:250}") long configuredFineRate) {
        if (configuredFineRate < 0) throw new IllegalArgumentException("Fine rate cannot be negative.");
        this.rentals = rentals; this.bikes = bikes; this.users = users; this.clock = clock; this.configuredFineRate = configuredFineRate;
    }

    public record CreateRental(Long bikeId, String renterEmail, String renterName, String renterPhone, Integer durationMinutes) {}
    public record RentalView(Long id, Long bikeId, String bikeName, String registrationNumber, String bikeModel,
                             Long ownerId, String ownerName, String ownerPhone, Long renterId, String renterName,
                             String renterEmail, String renterPhone, Enums.RentalStatus status, Integer durationMinutes,
                             Instant startTime, Instant expectedReturnTime, Instant endTime, long lateSeconds,
                             long fineRatePaisePerMinute, long estimatedFinePaise, Long finalLateSeconds,
                             Long finalFinePaise, String invitationCode, Instant serverTime) {}

    @Transactional
    public RentalView create(Long ownerId, CreateRental input) {
        if (input == null || input.bikeId() == null || input.durationMinutes() == null ||
                input.durationMinutes() < 1 || input.durationMinutes() > 43_200 ||
                input.renterEmail() == null || !input.renterEmail().matches("(?i)^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a bike, valid renter email, and duration from 1 minute to 30 days.");
        }
        Bike bike = bikes.lockById(input.bikeId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bike not found."));
        if (!bike.getOwner().getId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a bike's owner can create its rental.");
        if (rentals.existsByBikeIdAndStatusIn(bike.getId(), OPEN)) throw new ResponseStatusException(HttpStatus.CONFLICT, "This bike already has an open rental.");

        User owner = users.findById(ownerId).orElseThrow();
        String email = input.renterEmail().trim().toLowerCase();
        User renter = users.findByEmail(email).orElse(null);
        if (renter != null && renter.getId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot rent to your own account.");

        RentalSession rental = new RentalSession();
        rental.setBike(bike); rental.setOwner(owner); rental.setRenter(renter);
        rental.setRenterEmail(email); rental.setRenterName(clean(input.renterName())); rental.setRenterPhone(clean(input.renterPhone()));
        rental.setInvitationCode(UUID.randomUUID().toString()); rental.setAgreedDurationMinutes(input.durationMinutes());
        rental.setFineRatePaisePerMinute(configuredFineRate);
        return view(rentals.save(rental), ownerId);
    }

    @Transactional(readOnly = true)
    public List<RentalView> mine(Long userId) {
        Instant now = clock.instant();
        return rentals.findByOwnerIdOrRenterIdOrderByCreatedAtDesc(userId, userId).stream().map(r -> viewAt(r, userId, now)).toList();
    }

    @Transactional(readOnly = true)
    public RentalView get(Long userId, Long id) {
        RentalSession rental = rentals.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found."));
        authorizeViewer(rental, userId);
        return view(rental, userId);
    }

    @Transactional
    public RentalView join(Long userId, String code) {
        RentalSession rental = rentals.findByInvitationCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation link is invalid or has already been used."));
        User renter = users.findById(userId).orElseThrow();
        if (!rental.getRenterEmail().equalsIgnoreCase(renter.getEmail())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sign in with the email address this invitation was sent to.");
        if (rental.getRenter() != null && !rental.getRenter().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "This invitation is already linked to another account.");
        if (rental.getStatus() != Enums.RentalStatus.SCHEDULED) throw new ResponseStatusException(HttpStatus.CONFLICT, "This rental has already started.");
        rental.setRenter(renter);
        return view(rental, userId);
    }

    @Transactional
    public RentalView start(Long ownerId, Long id) {
        RentalSession rental = locked(id);
        authorizeOwner(rental, ownerId);
        if (rental.getStatus() != Enums.RentalStatus.SCHEDULED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a scheduled rental can be started.");
        if (rental.getRenter() == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "The renter must accept the invitation before the handoff.");
        Instant now = clock.instant();
        rental.setStartTime(now);
        rental.setExpectedReturnTime(now.plusSeconds(rental.getAgreedDurationMinutes() * 60L));
        rental.setStatus(Enums.RentalStatus.ACTIVE);
        return view(rental, ownerId);
    }

    @Transactional
    public RentalView end(Long ownerId, Long id) {
        RentalSession rental = locked(id);
        authorizeOwner(rental, ownerId);
        if (rental.getStatus() != Enums.RentalStatus.ACTIVE) throw new ResponseStatusException(HttpStatus.CONFLICT, "This rental is already completed or has not started.");
        Instant now = clock.instant();
        long lateSeconds = FineCalculator.lateSeconds(rental.getExpectedReturnTime(), now);
        rental.setEndTime(now); rental.setFinalLateSeconds(lateSeconds);
        rental.setFinalFinePaise(FineCalculator.finePaise(lateSeconds, rental.getFineRatePaisePerMinute()));
        rental.setStatus(Enums.RentalStatus.COMPLETED);
        return view(rental, ownerId);
    }

    private RentalSession locked(Long id) {
        return rentals.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found."));
    }
    private void authorizeOwner(RentalSession rental, Long userId) {
        if (!rental.getOwner().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner who created this rental can control it.");
    }
    private void authorizeViewer(RentalSession rental, Long userId) {
        if (!rental.getOwner().getId().equals(userId) && (rental.getRenter() == null || !rental.getRenter().getId().equals(userId)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This rental is private to its owner and renter.");
    }
    private RentalView view(RentalSession rental, Long viewerId) { return viewAt(rental, viewerId, clock.instant()); }
    private RentalView viewAt(RentalSession r, Long viewerId, Instant now) {
        boolean ownerView = r.getOwner().getId().equals(viewerId);
        long late = r.getStatus() == Enums.RentalStatus.ACTIVE ? FineCalculator.lateSeconds(r.getExpectedReturnTime(), now) :
                r.getStatus() == Enums.RentalStatus.COMPLETED ? r.getFinalLateSeconds() : 0;
        long fine = r.getStatus() == Enums.RentalStatus.COMPLETED ? r.getFinalFinePaise() : FineCalculator.finePaise(late, r.getFineRatePaisePerMinute());
        return new RentalView(r.getId(), r.getBike().getId(), r.getBike().getName(), r.getBike().getRegistrationNumber(), r.getBike().getModel(),
                r.getOwner().getId(), r.getOwner().getFullName(), r.getOwner().getPhone(), r.getRenter() == null ? null : r.getRenter().getId(),
                r.getRenterName() != null ? r.getRenterName() : r.getRenter() == null ? null : r.getRenter().getFullName(),
                r.getRenterEmail(), r.getRenterPhone(), r.getStatus(), r.getAgreedDurationMinutes(), r.getStartTime(), r.getExpectedReturnTime(),
                r.getEndTime(), late, r.getFineRatePaisePerMinute(), fine, r.getFinalLateSeconds(), r.getFinalFinePaise(),
                ownerView && r.getStatus() == Enums.RentalStatus.SCHEDULED ? r.getInvitationCode() : null, now);
    }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}

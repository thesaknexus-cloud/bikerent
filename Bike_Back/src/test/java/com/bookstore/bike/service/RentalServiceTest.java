package com.bookstore.bike.service;

import com.bookstore.bike.entity.Bike;
import com.bookstore.bike.entity.Enums;
import com.bookstore.bike.entity.RentalSession;
import com.bookstore.bike.entity.User;
import com.bookstore.bike.repo.BikeRepository;
import com.bookstore.bike.repo.RentalSessionRepository;
import com.bookstore.bike.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalServiceTest {
    private RentalSessionRepository rentals;
    private BikeRepository bikes;
    private UserRepository users;
    private RentalService service;
    private User owner;
    private User renter;
    private RentalSession active;
    private final Instant due = Instant.parse("2026-06-01T12:00:00Z");

    @BeforeEach void setUp() {
        rentals = mock(RentalSessionRepository.class); bikes = mock(BikeRepository.class); users = mock(UserRepository.class);
        owner = user(11L, "owner@example.test"); renter = user(22L, "renter@example.test");
        Bike bike = new Bike(); bike.setOwner(owner); bike.setName("City Commuter"); bike.setModel("Road 125"); bike.setRegistrationNumber("KA-01-AB-1234"); ReflectionTestUtils.setField(bike, "id", 3L);
        active = new RentalSession(); active.setBike(bike); active.setOwner(owner); active.setRenter(renter); active.setRenterEmail(renter.getEmail()); active.setStatus(Enums.RentalStatus.ACTIVE); active.setExpectedReturnTime(due);
        ReflectionTestUtils.setField(active, "id", 7L);
        service = new RentalService(rentals, bikes, users, Clock.fixed(due.plusSeconds(30), ZoneOffset.UTC), 250);
        when(rentals.lockById(7L)).thenReturn(Optional.of(active));
    }

    @Test void renterCannotEndOwnersRental() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.end(22L, 7L));
        assertEquals(403, error.getStatusCode().value());
        assertEquals(Enums.RentalStatus.ACTIVE, active.getStatus());
        assertNull(active.getEndTime());
    }

    @Test void onlyOwnerCanStartAndServerSetsStartAndDueTimes() {
        Bike bike = new Bike(); bike.setOwner(owner); bike.setName("City Commuter"); ReflectionTestUtils.setField(bike, "id", 3L);
        RentalSession scheduled = new RentalSession(); scheduled.setBike(bike); scheduled.setOwner(owner); scheduled.setRenter(renter);
        scheduled.setRenterEmail(renter.getEmail()); scheduled.setAgreedDurationMinutes(15); scheduled.setStatus(Enums.RentalStatus.SCHEDULED);
        ReflectionTestUtils.setField(scheduled, "id", 8L);
        when(rentals.lockById(8L)).thenReturn(Optional.of(scheduled));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.start(22L, 8L)).getStatusCode().value());
        RentalService.RentalView started = service.start(11L, 8L);
        assertEquals(due.plusSeconds(30), started.startTime());
        assertEquals(due.plusSeconds(30 + 15 * 60L), started.expectedReturnTime());
        assertEquals(Enums.RentalStatus.ACTIVE, started.status());
    }

    @Test void endFreezesServerTimeAndProportionalFineAndRejectsDuplicateEnd() {
        RentalService.RentalView completed = service.end(11L, 7L);
        assertEquals(Enums.RentalStatus.COMPLETED, active.getStatus());
        assertEquals(due.plusSeconds(30), completed.endTime());
        assertEquals(30, completed.finalLateSeconds());
        assertEquals(125, completed.finalFinePaise());
        ResponseStatusException repeated = assertThrows(ResponseStatusException.class, () -> service.end(11L, 7L));
        assertEquals(409, repeated.getStatusCode().value());
        assertEquals(due.plusSeconds(30), active.getEndTime());
    }

    @Test void rejectsDuplicateOpenBikeRental() {
        Bike bike = new Bike(); bike.setOwner(owner); ReflectionTestUtils.setField(bike, "id", 3L);
        when(bikes.lockById(3L)).thenReturn(Optional.of(bike));
        when(rentals.existsByBikeIdAndStatusIn(eq(3L), anyCollection())).thenReturn(true);
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () ->
                service.create(11L, new RentalService.CreateRental(3L, "renter@example.test", "Renter", null, 60)));
        assertEquals(409, error.getStatusCode().value());
        verify(rentals, never()).save(any());
    }

    @Test void rejectsInvalidRentalDurationBeforeTouchingDatabase() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () ->
                service.create(11L, new RentalService.CreateRental(3L, "renter@example.test", "Renter", null, 0)));
        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(bikes);
    }

    @Test void inviteCanOnlyBeClaimedByAccountWithInvitedEmail() {
        Bike bike = new Bike(); bike.setOwner(owner); bike.setName("City Commuter"); ReflectionTestUtils.setField(bike, "id", 3L);
        when(bikes.lockById(3L)).thenReturn(Optional.of(bike));
        when(rentals.existsByBikeIdAndStatusIn(eq(3L), anyCollection())).thenReturn(false);
        when(users.findById(11L)).thenReturn(Optional.of(owner));
        when(users.findByEmail("renter@example.test")).thenReturn(Optional.empty());
        when(rentals.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RentalService.RentalView invite = service.create(11L, new RentalService.CreateRental(3L, renter.getEmail(), "Renter", null, 15));
        when(users.findById(22L)).thenReturn(Optional.of(renter));
        when(rentals.findByInvitationCode(invite.invitationCode())).thenReturn(Optional.of(inviteEntity(bike, invite.invitationCode(), owner, renter.getEmail())));
        User stranger = user(33L,"stranger@example.test"); when(users.findById(33L)).thenReturn(Optional.of(stranger));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.join(33L, invite.invitationCode())).getStatusCode().value());
        RentalService.RentalView joined = service.join(22L, invite.invitationCode());
        assertEquals(22L, joined.renterId());
    }

    private RentalSession inviteEntity(Bike bike, String code, User owner, String email) {
        RentalSession rental = new RentalSession(); rental.setBike(bike); rental.setOwner(owner); rental.setRenterEmail(email);
        rental.setInvitationCode(code); rental.setAgreedDurationMinutes(15); return rental;
    }

    private User user(Long id, String email) {
        User user = new User(); user.setEmail(email); user.setFullName(email); ReflectionTestUtils.setField(user, "id", id); return user;
    }
}

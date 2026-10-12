package com.bookstore.bike.controller;

import com.bookstore.bike.entity.User;
import com.bookstore.bike.repo.UserRepository;
import com.bookstore.bike.service.RentalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {
    private final RentalService rentals;
    private final UserRepository users;

    public RentalController(RentalService rentals, UserRepository users) { this.rentals = rentals; this.users = users; }

    public record CreateRequest(@NotNull Long bikeId, @Email @NotBlank String renterEmail,
                                String renterName, String renterPhone,
                                @NotNull @Min(1) @Max(43200) Integer durationMinutes) {}
    public record JoinRequest(@NotBlank String code) {}

    @GetMapping("/mine") public List<RentalService.RentalView> mine() { return rentals.mine(me().getId()); }
    @GetMapping("/{id}") public RentalService.RentalView get(@PathVariable Long id) { return rentals.get(me().getId(), id); }
    @PostMapping public RentalService.RentalView create(@Valid @RequestBody CreateRequest request) {
        return rentals.create(me().getId(), new RentalService.CreateRental(request.bikeId(), request.renterEmail(), request.renterName(), request.renterPhone(), request.durationMinutes()));
    }
    @PostMapping("/join") public RentalService.RentalView join(@Valid @RequestBody JoinRequest request) { return rentals.join(me().getId(), request.code()); }
    @PostMapping("/{id}/start") public RentalService.RentalView start(@PathVariable Long id) { return rentals.start(me().getId(), id); }
    @PostMapping("/{id}/end") public RentalService.RentalView end(@PathVariable Long id) { return rentals.end(me().getId(), id); }

    private User me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email).orElseThrow();
    }
}

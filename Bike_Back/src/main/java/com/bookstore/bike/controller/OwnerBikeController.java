package com.bookstore.bike.controller;

import com.bookstore.bike.entity.Bike;
import com.bookstore.bike.repo.BikeRepository;
import com.bookstore.bike.repo.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my/bikes")
public class OwnerBikeController {
    private final BikeRepository bikes;
    private final UserRepository users;
    public OwnerBikeController(BikeRepository bikes, UserRepository users) { this.bikes = bikes; this.users = users; }
    @GetMapping public List<Bike> mine() {
        Long ownerId = users.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow().getId();
        return bikes.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }
}

package com.bookstore.bike.entity;

public final class Enums {
    private Enums() {}
    public enum Role { STUDENT, ADMIN }
    public enum VerificationStatus { PENDING, VERIFIED, REJECTED }
    public enum BikeType { SCOOTER, MOTORCYCLE, ELECTRIC_SCOOTER, ELECTRIC_BIKE, BICYCLE, OTHER }
    public enum FuelType { PETROL, ELECTRIC, NONE, HYBRID }
    public enum AvailabilityStatus { AVAILABLE, BOOKED, MAINTENANCE, INACTIVE }
    public enum BookingStatus { PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED, REJECTED }
    public enum PaymentStatus { PENDING, PAID, FAILED, REFUNDED }
    public enum AssistanceStatus { OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CANCELLED }
    public enum RentalStatus { SCHEDULED, ACTIVE, COMPLETED }
}

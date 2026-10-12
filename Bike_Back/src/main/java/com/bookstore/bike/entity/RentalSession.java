package com.bookstore.bike.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rental_sessions", indexes = {
        @Index(name = "idx_rental_owner_created", columnList = "owner_id,created_at"),
        @Index(name = "idx_rental_renter_created", columnList = "renter_id,created_at"),
        @Index(name = "idx_rental_bike_status", columnList = "bike_id,status")
})
public class RentalSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "bike_id", nullable = false)
    private Bike bike;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "renter_id")
    private User renter;

    @Column(nullable = false) private String renterEmail;
    private String renterName;
    private String renterPhone;
    @Column(nullable = false, unique = true) private String invitationCode;
    @Column(nullable = false) private int agreedDurationMinutes;
    @Column(nullable = false) private long fineRatePaisePerMinute = 250;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Enums.RentalStatus status = Enums.RentalStatus.SCHEDULED;
    private Instant startTime;
    private Instant expectedReturnTime;
    private Instant endTime;
    private Long finalLateSeconds;
    private Long finalFinePaise;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant updatedAt = Instant.now();

    @PreUpdate void updateTimestamp() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Bike getBike() { return bike; }
    public void setBike(Bike bike) { this.bike = bike; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public User getRenter() { return renter; }
    public void setRenter(User renter) { this.renter = renter; }
    public String getRenterEmail() { return renterEmail; }
    public void setRenterEmail(String renterEmail) { this.renterEmail = renterEmail; }
    public String getRenterName() { return renterName; }
    public void setRenterName(String renterName) { this.renterName = renterName; }
    public String getRenterPhone() { return renterPhone; }
    public void setRenterPhone(String renterPhone) { this.renterPhone = renterPhone; }
    public String getInvitationCode() { return invitationCode; }
    public void setInvitationCode(String invitationCode) { this.invitationCode = invitationCode; }
    public int getAgreedDurationMinutes() { return agreedDurationMinutes; }
    public void setAgreedDurationMinutes(int agreedDurationMinutes) { this.agreedDurationMinutes = agreedDurationMinutes; }
    public long getFineRatePaisePerMinute() { return fineRatePaisePerMinute; }
    public Enums.RentalStatus getStatus() { return status; }
    public void setStatus(Enums.RentalStatus status) { this.status = status; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getExpectedReturnTime() { return expectedReturnTime; }
    public void setExpectedReturnTime(Instant expectedReturnTime) { this.expectedReturnTime = expectedReturnTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public Long getFinalLateSeconds() { return finalLateSeconds; }
    public void setFinalLateSeconds(Long finalLateSeconds) { this.finalLateSeconds = finalLateSeconds; }
    public Long getFinalFinePaise() { return finalFinePaise; }
    public void setFinalFinePaise(Long finalFinePaise) { this.finalFinePaise = finalFinePaise; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setFineRatePaisePerMinute(long rate) { fineRatePaisePerMinute = rate; }
}

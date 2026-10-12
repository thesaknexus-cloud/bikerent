package com.bookstore.bike.repo;

import com.bookstore.bike.entity.Enums;
import com.bookstore.bike.entity.RentalSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RentalSessionRepository extends JpaRepository<RentalSession, Long> {
    List<RentalSession> findByOwnerIdOrRenterIdOrderByCreatedAtDesc(Long ownerId, Long renterId);
    boolean existsByBikeIdAndStatusIn(Long bikeId, Collection<Enums.RentalStatus> statuses);
    Optional<RentalSession> findByInvitationCode(String invitationCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RentalSession r where r.id = :id")
    Optional<RentalSession> lockById(@Param("id") Long id);
}

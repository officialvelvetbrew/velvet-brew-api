package com.cafe.velvetbrew.repository;

import com.cafe.velvetbrew.entity.Offer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Optional<Offer> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Offer o WHERE UPPER(o.code) = UPPER(:code)")
    Optional<Offer> findByCodeIgnoreCaseForUpdate(@Param("code") String code);

    @Query("""
            SELECT o FROM Offer o
            WHERE o.active = true
              AND (o.startsAt IS NULL OR o.startsAt <= :now)
              AND (o.endsAt IS NULL OR o.endsAt >= :now)
            ORDER BY o.createdAt DESC
            """)
    List<Offer> findCurrentlyActive(@Param("now") LocalDateTime now);
}

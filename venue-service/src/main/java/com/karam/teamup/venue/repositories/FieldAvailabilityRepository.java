package com.karam.teamup.venue.repositories;

import com.karam.teamup.venue.entities.FieldAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FieldAvailabilityRepository extends JpaRepository<FieldAvailability, UUID> {
    boolean existsByField_FieldIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
        UUID fieldId, String status, LocalDateTime endTime, LocalDateTime startTime);

}

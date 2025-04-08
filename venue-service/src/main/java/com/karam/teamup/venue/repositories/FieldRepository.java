package com.karam.teamup.venue.repositories;

import com.karam.teamup.venue.entities.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FieldRepository extends JpaRepository<Field, UUID> {
        List<Field> findByVenue_VenueId(UUID venueId);

        @Query(
           "SELECT f FROM Field f WHERE f.venue.venueId = :venueId AND f.fieldId NOT IN (" +
           "SELECT fa.field.fieldId FROM FieldAvailability fa " +
           "WHERE fa.status <> 'CANCELLED' " +
           "AND fa.startTime < :endTime " +
           "AND fa.endTime > :startTime)"
        )
        List<Field> findAvailableFieldsInVenue(
                @Param("venueId") UUID venueId,
                @Param("startTime")LocalDateTime startTime,
                @Param("endTime")LocalDateTime endTime
                );
}

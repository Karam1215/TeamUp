package com.karam.teamup.venue.repositories;

import com.karam.teamup.venue.entities.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
    Optional<Venue> findVenueByName(String name);
}

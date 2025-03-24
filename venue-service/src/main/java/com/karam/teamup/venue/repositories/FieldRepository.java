package com.karam.teamup.venue.repositories;

import com.karam.teamup.venue.entities.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FieldRepository extends JpaRepository<Field, UUID> {
}

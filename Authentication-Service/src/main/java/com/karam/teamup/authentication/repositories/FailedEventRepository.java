package com.karam.teamup.authentication.repositories;

import com.karam.teamup.authentication.entities.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedEventRepository extends JpaRepository<FailedEvent, Long> {
}

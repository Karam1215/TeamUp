package com.karam.teamup.match.repositories;

import com.karam.teamup.match.entities.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRequestRepository extends JpaRepository<MatchRequest, UUID> {
        List<MatchRequest> findByStatus(String status);
}

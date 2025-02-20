package com.karam.teamup.authentication.repositories;

import com.karam.teamup.authentication.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findPlayerByUsername(String username);
    Optional<User> findPlayerByEmail(String email);

}

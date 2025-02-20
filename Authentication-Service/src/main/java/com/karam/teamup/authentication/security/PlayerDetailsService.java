package com.karam.teamup.authentication.security;

import com.karam.teamup.authentication.entities.User;
import com.karam.teamup.authentication.exception.UserNameNotFoundException;
import com.karam.teamup.authentication.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findPlayerByUsername(username).orElseThrow(() ->
                new UserNameNotFoundException("Account is inactive: " + username));
        return new PlayerPrincipal(user);
    }
}
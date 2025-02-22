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
public class UserDetailsServiceCust implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username).orElseThrow(() ->
                new UserNameNotFoundException("Account is inactive: " + username));
        return new UserPrincipal(user);
    }
}
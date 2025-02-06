package com.karam.teamup.player.security;

import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.exceptions.UserNameNotFoundException;
import com.karam.teamup.player.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerDetailsService implements UserDetailsService {

    private final PlayerRepository playerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Player player = playerRepository.findPlayerByUserName(username).orElseThrow(() ->
                new UserNameNotFoundException("Account is inactive: " + username));
        return new PlayerPrincipal(player);
    }
}
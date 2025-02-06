package com.karam.teamup.player.mappers;

import com.karam.teamup.player.DTO.PlayerProfileDTO;
import com.karam.teamup.player.entities.Player;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-06T06:04:42+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class PlayerProfileMapperImpl implements PlayerProfileMapper {

    @Override
    public void updatePlayerProfile(Player player, PlayerProfileDTO playerProfileDTO) {
        if ( playerProfileDTO == null ) {
            return;
        }

        if ( playerProfileDTO.userName() != null ) {
            player.setUserName( playerProfileDTO.userName() );
        }
        if ( playerProfileDTO.firstName() != null ) {
            player.setFirstName( playerProfileDTO.firstName() );
        }
        if ( playerProfileDTO.lastName() != null ) {
            player.setLastName( playerProfileDTO.lastName() );
        }
        if ( playerProfileDTO.birthDate() != null ) {
            player.setBirthDate( playerProfileDTO.birthDate() );
        }
        if ( playerProfileDTO.gender() != null ) {
            player.setGender( playerProfileDTO.gender() );
        }
        if ( playerProfileDTO.city() != null ) {
            player.setCity( playerProfileDTO.city() );
        }
        if ( playerProfileDTO.profilePicture() != null ) {
            player.setProfilePicture( playerProfileDTO.profilePicture() );
        }
        if ( playerProfileDTO.createdAt() != null ) {
            player.setCreatedAt( playerProfileDTO.createdAt() );
        }
        if ( playerProfileDTO.bio() != null ) {
            player.setBio( playerProfileDTO.bio() );
        }
        if ( playerProfileDTO.sport() != null ) {
            player.setSport( playerProfileDTO.sport() );
        }
    }

    @Override
    public PlayerProfileDTO toDTO(Player player) {
        if ( player == null ) {
            return null;
        }

        String userName = null;
        String firstName = null;
        String lastName = null;
        LocalDate birthDate = null;
        String gender = null;
        String city = null;
        String profilePicture = null;
        LocalDateTime createdAt = null;
        String bio = null;
        String sport = null;

        userName = player.getUserName();
        firstName = player.getFirstName();
        lastName = player.getLastName();
        birthDate = player.getBirthDate();
        gender = player.getGender();
        city = player.getCity();
        profilePicture = player.getProfilePicture();
        createdAt = player.getCreatedAt();
        bio = player.getBio();
        sport = player.getSport();

        PlayerProfileDTO playerProfileDTO = new PlayerProfileDTO( userName, firstName, lastName, birthDate, gender, city, profilePicture, createdAt, bio, sport );

        return playerProfileDTO;
    }
}

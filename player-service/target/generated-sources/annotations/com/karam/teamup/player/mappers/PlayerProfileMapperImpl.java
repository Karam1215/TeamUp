package com.karam.teamup.player.mappers;

import com.karam.teamup.player.DTO.PlayerProfileDTO;
import com.karam.teamup.player.DTO.UpdatePlayerProfileDTO;
import com.karam.teamup.player.entities.Player;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-06T17:48:22+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class PlayerProfileMapperImpl implements PlayerProfileMapper {

    @Override
    public void updatePlayerProfile(Player player, UpdatePlayerProfileDTO updatePlayerProfileDTO) {
        if ( updatePlayerProfileDTO == null ) {
            return;
        }

        if ( updatePlayerProfileDTO.firstName() != null ) {
            player.setFirstName( updatePlayerProfileDTO.firstName() );
        }
        if ( updatePlayerProfileDTO.lastName() != null ) {
            player.setLastName( updatePlayerProfileDTO.lastName() );
        }
        if ( updatePlayerProfileDTO.birthDate() != null ) {
            player.setBirthDate( updatePlayerProfileDTO.birthDate() );
        }
        if ( updatePlayerProfileDTO.gender() != null ) {
            player.setGender( updatePlayerProfileDTO.gender() );
        }
        if ( updatePlayerProfileDTO.city() != null ) {
            player.setCity( updatePlayerProfileDTO.city() );
        }
        if ( updatePlayerProfileDTO.bio() != null ) {
            player.setBio( updatePlayerProfileDTO.bio() );
        }
        if ( updatePlayerProfileDTO.sport() != null ) {
            player.setSport( updatePlayerProfileDTO.sport() );
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

package com.karam.teamup.player.mappers;


import com.karam.teamup.player.dto.PlayerProfileDTO;
import com.karam.teamup.player.dto.UpdatePlayerProfileDTO;
import com.karam.teamup.player.entities.Player;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PlayerProfileMapper {
    @Mapping(target = "playerId", source = "playerId")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "bio", source = "bio")
    @Mapping(target = "sport", source = "sport")
    void updatePlayerProfile(@MappingTarget Player player, UpdatePlayerProfileDTO updatePlayerProfileDTO);

    PlayerProfileDTO toDTO(Player player);
}

package com.karam.teamup.player.mappers;


import com.karam.teamup.player.DTO.PlayerProfileDTO;
import com.karam.teamup.player.entities.Player;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PlayerProfileMapper {
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "profilePicture", source = "profilePicture")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "bio", source = "bio")
    @Mapping(target = "sport", source = "sport")
    void updatePlayerProfile(@MappingTarget Player player, PlayerProfileDTO playerProfileDTO);

    PlayerProfileDTO toDTO(Player player);

}

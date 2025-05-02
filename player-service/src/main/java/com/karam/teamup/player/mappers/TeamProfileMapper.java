package com.karam.teamup.player.mappers;

import com.karam.teamup.player.dto.UpdateTeamsProfileDTO;
import com.karam.teamup.player.entities.Team;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TeamProfileMapper {

    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "ranking", source = "ranking")
    @Mapping(target = "preferredStartTime", source = "preferredStartTime")
    @Mapping(target = "preferredEndTime", source = "preferredEndTime")
    @Mapping(target = "preferredVenues", source = "preferredVenues")
    void updateTeamsProfile(@MappingTarget Team team, UpdateTeamsProfileDTO updateTeamsProfileDTO);

}

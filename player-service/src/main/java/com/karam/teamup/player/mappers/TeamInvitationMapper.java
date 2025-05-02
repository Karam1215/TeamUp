package com.karam.teamup.player.mappers;


import com.karam.teamup.player.dto.TeamInvitationDTO;
import com.karam.teamup.player.entities.Team;
import com.karam.teamup.player.entities.TeamInvitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TeamInvitationMapper {

    @Mapping(source = "team.name", target = "teamName")
    TeamInvitationDTO toDTO(TeamInvitation teamInvitation, Team team);

}

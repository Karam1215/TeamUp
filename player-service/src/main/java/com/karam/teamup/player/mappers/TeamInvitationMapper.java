package com.karam.teamup.player.mappers;


import com.karam.teamup.player.dto.PlayerProfileDTO;
import com.karam.teamup.player.dto.TeamInvitationDTO;
import com.karam.teamup.player.dto.UpdatePlayerProfileDTO;
import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.entities.Team;
import com.karam.teamup.player.entities.TeamInvitation;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TeamInvitationMapper {

    @Mapping(source = "team.name", target = "teamName")
    TeamInvitationDTO toDTO(TeamInvitation teamInvitation, Team team);

}

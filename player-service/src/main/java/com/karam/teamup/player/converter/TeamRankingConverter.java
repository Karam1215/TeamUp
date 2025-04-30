package com.karam.teamup.player.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.karam.teamup.player.enums.TeamRanking;

@Converter(autoApply = true)
public class TeamRankingConverter implements AttributeConverter<TeamRanking, String> {
    @Override
    public String convertToDatabaseColumn(TeamRanking ranking) {
        return ranking.name().toLowerCase();
    }

    @Override
    public TeamRanking convertToEntityAttribute(String dbData) {
        return TeamRanking.valueOf(dbData.toUpperCase());
    }
}

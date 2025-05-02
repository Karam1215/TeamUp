package com.karam.teamup.player.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Converter
public class JsonbUUIDListConverter implements AttributeConverter<List<UUID>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<UUID> preferredVenues) {
        try {
            if (preferredVenues != null) {
                return objectMapper.writeValueAsString(preferredVenues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UUID> convertToEntityAttribute(String dbData) {
        try {
            if (dbData != null) {
                return objectMapper.readValue(dbData, ArrayList.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.karam.teamup.venue.mappers;

import com.karam.teamup.venue.dto.UpdateVenueProfileDTO;
import com.karam.teamup.venue.entities.Venue;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VenueProfileMapper {

    @Mapping(target = "address", source = "address")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "openingTime", source = "openingTime")
    @Mapping(target = "closingTime", source = "closingTime")
    void updateVenueProfile(@MappingTarget Venue venue, UpdateVenueProfileDTO updateVenueProfileDTO);

}

package com.karam.teamup.venue.mappers;

import com.karam.teamup.venue.dto.UpdateVenueProfileDTO;
import com.karam.teamup.venue.entities.Venue;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-14T18:01:11+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.6 (Ubuntu)"
)
@Component
public class VenueProfileMapperImpl implements VenueProfileMapper {

    @Override
    public void updateVenueProfile(Venue venue, UpdateVenueProfileDTO updateVenueProfileDTO) {
        if ( updateVenueProfileDTO == null ) {
            return;
        }

        if ( updateVenueProfileDTO.address() != null ) {
            venue.setAddress( updateVenueProfileDTO.address() );
        }
        if ( updateVenueProfileDTO.region() != null ) {
            venue.setRegion( updateVenueProfileDTO.region() );
        }
        if ( updateVenueProfileDTO.latitude() != null ) {
            venue.setLatitude( updateVenueProfileDTO.latitude() );
        }
        if ( updateVenueProfileDTO.longitude() != null ) {
            venue.setLongitude( updateVenueProfileDTO.longitude() );
        }
        if ( updateVenueProfileDTO.description() != null ) {
            venue.setDescription( updateVenueProfileDTO.description() );
        }
        if ( updateVenueProfileDTO.phoneNumber() != null ) {
            venue.setPhoneNumber( updateVenueProfileDTO.phoneNumber() );
        }
        if ( updateVenueProfileDTO.openingTime() != null ) {
            venue.setOpeningTime( updateVenueProfileDTO.openingTime() );
        }
        if ( updateVenueProfileDTO.closingTime() != null ) {
            venue.setClosingTime( updateVenueProfileDTO.closingTime() );
        }
    }
}

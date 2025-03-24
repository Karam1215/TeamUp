package com.karam.teamup.venue.services;
import com.karam.teamup.venue.repositories.FieldAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FieldAvailabilityService {
    private final FieldAvailabilityRepository fieldAvailabilityRepository;

}

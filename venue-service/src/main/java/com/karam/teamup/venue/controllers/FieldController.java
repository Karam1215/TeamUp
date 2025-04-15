package com.karam.teamup.venue.controllers;
import com.karam.teamup.venue.dto.CreateFieldDTO;
import com.karam.teamup.venue.services.FieldService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/venue/field")
public class FieldController {
    private final FieldService fieldService;

    @PostMapping
    public ResponseEntity<String> createField(@RequestHeader("X-Username") String username,
                                              @Valid @RequestBody CreateFieldDTO fieldDTO) {
        log.info("Getting creating-field request for field: {}", fieldDTO);
        return fieldService.createField(fieldDTO, username);
    }
}
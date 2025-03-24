package com.karam.teamup.match.controllers;

import com.karam.teamup.match.services.MatchmakingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matchmaking")
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @PostMapping("/process")
    public String processMatches() {
        matchmakingService.matchTeams();
        return "Matching process completed.";
    }
}


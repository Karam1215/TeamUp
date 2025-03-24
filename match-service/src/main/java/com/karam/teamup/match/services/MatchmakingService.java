package com.karam.teamup.match.services;

import com.karam.teamup.match.entities.Match;
import com.karam.teamup.match.entities.MatchRequest;
import com.karam.teamup.match.repositories.MatchRepository;
import com.karam.teamup.match.repositories.MatchRequestRepository;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MatchmakingService {

    private final MatchRequestRepository matchRequestRepository;
    private final MatchRepository matchRepository;

    public MatchmakingService(MatchRequestRepository matchRequestRepository, MatchRepository matchRepository) {
        this.matchRequestRepository = matchRequestRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public void matchTeams() {
        List<MatchRequest> pendingRequests = matchRequestRepository.findByStatus("pending");
        if (pendingRequests.size() < 2) return; // Not enough teams

        List<DoublePoint> points = new ArrayList<>();
        Map<DoublePoint, MatchRequest> pointToRequest = new HashMap<>();

        for (MatchRequest request : pendingRequests) {
            double rankValue = getRankValue(request.getRanking());
            double startTime = request.getPreferredStartTime().getHour();
            double venue = request.getPreferredVenues().hashCode(); // Simple numerical representation

            double[] data = { rankValue, startTime, venue };
            DoublePoint point = new DoublePoint(data);

            points.add(point);
            pointToRequest.put(point, request);
        }

        KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(pendingRequests.size() / 2);
        List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);

        for (Cluster<DoublePoint> cluster : clusters) {
            List<DoublePoint> clusterPoints = cluster.getPoints();
            if (clusterPoints.size() < 2) continue;

            for (int i = 0; i < clusterPoints.size() - 1; i += 2) {
                MatchRequest team1 = pointToRequest.get(clusterPoints.get(i));
                MatchRequest team2 = pointToRequest.get(clusterPoints.get(i + 1));

                Match match = new Match();
                match.setTeam1Id(team1.getTeamId());
                match.setTeam2Id(team2.getTeamId());
                match.setScheduledTime(LocalDateTime.now().plusHours(2)); // Example
                match.setVenue(UUID.randomUUID()); // Example venue assignment

                team1.setStatus("matched");
                team2.setStatus("matched");

                matchRepository.save(match);
                matchRequestRepository.save(team1);
                matchRequestRepository.save(team2);
            }
        }
    }

    private double getRankValue(String ranking) {
        return switch (ranking.toLowerCase()) {
            case "beginner" -> 0;
            case "medium" -> 1;
            case "advanced" -> 2;
            case "world-class" -> 3;
            default -> -1;
        };
    }
}

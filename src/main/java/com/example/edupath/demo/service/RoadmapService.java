package com.example.edupath.demo.service;

import com.example.edupath.demo.dto.*;

import java.util.List;

public interface RoadmapService {

    RoadmapResponse generateAiRoadmap(String userEmail, GenerateRoadmapRequest request);

    RoadmapResponse createManualRoadmap(String userEmail, RoadmapRequest request);

    List<RoadmapResponse> getRoadmaps(String userEmail);

    RoadmapResponse getRoadmapById(Long roadmapId);

    RoadmapNodeDto addNode(Long roadmapId, String userEmail, boolean isAdmin, RoadmapNodeDto nodeDto);

    RoadmapEdgeDto addEdge(Long roadmapId, String userEmail, boolean isAdmin, RoadmapEdgeDto edgeDto);

    void deleteRoadmap(Long roadmapId, String userEmail, boolean isAdmin);

    void deleteNode(Long nodeId, String userEmail, boolean isAdmin);

    void deleteEdge(Long sourceNodeId, Long targetNodeId, String userEmail, boolean isAdmin);
}

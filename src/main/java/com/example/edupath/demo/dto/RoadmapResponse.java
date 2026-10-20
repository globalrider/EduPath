package com.example.edupath.demo.dto;

import com.example.edupath.demo.model.Roadmap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoadmapResponse {

    private Long roadmapId;
    private Long userId;
    private String userName;
    private String goal;
    private String title;
    private LocalDateTime createdAt;
    private List<RoadmapNodeDto> nodes = new ArrayList<>();
    private List<RoadmapEdgeDto> edges = new ArrayList<>();

    public RoadmapResponse() {
    }

    public RoadmapResponse(Long roadmapId, Long userId, String userName, String goal, String title, LocalDateTime createdAt, List<RoadmapNodeDto> nodes, List<RoadmapEdgeDto> edges) {
        this.roadmapId = roadmapId;
        this.userId = userId;
        this.userName = userName;
        this.goal = goal;
        this.title = title;
        this.createdAt = createdAt;
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
    }

    public static RoadmapResponse fromEntity(Roadmap roadmap, List<RoadmapNodeDto> nodes, List<RoadmapEdgeDto> edges) {
        if (roadmap == null) return null;
        return new RoadmapResponse(
            roadmap.getRoadmapId(),
            roadmap.getUser() != null ? roadmap.getUser().getId() : null,
            roadmap.getUser() != null ? roadmap.getUser().getName() : null,
            roadmap.getGoal(),
            roadmap.getTitle(),
            roadmap.getCreatedAt(),
            nodes,
            edges
        );
    }

    public Long getRoadmapId() {
        return roadmapId;
    }

    public void setRoadmapId(Long roadmapId) {
        this.roadmapId = roadmapId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<RoadmapNodeDto> getNodes() {
        return nodes;
    }

    public void setNodes(List<RoadmapNodeDto> nodes) {
        this.nodes = nodes;
    }

    public List<RoadmapEdgeDto> getEdges() {
        return edges;
    }

    public void setEdges(List<RoadmapEdgeDto> edges) {
        this.edges = edges;
    }
}

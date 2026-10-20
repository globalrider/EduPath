package com.example.edupath.demo.dto;

import com.example.edupath.demo.model.RoadmapEdge;
import jakarta.validation.constraints.NotNull;

public class RoadmapEdgeDto {

    @NotNull(message = "Source node ID is required")
    private Long sourceNodeId;

    @NotNull(message = "Target node ID is required")
    private Long targetNodeId;

    private String sourceTitle;
    private String targetTitle;

    public RoadmapEdgeDto() {
    }

    public RoadmapEdgeDto(Long sourceNodeId, Long targetNodeId, String sourceTitle, String targetTitle) {
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.sourceTitle = sourceTitle;
        this.targetTitle = targetTitle;
    }

    public static RoadmapEdgeDto fromEntity(RoadmapEdge edge) {
        if (edge == null) return null;
        return new RoadmapEdgeDto(
            edge.getId() != null ? edge.getId().getSourceNodeId() : (edge.getSourceNode() != null ? edge.getSourceNode().getNodeId() : null),
            edge.getId() != null ? edge.getId().getTargetNodeId() : (edge.getTargetNode() != null ? edge.getTargetNode().getNodeId() : null),
            edge.getSourceNode() != null ? edge.getSourceNode().getTitle() : null,
            edge.getTargetNode() != null ? edge.getTargetNode().getTitle() : null
        );
    }

    public Long getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(Long sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public Long getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(Long targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    public String getTargetTitle() {
        return targetTitle;
    }

    public void setTargetTitle(String targetTitle) {
        this.targetTitle = targetTitle;
    }
}

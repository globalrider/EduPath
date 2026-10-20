package com.example.edupath.demo.dto;

import com.example.edupath.demo.model.RoadmapNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoadmapNodeDto {

    private Long nodeId;
    private Long roadmapId;

    @NotBlank(message = "Node title is required")
    @Size(max = 200, message = "Node title must not exceed 200 characters")
    private String title;

    private String description;
    private Integer semester;
    private Integer sequenceOrder;

    public RoadmapNodeDto() {
    }

    public RoadmapNodeDto(Long nodeId, Long roadmapId, String title, String description, Integer semester, Integer sequenceOrder) {
        this.nodeId = nodeId;
        this.roadmapId = roadmapId;
        this.title = title;
        this.description = description;
        this.semester = semester;
        this.sequenceOrder = sequenceOrder;
    }

    public static RoadmapNodeDto fromEntity(RoadmapNode node) {
        if (node == null) return null;
        return new RoadmapNodeDto(
            node.getNodeId(),
            node.getRoadmap() != null ? node.getRoadmap().getRoadmapId() : null,
            node.getTitle(),
            node.getDescription(),
            node.getSemester(),
            node.getSequenceOrder()
        );
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getRoadmapId() {
        return roadmapId;
    }

    public void setRoadmapId(Long roadmapId) {
        this.roadmapId = roadmapId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
}

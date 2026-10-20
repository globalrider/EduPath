package com.example.edupath.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RoadmapEdgeId implements Serializable {

    @Column(name = "source_node_id")
    private Long sourceNodeId;

    @Column(name = "target_node_id")
    private Long targetNodeId;

    public RoadmapEdgeId() {
    }

    public RoadmapEdgeId(Long sourceNodeId, Long targetNodeId) {
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoadmapEdgeId that = (RoadmapEdgeId) o;
        return Objects.equals(sourceNodeId, that.sourceNodeId) &&
               Objects.equals(targetNodeId, that.targetNodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceNodeId, targetNodeId);
    }
}

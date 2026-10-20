package com.example.edupath.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "roadmap_edges")
public class RoadmapEdge {

    @EmbeddedId
    private RoadmapEdgeId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("sourceNodeId")
    @JoinColumn(name = "source_node_id", nullable = false)
    @JsonIgnoreProperties({"roadmap", "description"})
    private RoadmapNode sourceNode;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("targetNodeId")
    @JoinColumn(name = "target_node_id", nullable = false)
    @JsonIgnoreProperties({"roadmap", "description"})
    private RoadmapNode targetNode;

    public RoadmapEdge() {
        this.id = new RoadmapEdgeId();
    }

    public RoadmapEdge(RoadmapNode sourceNode, RoadmapNode targetNode) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.id = new RoadmapEdgeId(
            sourceNode != null ? sourceNode.getNodeId() : null,
            targetNode != null ? targetNode.getNodeId() : null
        );
    }

    public RoadmapEdgeId getId() {
        return id;
    }

    public void setId(RoadmapEdgeId id) {
        this.id = id;
    }

    public RoadmapNode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(RoadmapNode sourceNode) {
        this.sourceNode = sourceNode;
        if (this.id == null) {
            this.id = new RoadmapEdgeId();
        }
        if (sourceNode != null) {
            this.id.setSourceNodeId(sourceNode.getNodeId());
        }
    }

    public RoadmapNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(RoadmapNode targetNode) {
        this.targetNode = targetNode;
        if (this.id == null) {
            this.id = new RoadmapEdgeId();
        }
        if (targetNode != null) {
            this.id.setTargetNodeId(targetNode.getNodeId());
        }
    }
}

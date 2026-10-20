package com.example.edupath.demo.repository;

import com.example.edupath.demo.model.RoadmapEdge;
import com.example.edupath.demo.model.RoadmapEdgeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapEdgeRepository extends JpaRepository<RoadmapEdge, RoadmapEdgeId> {

    @Query("SELECT e FROM RoadmapEdge e WHERE e.sourceNode.roadmap.roadmapId = :roadmapId")
    List<RoadmapEdge> findByRoadmapId(@Param("roadmapId") Long roadmapId);

    @Modifying
    @Query("DELETE FROM RoadmapEdge e WHERE e.sourceNode.roadmap.roadmapId = :roadmapId")
    void deleteByRoadmapId(@Param("roadmapId") Long roadmapId);

    @Modifying
    @Query("DELETE FROM RoadmapEdge e WHERE e.sourceNode.nodeId = :nodeId OR e.targetNode.nodeId = :nodeId")
    void deleteByNodeId(@Param("nodeId") Long nodeId);
}

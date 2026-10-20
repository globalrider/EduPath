package com.example.edupath.demo.repository;

import com.example.edupath.demo.model.RoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {

    List<RoadmapNode> findByRoadmap_RoadmapIdOrderBySequenceOrderAsc(Long roadmapId);

    @Modifying
    @Query("DELETE FROM RoadmapNode n WHERE n.roadmap.roadmapId = :roadmapId")
    void deleteByRoadmapId(@Param("roadmapId") Long roadmapId);
}

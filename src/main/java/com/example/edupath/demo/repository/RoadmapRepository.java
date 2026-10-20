package com.example.edupath.demo.repository;

import com.example.edupath.demo.model.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    List<Roadmap> findByUser_Id(Long userId);

    List<Roadmap> findByGoalContainingIgnoreCaseOrTitleContainingIgnoreCase(String goal, String title);
}

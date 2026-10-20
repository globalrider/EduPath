package com.example.edupath.demo.controller;

import com.example.edupath.demo.dto.*;
import com.example.edupath.demo.service.RoadmapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    /**
     * AI Career Roadmap Generator (Spring AI / Gemini Path Generator)
     * Generates adaptive milestones, semester breakdown, and directed graph prerequisite edges
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<RoadmapResponse>> generateRoadmap(
            Authentication authentication,
            @RequestBody(required = false) GenerateRoadmapRequest request
    ) {
        if (request == null || request.getGoal() == null || request.getGoal().isBlank()) {
            if (request == null) {
                request = new GenerateRoadmapRequest("Full Stack Java Developer", "AI Career Path: Full Stack Java Developer", 3);
            } else {
                request.setGoal("Full Stack Java Developer");
                if (request.getTitle() == null || request.getTitle().isBlank()) {
                    request.setTitle("AI Career Path: Full Stack Java Developer");
                }
            }
        }
        RoadmapResponse response = roadmapService.generateAiRoadmap(authentication.getName(), request);
        return new ResponseEntity<>(
            ApiResponse.success("AI career roadmap and graph dataset generated successfully", response),
            HttpStatus.CREATED
        );
    }

    /**
     * Create custom career roadmap
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoadmapResponse>> createRoadmap(
            Authentication authentication,
            @Valid @RequestBody RoadmapRequest request
    ) {
        RoadmapResponse response = roadmapService.createManualRoadmap(authentication.getName(), request);
        return new ResponseEntity<>(
            ApiResponse.success("Roadmap container created successfully", response),
            HttpStatus.CREATED
        );
    }

    /**
     * List all roadmaps
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoadmapResponse>>> getRoadmaps(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        List<RoadmapResponse> list = roadmapService.getRoadmaps(email);
        return ResponseEntity.ok(ApiResponse.success("Career roadmaps retrieved", list));
    }

    /**
     * Get roadmap graph with full nodes and directed edges (for graph visualization)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoadmapResponse>> getRoadmapById(@PathVariable Long id) {
        RoadmapResponse response = roadmapService.getRoadmapById(id);
        return ResponseEntity.ok(ApiResponse.success("Roadmap graph details retrieved", response));
    }

    /**
     * Add a node / milestone to an existing roadmap
     */
    @PostMapping("/{id}/nodes")
    public ResponseEntity<ApiResponse<RoadmapNodeDto>> addNode(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody RoadmapNodeDto nodeDto
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        RoadmapNodeDto created = roadmapService.addNode(id, authentication.getName(), isAdmin, nodeDto);
        return new ResponseEntity<>(
            ApiResponse.success("Roadmap node milestone added", created),
            HttpStatus.CREATED
        );
    }

    /**
     * Add a directed edge connecting prerequisite source node to dependent target node
     */
    @PostMapping("/{id}/edges")
    public ResponseEntity<ApiResponse<RoadmapEdgeDto>> addEdge(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody RoadmapEdgeDto edgeDto
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        RoadmapEdgeDto created = roadmapService.addEdge(id, authentication.getName(), isAdmin, edgeDto);
        return new ResponseEntity<>(
            ApiResponse.success("Directed prerequisite edge connected", created),
            HttpStatus.CREATED
        );
    }

    /**
     * Delete entire roadmap
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoadmap(
            @PathVariable Long id,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        roadmapService.deleteRoadmap(id, authentication.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Roadmap deleted successfully", null));
    }

    /**
     * Delete a node from roadmap
     */
    @DeleteMapping("/nodes/{nodeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNode(
            @PathVariable Long nodeId,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        roadmapService.deleteNode(nodeId, authentication.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Roadmap node deleted successfully", null));
    }

    /**
     * Delete a directed edge between nodes
     */
    @DeleteMapping("/edges")
    public ResponseEntity<ApiResponse<Void>> deleteEdge(
            @RequestParam Long sourceNodeId,
            @RequestParam Long targetNodeId,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        roadmapService.deleteEdge(sourceNodeId, targetNodeId, authentication.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Roadmap edge removed successfully", null));
    }
}

package com.example.edupath.demo.controller;

import com.example.edupath.demo.dto.AiPathRequest;
import com.example.edupath.demo.dto.AiPathResponse;
import com.example.edupath.demo.dto.ApiResponse;
import com.example.edupath.demo.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai-assistant")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * Screen 4: Spring AI / Gemini Path Generator
     * Goal input -> Generates adaptive milestones & recommended skills
     */
    @PostMapping("/generate-path")
    public ResponseEntity<ApiResponse<AiPathResponse>> generatePath(@Valid @RequestBody AiPathRequest request) {
        AiPathResponse response = aiAssistantService.generatePath(request);
        return ResponseEntity.ok(ApiResponse.success("Gemini generated adaptive milestones successfully", response));
    }
}

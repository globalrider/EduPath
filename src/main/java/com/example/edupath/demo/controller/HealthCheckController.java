package com.example.edupath.demo.controller;

import com.example.edupath.demo.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> statusDetails = new HashMap<>();
        statusDetails.put("status", "UP");
        statusDetails.put("service", "EduPath Backend API");
        statusDetails.put("serverTime", LocalDateTime.now().toString());
        statusDetails.put("version", "1.0.0");

        return ResponseEntity.ok(ApiResponse.success("EduPath API is running smoothly", statusDetails));
    }
}

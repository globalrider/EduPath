package com.example.edupath.demo.controller;

import com.example.edupath.demo.dto.AlumniProfileDto;
import com.example.edupath.demo.dto.ApiResponse;
import com.example.edupath.demo.service.AlumniService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alumni")
public class AlumniController {

    private final AlumniService alumniService;

    public AlumniController(AlumniService alumniService) {
        this.alumniService = alumniService;
    }

    /**
     * List all alumni profiles
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlumniProfileDto>>> getAllAlumni() {
        List<AlumniProfileDto> list = alumniService.getAllAlumni();
        return ResponseEntity.ok(ApiResponse.success("Alumni profiles retrieved successfully", list));
    }

    /**
     * Get specific alumni profile by user ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlumniProfileDto>> getAlumniById(@PathVariable Long id) {
        AlumniProfileDto dto = alumniService.getAlumniById(id);
        return ResponseEntity.ok(ApiResponse.success("Alumni profile details retrieved", dto));
    }

    /**
     * Create or update current logged-in alumnus profile
     * Only ALUMNI or ADMIN roles permitted
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ALUMNI', 'ADMIN')")
    public ResponseEntity<ApiResponse<AlumniProfileDto>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody AlumniProfileDto request
    ) {
        AlumniProfileDto updated = alumniService.updateCurrentAlumniProfile(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Alumni profile updated successfully", updated));
    }

    /**
     * Admin update for an alumni profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AlumniProfileDto>> updateAlumniByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AlumniProfileDto request
    ) {
        AlumniProfileDto updated = alumniService.updateAlumniProfileByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Alumni profile updated by admin", updated));
    }
}

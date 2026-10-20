package com.example.edupath.demo.controller;

import com.example.edupath.demo.dto.ApiResponse;
import com.example.edupath.demo.dto.PlaybookRequest;
import com.example.edupath.demo.dto.PlaybookResponse;
import com.example.edupath.demo.service.PlaybookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/playbooks")
public class PlaybookController {

    private final PlaybookService playbookService;

    public PlaybookController(PlaybookService playbookService) {
        this.playbookService = playbookService;
    }

    /**
     * Senior / Alumni Guidance Feed (Junior Feed)
     * Filters: category (Academics, Internships, Higher Studies, Placement), status (PUBLISHED, DRAFT), search
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaybookResponse>>> getPlaybooks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        List<PlaybookResponse> playbooks = playbookService.getAllPlaybooks(category, status, search);
        return ResponseEntity.ok(ApiResponse.success("Guidance playbooks retrieved successfully", playbooks));
    }

    /**
     * Get specific playbook guidance post by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaybookResponse>> getPlaybookById(@PathVariable Long id) {
        PlaybookResponse response = playbookService.getPlaybookById(id);
        return ResponseEntity.ok(ApiResponse.success("Playbook details retrieved", response));
    }

    /**
     * Create / Publish a guidance playbook
     * Only SENIOR, ALUMNI, and ADMIN roles can publish (PDF requirement)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SENIOR', 'ALUMNI', 'ADMIN')")
    public ResponseEntity<ApiResponse<PlaybookResponse>> createPlaybook(
            Authentication authentication,
            @Valid @RequestBody PlaybookRequest request
    ) {
        PlaybookResponse created = playbookService.createPlaybook(authentication.getName(), request);
        return new ResponseEntity<>(
            ApiResponse.success("Guidance playbook published successfully", created),
            HttpStatus.CREATED
        );
    }

    /**
     * Update guidance playbook post
     * Permitted for author or ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SENIOR', 'ALUMNI', 'ADMIN')")
    public ResponseEntity<ApiResponse<PlaybookResponse>> updatePlaybook(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody PlaybookRequest request
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        PlaybookResponse updated = playbookService.updatePlaybook(id, authentication.getName(), isAdmin, request);
        return ResponseEntity.ok(ApiResponse.success("Playbook guide updated successfully", updated));
    }

    /**
     * Delete guidance playbook post
     * Permitted for author or ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SENIOR', 'ALUMNI', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlaybook(
            @PathVariable Long id,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        playbookService.deletePlaybook(id, authentication.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Playbook guide deleted successfully", null));
    }
}

package com.example.edupath.demo.controller;

import com.example.edupath.demo.dto.AnswerRequest;
import com.example.edupath.demo.dto.AnswerResponse;
import com.example.edupath.demo.dto.ApiResponse;
import com.example.edupath.demo.dto.QuestionRequest;
import com.example.edupath.demo.dto.QuestionResponse;
import com.example.edupath.demo.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * List discussion questions
     * Filters: category (Academics, Internships, Higher Studies, Placement), search
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        List<QuestionResponse> questions = questionService.getAllQuestions(category, search);
        return ResponseEntity.ok(ApiResponse.success("Questions retrieved successfully", questions));
    }

    /**
     * Get specific question thread with full list of answers
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(@PathVariable Long id) {
        QuestionResponse question = questionService.getQuestionById(id);
        return ResponseEntity.ok(ApiResponse.success("Question thread details retrieved", question));
    }

    /**
     * Submit a new question
     * Authenticated users of any role (JUNIOR, SENIOR, ALUMNI, ADMIN)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            Authentication authentication,
            @Valid @RequestBody QuestionRequest request
    ) {
        String email = (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser"))
                ? authentication.getName()
                : "vignesh@edupath.edu"; // Pre-seeded Junior default

        QuestionResponse created = questionService.createQuestion(email, request);
        return new ResponseEntity<>(
            ApiResponse.success("Question submitted successfully", created),
            HttpStatus.CREATED
        );
    }

    /**
     * Delete a question
     * Author or ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "vignesh@edupath.edu";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        questionService.deleteQuestion(id, email, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Question deleted successfully", null));
    }

    /**
     * Add an answer to a question
     * Authenticated users
     */
    @PostMapping("/{id}/answers")
    public ResponseEntity<ApiResponse<AnswerResponse>> addAnswer(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody AnswerRequest request
    ) {
        String email = (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser"))
                ? authentication.getName()
                : "aravind@edupath.edu"; // Pre-seeded Senior default

        AnswerResponse created = questionService.addAnswer(id, email, request);
        return new ResponseEntity<>(
            ApiResponse.success("Answer posted successfully", created),
            HttpStatus.CREATED
        );
    }

    /**
     * Toggle / Mark an answer as verified (Alumni / Senior verification badge)
     * SENIOR, ALUMNI, ADMIN, or Question author
     */
    @RequestMapping(value = "/answers/{answerId}/verify", method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    @PreAuthorize("hasAnyRole('SENIOR', 'ALUMNI', 'ADMIN')")
    public ResponseEntity<ApiResponse<AnswerResponse>> verifyAnswer(
            @PathVariable Long answerId,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        AnswerResponse verified = questionService.verifyAnswer(answerId, authentication.getName(), isAdmin);
        String msg = Boolean.TRUE.equals(verified.getIsVerifiedAnswer())
                ? "Answer marked as verified with Alumni badge"
                : "Answer verification removed";
        return ResponseEntity.ok(ApiResponse.success(msg, verified));
    }

    /**
     * Delete an answer
     * Author or ADMIN
     */
    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(
            @PathVariable Long answerId,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        questionService.deleteAnswer(answerId, authentication.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Answer deleted successfully", null));
    }
}

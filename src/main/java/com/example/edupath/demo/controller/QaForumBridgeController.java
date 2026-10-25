package com.example.edupath.demo.controller;

import com.example.edupath.demo.model.Answer;
import com.example.edupath.demo.model.Question;
import com.example.edupath.demo.model.Role;
import com.example.edupath.demo.model.User;
import com.example.edupath.demo.repository.AnswerRepository;
import com.example.edupath.demo.repository.QuestionRepository;
import com.example.edupath.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api")
public class QaForumBridgeController {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    // Track active local preview role
    private final AtomicReference<String> activePreviewRole = new AtomicReference<>("JUNIOR");

    public QaForumBridgeController(
            QuestionRepository questionRepository,
            AnswerRepository answerRepository,
            UserRepository userRepository
    ) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken() {
        return Map.of(
                "token", "edupath-qa-token",
                "headerName", "X-CSRF-TOKEN",
                "parameterName", "_csrf"
        );
    }

    @GetMapping("/dev/preview-role")
    public Map<String, Object> getPreviewRole(HttpSession session) {
        String role = (String) session.getAttribute("PREVIEW_ROLE");
        if (role == null) {
            role = activePreviewRole.get();
        }
        return Map.of(
                "available", true,
                "role", role != null ? role : "JUNIOR"
        );
    }

    @PostMapping("/dev/preview-role")
    public Map<String, Object> setPreviewRole(@RequestBody Map<String, String> body, HttpSession session) {
        String role = body.getOrDefault("role", "JUNIOR").toUpperCase(Locale.ROOT);
        activePreviewRole.set(role);
        session.setAttribute("PREVIEW_ROLE", role);

        User user = getTargetUserForRole(role);
        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "role", role
        );
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication, HttpSession session) {
        String sessionRole = (String) session.getAttribute("PREVIEW_ROLE");
        final String effectiveRole = (sessionRole != null) ? sessionRole : activePreviewRole.get();

        User user;
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            user = userRepository.findByEmail(authentication.getName()).orElseGet(() -> getTargetUserForRole(effectiveRole));
        } else {
            user = getTargetUserForRole(effectiveRole);
        }

        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
    }

    @GetMapping("/junior/questions")
    public List<Map<String, Object>> getJuniorQuestions(Authentication authentication, HttpSession session) {
        List<Question> questions = questionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Question q : questions) {
            List<Answer> answers = answerRepository.findByQuestion_QuestionIdOrderByCreatedAtAsc(q.getQuestionId());
            result.add(formatQuestionResponse(q, answers));
        }
        return result;
    }

    @PostMapping("/junior/questions")
    public ResponseEntity<Map<String, Object>> createJuniorQuestion(
            @RequestBody Map<String, String> payload,
            Authentication authentication,
            HttpSession session
    ) {
        User junior = null;
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            junior = userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        if (junior == null) {
            String sessionRole = (String) session.getAttribute("PREVIEW_ROLE");
            String role = (sessionRole != null) ? sessionRole : activePreviewRole.get();
            junior = getTargetUserForRole(role != null && role.equalsIgnoreCase("SENIOR") ? "SENIOR" : "JUNIOR");
        }

        String title = payload.getOrDefault("title", "Untitled Question");
        String category = payload.getOrDefault("category", "Academics");
        String questionBody = payload.getOrDefault("questionBody", "");

        Question q = new Question(junior, title, questionBody, category);
        Question saved = questionRepository.save(q);

        return new ResponseEntity<>(formatQuestionResponse(saved, Collections.emptyList()), HttpStatus.CREATED);
    }

    @GetMapping("/junior/questions/{questionId}")
    public ResponseEntity<Map<String, Object>> getJuniorQuestion(@PathVariable Long questionId) {
        return questionRepository.findById(questionId)
                .map(q -> {
                    List<Answer> answers = answerRepository.findByQuestion_QuestionIdOrderByCreatedAtAsc(q.getQuestionId());
                    return ResponseEntity.ok(formatQuestionResponse(q, answers));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/senior/questions")
    public List<Map<String, Object>> getSeniorOpenQuestions() {
        List<Question> questions = questionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Question q : questions) {
            // Strictly open questions only (no answers posted yet)
            List<Answer> answers = answerRepository.findByQuestion_QuestionIdOrderByCreatedAtAsc(q.getQuestionId());
            boolean isAnswered = answers != null && !answers.isEmpty();
            if (!isAnswered) {
                result.add(formatQuestionResponse(q, answers));
            }
        }
        return result;
    }

    @PostMapping("/senior/questions/{questionId}/answers")
    public ResponseEntity<Void> answerQuestion(
            @PathVariable Long questionId,
            @RequestBody Map<String, String> payload,
            Authentication authentication,
            HttpSession session
    ) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        User senior = null;
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            senior = userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        if (senior == null) {
            String sessionRole = (String) session.getAttribute("PREVIEW_ROLE");
            String role = (sessionRole != null) ? sessionRole : activePreviewRole.get();
            senior = getTargetUserForRole(role != null && role.equalsIgnoreCase("ALUMNI") ? "ALUMNI" : "SENIOR");
        }

        String body = payload.getOrDefault("answerBody", "");
        Answer answer = new Answer(question, senior, body, true);
        answerRepository.save(answer);

        // Also add to question's in-memory list for immediate consistency
        if (question.getAnswers() == null) {
            question.setAnswers(new ArrayList<>());
        }
        question.getAnswers().add(answer);
        questionRepository.save(question);

        return ResponseEntity.noContent().build();
    }

    private User getTargetUserForRole(String role) {
        if ("SENIOR".equalsIgnoreCase(role)) {
            return userRepository.findByEmail("aravind@edupath.edu")
                    .orElseGet(() -> userRepository.findAll().stream()
                            .filter(u -> u.getRole() == Role.SENIOR)
                            .findFirst()
                            .orElse(null));
        } else if ("ALUMNI".equalsIgnoreCase(role)) {
            return userRepository.findByEmail("deepa@amazon.com")
                    .orElseGet(() -> userRepository.findAll().stream()
                            .filter(u -> u.getRole() == Role.ALUMNI)
                            .findFirst()
                            .orElse(null));
        } else {
            return userRepository.findByEmail("vignesh@edupath.edu")
                    .orElseGet(() -> userRepository.findAll().stream()
                            .filter(u -> u.getRole() == Role.JUNIOR)
                            .findFirst()
                            .orElse(null));
        }
    }

    private Map<String, Object> formatQuestionResponse(Question q, List<Answer> answers) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", q.getQuestionId());
        map.put("authorId", q.getAuthor() != null ? q.getAuthor().getId() : 0L);
        map.put("authorName", q.getAuthor() != null ? q.getAuthor().getName() : "Junior Student");
        map.put("title", q.getTitle());
        map.put("category", q.getCategory());
        map.put("questionBody", q.getQuestionBody());

        boolean hasAnswers = answers != null && !answers.isEmpty();
        map.put("status", hasAnswers ? "ANSWERED" : "OPEN");
        map.put("createdAt", q.getCreatedAt() != null ? q.getCreatedAt().toString() : Instant.now().toString());

        if (hasAnswers) {
            Answer firstAns = answers.get(0);
            Map<String, Object> ansMap = new LinkedHashMap<>();
            ansMap.put("id", firstAns.getAnswerId());
            ansMap.put("questionId", q.getQuestionId());
            ansMap.put("authorId", firstAns.getAuthor() != null ? firstAns.getAuthor().getId() : 0L);
            ansMap.put("authorName", firstAns.getAuthor() != null ? firstAns.getAuthor().getName() : "Senior Mentor");
            ansMap.put("answerBody", firstAns.getAnswerBody());
            ansMap.put("verified", firstAns.getIsVerifiedAnswer() != null && firstAns.getIsVerifiedAnswer());
            ansMap.put("createdAt", firstAns.getCreatedAt() != null ? firstAns.getCreatedAt().toString() : Instant.now().toString());
            map.put("acceptedAnswer", ansMap);
            map.put("answers", List.of(ansMap));
        } else {
            map.put("acceptedAnswer", null);
            map.put("answers", Collections.emptyList());
        }

        return map;
    }
}

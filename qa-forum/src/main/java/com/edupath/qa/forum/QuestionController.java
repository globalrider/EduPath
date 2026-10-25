package com.edupath.qa.forum;

import com.edupath.qa.user.AuthenticatedUserService;
import com.edupath.qa.user.PortalUserDetails;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionRepository questionRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public QuestionController(
            QuestionService questionService,
            QuestionRepository questionRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.questionService = questionService;
        this.questionRepository = questionRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/junior/questions")
    public List<QuestionResponse> juniorQuestions(Authentication authentication) {
        PortalUserDetails user = authenticatedUserService.requireForumUser(authentication);
        return questionService.listJuniorQuestions(user.id());
    }

    @PostMapping("/junior/questions")
    public ResponseEntity<QuestionResponse> createQuestion(
            Authentication authentication,
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        PortalUserDetails user = authenticatedUserService.requireForumUser(authentication);
        QuestionResponse question = questionService.createQuestion(user.id(), request);
        return ResponseEntity.created(URI.create("/api/junior/questions/" + question.id())).body(question);
    }

    @GetMapping("/junior/questions/{questionId}")
    public QuestionResponse juniorQuestion(
            Authentication authentication,
            @PathVariable long questionId
    ) {
        PortalUserDetails user = authenticatedUserService.requireForumUser(authentication);
        return questionRepository.findForJunior(questionId, user.id())
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
    }

    @GetMapping("/senior/questions")
    public List<QuestionResponse> seniorQuestions(Authentication authentication) {
        authenticatedUserService.requireForumUser(authentication);
        return questionService.listOpenQuestions();
    }

    @PostMapping("/senior/questions/{questionId}/answers")
    public ResponseEntity<Void> answerQuestion(
            Authentication authentication,
            @PathVariable long questionId,
            @Valid @RequestBody CreateAnswerRequest request
    ) {
        PortalUserDetails user = authenticatedUserService.requireForumUser(authentication);
        questionService.answerQuestion(questionId, user.id(), request);
        return ResponseEntity.noContent().build();
    }
}

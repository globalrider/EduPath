package com.example.edupath.demo.service.impl;

import com.example.edupath.demo.dto.AnswerRequest;
import com.example.edupath.demo.dto.AnswerResponse;
import com.example.edupath.demo.dto.QuestionRequest;
import com.example.edupath.demo.dto.QuestionResponse;
import com.example.edupath.demo.exception.ResourceNotFoundException;
import com.example.edupath.demo.model.Answer;
import com.example.edupath.demo.model.Question;
import com.example.edupath.demo.model.Role;
import com.example.edupath.demo.model.User;
import com.example.edupath.demo.repository.AnswerRepository;
import com.example.edupath.demo.repository.QuestionRepository;
import com.example.edupath.demo.repository.UserRepository;
import com.example.edupath.demo.service.QuestionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository, AnswerRepository answerRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getAllQuestions(String category, String search) {
        List<Question> list;

        if (search != null && !search.isBlank()) {
            list = questionRepository.findByTitleContainingIgnoreCaseOrQuestionBodyContainingIgnoreCase(search, search);
        } else if (category != null && !category.equalsIgnoreCase("ALL") && !category.isBlank()) {
            list = questionRepository.findByCategoryIgnoreCase(category);
        } else {
            list = questionRepository.findAll();
        }

        return list.stream()
                .map(q -> QuestionResponse.fromEntity(q, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));

        // Load answers ordered by creation time
        List<Answer> answers = answerRepository.findByQuestion_QuestionIdOrderByCreatedAtAsc(id);
        question.setAnswers(answers);

        return QuestionResponse.fromEntity(question, true);
    }

    @Override
    public QuestionResponse createQuestion(String userEmail, QuestionRequest request) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Question question = new Question();
        question.setAuthor(author);
        question.setTitle(request.getTitle());
        question.setQuestionBody(request.getQuestionBody());
        question.setCategory(request.getCategory() != null ? request.getCategory() : "Academics");

        Question saved = questionRepository.save(question);
        return QuestionResponse.fromEntity(saved, true);
    }

    @Override
    public void deleteQuestion(Long id, String userEmail, boolean isAdmin) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));

        if (!isAdmin && !question.getAuthor().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You can only delete questions that you authored");
        }

        questionRepository.delete(question);
    }

    @Override
    public AnswerResponse addAnswer(Long questionId, String userEmail, AnswerRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));

        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAuthor(author);
        answer.setAnswerBody(request.getAnswerBody());
        boolean isSenior = author.getRole() == Role.SENIOR || author.getRole() == Role.ALUMNI || author.getRole() == Role.ADMIN;
        answer.setIsVerifiedAnswer(isSenior);

        Answer saved = answerRepository.save(answer);
        return AnswerResponse.fromEntity(saved);
    }

    @Override
    public AnswerResponse verifyAnswer(Long answerId, String userEmail, boolean isAdmin) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with ID: " + answerId));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Verification permission: ALUMNI, SENIOR, ADMIN, or the Question author
        boolean isQuestionAuthor = answer.getQuestion().getAuthor().getId().equals(currentUser.getId());
        boolean hasPrivilege = currentUser.getRole() == Role.ALUMNI ||
                               currentUser.getRole() == Role.SENIOR ||
                               currentUser.getRole() == Role.ADMIN;

        if (!isAdmin && !hasPrivilege && !isQuestionAuthor) {
            throw new AccessDeniedException("Only Alumni, Seniors, Admins, or the Question Author can mark an answer as verified");
        }

        answer.setIsVerifiedAnswer(!Boolean.TRUE.equals(answer.getIsVerifiedAnswer())); // Toggle verification
        Answer updated = answerRepository.save(answer);
        return AnswerResponse.fromEntity(updated);
    }

    @Override
    public void deleteAnswer(Long answerId, String userEmail, boolean isAdmin) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with ID: " + answerId));

        if (!isAdmin && !answer.getAuthor().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You can only delete answers that you authored");
        }

        answerRepository.delete(answer);
    }
}

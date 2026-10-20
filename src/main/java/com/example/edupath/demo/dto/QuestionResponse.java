package com.example.edupath.demo.dto;

import com.example.edupath.demo.model.Question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionResponse {

    private Long questionId;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String title;
    private String questionBody;
    private String category;
    private LocalDateTime createdAt;
    private int answerCount;
    private List<AnswerResponse> answers = new ArrayList<>();

    public QuestionResponse() {
    }

    public QuestionResponse(Long questionId, Long authorId, String authorName, String authorRole, String title, String questionBody, String category, LocalDateTime createdAt, int answerCount, List<AnswerResponse> answers) {
        this.questionId = questionId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.title = title;
        this.questionBody = questionBody;
        this.category = category;
        this.createdAt = createdAt;
        this.answerCount = answerCount;
        this.answers = answers != null ? answers : new ArrayList<>();
    }

    public static QuestionResponse fromEntity(Question q, boolean includeAnswers) {
        if (q == null) return null;
        List<AnswerResponse> answerList = new ArrayList<>();
        if (includeAnswers && q.getAnswers() != null) {
            answerList = q.getAnswers().stream()
                    .map(AnswerResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        int count = (q.getAnswers() != null) ? q.getAnswers().size() : 0;

        return new QuestionResponse(
            q.getQuestionId(),
            q.getAuthor() != null ? q.getAuthor().getId() : null,
            q.getAuthor() != null ? q.getAuthor().getName() : null,
            q.getAuthor() != null && q.getAuthor().getRole() != null ? q.getAuthor().getRole().name() : null,
            q.getTitle(),
            q.getQuestionBody(),
            q.getCategory(),
            q.getCreatedAt(),
            count,
            answerList
        );
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestionBody() {
        return questionBody;
    }

    public void setQuestionBody(String questionBody) {
        this.questionBody = questionBody;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    public List<AnswerResponse> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerResponse> answers) {
        this.answers = answers;
    }
}

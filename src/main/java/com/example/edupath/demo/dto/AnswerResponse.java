package com.example.edupath.demo.dto;

import com.example.edupath.demo.model.Answer;

import java.time.LocalDateTime;

public class AnswerResponse {

    private Long answerId;
    private Long questionId;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String answerBody;
    private Boolean isVerifiedAnswer;
    private LocalDateTime createdAt;

    public AnswerResponse() {
    }

    public AnswerResponse(Long answerId, Long questionId, Long authorId, String authorName, String authorRole, String answerBody, Boolean isVerifiedAnswer, LocalDateTime createdAt) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.answerBody = answerBody;
        this.isVerifiedAnswer = isVerifiedAnswer;
        this.createdAt = createdAt;
    }

    public static AnswerResponse fromEntity(Answer answer) {
        if (answer == null) return null;
        return new AnswerResponse(
            answer.getAnswerId(),
            answer.getQuestion() != null ? answer.getQuestion().getQuestionId() : null,
            answer.getAuthor() != null ? answer.getAuthor().getId() : null,
            answer.getAuthor() != null ? answer.getAuthor().getName() : null,
            answer.getAuthor() != null && answer.getAuthor().getRole() != null ? answer.getAuthor().getRole().name() : null,
            answer.getAnswerBody(),
            answer.getIsVerifiedAnswer(),
            answer.getCreatedAt()
        );
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
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

    public String getAnswerBody() {
        return answerBody;
    }

    public void setAnswerBody(String answerBody) {
        this.answerBody = answerBody;
    }

    public Boolean getIsVerifiedAnswer() {
        return isVerifiedAnswer;
    }

    public void setIsVerifiedAnswer(Boolean isVerifiedAnswer) {
        this.isVerifiedAnswer = isVerifiedAnswer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

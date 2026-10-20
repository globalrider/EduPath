package com.example.edupath.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties("answers")
    private Question question;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @NotBlank(message = "Answer body is required")
    @Column(name = "answer_body", columnDefinition = "TEXT", nullable = false)
    private String answerBody;

    @Column(name = "is_verified_answer", nullable = false)
    private Boolean isVerifiedAnswer = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Answer() {
    }

    public Answer(Question question, User author, String answerBody, Boolean isVerifiedAnswer) {
        this.question = question;
        this.author = author;
        this.answerBody = answerBody;
        this.isVerifiedAnswer = isVerifiedAnswer != null ? isVerifiedAnswer : false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isVerifiedAnswer == null) {
            this.isVerifiedAnswer = false;
        }
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
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

package com.example.edupath.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class AnswerRequest {

    @NotBlank(message = "Answer body cannot be empty")
    private String answerBody;

    public AnswerRequest() {
    }

    public AnswerRequest(String answerBody) {
        this.answerBody = answerBody;
    }

    public String getAnswerBody() {
        return answerBody;
    }

    public void setAnswerBody(String answerBody) {
        this.answerBody = answerBody;
    }
}

package com.example.edupath.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QuestionRequest {

    @NotBlank(message = "Question title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Question body cannot be empty")
    private String questionBody;

    @Size(max = 40, message = "Category cannot exceed 40 characters")
    private String category; // Academics / Internships / Higher Studies / Placement

    public QuestionRequest() {
    }

    public QuestionRequest(String title, String questionBody, String category) {
        this.title = title;
        this.questionBody = questionBody;
        this.category = category;
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
}

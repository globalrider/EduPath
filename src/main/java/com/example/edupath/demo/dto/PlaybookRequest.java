package com.example.edupath.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PlaybookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    private String markdownContent;

    @Size(max = 40, message = "Category cannot exceed 40 characters")
    private String category; // Academics / Internships / Higher Studies / Placement

    @Size(max = 20, message = "Status cannot exceed 20 characters")
    private String status = "PUBLISHED"; // DRAFT / PUBLISHED

    public PlaybookRequest() {
    }

    public PlaybookRequest(String title, String markdownContent, String category, String status) {
        this.title = title;
        this.markdownContent = markdownContent;
        this.category = category;
        this.status = (status != null && !status.isBlank()) ? status : "PUBLISHED";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMarkdownContent() {
        return markdownContent;
    }

    public void setMarkdownContent(String markdownContent) {
        this.markdownContent = markdownContent;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

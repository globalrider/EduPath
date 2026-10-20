package com.example.edupath.demo.dto;

import com.example.edupath.demo.model.PlaybookPost;

import java.time.LocalDateTime;

public class PlaybookResponse {

    private Long postId;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String title;
    private String markdownContent;
    private String category;
    private String status;
    private LocalDateTime createdAt;

    public PlaybookResponse() {
    }

    public PlaybookResponse(Long postId, Long authorId, String authorName, String authorRole, String title, String markdownContent, String category, String status, LocalDateTime createdAt) {
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.title = title;
        this.markdownContent = markdownContent;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static PlaybookResponse fromEntity(PlaybookPost post) {
        if (post == null) return null;
        return new PlaybookResponse(
            post.getPostId(),
            post.getAuthor() != null ? post.getAuthor().getId() : null,
            post.getAuthor() != null ? post.getAuthor().getName() : null,
            post.getAuthor() != null && post.getAuthor().getRole() != null ? post.getAuthor().getRole().name() : null,
            post.getTitle(),
            post.getMarkdownContent(),
            post.getCategory(),
            post.getStatus(),
            post.getCreatedAt()
        );
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

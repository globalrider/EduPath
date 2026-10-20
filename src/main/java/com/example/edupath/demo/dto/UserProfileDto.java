package com.example.edupath.demo.dto;

import com.example.edupath.demo.model.Role;

import java.time.LocalDateTime;

public class UserProfileDto {

    private Long userId;
    private String regNo;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private AlumniProfileDto alumniProfile;

    public UserProfileDto() {
    }

    public UserProfileDto(Long userId, String regNo, String name, String email, Role role, LocalDateTime createdAt, AlumniProfileDto alumniProfile) {
        this.userId = userId;
        this.regNo = regNo;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.alumniProfile = alumniProfile;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AlumniProfileDto getAlumniProfile() {
        return alumniProfile;
    }

    public void setAlumniProfile(AlumniProfileDto alumniProfile) {
        this.alumniProfile = alumniProfile;
    }
}

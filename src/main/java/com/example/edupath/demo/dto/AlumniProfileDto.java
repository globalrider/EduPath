package com.example.edupath.demo.dto;

import com.example.edupath.demo.model.AlumniProfile;
import jakarta.validation.constraints.Size;

public class AlumniProfileDto {

    private Long alumniId;
    private String name;
    private String email;

    @Size(max = 150, message = "Current company must not exceed 150 characters")
    private String currentCompany;

    @Size(max = 150, message = "Job title must not exceed 150 characters")
    private String jobTitle;

    public AlumniProfileDto() {
    }

    public AlumniProfileDto(Long alumniId, String name, String email, String currentCompany, String jobTitle) {
        this.alumniId = alumniId;
        this.name = name;
        this.email = email;
        this.currentCompany = currentCompany;
        this.jobTitle = jobTitle;
    }

    public static AlumniProfileDto fromEntity(AlumniProfile profile) {
        if (profile == null) return null;
        return new AlumniProfileDto(
            profile.getAlumniId(),
            profile.getUser() != null ? profile.getUser().getName() : null,
            profile.getUser() != null ? profile.getUser().getEmail() : null,
            profile.getCurrentCompany(),
            profile.getJobTitle()
        );
    }

    public Long getAlumniId() {
        return alumniId;
    }

    public void setAlumniId(Long alumniId) {
        this.alumniId = alumniId;
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

    public String getCurrentCompany() {
        return currentCompany;
    }

    public void setCurrentCompany(String currentCompany) {
        this.currentCompany = currentCompany;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}

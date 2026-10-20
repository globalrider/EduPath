package com.example.edupath.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "alumni_profiles")
public class AlumniProfile implements Persistable<Long> {

    @Id
    @Column(name = "alumni_id")
    private Long alumniId;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "alumni_id")
    @JsonIgnore
    private User user;

    @Size(max = 150, message = "Current company must not exceed 150 characters")
    @Column(name = "current_company", length = 150)
    private String currentCompany;

    @Size(max = 150, message = "Job title must not exceed 150 characters")
    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Transient
    private boolean isNewEntity = true;

    public AlumniProfile() {
    }

    public AlumniProfile(User user, String currentCompany, String jobTitle) {
        this.user = user;
        this.alumniId = (user != null) ? user.getId() : null;
        this.currentCompany = currentCompany;
        this.jobTitle = jobTitle;
        this.isNewEntity = true;
    }

    @Override
    public Long getId() {
        return alumniId;
    }

    @Override
    public boolean isNew() {
        return isNewEntity;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNewEntity = false;
    }

    public Long getAlumniId() {
        return alumniId;
    }

    public void setAlumniId(Long alumniId) {
        this.alumniId = alumniId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.alumniId = user.getId();
        }
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

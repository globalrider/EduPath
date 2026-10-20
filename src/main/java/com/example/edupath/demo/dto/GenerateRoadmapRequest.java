package com.example.edupath.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GenerateRoadmapRequest {

    @NotBlank(message = "Career goal is required (e.g. 'Full Stack Developer', 'Cloud & DevOps Engineer')")
    @Size(max = 255, message = "Goal must not exceed 255 characters")
    private String goal;

    @Size(max = 200, message = "Custom title is optional")
    private String title;

    private Integer targetSemester;

    public GenerateRoadmapRequest() {
    }

    public GenerateRoadmapRequest(String goal, String title, Integer targetSemester) {
        this.goal = goal;
        this.title = title;
        this.targetSemester = targetSemester;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTargetSemester() {
        return targetSemester;
    }

    public void setTargetSemester(Integer targetSemester) {
        this.targetSemester = targetSemester;
    }
}

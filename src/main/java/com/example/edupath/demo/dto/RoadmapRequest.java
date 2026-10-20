package com.example.edupath.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoadmapRequest {

    @NotBlank(message = "Career goal is required")
    @Size(max = 255, message = "Goal must not exceed 255 characters")
    private String goal;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    public RoadmapRequest() {
    }

    public RoadmapRequest(String goal, String title) {
        this.goal = goal;
        this.title = title;
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
}

package com.example.edupath.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class AiPathRequest {

    @NotBlank(message = "Goal cannot be empty (e.g., Targeting Off-Campus Cloud SDE by Sem 7)")
    private String goal;

    private Integer currentSemester = 3;

    private Double currentCgpa;

    public AiPathRequest() {
    }

    public AiPathRequest(String goal, Integer currentSemester, Double currentCgpa) {
        this.goal = goal;
        this.currentSemester = currentSemester;
        this.currentCgpa = currentCgpa;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public Integer getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(Integer currentSemester) {
        this.currentSemester = currentSemester;
    }

    public Double getCurrentCgpa() {
        return currentCgpa;
    }

    public void setCurrentCgpa(Double currentCgpa) {
        this.currentCgpa = currentCgpa;
    }
}

package com.example.edupath.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AiPathResponse {

    private String goal;
    private Integer currentSemester;
    private List<String> adaptiveMilestones;
    private List<String> recommendedRoadmapSkills;
    private String seniorMentorAdvice;
    private LocalDateTime generatedAt;

    public AiPathResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    public AiPathResponse(String goal, Integer currentSemester, List<String> adaptiveMilestones, List<String> recommendedRoadmapSkills, String seniorMentorAdvice) {
        this.goal = goal;
        this.currentSemester = currentSemester;
        this.adaptiveMilestones = adaptiveMilestones;
        this.recommendedRoadmapSkills = recommendedRoadmapSkills;
        this.seniorMentorAdvice = seniorMentorAdvice;
        this.generatedAt = LocalDateTime.now();
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

    public List<String> getAdaptiveMilestones() {
        return adaptiveMilestones;
    }

    public void setAdaptiveMilestones(List<String> adaptiveMilestones) {
        this.adaptiveMilestones = adaptiveMilestones;
    }

    public List<String> getRecommendedRoadmapSkills() {
        return recommendedRoadmapSkills;
    }

    public void setRecommendedRoadmapSkills(List<String> recommendedRoadmapSkills) {
        this.recommendedRoadmapSkills = recommendedRoadmapSkills;
    }

    public String getSeniorMentorAdvice() {
        return seniorMentorAdvice;
    }

    public void setSeniorMentorAdvice(String seniorMentorAdvice) {
        this.seniorMentorAdvice = seniorMentorAdvice;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}

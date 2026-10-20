package com.example.edupath.demo.service.impl;

import com.example.edupath.demo.dto.AiPathRequest;
import com.example.edupath.demo.dto.AiPathResponse;
import com.example.edupath.demo.service.AiAssistantService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    @Override
    public AiPathResponse generatePath(AiPathRequest request) {
        String goal = request.getGoal();
        int sem = request.getCurrentSemester() != null ? request.getCurrentSemester() : 3;

        List<String> adaptiveMilestones = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        String mentorAdvice;

        String lowerGoal = goal.toLowerCase();

        if (lowerGoal.contains("cloud") || lowerGoal.contains("devops") || lowerGoal.contains("sde")) {
            adaptiveMilestones.add("1. Learn AWS ECS & Docker containerization (Sem " + (sem + 2) + ")");
            adaptiveMilestones.add("2. Contribute to Open Source Spring repositories (Sem " + (sem + 3) + ")");
            adaptiveMilestones.add("3. Complete 5 mock interviews on EduPath Q&A Forum");
            adaptiveMilestones.add("4. Build and deploy a multi-container microservice with CI/CD pipeline");

            skills.addAll(List.of("Docker & Kubernetes", "AWS / GCP Core Services", "Spring Boot Microservices", "PostgreSQL / MySQL Indexing", "Kafka / RabbitMQ"));
            mentorAdvice = "Focus heavily on system design basics and solve at least 150 LeetCode Mediums before starting campus recruitment drives.";
        } else if (lowerGoal.contains("ai") || lowerGoal.contains("ml") || lowerGoal.contains("data")) {
            adaptiveMilestones.add("1. Master NumPy, Pandas, and Scikit-Learn with real Kaggle datasets (Sem " + (sem + 1) + ")");
            adaptiveMilestones.add("2. Build an end-to-end RAG application with LangChain and vector databases (Sem " + (sem + 2) + ")");
            adaptiveMilestones.add("3. Publish an open-source technical paper or project breakdown on Senior Feed (Sem " + (sem + 3) + ")");

            skills.addAll(List.of("Python", "PyTorch / TensorFlow", "Vector DBs (Chroma/Pinecone)", "FastAPI / Spring AI", "LLM Fine-tuning"));
            mentorAdvice = "Math and linear algebra fundamentals are critical. Pair your ML models with clean REST APIs.";
        } else {
            adaptiveMilestones.add("1. Solidify Core Data Structures (Arrays, Trees, Graphs, DP) by Sem " + (sem + 1));
            adaptiveMilestones.add("2. Build 2 enterprise-grade portfolio projects using Spring Boot and React");
            adaptiveMilestones.add("3. Participate in 3 college hackathons and engage actively with alumni on the mentorship portal");

            skills.addAll(List.of("Core Java OOP", "Spring Boot REST APIs", "Database Optimization", "System Design", "Git & GitHub"));
            mentorAdvice = "Consistency matters more than cramming. Dedicate 2 hours daily to algorithmic problem solving.";
        }

        return new AiPathResponse(
            goal,
            sem,
            adaptiveMilestones,
            skills,
            mentorAdvice
        );
    }
}

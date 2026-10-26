package com.example.edupath.demo.service.impl;

import com.example.edupath.demo.dto.AiPathRequest;
import com.example.edupath.demo.dto.AiPathResponse;
import com.example.edupath.demo.service.AiAssistantService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantServiceImpl.class);

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String configuredApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String configuredModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public AiAssistantServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public AiPathResponse generatePath(AiPathRequest request) {
        String apiKey = resolveApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is not configured. Please set the GEMINI_API_KEY environment variable or gemini.api.key property."
            );
        }

        if (request == null || request.getGoal() == null || request.getGoal().isBlank()) {
            throw new IllegalArgumentException("Career goal cannot be empty.");
        }

        int semester = request.getCurrentSemester() != null ? request.getCurrentSemester() : 3;
        if (semester < 1 || semester > 8) {
            throw new IllegalArgumentException("Current semester must be between 1 and 8.");
        }

        Double cgpa = request.getCurrentCgpa();
        if (cgpa != null && (!Double.isFinite(cgpa) || cgpa < 0 || cgpa > 10)) {
            throw new IllegalArgumentException("CGPA must be between 0 and 10.");
        }

        String model = (configuredModel != null && !configuredModel.isBlank())
                ? configuredModel.trim()
                : "gemini-2.5-flash";

        try {
            return callGemini(apiKey, model, request, semester, cgpa);
        } catch (Exception e) {
            log.warn("Gemini API call failed: {}. Generating adaptive fallback roadmap.", e.getMessage());
            // If Gemini call failed (e.g. key leaked, quota reached, network issue), provide graceful fallback
            return generateFallbackRoadmap(request.getGoal().trim(), semester, cgpa, e.getMessage());
        }
    }

    private String resolveApiKey() {
        if (configuredApiKey != null && !configuredApiKey.isBlank()) {
            return configuredApiKey.trim();
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        return System.getProperty("GEMINI_API_KEY");
    }

    private AiPathResponse callGemini(String apiKey, String model, AiPathRequest request, int semester, Double cgpa)
            throws IOException, InterruptedException {

        String prompt = """
                You are EduPath, an expert career roadmap mentor for undergraduate computer science students in India.
                Generate a realistic, personalized career roadmap.

                Student profile:
                - Career goal: %s
                - Current semester: %d
                - Current CGPA: %s

                Requirements:
                1. Adapt the roadmap to the student's career goal, current semester, and academic position.
                2. Give 5 to 8 ordered, actionable milestones.
                3. Include practical projects, prerequisites, and internship or placement preparation when relevant.
                4. Do not assume the student already knows advanced skills.
                5. Recommend 5 to 8 relevant technical skills.
                6. Provide specific, useful senior mentor advice.
                7. Use realistic semester numbers from 1 to 8.
                8. Avoid guaranteeing jobs, internships, or outcomes.
                9. Return only valid JSON with these exact fields:
                   goal, currentSemester, adaptiveMilestones, recommendedRoadmapSkills, seniorMentorAdvice.
                10. adaptiveMilestones and recommendedRoadmapSkills must be arrays of strings.
                11. Do not wrap the JSON in Markdown code fences.
                """.formatted(
                request.getGoal().trim(),
                semester,
                cgpa == null ? "Not provided" : cgpa.toString()
        );

        ObjectNode payload = objectMapper.createObjectNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", prompt);

        ObjectNode content = objectMapper.createObjectNode();
        content.putArray("parts").add(part);
        payload.putArray("contents").add(content);

        ObjectNode generationConfig = payload.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("temperature", 0.4);
        generationConfig.put("maxOutputTokens", 3000);

        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 503 || response.statusCode() == 429) {
            throw new IllegalStateException("Our AI model is currently busy. Please try again in a few moments.");
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorDetail = "";
            try {
                JsonNode errRoot = objectMapper.readTree(response.body());
                JsonNode msgNode = errRoot.path("error").path("message");
                if (msgNode.isTextual() && !msgNode.asText().isBlank()) {
                    errorDetail = msgNode.asText();
                }
            } catch (Exception ignored) {}

            if (errorDetail.contains("leaked")) {
                throw new IllegalStateException(
                        "Your Gemini API key was reported as leaked by Google. Please generate a fresh key at https://aistudio.google.com/app/apikey and update GEMINI_API_KEY."
                );
            }
            throw new IllegalStateException(
                    "AI service request failed with HTTP " + response.statusCode() + ": " + (errorDetail.isBlank() ? response.body() : errorDetail)
            );
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.path("candidates");

        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new IllegalStateException("Gemini returned no usable response.");
        }

        JsonNode parts = candidates.get(0).path("content").path("parts");
        StringBuilder generatedText = new StringBuilder();

        if (parts.isArray()) {
            for (JsonNode responsePart : parts) {
                JsonNode textNode = responsePart.get("text");
                if (textNode != null && textNode.isTextual()) {
                    generatedText.append(textNode.asText());
                }
            }
        }

        String rawJson = stripMarkdownFences(generatedText.toString());
        if (rawJson.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty roadmap.");
        }

        AiPathResponse result = objectMapper.readValue(rawJson, AiPathResponse.class);

        result.setGoal(request.getGoal().trim());
        result.setCurrentSemester(semester);

        if (result.getAdaptiveMilestones() == null || result.getAdaptiveMilestones().isEmpty()
                || result.getRecommendedRoadmapSkills() == null || result.getRecommendedRoadmapSkills().isEmpty()
                || result.getSeniorMentorAdvice() == null || result.getSeniorMentorAdvice().isBlank()) {
            throw new IllegalStateException("Gemini returned an incomplete roadmap.");
        }

        result.setAdaptiveMilestones(cleanList(result.getAdaptiveMilestones()));
        result.setRecommendedRoadmapSkills(cleanList(result.getRecommendedRoadmapSkills()));

        return result;
    }

    private String stripMarkdownFences(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private List<String> cleanList(List<String> values) {
        List<String> cleaned = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                cleaned.add(value.trim());
            }
        }
        if (cleaned.isEmpty()) {
            throw new IllegalStateException("Gemini returned an empty roadmap section.");
        }
        return cleaned;
    }

    private AiPathResponse generateFallbackRoadmap(String goal, int sem, Double cgpa, String reason) {
        String lowerGoal = goal.toLowerCase();
        List<String> adaptiveMilestones = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        String mentorAdvice;

        if (lowerGoal.contains("cloud") || lowerGoal.contains("devops") || lowerGoal.contains("sde") || lowerGoal.contains("backend") || lowerGoal.contains("software")) {
            adaptiveMilestones.add("Sem " + sem + ": Solidify Advanced OOP in Java/C++ and master Core Data Structures (Trees, Graphs, DP)");
            adaptiveMilestones.add("Sem " + Math.min(sem + 1, 8) + ": Build full-stack enterprise microservices using Spring Boot, Hibernate, and PostgreSQL");
            adaptiveMilestones.add("Sem " + Math.min(sem + 2, 8) + ": Containerize apps with Docker, set up CI/CD GitHub Actions, and deploy to AWS / GCP");
            adaptiveMilestones.add("Sem " + Math.min(sem + 2, 8) + ": Solve 150+ LeetCode Medium problems focusing on Sliding Window and Graphs");
            adaptiveMilestones.add("Sem " + Math.min(sem + 3, 8) + ": Complete High-Level & Low-Level System Design mock rounds and contribute to open source");
            adaptiveMilestones.add("Sem " + Math.min(sem + 3, 8) + ": Apply for summer SDE internships and participate in college hackathons");

            skills.addAll(List.of("Java / Spring Boot", "Docker & Kubernetes", "PostgreSQL / MySQL", "AWS Core Services", "System Design (LLD/HLD)", "Git & CI/CD Pipelines"));
            mentorAdvice = "Focus on clean, production-ready code. Hiring managers prioritize students who understand database indexing, REST design, and concurrency fundamentals.";
        } else if (lowerGoal.contains("ai") || lowerGoal.contains("ml") || lowerGoal.contains("data") || lowerGoal.contains("machine learning")) {
            adaptiveMilestones.add("Sem " + sem + ": Master Linear Algebra, Multivariable Calculus, and Python Data Stack (NumPy, Pandas, Scikit-Learn)");
            adaptiveMilestones.add("Sem " + Math.min(sem + 1, 8) + ": Build end-to-end Machine Learning pipelines on Kaggle datasets and track experiments");
            adaptiveMilestones.add("Sem " + Math.min(sem + 2, 8) + ": Deep Learning with PyTorch — build Convolutional & Transformer architectures");
            adaptiveMilestones.add("Sem " + Math.min(sem + 2, 8) + ": Build LLM / RAG applications using LangChain, Vector Databases (Chroma/Pinecone), and FastAPI");
            adaptiveMilestones.add("Sem " + Math.min(sem + 3, 8) + ": Publish technical walk-throughs or research summaries on EduPath Senior Feed");
            adaptiveMilestones.add("Sem " + Math.min(sem + 3, 8) + ": Target AI/ML Engineer and Data Science internships with verified portfolio projects");

            skills.addAll(List.of("Python", "PyTorch", "Scikit-Learn", "Vector Databases", "FastAPI / Spring AI", "LLM RAG Systems", "SQL & Data Modeling"));
            mentorAdvice = "Do not treat ML libraries like black boxes. Understand the gradient descent math under the hood and always deploy your models behind interactive APIs.";
        } else {
            adaptiveMilestones.add("Sem " + sem + ": Core Computer Science Fundamentals — Master Data Structures, Algorithms, and OOP Principles");
            adaptiveMilestones.add("Sem " + Math.min(sem + 1, 8) + ": Build 2 production-quality full-stack applications with authenticated REST APIs and database persistence");
            adaptiveMilestones.add("Sem " + Math.min(sem + 2, 8) + ": Practice algorithmic problem solving daily on LeetCode / HackerRank (target 200+ solved)");
            adaptiveMilestones.add("Sem " + Math.min(sem + 2, 8) + ": Study Operating Systems, DBMS internals, and Computer Networks for placement screening");
            adaptiveMilestones.add("Sem " + Math.min(sem + 3, 8) + ": Build capstone portfolio project, optimize GitHub profile, and attend mock interview drives");

            skills.addAll(List.of("Core Java / OOP", "Data Structures & Algorithms", "Spring Boot REST APIs", "Database Optimization", "Git & GitHub Collaboration"));
            mentorAdvice = "Consistency beats intensity. Dedicate 90 minutes every day to algorithmic thinking and build projects that solve real campus problems.";
        }

        if (reason != null && !reason.isBlank()) {
            mentorAdvice += " [Note: " + reason + "]";
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

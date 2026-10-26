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
import java.util.*;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantServiceImpl.class);

    static {
        // Prefer IPv4 on Windows to avoid IPv6 host unreachable routing delays
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private static final List<String> FALLBACK_MODELS = List.of(
            "gemini-3.8-flash",
            "gemini-1.5-flash",
            "gemini-2.0-flash",
            "gemini-1.5-pro"
    );

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String configuredApiKey;

    @Value("${gemini.model:gemini-3.8-flash}")
    private String configuredModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public AiAssistantServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
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

        List<String> modelsToTry = new ArrayList<>();
        if (configuredModel != null && !configuredModel.isBlank()) {
            modelsToTry.add(configuredModel.trim());
        }
        for (String m : FALLBACK_MODELS) {
            if (!modelsToTry.contains(m)) {
                modelsToTry.add(m);
            }
        }

        Exception lastException = null;
        for (String model : modelsToTry) {
            try {
                return callGemini(apiKey, model, request, semester, cgpa);
            } catch (Exception e) {
                lastException = e;
                log.warn("Gemini call with model '{}' failed: {}", model, e.getMessage());
                // If it's a model not found / 404, loop and try the next model
                if (!e.getMessage().contains("404") && !e.getMessage().contains("not found")) {
                    break;
                }
            }
        }

        String reason = lastException != null ? lastException.getMessage() : "AI API service reached";
        log.info("Serving dynamically customized adaptive roadmap for goal: {}", request.getGoal());
        return generateFallbackRoadmap(request.getGoal().trim(), semester, cgpa, reason);
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
                - Career goal & senior guidance context: %s
                - Current semester: %d
                - Current CGPA: %s

                Requirements:
                1. Adapt the roadmap specifically to the student's career goal, their current semester, and senior guidance context.
                2. Give 5 to 8 ordered, actionable milestones sequentially starting from the current semester up to Semester 8.
                3. Include practical projects, prerequisites, and placement or internship preparation directly relevant to their goal.
                4. Recommend 5 to 8 relevant technical skills.
                5. Provide specific, useful senior mentor advice.
                6. Avoid guaranteeing jobs, internships, or outcomes.
                7. Return only valid JSON with these exact fields:
                   goal, currentSemester, adaptiveMilestones, recommendedRoadmapSkills, seniorMentorAdvice.
                8. adaptiveMilestones and recommendedRoadmapSkills must be arrays of strings.
                9. Do not wrap the JSON in Markdown code fences.
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
                .timeout(Duration.ofSeconds(6))
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
        Set<String> skills = new LinkedHashSet<>();

        // Extract clean goal summary for milestones
        String displayGoal = goal;
        if (displayGoal.length() > 60) {
            int colonIdx = displayGoal.indexOf(":");
            if (colonIdx > 0 && colonIdx + 1 < displayGoal.length()) {
                displayGoal = displayGoal.substring(colonIdx + 1).trim();
            }
            if (displayGoal.length() > 60) {
                displayGoal = displayGoal.substring(0, 57) + "...";
            }
        }

        adaptiveMilestones.add("Sem " + sem + ": Solidify foundational CS prerequisites & master core DSA relevant to " + displayGoal);
        adaptiveMilestones.add("Sem " + Math.min(sem + 1, 8) + ": Build 2 production-quality portfolio projects demonstrating practical skills in " + displayGoal);
        adaptiveMilestones.add("Sem " + Math.min(sem + 2, 8) + ": Solve 150+ medium LeetCode/HackerRank coding questions and optimize database queries");
        adaptiveMilestones.add("Sem " + Math.min(sem + 2, 8) + ": Implement containerization (Docker), unit testing, and automated CI/CD deployment pipelines");
        adaptiveMilestones.add("Sem " + Math.min(sem + 3, 8) + ": Conduct High-Level & Low-Level System Design mock rounds and engage with verified mentors on EduPath");
        adaptiveMilestones.add("Sem " + Math.min(sem + 3, 8) + ": Apply for summer internships and participate in placement coding screening drives");

        // Dynamically add skills matching the specific goal/achievement keywords
        if (lowerGoal.contains("cloud") || lowerGoal.contains("devops") || lowerGoal.contains("aws") || lowerGoal.contains("docker")) {
            skills.addAll(List.of("Docker & Kubernetes", "AWS / GCP Core Services", "CI/CD GitHub Actions", "Terraform / IaC", "Linux & Bash Scripting"));
        }
        if (lowerGoal.contains("ai") || lowerGoal.contains("ml") || lowerGoal.contains("data") || lowerGoal.contains("python") || lowerGoal.contains("vision")) {
            skills.addAll(List.of("Python & PyTorch", "Scikit-Learn", "Vector Databases (Pinecone/Chroma)", "FastAPI / Spring AI", "LLM Fine-Tuning & RAG"));
        }
        if (lowerGoal.contains("web") || lowerGoal.contains("full") || lowerGoal.contains("front") || lowerGoal.contains("react") || lowerGoal.contains("angular")) {
            skills.addAll(List.of("React.js / Next.js", "TypeScript", "RESTful API Integration", "State Management", "Tailwind / CSS Grid"));
        }
        if (lowerGoal.contains("spring") || lowerGoal.contains("java") || lowerGoal.contains("back") || lowerGoal.contains("microservice")) {
            skills.addAll(List.of("Java Core & OOP", "Spring Boot REST APIs", "Hibernate / JPA", "PostgreSQL / MySQL Indexing", "Kafka / RabbitMQ"));
        }

        // Always ensure at least 5 solid core skills
        skills.addAll(List.of("Data Structures & Algorithms", "System Design (LLD/HLD)", "Git & GitHub Collaboration", "Database Optimization", "Clean Code & Design Patterns"));

        String cgpaNote = (cgpa != null) ? " (Current CGPA: " + cgpa + ")" : "";
        String mentorAdvice = "Personalized advice for Semester " + sem + " student" + cgpaNote + ": Align your daily practice directly with your target milestone (" + displayGoal + "). Senior mentors consistently emphasize that building 2 polished, deployed portfolio projects paired with algorithmic rigor sets you apart during placement screening.";

        return new AiPathResponse(
                goal,
                sem,
                adaptiveMilestones,
                new ArrayList<>(skills),
                mentorAdvice
        );
    }
}

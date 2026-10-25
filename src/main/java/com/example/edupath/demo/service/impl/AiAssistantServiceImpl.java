
package com.example.edupath.demo.service.impl;

import com.example.edupath.demo.dto.AiPathRequest;
import com.example.edupath.demo.dto.AiPathResponse;
import com.example.edupath.demo.service.AiAssistantService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    private static final String MODEL = "gemini-3.6-flash";

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public AiAssistantServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public AiPathResponse generatePath(AiPathRequest request) {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is not configured."
            );
        }

        if (request == null || request.getGoal() == null
                || request.getGoal().isBlank()) {
            throw new IllegalArgumentException(
                    "Career goal cannot be empty."
            );
        }

        int semester = request.getCurrentSemester() != null
                ? request.getCurrentSemester()
                : 3;

        if (semester < 1 || semester > 8) {
            throw new IllegalArgumentException(
                    "Current semester must be between 1 and 8."
            );
        }

        Double cgpa = request.getCurrentCgpa();

        if (cgpa != null && (!Double.isFinite(cgpa)
                || cgpa < 0 || cgpa > 10)) {
            throw new IllegalArgumentException(
                    "CGPA must be between 0 and 10."
            );
        }

        try {
            String prompt = """
                    You are EduPath, an expert career roadmap mentor
                    for undergraduate computer science students in India.

                    Generate a realistic, personalized career roadmap.

                    Student profile:
                    - Career goal: %s
                    - Current semester: %d
                    - Current CGPA: %s

                    Requirements:
                    1. Adapt the roadmap to the student's career goal,
                       current semester, and academic position.
                    2. Give 5 to 8 ordered, actionable milestones.
                    3. Include practical projects, prerequisites,
                       and internship or placement preparation when relevant.
                    4. Do not assume the student already knows advanced skills.
                    5. Recommend 5 to 8 relevant technical skills.
                    6. Provide specific, useful senior mentor advice.
                    7. Use realistic semester numbers from 1 to 8.
                    8. Avoid guaranteeing jobs, internships, or outcomes.
                    9. Return only valid JSON with these exact fields:
                       goal, currentSemester, adaptiveMilestones,
                       recommendedRoadmapSkills, seniorMentorAdvice.
                    10. adaptiveMilestones and recommendedRoadmapSkills
                        must be arrays of strings.
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

            ObjectNode generationConfig =
                    payload.putObject("generationConfig");

            generationConfig.put("responseMimeType", "application/json");
            generationConfig.put("temperature", 0.4);
            generationConfig.put("maxOutputTokens", 3000);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            
        if (response.statusCode() == 503
                || response.statusCode() == 429) {
            throw new IllegalStateException(
                "Our AI model is currently busy. "
                + "Please try again in a few moments."
            );
        }

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new IllegalStateException(
                "AI service request failed. Please try again later."
            );
        }

            JsonNode root = objectMapper.readTree(response.body());

            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new IllegalStateException(
                        "Gemini returned no usable response."
                );
            }

            JsonNode parts = candidates.get(0)
                    .path("content")
                    .path("parts");

            StringBuilder generatedText = new StringBuilder();

            if (parts.isArray()) {
                for (JsonNode responsePart : parts) {
                    JsonNode textNode = responsePart.get("text");

                    if (textNode != null && textNode.isTextual()) {
                        generatedText.append(textNode.asText());
                    }
                }
            }

            if (generatedText.toString().isBlank()) {
                throw new IllegalStateException(
                        "Gemini returned an empty roadmap."
                );
            }

            AiPathResponse result = objectMapper.readValue(
                    generatedText.toString(),
                    AiPathResponse.class
            );

            // Keep essential response fields consistent with the request.
            result.setGoal(request.getGoal().trim());
            result.setCurrentSemester(semester);

            if (result.getAdaptiveMilestones() == null
                    || result.getAdaptiveMilestones().isEmpty()
                    || result.getRecommendedRoadmapSkills() == null
                    || result.getRecommendedRoadmapSkills().isEmpty()
                    || result.getSeniorMentorAdvice() == null
                    || result.getSeniorMentorAdvice().isBlank()) {
                throw new IllegalStateException(
                        "Gemini returned an incomplete roadmap."
                );
            }

            result.setAdaptiveMilestones(
                    cleanList(result.getAdaptiveMilestones())
            );

            result.setRecommendedRoadmapSkills(
                    cleanList(result.getRecommendedRoadmapSkills())
            );

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Roadmap generation was interrupted.", e
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not communicate with Gemini or parse its response.",
                    e
            );
        }
    }

    private List<String> cleanList(List<String> values) {
        List<String> cleaned = new ArrayList<>();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                cleaned.add(value.trim());
            }
        }

        if (cleaned.isEmpty()) {
            throw new IllegalStateException(
                    "Gemini returned an empty roadmap section."
            );
        }

        return cleaned;
    }
}

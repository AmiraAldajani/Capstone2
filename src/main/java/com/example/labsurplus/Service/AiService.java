package com.example.labsurplus.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiService {

    private final RestClient restClient;
    private final String model;

    // المفتاح والموديل ينقرون من application.properties، مو مكتوبين في الكود
    public AiService(@Value("${gemini.api.key}") String apiKey,
                     @Value("${ai.model}") String model) {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
        this.model = model;
    }

    // يرجع رد الموديل كنص، أو null لو فشل الطلب
    public String ask(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of("maxOutputTokens", 2048));
        try {
            GeminiResponse response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GeminiResponse.class);
            return extractText(response);
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("AI rate limit reached (free tier). Try again later");
            return null;
        } catch (RestClientException e) {
            log.error("AI call failed: {}", e.getMessage());
            return null;
        }
    }

    // الرد يجي على شكل candidates -> content -> parts، نجمع النصوص من أول candidate
    private String extractText(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty())
            return null;
        Candidate first = response.candidates().get(0);
        if (first.content() == null || first.content().parts() == null)
            return null;
        StringBuilder text = new StringBuilder();
        for (Part part : first.content().parts())
            if (part.text() != null && !Boolean.TRUE.equals(part.thought()))
                text.append(part.text());
        return text.isEmpty() ? null : text.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeminiResponse(List<Candidate> candidates) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content, String finishReason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(List<Part> parts, String role) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(String text, Boolean thought) {}
}

package com.studymate.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final WebClient webClient;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:llama3.2:3b}")
    private String model;

    public AiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String generate(String feature, String userText) {

        String prompt = buildPrompt(feature, userText);

        try {
            JsonNode response = webClient.post()
                .uri(ollamaUrl + "/api/chat")
                .bodyValue(Map.of(
                    "model", model,
                    "messages", List.of(
                        Map.of(
                            "role", "user",
                            "content", prompt
                        )
                    ),
                    "stream", false
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(180));

            String output = "";

            if (response != null) {
                output = response.path("message")
                    .path("content")
                    .asText("");
            }

            if (output.isBlank()) {
                throw new IllegalStateException(
                    "AI returned an empty response. Please try again."
                );
            }

            return output.trim();

        } catch (WebClientResponseException ex) {
            throw new IllegalStateException(
                "Ollama request failed (HTTP "
                + ex.getStatusCode().value() + "). "
                + ex.getResponseBodyAsString(),
                ex
            );

        } catch (IllegalStateException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new IllegalStateException(
                "Could not connect to Ollama. Check whether Ollama is running.",
                ex
            );
        }
    }

    private String buildPrompt(String feature, String text) {

        String instructions;

        switch (feature.toLowerCase()) {

            case "summarizer":
                instructions = "Summarize the material. Use a clear title, key points, important definitions, and a short recap. Preserve facts and do not invent information. Use student-friendly language.";
                break;

            case "quiz":
                instructions = "Create 5 multiple-choice questions strictly from the material. Give four options (A-D) for each question, then an answer key with a one-sentence explanation. Avoid ambiguous questions.";
                break;

            case "explainer":
                instructions = "Explain the topic in simple language for a college student. Include a definition, step-by-step explanation, one practical example, and a short recap.";
                break;

            case "improver":
                instructions = "Improve the student's answer without changing its meaning. Correct grammar, structure and clarity. Return the improved answer first, followed by a short list of improvements. Do not add unsupported facts.";
                break;

            case "flashcards":
                instructions = "Create 8 concise study flashcards from the material. Format each as Card number, Question, and Answer. Keep answers short and grounded in the material.";
                break;

            case "assistant":
                instructions = "You are StudyMate AI, a responsible study assistant. Answer clearly and accurately, using examples when useful. If information is missing, say so instead of inventing facts.";
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown feature selected."
                );
        }

        return "You are StudyMate AI, an educational assistant.\n\n"
            + "Instructions:\n"
            + instructions
            + "\n\nStudent input:\n"
            + text;
    }
}

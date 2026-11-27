
package com.studymate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.embedding-model:nomic-embed-text}")
    private String embeddingModel;

    public EmbeddingService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper) {

        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public List<Double> generateEmbedding(String text) {

        try {
            Map<String, Object> request = Map.of(
                    "model", embeddingModel,
                    "input", text
            );

            String response = webClient.post()
                    .uri(ollamaUrl + "/api/embed")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);

            JsonNode embeddingNode =
                    root.path("embeddings").path(0);

            if (!embeddingNode.isArray()
                    || embeddingNode.isEmpty()) {
                throw new IllegalStateException(
                        "Ollama returned an empty embedding."
                );
            }

            List<Double> embedding = new ArrayList<>();

            for (JsonNode value : embeddingNode) {
                embedding.add(value.asDouble());
            }

            return embedding;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate document embedding: "
                            + e.getMessage(), e
            );
        }
    }
}


package com.studymate.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class VectorStore {

    private final List<DocumentChunk> chunks = new ArrayList<>();

    public void addChunk(
            String filename,
            String text,
            List<Double> embedding) {

        DocumentChunk chunk = new DocumentChunk(
                filename,
                text,
                embedding
        );

        chunks.add(chunk);
    }

    public List<DocumentChunk> search(
            List<Double> queryEmbedding,
            int topK) {

        return chunks.stream()
                .map(chunk -> new SearchResult(
                        chunk,
                        cosineSimilarity(
                                queryEmbedding,
                                chunk.embedding()
                        )
                ))
                .sorted(Comparator.comparingDouble(
                        SearchResult::score
                ).reversed())
                .limit(topK)
                .map(SearchResult::chunk)
                .toList();
    }

    private double cosineSimilarity(
            List<Double> vectorA,
            List<Double> vectorB) {

        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException(
                    "Embedding dimensions do not match."
            );
        }

        double dotProduct = 0;
        double magnitudeA = 0;
        double magnitudeB = 0;

        for (int i = 0; i < vectorA.size(); i++) {

            dotProduct += vectorA.get(i) * vectorB.get(i);

            magnitudeA += vectorA.get(i) * vectorA.get(i);

            magnitudeB += vectorB.get(i) * vectorB.get(i);
        }

        if (magnitudeA == 0 || magnitudeB == 0) {
            return 0;
        }

        return dotProduct /
                (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }

    public record DocumentChunk(
            String filename,
            String text,
            List<Double> embedding) {
    }

    private record SearchResult(
            DocumentChunk chunk,
            double score) {
    }
}

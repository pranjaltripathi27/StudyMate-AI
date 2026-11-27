
package com.studymate.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final DocumentService documentService;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final AiService aiService;

    public RagService(
            DocumentService documentService,
            TextChunker textChunker,
            EmbeddingService embeddingService,
            VectorStore vectorStore,
            AiService aiService) {

        this.documentService = documentService;
        this.textChunker = textChunker;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.aiService = aiService;
    }

    public String processDocument(
            org.springframework.web.multipart.MultipartFile file)
            throws java.io.IOException {

        String text = documentService.extractText(file);

        List<String> chunks = textChunker.splitText(text);

        for (String chunk : chunks) {

            List<Double> embedding =
                    embeddingService.generateEmbedding(chunk);

            vectorStore.addChunk(
                    file.getOriginalFilename(),
                    chunk,
                    embedding
            );
        }

        return "Document processed successfully. Total chunks: "
                + chunks.size();
    }

    public String askQuestion(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question cannot be empty."
            );
        }

        List<Double> queryEmbedding =
                embeddingService.generateEmbedding(question);

        List<VectorStore.DocumentChunk> results =
                vectorStore.search(queryEmbedding, 3);

        if (results.isEmpty()) {
            return "No relevant document content found.";
        }

        String context = results.stream()
                .map(VectorStore.DocumentChunk::text)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                Answer the question using ONLY the document context below.

                If the answer is not available in the context,
                clearly say that the uploaded documents do not contain
                sufficient information.

                DOCUMENT CONTEXT:
                %s

                QUESTION:
                %s

                Provide a clear and accurate answer.
                """.formatted(context, question);

        return aiService.generateRagAnswer(prompt);
    }
}

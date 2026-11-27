
package com.studymate.controller;

import com.studymate.service.DocumentService;
import com.studymate.service.RagService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin
public class DocumentController {

    private final DocumentService documentService;
    private final RagService ragService;

    public DocumentController(
            DocumentService documentService,
            RagService ragService) {

        this.documentService = documentService;
        this.ragService = ragService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file) {

        try {
            String text = documentService.extractText(file);

            if (text == null || text.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No readable text found in document.")
                );
            }

            String ragMessage = ragService.processDocument(file);

            return ResponseEntity.ok(
                    Map.of(
                            "filename", file.getOriginalFilename(),
                            "message", "Document uploaded and text extracted successfully.",
                            "ragStatus", ragMessage,
                            "textLength", text.length(),
                            "text", text
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Unable to read the uploaded document.")
            );
        }
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askDocumentQuestion(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");

        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Question cannot be empty.")
            );
        }

        try {
            String answer = ragService.askQuestion(question);

            return ResponseEntity.ok(
                    Map.of(
                            "question", question,
                            "answer", answer
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Unable to process your question.")
            );
        }
    }
}

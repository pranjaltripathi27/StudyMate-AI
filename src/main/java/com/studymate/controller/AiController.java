package com.studymate.controller;
import com.studymate.dto.AiRequest;
import com.studymate.service.AiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private static final Set<String> FEATURES = Set.of(
        "summarizer", "quiz", "explainer", "improver", "flashcards", "assistant");
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/{feature}")
    public Map<String, String> generate(@PathVariable String feature, @Valid @RequestBody AiRequest request) {
        String normalized = feature.toLowerCase();
        if (!FEATURES.contains(normalized)) throw new IllegalArgumentException("Unsupported feature.");
        return Map.of("result", aiService.generate(normalized, request.text().trim()));
    }
}

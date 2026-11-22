package com.studymate.dto;
import jakarta.validation.constraints.NotBlank;

public record AiRequest(@NotBlank(message = "Please enter some text first.") String text) {}

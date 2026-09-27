package com.newsplatform.newspaper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record NewspaperRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 240, message = "Title must be 240 characters or fewer") String title,
        @NotBlank(message = "Edition is required")
        @Size(max = 120, message = "Edition must be 120 characters or fewer") String edition,
        @NotNull(message = "Edition date is required") LocalDate editionDate
) { }

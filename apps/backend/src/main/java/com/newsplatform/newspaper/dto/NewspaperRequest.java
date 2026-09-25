package com.newsplatform.newspaper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record NewspaperRequest(
        @NotBlank @Size(max = 240) String title,
        @NotBlank @Size(max = 120) String edition,
        @NotNull LocalDate editionDate
) { }

package com.newsplatform.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(@NotBlank @Size(max = 120) String name, @Size(max = 140) String slug) { }

package com.newsplatform.category.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(@NotBlank @Size(max = 120) String name, @Size(max = 140) String slug,
                              @Size(max = 500) String description, String status, @Min(0) @Max(100000) Integer displayOrder,
                              java.util.UUID parentId) { }

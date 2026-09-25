package com.newsplatform.common.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {
    private SlugUtils() { }
    public static String slugify(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKD)
                .replaceAll("[^\\p{ASCII}]", "").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        return normalized.isBlank() ? "story" : normalized;
    }
}

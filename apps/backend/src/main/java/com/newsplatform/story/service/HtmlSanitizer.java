package com.newsplatform.story.service;

import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {
    public String sanitize(String html) {
        if (html == null || html.isBlank()) return "";
        String safe = html.replaceAll("(?is)<(script|style|iframe|object|embed|form)[^>]*>.*?</\\1>", "");
        safe = safe.replaceAll("(?i)\\s+on[a-z]+\\s*=\\s*(\\\"[^\\\"]*\\\"|'[^']*'|[^\\s>]+)", "");
        safe = safe.replaceAll("(?i)(href|src)\\s*=\\s*(\\\"|')\\s*javascript:[^\\\"']*\\2", "$1=\\\"#\\\"");
        return safe.replaceAll("(?is)<(?!/?(?:p|br|h[1-6]|strong|b|em|i|a|ul|ol|li)(?:\\s|/?>))[^>]+>", "").trim();
    }
}

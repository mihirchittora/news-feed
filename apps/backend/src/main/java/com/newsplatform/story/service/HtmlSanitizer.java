package com.newsplatform.story.service;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlSanitizer {
    private static final Set<String> ALLOWED_TAGS = Set.of("p", "br", "h1", "h2", "h3", "h4", "h5", "h6", "strong", "b", "em", "i", "a", "ul", "ol", "li");
    private static final Pattern TAG = Pattern.compile("(?is)<!--.*?-->|<![^>]*>|</?([a-z][a-z0-9]*)\\b([^>]*)>");
    private static final Pattern ATTRIBUTE = Pattern.compile("(?is)([a-z_:][a-z0-9_:. -]*)\\s*=\\s*(?:\\\"([^\\\"]*)\\\"|'([^']*)'|([^\\s>]+))");

    public String sanitize(String html) {
        if (html == null || html.isBlank()) return "";
        Matcher tags = TAG.matcher(html);
        StringBuilder safe = new StringBuilder(html.length());
        int cursor = 0;
        while (tags.find()) {
            appendEscapedText(safe, html.substring(cursor, tags.start()));
            String source = tags.group();
            String tag = tags.group(1);
            if (tag != null) appendTag(safe, tag.toLowerCase(Locale.ROOT), tags.group(2), source.startsWith("</"));
            cursor = tags.end();
        }
        appendEscapedText(safe, html.substring(cursor));
        return safe.toString().trim();
    }

    private void appendTag(StringBuilder output, String tag, String rawAttributes, boolean closing) {
        if (!ALLOWED_TAGS.contains(tag)) return;
        if (closing) {
            if (!"br".equals(tag)) output.append("</").append(tag).append('>');
            return;
        }
        output.append('<').append(tag);
        if ("a".equals(tag)) {
            Matcher attributes = ATTRIBUTE.matcher(rawAttributes == null ? "" : rawAttributes);
            while (attributes.find()) {
                String name = attributes.group(1).toLowerCase(Locale.ROOT).trim();
                String value = firstNonNull(attributes.group(2), attributes.group(3), attributes.group(4));
                if (("href".equals(name) && isSafeLink(value)) || "title".equals(name)) {
                    output.append(' ').append(name).append("=\"").append(HtmlUtils.htmlEscape(value)).append("\"");
                }
            }
        }
        output.append('>');
    }

    private boolean isSafeLink(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            return scheme != null && Set.of("http", "https", "mailto").contains(scheme.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void appendEscapedText(StringBuilder output, String text) {
        if (!text.isEmpty()) output.append(HtmlUtils.htmlEscape(text));
    }

    private String firstNonNull(String... values) {
        for (String value : values) if (value != null) return value;
        return "";
    }
}

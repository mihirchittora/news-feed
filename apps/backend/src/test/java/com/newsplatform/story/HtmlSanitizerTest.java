package com.newsplatform.story;

import com.newsplatform.story.service.HtmlSanitizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerTest {
    private final HtmlSanitizer sanitizer = new HtmlSanitizer();

    @Test
    void keepsEditorialMarkupAndRemovesExecutableMarkup() {
        String result = sanitizer.sanitize("<h2>Headline</h2><p onclick=\"alert(1)\">Body <strong>bold</strong></p><script>alert(1)</script><iframe src=\"x\"></iframe>");
        assertThat(result).contains("<h2>Headline</h2>", "<strong>bold</strong>").doesNotContain("script", "iframe", "onclick");
    }

    @Test
    void removesJavascriptLinks() {
        assertThat(sanitizer.sanitize("<a href=\"javascript:alert(1)\">bad</a>")).doesNotContain("javascript:");
    }

    @Test
    void rejectsUnsafeProtocolsAndEmbeddedDocuments() {
        String result = sanitizer.sanitize("<p onmouseover=alert(1)>Body</p><a href=\"data:text/html,evil\">bad</a><object data=\"x\"></object>");

        assertThat(result).isEqualTo("<p>Body</p><a>bad</a>");
        assertThat(result).doesNotContainIgnoringCase("onmouseover", "data:", "object");
    }
}

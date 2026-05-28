package com.finalyear.liwatch.negotiation.negotiation_management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.negotiation.Negotiation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class FairValueSuggestionService {

    private static final Duration REFRESH_COOLDOWN = Duration.ofMinutes(10);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${liwatch.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${liwatch.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    public String ensureSuggestion(Negotiation negotiation) {
        if (negotiation.getFairValueSuggestion() != null && !negotiation.getFairValueSuggestion().isBlank()) {
            return negotiation.getFairValueSuggestion();
        }
        String suggestion = callGeminiForSuggestion(negotiation.getBarter());
        negotiation.setFairValueSuggestion(suggestion);
        negotiation.setSuggestionUpdatedAt(LocalDateTime.now());
        return suggestion;
    }

    public String refreshSuggestion(Negotiation negotiation) {
        if (isCooldownActive(negotiation)) {
            throw new IllegalStateException("Please wait before refreshing again");
        }
        String suggestion = callGeminiForSuggestion(negotiation.getBarter());
        negotiation.setFairValueSuggestion(suggestion);
        negotiation.setSuggestionUpdatedAt(LocalDateTime.now());
        return suggestion;
    }

    public boolean isCooldownActive(Negotiation negotiation) {
        LocalDateTime updatedAt = negotiation.getSuggestionUpdatedAt();
        if (updatedAt == null) {
            return false;
        }
        return updatedAt.plus(REFRESH_COOLDOWN).isAfter(LocalDateTime.now());
    }

    private String callGeminiForSuggestion(Barter barter) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return "Slightly uneven\nGemini API key is not configured yet; users should compare item condition, demand, and local resale value before confirming.";
        }

        String offeredItemName = safe(barter.getPostA() != null ? barter.getPostA().getTitle() : null);
        String offeredItemDetails = safe(barter.getPostA() != null ? barter.getPostA().getDescription() : null);
        String requestedItemName = safe(barter.getPostB() != null ? barter.getPostB().getTitle() : null);
        String requestedItemDetails = safe(barter.getPostB() != null ? barter.getPostB().getDescription() : null);

        String prompt = "You are a fair value advisor for a barter platform in Ethiopia.\n"
                + "User 1 is offering: " + offeredItemName + " - " + offeredItemDetails + ".\n"
                + "User 2 is offering: " + requestedItemName + " - " + requestedItemDetails + ".\n"
                + "Is this a fair trade? Reply in exactly 2 lines:\n"
                + "Line 1: 'Fair trade', 'Slightly uneven', or 'Uneven trade'.\n"
                + "Line 2: One advice sentence for both users, max 50 words.";

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + geminiModel + ":generateContent?key=" + geminiApiKey;

            String requestBody = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": %s
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(OBJECT_MAPPER.writeValueAsString(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "Slightly uneven\nAI advisor is temporarily unavailable; compare both items' condition and market demand before finalizing.";
            }

            JsonNode root = OBJECT_MAPPER.readTree(response.body());
            JsonNode textNode = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");

            String output = textNode.isMissingNode() ? "" : textNode.asText("").trim();
            return normalizeTwoLineSuggestion(output);
        } catch (Exception e) {
            return "Slightly uneven\nAI advisor is temporarily unavailable; users should verify condition, accessories, and local value before confirming.";
        }
    }

    private String normalizeTwoLineSuggestion(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Slightly uneven\nReview each item's condition and local market value before accepting this trade.";
        }
        String[] lines = raw.replace("\r", "").split("\n");
        String line1 = lines.length > 0 ? lines[0].trim() : "";
        String line2 = lines.length > 1 ? lines[1].trim() : "";

        if (!line1.equals("Fair trade") && !line1.equals("Slightly uneven") && !line1.equals("Uneven trade")) {
            line1 = "Slightly uneven";
        }
        if (line2.isBlank()) {
            line2 = "Compare condition, demand, and replacement cost for both items before you finalize.";
        }
        return line1 + "\n" + line2;
    }

    private String safe(String input) {
        return input == null || input.isBlank() ? "N/A" : input.trim();
    }
}

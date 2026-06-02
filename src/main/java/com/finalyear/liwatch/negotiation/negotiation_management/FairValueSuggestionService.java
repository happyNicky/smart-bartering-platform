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

    @Value("${liwatch.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private boolean isFallbackSuggestion(String suggestion) {
        if (suggestion == null) return true;
        return suggestion.contains("temporarily unavailable") || suggestion.contains("not configured yet");
    }

    public String ensureSuggestion(Negotiation negotiation) {
        if (negotiation.getFairValueSuggestion() != null 
                && !negotiation.getFairValueSuggestion().isBlank() 
                && negotiation.getFairnessScore() != null
                && !isFallbackSuggestion(negotiation.getFairValueSuggestion())) {
            return negotiation.getFairValueSuggestion();
        }
        String suggestion = callGeminiForSuggestion(negotiation);
        negotiation.setFairValueSuggestion(suggestion);
        negotiation.setSuggestionUpdatedAt(LocalDateTime.now());
        return suggestion;
    }

    public String refreshSuggestion(Negotiation negotiation) {
        if (isCooldownActive(negotiation)) {
            throw new IllegalStateException("Please wait before refreshing again");
        }
        String suggestion = callGeminiForSuggestion(negotiation);
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

    private String callGeminiForSuggestion(Negotiation negotiation) {
        Barter barter = negotiation.getBarter();
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            negotiation.setFairnessScore(70.0);
            return "Slightly uneven\nGemini API key is not configured yet; users should compare item condition, demand, and local resale value before confirming.";
        }

        String offeredItemName = safe(barter.getPostA() != null ? barter.getPostA().getTitle() : null);
        String offeredItemDetails = safe(barter.getPostA() != null ? barter.getPostA().getDescription() : null);
        String requestedItemName = safe(barter.getPostB() != null ? barter.getPostB().getTitle() : null);
        String requestedItemDetails = safe(barter.getPostB() != null ? barter.getPostB().getDescription() : null);

        String prompt = "You are a fair value advisor for a barter platform in Ethiopia.\n"
                + "User 1 is offering: " + offeredItemName + " - " + offeredItemDetails + ".\n"
                + "User 2 is offering: " + requestedItemName + " - " + requestedItemDetails + ".\n"
                + "Is this a fair trade? Reply in exactly 3 lines:\n"
                + "Line 1: 'Fair trade', 'Slightly uneven', or 'Uneven trade'.\n"
                + "Line 2: A numerical fairness score from 0 to 100 representing the fairness of the trade (just the integer number, e.g. '85').\n"
                ;

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
                System.err.println("Gemini request failed. Status: " + response.statusCode() + ", Body: " + response.body());
                negotiation.setFairnessScore(70.0);
                return "Slightly uneven\nAI advisor is temporarily unavailable; compare both items' condition and market demand before finalizing.";
            }

            JsonNode root = OBJECT_MAPPER.readTree(response.body());
            JsonNode textNode = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");

            String output = textNode.isMissingNode() ? "" : textNode.asText("").trim();
            return normalizeThreeLineSuggestion(negotiation, output);
        } catch (Exception e) {
            System.err.println("Exception calling Gemini: ");
            e.printStackTrace();
            negotiation.setFairnessScore(70.0);
            return "Slightly uneven\nAI advisor is temporarily unavailable; users should verify condition, accessories, and local value before confirming.";
        }
    }

    private String normalizeThreeLineSuggestion(Negotiation negotiation, String raw) {
        if (raw == null || raw.isBlank()) {
            negotiation.setFairnessScore(70.0);
            return "Slightly uneven\nReview each item's condition and local market value before accepting this trade.";
        }
        
        java.util.List<String> cleanLines = new java.util.ArrayList<>();
        for (String line : raw.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("`")) {
                continue;
            }
            trimmed = trimmed.replaceAll("^(?i)(Line\\s*\\d+\\s*:\\s*|\\d+\\.\\s*|Status\\s*:\\s*|Score\\s*:\\s*|Advice\\s*:\\s*)", "");
            trimmed = trimmed.trim();
            if (!trimmed.isEmpty()) {
                cleanLines.add(trimmed);
            }
        }

        String line1 = cleanLines.size() > 0 ? cleanLines.get(0) : "";
        String line2 = cleanLines.size() > 1 ? cleanLines.get(1) : "";
        
        StringBuilder line3Builder = new StringBuilder();
        for (int i = 2; i < cleanLines.size(); i++) {
            if (line3Builder.length() > 0) {
                line3Builder.append(" ");
            }
            line3Builder.append(cleanLines.get(i));
        }
        String line3 = line3Builder.toString().trim();

        if (!line1.equals("Fair trade") && !line1.equals("Slightly uneven") && !line1.equals("Uneven trade")) {
            String lower = line1.toLowerCase();
            if (lower.contains("fair")) {
                line1 = "Fair trade";
            } else if (lower.contains("uneven")) {
                if (lower.contains("slightly")) {
                    line1 = "Slightly uneven";
                } else {
                    line1 = "Uneven trade";
                }
            } else {
                line1 = "Slightly uneven";
            }
        }

        double score = 70.0;
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\d+");
            java.util.regex.Matcher m = p.matcher(line2);
            if (m.find()) {
                score = Double.parseDouble(m.group());
            } else {
                if ("Fair trade".equals(line1)) score = 90.0;
                else if ("Uneven trade".equals(line1)) score = 40.0;
                else score = 70.0;
            }
            if (score < 0) score = 0;
            if (score > 100) score = 100;
        } catch (Exception e) {
            if ("Fair trade".equals(line1)) score = 90.0;
            else if ("Uneven trade".equals(line1)) score = 40.0;
            else score = 70.0;
        }
        negotiation.setFairnessScore(score);

        if (line3.isBlank()) {
            line3 = "Compare condition, demand, and replacement cost for both items before you finalize.";
        }
        return line1 + "\n" + line3;
    }

    private String safe(String input) {
        return input == null || input.isBlank() ? "N/A" : input.trim();
    }
}

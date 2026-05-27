package com.finalyear.liwatch.userprofile.enums;

import java.math.BigDecimal;

public enum BadgeLevel {
    LEVEL_1("Level 1", "New"),
    LEVEL_2("Level 2", "Rising"),
    LEVEL_3("Level 3", "Trusted"),
    LEVEL_4("Level 4", "Verified Trader"),
    LEVEL_5("Level 5", "Elite");

    private final String level;
    private final String label;

    BadgeLevel(String level, String label) {
        this.level = level;
        this.label = label;
    }

    public String getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    public static BadgeLevel fromTrustScore(BigDecimal score) {
        if (score == null) {
            return LEVEL_1;
        }
        double value = score.doubleValue();
        if (value >= 4.50) {
            return LEVEL_5;
        }
        if (value >= 3.75) {
            return LEVEL_4;
        }
        if (value >= 3.00) {
            return LEVEL_3;
        }
        if (value >= 2.00) {
            return LEVEL_2;
        }
        return LEVEL_1;
    }
}

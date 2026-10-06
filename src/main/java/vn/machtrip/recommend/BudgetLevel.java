package vn.machtrip.recommend;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BudgetLevel {
    LOW, MEDIUM, HIGH;

    @JsonValue
    public String value() {
        return name().toLowerCase();
    }
}

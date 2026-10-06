package vn.machtrip.recommend;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PartyType {
    SOLO, COUPLE, FRIENDS, FAMILY;

    @JsonValue
    public String value() {
        return name().toLowerCase();
    }
}

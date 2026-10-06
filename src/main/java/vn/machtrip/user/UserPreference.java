package vn.machtrip.user;

import java.util.List;
import java.util.UUID;

/** Stored travel preferences of a user. Values mirror the {@code user_preference} columns. */
public record UserPreference(UUID userId, double energy, String budgetLevel, List<String> tags, String partyType) {
}

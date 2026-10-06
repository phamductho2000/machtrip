package vn.machtrip.recommend;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * @param score         quality_score x match; higher is better
 * @param lowConfidence true when fewer than two distinct creators mention the place
 * @param reason        short human-readable explanation (Vietnamese)
 */
public record Recommendation(UUID placeId, String name, String category, String city, double lat, double lng,
                             long distanceMeters, Integer priceMin, Integer priceMax, double score,
                             @Schema(description = "true when fewer than 2 distinct creators mention the place")
                             boolean lowConfidence, List<String> matchedTags, String reason) {
}

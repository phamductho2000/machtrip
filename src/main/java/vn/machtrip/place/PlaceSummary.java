package vn.machtrip.place;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

/**
 * Public view of a matched place.
 *
 * @param energy         0 (very chill) .. 1 (very high energy)
 * @param priceMin       VND per person, null when unknown
 * @param distanceMeters from the searched point; only set when the caller supplied one
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaceSummary(UUID id, String name, String category, String city, String address,
                           double lat, double lng, double energy, List<String> tags, List<String> bestFor,
                           Integer priceMin, Integer priceMax, double qualityScore,
                           int mentionCount, int creatorCount, String resolveStatus, Double distanceMeters) {

    public PlaceSummary withDistanceMeters(Double distanceMeters) {
        return new PlaceSummary(id, name, category, city, address, lat, lng, energy, tags, bestFor,
                priceMin, priceMax, qualityScore, mentionCount, creatorCount, resolveStatus, distanceMeters);
    }
}

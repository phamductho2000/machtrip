package vn.machtrip.place.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import vn.machtrip.content.PlaceVideo;
import vn.machtrip.place.PlaceSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;

record PlaceDetail(PlaceSummary place, List<Fact> facts, List<PlaceVideo> topVideos) {

    /**
     * A fact about the place. {@code type} is open-ended; known types are {@code price_median}
     * ({"amount", "currency"}), {@code tip} and {@code warning} ({"text"}).
     */
    record Fact(String type, @Schema(description = "Type-specific JSON payload") Map<String, Object> value,
                Evidence evidence) {
    }

    /** Where a fact was observed: the video, the second it starts at, and when it was observed. */
    record Evidence(String videoId, String webVideoUrl, int tStartSec, Instant observedAt) {
    }
}

package vn.machtrip.content;

import java.time.Instant;

/**
 * A video that mentions a place, shown via TikTok embed. The author is deliberately not exposed.
 *
 * @param tStartSec second at which the place appears in the video
 * @param rank      1 = best video for the place
 */
public record PlaceVideo(String tiktokId, String webVideoUrl, int tStartSec, int rank, Instant createdAt,
                         long playCount, long likeCount, long saveCount, long commentCount, long shareCount,
                         String language, String embedStatus) {
}

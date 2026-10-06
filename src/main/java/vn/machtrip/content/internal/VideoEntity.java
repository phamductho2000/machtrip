package vn.machtrip.content.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

/** Read-only view of {@code video}. {@code author_hash} is intentionally not mapped. */
@Entity
@Immutable
@Table(name = "video")
class VideoEntity {

    @Id
    String tiktokId;
    String webVideoUrl;
    Instant createdAt;
    long playCount;
    long likeCount;
    long saveCount;
    long commentCount;
    long shareCount;
    String language;
    String embedStatus;
}

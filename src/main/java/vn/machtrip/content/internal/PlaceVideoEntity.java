package vn.machtrip.content.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "place_video")
@IdClass(PlaceVideoEntity.Key.class)
class PlaceVideoEntity {

    @Id
    UUID placeId;
    @Id
    String videoId;
    int rank;
    int tStartSec;

    record Key(UUID placeId, String videoId) implements Serializable {
    }
}

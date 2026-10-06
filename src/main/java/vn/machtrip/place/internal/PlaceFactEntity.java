package vn.machtrip.place.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "place_fact")
class PlaceFactEntity {

    @Id
    UUID id;
    UUID placeId;
    String type;
    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> value;
    String evidenceVideoId;
    int tStartSec;
    Instant observedAt;
}

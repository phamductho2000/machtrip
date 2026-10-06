package vn.machtrip.place.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.UUID;

/** Read-only view of {@code place}; the pipeline is the only writer. */
@Entity
@Immutable
@Table(name = "place")
class PlaceEntity {

    @Id
    UUID id;
    String name;
    String category;
    String city;
    String address;
    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(columnDefinition = "geography(Point,4326)")
    Point geom;
    float energy;
    @JdbcTypeCode(SqlTypes.ARRAY)
    List<String> tags;
    @JdbcTypeCode(SqlTypes.ARRAY)
    List<String> bestFor;
    Integer priceMin;
    Integer priceMax;
    float qualityScore;
    int mentionCount;
    int creatorCount;
    String resolveStatus;
}

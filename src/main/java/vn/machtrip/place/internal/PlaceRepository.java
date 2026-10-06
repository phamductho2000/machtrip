package vn.machtrip.place.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {

    Optional<PlaceEntity> findByIdAndResolveStatus(UUID id, String resolveStatus);

    /**
     * Every filter is optional (null = ignored). {@code CAST(:x AS type)} lets PostgreSQL type a null parameter.
     * City and name match ignoring case and Vietnamese diacritics. With a point, results are nearest first.
     * The pageable must be unsorted: ordering is part of the query.
     */
    @Query(value = """
            SELECT p.* FROM place p
            WHERE p.resolve_status = 'matched'
              AND (CAST(:city AS text) IS NULL
                   OR lower(unaccent(p.city)) = lower(unaccent(CAST(:city AS text))))
              AND (CAST(:category AS text) IS NULL OR p.category = CAST(:category AS text))
              AND (CAST(:q AS text) IS NULL
                   OR lower(unaccent(p.name)) LIKE '%' || lower(unaccent(CAST(:q AS text))) || '%' ESCAPE '#')
              AND (CAST(:radius AS double precision) IS NULL
                   OR ST_DWithin(p.geom,
                         CAST(ST_SetSRID(ST_MakePoint(CAST(:lng AS double precision), CAST(:lat AS double precision)), 4326) AS geography),
                         CAST(:radius AS double precision)))
            ORDER BY
              CASE WHEN CAST(:lat AS double precision) IS NULL THEN NULL
                   ELSE ST_Distance(p.geom,
                         CAST(ST_SetSRID(ST_MakePoint(CAST(:lng AS double precision), CAST(:lat AS double precision)), 4326) AS geography))
              END,
              p.quality_score DESC, p.name, p.id
            """,
            countQuery = """
            SELECT count(*) FROM place p
            WHERE p.resolve_status = 'matched'
              AND (CAST(:city AS text) IS NULL
                   OR lower(unaccent(p.city)) = lower(unaccent(CAST(:city AS text))))
              AND (CAST(:category AS text) IS NULL OR p.category = CAST(:category AS text))
              AND (CAST(:q AS text) IS NULL
                   OR lower(unaccent(p.name)) LIKE '%' || lower(unaccent(CAST(:q AS text))) || '%' ESCAPE '#')
              AND (CAST(:radius AS double precision) IS NULL
                   OR ST_DWithin(p.geom,
                         CAST(ST_SetSRID(ST_MakePoint(CAST(:lng AS double precision), CAST(:lat AS double precision)), 4326) AS geography),
                         CAST(:radius AS double precision)))
            """,
            nativeQuery = true)
    Page<PlaceEntity> search(@Param("city") String city, @Param("category") String category, @Param("q") String q,
                             @Param("lat") Double lat, @Param("lng") Double lng, @Param("radius") Double radius,
                             Pageable pageable);

    @Query(value = """
            SELECT p.* FROM place p
            WHERE p.resolve_status = 'matched'
              AND ST_DWithin(p.geom,
                    CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography), :radius)
            """, nativeQuery = true)
    List<PlaceEntity> findMatchedWithin(@Param("lat") double lat, @Param("lng") double lng,
                                        @Param("radius") double radius);
}

package vn.machtrip.place.internal;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.machtrip.common.GeoProvider;
import vn.machtrip.common.InvalidRequestException;
import vn.machtrip.common.NotFoundException;
import vn.machtrip.common.PageResponse;
import vn.machtrip.content.PlaceVideo;
import vn.machtrip.content.VideoCatalog;
import vn.machtrip.place.PlaceCatalog;
import vn.machtrip.place.PlaceSummary;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
class PlaceService implements PlaceCatalog {

    private static final String MATCHED = "matched";
    private static final int DETAIL_VIDEO_LIMIT = 3;
    private static final int VIDEO_LIST_LIMIT = 50;

    private final PlaceRepository places;
    private final PlaceFactRepository facts;
    private final VideoCatalog videos;
    private final GeoProvider geo;

    PlaceService(PlaceRepository places, PlaceFactRepository facts, VideoCatalog videos, GeoProvider geo) {
        this.places = places;
        this.facts = facts;
        this.videos = videos;
        this.geo = geo;
    }

    PageResponse<PlaceSummary> search(String city, String category, String q, Double lat, Double lng,
                                      Integer radiusMeters, int page, int size) {
        if ((lat == null) != (lng == null)) {
            throw new InvalidRequestException("lat and lng must be provided together");
        }
        if (radiusMeters != null && lat == null) {
            throw new InvalidRequestException("radiusMeters requires lat and lng");
        }
        var found = places.search(blankToNull(city), blankToNull(category), likePattern(q), lat, lng,
                radiusMeters == null ? null : radiusMeters.doubleValue(), PageRequest.of(page, size));
        return PageResponse.of(found, e -> toSummary(e, lat, lng));
    }

    PlaceDetail detail(UUID id) {
        PlaceEntity place = requireMatched(id);
        List<PlaceFactEntity> factRows = facts.findByPlaceIdOrderByTypeAscObservedAtDesc(id);
        Map<String, String> urls = videos.webVideoUrls(factRows.stream().map(f -> f.evidenceVideoId).distinct().toList());
        List<PlaceDetail.Fact> factDtos = factRows.stream()
                .map(f -> new PlaceDetail.Fact(f.type, f.value,
                        new PlaceDetail.Evidence(f.evidenceVideoId, urls.get(f.evidenceVideoId), f.tStartSec, f.observedAt)))
                .toList();
        return new PlaceDetail(toSummary(place, null, null), factDtos, videos.videosForPlace(id, DETAIL_VIDEO_LIMIT));
    }

    List<PlaceVideo> videosOf(UUID id) {
        requireMatched(id);
        return videos.videosForPlace(id, VIDEO_LIST_LIMIT);
    }

    @Override
    public List<PlaceSummary> findMatchedWithin(double lat, double lng, int radiusMeters) {
        return places.findMatchedWithin(lat, lng, radiusMeters).stream().map(e -> toSummary(e, null, null)).toList();
    }

    private PlaceEntity requireMatched(UUID id) {
        return places.findByIdAndResolveStatus(id, MATCHED)
                .orElseThrow(() -> new NotFoundException("Place not found: " + id));
    }

    private PlaceSummary toSummary(PlaceEntity e, Double fromLat, Double fromLng) {
        double lat = e.geom.getY();
        double lng = e.geom.getX();
        Double distance = fromLat == null ? null : (double) Math.round(geo.distanceMeters(fromLat, fromLng, lat, lng));
        return new PlaceSummary(e.id, e.name, e.category, e.city, e.address, lat, lng, clean(e.energy),
                e.tags, e.bestFor, e.priceMin, e.priceMax, clean(e.qualityScore), e.mentionCount, e.creatorCount,
                e.resolveStatus, distance);
    }

    /** DB columns are REAL (float4); widening to double would leak noise such as 0.15000000596. */
    private static double clean(float value) {
        return Double.parseDouble(Float.toString(value));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** Escapes LIKE wildcards (escape char is '#', see PlaceRepository). */
    private static String likePattern(String q) {
        String trimmed = blankToNull(q);
        return trimmed == null ? null : trimmed.replace("#", "##").replace("%", "#%").replace("_", "#_");
    }
}

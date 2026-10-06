package vn.machtrip.recommend.internal;

import org.springframework.stereotype.Component;
import vn.machtrip.common.GeoProvider;
import vn.machtrip.place.PlaceSummary;
import vn.machtrip.recommend.PartyType;
import vn.machtrip.recommend.Recommendation;
import vn.machtrip.recommend.RecommendationRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pure ranking logic, no I/O.
 * <ol>
 *   <li>Hard filters: resolve_status = matched, within maxDistanceMeters, price_min within the budget cap.</li>
 *   <li>rank = quality_score x match, where match = (1 - |user.energy - place.energy|) + tagBonus x matches.
 *       A match is a requested tag the place has, plus one if the place is best for the requested party type.</li>
 *   <li>Places with fewer than {@value #MIN_CREATORS} distinct creators are flagged lowConfidence.</li>
 * </ol>
 */
@Component
class RecommendationEngine {

    static final int MIN_CREATORS = 2;
    private static final Locale VI = Locale.forLanguageTag("vi-VN");

    private final GeoProvider geo;
    private final RecommendProperties props;

    RecommendationEngine(GeoProvider geo, RecommendProperties props) {
        this.geo = geo;
        this.props = props;
    }

    List<Recommendation> rank(RecommendationRequest request, Collection<PlaceSummary> candidates) {
        Set<String> wantedTags = Set.copyOf(normalizeTags(request.tags()));
        Integer budgetCap = props.capFor(request.budgetLevel());

        return candidates.stream()
                .filter(place -> "matched".equals(place.resolveStatus()))
                .map(place -> new Candidate(place, geo.distanceMeters(request.lat(), request.lng(), place.lat(), place.lng())))
                .filter(c -> c.distance() <= request.maxDistanceMeters())
                .filter(c -> withinBudget(c.place(), budgetCap))
                .map(c -> score(c, request, wantedTags))
                .sorted(Comparator.comparingDouble(Scored::score).reversed()
                        .thenComparing(Comparator.comparingInt((Scored s) -> s.candidate().place().creatorCount()).reversed())
                        .thenComparing(s -> s.candidate().place().name())
                        .thenComparing(s -> s.candidate().place().id()))
                .limit(props.maxResults())
                .map(s -> toRecommendation(s, request))
                .toList();
    }

    /** Lower-cased, trimmed, distinct and sorted, so equal requests normalise (and cache) identically. */
    static List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream().map(t -> t.trim().toLowerCase(Locale.ROOT)).filter(t -> !t.isEmpty())
                .distinct().sorted().toList();
    }

    /** A place without a known price is kept: missing data is not evidence that it is too expensive. */
    private static boolean withinBudget(PlaceSummary place, Integer cap) {
        return cap == null || place.priceMin() == null || place.priceMin() <= cap;
    }

    private Scored score(Candidate c, RecommendationRequest request, Set<String> wantedTags) {
        PlaceSummary place = c.place();
        List<String> matchedTags = place.tags().stream()
                .map(t -> t.toLowerCase(Locale.ROOT)).filter(wantedTags::contains).toList();
        boolean partyMatch = request.partyType() != null
                && place.bestFor().stream().anyMatch(b -> b.equalsIgnoreCase(request.partyType().value()));
        double match = (1 - Math.abs(request.energy() - place.energy()))
                + props.tagBonus() * (matchedTags.size() + (partyMatch ? 1 : 0));
        return new Scored(c, place.qualityScore() * match, matchedTags, partyMatch);
    }

    private Recommendation toRecommendation(Scored s, RecommendationRequest request) {
        PlaceSummary p = s.candidate().place();
        boolean lowConfidence = p.creatorCount() < MIN_CREATORS;
        return new Recommendation(p.id(), p.name(), p.category(), p.city(), p.lat(), p.lng(),
                Math.round(s.candidate().distance()), p.priceMin(), p.priceMax(),
                Math.round(s.score() * 10_000) / 10_000.0, lowConfidence, s.matchedTags(),
                reason(s, lowConfidence));
    }

    private static String reason(Scored s, boolean lowConfidence) {
        PlaceSummary p = s.candidate().place();
        var parts = new ArrayList<String>();
        parts.add(p.energy() <= 0.35 ? "Yên tĩnh, thư giãn"
                : p.energy() >= 0.65 ? "Sôi động, nhiều hoạt động" : "Nhịp độ vừa phải");
        if (!s.matchedTags().isEmpty()) {
            parts.add("khớp sở thích: " + String.join(", ", s.matchedTags()));
        }
        if (s.partyMatch()) {
            parts.add("hợp với nhóm của bạn");
        }
        parts.add("cách bạn " + formatDistance(s.candidate().distance()));
        parts.add(p.creatorCount() + " creator nhắc tới");
        if (lowConfidence) {
            parts.add("ít nguồn xác nhận, nên kiểm tra thêm");
        }
        return parts.stream().collect(Collectors.joining(", "));
    }

    private static String formatDistance(double meters) {
        return meters < 1000 ? Math.round(meters) + " m" : String.format(VI, "%.1f km", meters / 1000);
    }

    private record Candidate(PlaceSummary place, double distance) {
    }

    private record Scored(Candidate candidate, double score, List<String> matchedTags, boolean partyMatch) {
    }
}

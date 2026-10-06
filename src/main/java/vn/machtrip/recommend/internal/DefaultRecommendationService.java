package vn.machtrip.recommend.internal;

import org.springframework.stereotype.Service;
import vn.machtrip.place.PlaceCatalog;
import vn.machtrip.recommend.Recommendation;
import vn.machtrip.recommend.RecommendationRequest;
import vn.machtrip.recommend.RecommendationService;

import java.util.List;

@Service
class DefaultRecommendationService implements RecommendationService {

    private final PlaceCatalog places;
    private final RecommendationEngine engine;
    private final RecommendationCache cache;

    DefaultRecommendationService(PlaceCatalog places, RecommendationEngine engine, RecommendationCache cache) {
        this.places = places;
        this.engine = engine;
        this.cache = cache;
    }

    @Override
    public List<Recommendation> recommend(RecommendationRequest request) {
        String key = cache.keyFor(request);
        return cache.get(key).orElseGet(() -> {
            // The DB narrows candidates with the spatial index; the engine remains the source of truth for the rules.
            var candidates = places.findMatchedWithin(request.lat(), request.lng(), request.maxDistanceMeters());
            List<Recommendation> result = engine.rank(request, candidates);
            cache.put(key, result);
            return result;
        });
    }
}

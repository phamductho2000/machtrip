package vn.machtrip.recommend;

import java.util.List;

/** Public API of the recommend module. */
public interface RecommendationService {

    List<Recommendation> recommend(RecommendationRequest request);
}

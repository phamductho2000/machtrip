package vn.machtrip.recommend.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.machtrip.recommend.Recommendation;
import vn.machtrip.recommend.RecommendationRequest;
import vn.machtrip.recommend.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations")
class RecommendationController {

    private final RecommendationService service;

    RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Ranked places for a traveller profile",
            description = "Hard filters (budget, distance, matched only), then rank = quality_score x match. "
                    + "Places mentioned by fewer than 2 creators are returned with lowConfidence=true.")
    List<Recommendation> recommend(@Valid @RequestBody RecommendationRequest request) {
        return service.recommend(request);
    }
}

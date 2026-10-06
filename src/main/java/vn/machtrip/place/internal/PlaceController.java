package vn.machtrip.place.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.machtrip.common.PageResponse;
import vn.machtrip.content.PlaceVideo;
import vn.machtrip.place.PlaceSummary;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/places")
@Tag(name = "Places")
class PlaceController {

    private final PlaceService service;

    PlaceController(PlaceService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Search matched places",
            description = "Only places with resolve_status=matched. City and q ignore case and diacritics. "
                    + "With lat/lng, results are nearest first and carry distanceMeters; radiusMeters also filters.")
    PageResponse<PlaceSummary> search(
            @Parameter(description = "e.g. 'Da Lat' or 'Đà Lạt'") @RequestParam(required = false) String city,
            @Parameter(description = "eat | play | stay | transport")
            @RequestParam(required = false) @Pattern(regexp = "eat|play|stay|transport",
                    message = "must be one of eat, play, stay, transport") String category,
            @Parameter(description = "Substring of the place name") @RequestParam(required = false) String q,
            @RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90") Double lat,
            @RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180") Double lng,
            @RequestParam(required = false) @Positive Integer radiusMeters,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.search(city, category, q, lat, lng, radiusMeters, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Place detail with evidence-backed facts and its top 3 videos")
    PlaceDetail detail(@PathVariable UUID id) {
        return service.detail(id);
    }

    @GetMapping("/{id}/videos")
    @Operation(summary = "Videos mentioning the place, best first")
    List<PlaceVideo> videos(@PathVariable UUID id) {
        return service.videosOf(id);
    }
}

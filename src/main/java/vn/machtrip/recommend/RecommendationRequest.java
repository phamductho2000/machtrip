package vn.machtrip.recommend;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RecommendationRequest(
        @Schema(description = "0 = very chill, 1 = very high energy")
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double energy,
        @NotNull BudgetLevel budgetLevel,
        @Schema(description = "Optional; each tag that a place also has adds a bonus")
        @Size(max = 20) List<@NotBlank String> tags,
        @Schema(description = "Optional; counts as one more matching tag when the place is best for this party")
        PartyType partyType,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double lat,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double lng,
        @NotNull @Positive Integer maxDistanceMeters) {
}

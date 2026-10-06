package vn.machtrip.recommend.internal;

import org.junit.jupiter.api.Test;
import vn.machtrip.common.HaversineGeoProvider;
import vn.machtrip.place.PlaceSummary;
import vn.machtrip.recommend.BudgetLevel;
import vn.machtrip.recommend.PartyType;
import vn.machtrip.recommend.Recommendation;
import vn.machtrip.recommend.RecommendationRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class RecommendationEngineTest {

    // Request origin; places below sit at this point unless a test moves them.
    private static final double LAT = 11.9404;
    private static final double LNG = 108.4419;

    private final RecommendProperties props = new RecommendProperties(
            Duration.ofMinutes(10), 0.1, 20, new RecommendProperties.BudgetCaps(100_000, 400_000));
    private final RecommendationEngine engine = new RecommendationEngine(new HaversineGeoProvider(), props);

    @Test
    void lowEnergyRanksChillPlacesAboveHighEnergyOnes() {
        var chill = place("Chill lake").energy(0.15).quality(0.9).build();
        var wild = place("Canyoning").energy(0.95).quality(0.9).build();

        var result = engine.rank(request(0.2).build(), List.of(wild, chill));

        assertThat(names(result)).containsExactly("Chill lake", "Canyoning");
    }

    @Test
    void highEnergyRanksHighEnergyPlacesFirst() {
        var chill = place("Chill lake").energy(0.15).quality(0.9).build();
        var wild = place("Canyoning").energy(0.95).quality(0.9).build();

        var result = engine.rank(request(0.9).build(), List.of(chill, wild));

        assertThat(names(result)).containsExactly("Canyoning", "Chill lake");
    }

    @Test
    void scoreIsQualityTimesMatchWithTagBonus() {
        var p = place("P").energy(0.5).quality(0.8).tags("cafe", "view", "cheap").build();

        var result = engine.rank(request(0.3).tags("Cafe", " view ", "unknown").build(), List.of(p));

        // match = (1 - |0.3 - 0.5|) + 0.1 * 2 matching tags = 1.0 ; score = 0.8 * 1.0
        assertThat(result).singleElement().satisfies(r -> {
            assertThat(r.score()).isEqualTo(0.8);
            assertThat(r.matchedTags()).containsExactly("cafe", "view");
        });
    }

    @Test
    void tagBonusCanLiftLowerQualityPlaceAboveHigherQualityOne() {
        var plain = place("Plain").energy(0.5).quality(0.8).build();
        var tagged = place("Tagged").energy(0.5).quality(0.7).tags("cafe", "view").build();

        var result = engine.rank(request(0.5).tags("cafe", "view").build(), List.of(plain, tagged));

        assertThat(names(result)).containsExactly("Tagged", "Plain"); // 0.7 * 1.2 > 0.8 * 1.0
    }

    @Test
    void partyTypeCountsAsOneMoreMatchingTag() {
        var forCouples = place("Couples").energy(0.5).quality(0.8).bestFor("couple").build();

        var result = engine.rank(request(0.5).party(PartyType.COUPLE).build(), List.of(forCouples));

        assertThat(result.getFirst().score()).isEqualTo(0.88); // 0.8 * (1 + 0.1)
    }

    @Test
    void placesThatAreNotMatchedAreNeverReturned() {
        var ambiguous = place("Ambiguous").status("ambiguous").build();
        var unverified = place("Unverified").status("unverified").build();
        var matched = place("Matched").build();

        var result = engine.rank(request(0.5).build(), List.of(ambiguous, unverified, matched));

        assertThat(names(result)).containsExactly("Matched");
    }

    @Test
    void placesBeyondMaxDistanceAreFilteredOut() {
        var near = place("Near").at(LAT + 0.005, LNG).build();   // ~555 m
        var far = place("Far").at(LAT + 0.05, LNG).build();      // ~5.5 km

        var result = engine.rank(request(0.5).maxDistance(1_000).build(), List.of(near, far));

        assertThat(names(result)).containsExactly("Near");
        assertThat(result.getFirst().distanceMeters()).isBetween(540L, 570L);
    }

    @Test
    void budgetFiltersOnPriceMinAndKeepsPlacesWithUnknownPrice() {
        var free = place("Free").price(0, 0).build();
        var mid = place("Mid").price(250_000, 300_000).build();
        var pricey = place("Pricey").price(600_000, 900_000).build();
        var unknown = place("Unknown").price(null, null).build();
        var all = List.of(free, mid, pricey, unknown);

        assertThat(names(engine.rank(request(0.5).budget(BudgetLevel.LOW).build(), all)))
                .containsExactlyInAnyOrder("Free", "Unknown");
        assertThat(names(engine.rank(request(0.5).budget(BudgetLevel.MEDIUM).build(), all)))
                .containsExactlyInAnyOrder("Free", "Mid", "Unknown");
        assertThat(names(engine.rank(request(0.5).budget(BudgetLevel.HIGH).build(), all)))
                .containsExactlyInAnyOrder("Free", "Mid", "Pricey", "Unknown");
    }

    @Test
    void placesWithFewerThanTwoCreatorsAreFlaggedLowConfidence() {
        var oneCreator = place("One").creators(1).build();
        var noCreators = place("None").creators(0).build();
        var twoCreators = place("Two").creators(2).build();

        var result = engine.rank(request(0.5).build(), List.of(oneCreator, noCreators, twoCreators));

        assertThat(result).extracting(Recommendation::name, Recommendation::lowConfidence)
                .containsExactlyInAnyOrder(
                        tuple("One", true),
                        tuple("None", true),
                        tuple("Two", false));
    }

    @Test
    void lowConfidenceIsOnlyAFlagAndDoesNotChangeTheScore() {
        var weak = place("Weak").creators(1).quality(0.9).build();
        var strong = place("Strong").creators(5).quality(0.5).build();

        var result = engine.rank(request(0.5).build(), List.of(strong, weak));

        assertThat(names(result)).containsExactly("Weak", "Strong");
    }

    @Test
    void reasonExplainsVibeMatchedTagsAndLowConfidence() {
        var p = place("Cafe").energy(0.2).tags("chill").creators(1).build();

        var reason = engine.rank(request(0.2).tags("chill").build(), List.of(p)).getFirst().reason();

        assertThat(reason).contains("Yên tĩnh").contains("chill").contains("1 creator").contains("ít nguồn xác nhận");
    }

    @Test
    void equalScoresAreOrderedDeterministically() {
        var b = place("B").creators(3).build();
        var a = place("A").creators(3).build();
        var moreCreators = place("Z").creators(9).build();

        var result = engine.rank(request(0.5).build(), List.of(b, a, moreCreators));

        assertThat(names(result)).containsExactly("Z", "A", "B");
    }

    @Test
    void resultIsCappedAtMaxResults() {
        var many = new ArrayList<PlaceSummary>();
        for (int i = 0; i < 30; i++) {
            many.add(place("P" + i).build());
        }

        assertThat(engine.rank(request(0.5).build(), many)).hasSize(20);
    }

    private static List<String> names(List<Recommendation> result) {
        return result.stream().map(Recommendation::name).toList();
    }

    private static RequestBuilder request(double energy) {
        return new RequestBuilder(energy);
    }

    private static PlaceBuilder place(String name) {
        return new PlaceBuilder(name);
    }

    private static final class RequestBuilder {
        private final double energy;
        private BudgetLevel budget = BudgetLevel.HIGH;
        private List<String> tags = List.of();
        private PartyType party;
        private int maxDistance = 50_000;

        RequestBuilder(double energy) {
            this.energy = energy;
        }

        RequestBuilder budget(BudgetLevel budget) {
            this.budget = budget;
            return this;
        }

        RequestBuilder tags(String... tags) {
            this.tags = List.of(tags);
            return this;
        }

        RequestBuilder party(PartyType party) {
            this.party = party;
            return this;
        }

        RequestBuilder maxDistance(int meters) {
            this.maxDistance = meters;
            return this;
        }

        RecommendationRequest build() {
            return new RecommendationRequest(energy, budget, tags, party, LAT, LNG, maxDistance);
        }
    }

    private static final class PlaceBuilder {
        private final String name;
        private double energy = 0.5;
        private double quality = 0.5;
        private int creators = 3;
        private List<String> tags = List.of();
        private List<String> bestFor = List.of();
        private Integer priceMin = 0;
        private Integer priceMax = 0;
        private double lat = LAT;
        private double lng = LNG;
        private String status = "matched";

        PlaceBuilder(String name) {
            this.name = name;
        }

        PlaceBuilder energy(double energy) {
            this.energy = energy;
            return this;
        }

        PlaceBuilder quality(double quality) {
            this.quality = quality;
            return this;
        }

        PlaceBuilder creators(int creators) {
            this.creators = creators;
            return this;
        }

        PlaceBuilder tags(String... tags) {
            this.tags = List.of(tags);
            return this;
        }

        PlaceBuilder bestFor(String... bestFor) {
            this.bestFor = List.of(bestFor);
            return this;
        }

        PlaceBuilder price(Integer min, Integer max) {
            this.priceMin = min;
            this.priceMax = max;
            return this;
        }

        PlaceBuilder at(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
            return this;
        }

        PlaceBuilder status(String status) {
            this.status = status;
            return this;
        }

        PlaceSummary build() {
            // Ids derived from the name keep the tie-break deterministic.
            return new PlaceSummary(UUID.nameUUIDFromBytes(name.getBytes()), name, "play", "Đà Lạt", null, lat, lng,
                    energy, tags, bestFor, priceMin, priceMax, quality, creators, creators, status, null);
        }
    }
}

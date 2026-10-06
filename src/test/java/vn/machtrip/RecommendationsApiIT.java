package vn.machtrip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecommendationsApiIT extends AbstractIntegrationTest {

    private static final String KEY_PATTERN = "machtrip:reco:v1:*";

    @Autowired
    MockMvc mvc;
    @Autowired
    StringRedisTemplate redis;
    @Autowired
    ObjectMapper json;

    @BeforeEach
    void clearCache() {
        Set<String> keys = redis.keys(KEY_PATTERN);
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }

    @Test
    void lowEnergyRanksChillPlacesAboveHighEnergyOnes() throws Exception {
        List<String> ranked = names(recommend("""
                {"energy": 0.2, "budgetLevel": "high", "tags": [], "partyType": "couple",
                 "lat": 11.9404, "lng": 108.4419, "maxDistanceMeters": 50000}"""));

        assertThat(ranked).containsExactly("Hồ Xuân Hương", "Đồi chè Cầu Đất", "Cà phê Đồi Thông Xanh",
                "Bánh tráng nướng chợ đêm Đà Lạt", "Canyoning thác Datanla");
    }

    @Test
    void highEnergyRanksCanyoningFirst() throws Exception {
        List<String> ranked = names(recommend("""
                {"energy": 0.95, "budgetLevel": "high", "lat": 11.9404, "lng": 108.4419, "maxDistanceMeters": 50000}"""));

        assertThat(ranked.getFirst()).isEqualTo("Canyoning thác Datanla");
    }

    @Test
    void placesWithFewerThanTwoCreatorsAreFlaggedLowConfidence() throws Exception {
        JsonNode result = recommend("""
                {"energy": 0.2, "budgetLevel": "high", "lat": 11.9404, "lng": 108.4419, "maxDistanceMeters": 50000}""");

        for (JsonNode item : result) {
            boolean expected = item.get("name").asText().equals("Cà phê Đồi Thông Xanh"); // the only 1-creator place
            assertThat(item.get("lowConfidence").asBoolean()).as(item.get("name").asText()).isEqualTo(expected);
        }
    }

    @Test
    void matchingTagsAreReportedAndExplainedInTheReason() throws Exception {
        JsonNode result = recommend("""
                {"energy": 0.2, "budgetLevel": "high", "tags": ["Pine-Forest", "photo"],
                 "lat": 11.9404, "lng": 108.4419, "maxDistanceMeters": 50000}""");

        JsonNode cafe = find(result, "Cà phê Đồi Thông Xanh");
        assertThat(texts(cafe.get("matchedTags"))).containsExactlyInAnyOrder("pine-forest", "photo");
        assertThat(cafe.get("reason").asText()).contains("pine-forest").contains("ít nguồn xác nhận");
    }

    @Test
    void budgetAndDistanceAreHardFilters() throws Exception {
        assertThat(names(recommend("""
                {"energy": 0.9, "budgetLevel": "low", "lat": 11.9404, "lng": 108.4419, "maxDistanceMeters": 50000}""")))
                .doesNotContain("Canyoning thác Datanla")
                .hasSize(4);
        assertThat(names(recommend("""
                {"energy": 0.2, "budgetLevel": "high", "lat": 11.9404, "lng": 108.4419, "maxDistanceMeters": 1000}""")))
                .containsExactlyInAnyOrder("Hồ Xuân Hương", "Bánh tráng nướng chợ đêm Đà Lạt");
    }

    @Test
    void resultsAreCachedInRedisWithTtlAndServedFromCache() throws Exception {
        String body = """
                {"energy": 0.3, "budgetLevel": "medium", "tags": ["view", "chill"],
                 "lat": 11.9404, "lng": 108.4419, "maxDistanceMeters": 30000}""";
        String reordered = """
                {"maxDistanceMeters": 30000, "lng": 108.4419, "lat": 11.9404, "budgetLevel": "medium",
                 "tags": ["CHILL", "view "], "energy": 0.3}""";

        JsonNode first = recommend(body);

        Set<String> keys = redis.keys(KEY_PATTERN);
        assertThat(keys).hasSize(1);
        String key = keys.iterator().next();
        assertThat(key).matches("machtrip:reco:v1:[0-9a-f]{64}");
        assertThat(redis.getExpire(key, TimeUnit.SECONDS)).isBetween(1L, 600L);

        // Replace the cached value: only a cache hit can return it. Same request in another form hits the same key.
        redis.opsForValue().set(key, "[]");
        assertThat(recommend(reordered)).isEmpty();
        assertThat(first).isNotEmpty();
    }

    @Test
    void invalidRequestsReturnUnifiedErrorBodyWithFieldDetails() throws Exception {
        mvc.perform(post("/api/v1/recommendations").contentType(MediaType.APPLICATION_JSON).content("""
                        {"energy": 1.5, "budgetLevel": "high", "lat": 11.9, "lng": 108.4, "maxDistanceMeters": 100}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.details[0].field").value("energy"));
        mvc.perform(post("/api/v1/recommendations").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.length()").value(5));
        mvc.perform(post("/api/v1/recommendations").contentType(MediaType.APPLICATION_JSON).content("""
                        {"energy": 0.5, "budgetLevel": "free", "lat": 11.9, "lng": 108.4, "maxDistanceMeters": 100}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(post("/api/v1/recommendations").contentType(MediaType.APPLICATION_JSON).content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void emptyResultIsAnEmptyList() throws Exception {
        mvc.perform(post("/api/v1/recommendations").contentType(MediaType.APPLICATION_JSON).content("""
                        {"energy": 0.5, "budgetLevel": "high", "lat": 21.0285, "lng": 105.8542, "maxDistanceMeters": 5000}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private JsonNode recommend(String body) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString());
    }

    private static List<String> names(JsonNode items) {
        List<String> names = new ArrayList<>();
        items.forEach(i -> names.add(i.get("name").asText()));
        return names;
    }

    private static List<String> texts(JsonNode array) {
        List<String> texts = new ArrayList<>();
        array.forEach(n -> texts.add(n.asText()));
        return texts;
    }

    private static JsonNode find(JsonNode items, String name) {
        for (JsonNode item : items) {
            if (item.get("name").asText().equals(name)) {
                return item;
            }
        }
        throw new AssertionError("Not in result: " + name);
    }
}

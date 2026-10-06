package vn.machtrip;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlacesApiIT extends AbstractIntegrationTest {

    private static final String LAKE = "a0000000-0000-4000-8000-000000000001";
    private static final String TEA_HILL = "a0000000-0000-4000-8000-000000000002";
    private static final String STREET_FOOD = "a0000000-0000-4000-8000-000000000004";
    private static final String AMBIGUOUS = "b0000000-0000-4000-8000-000000000001";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void removeExtraRows() {
        jdbc.update("DELETE FROM place WHERE id = ?::uuid", AMBIGUOUS);
    }

    @Test
    void listsSeededPlacesOfDaLat() throws Exception {
        mvc.perform(get("/api/v1/places").param("city", "Da Lat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(5))
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.items[*].name", containsInAnyOrder("Hồ Xuân Hương", "Đồi chè Cầu Đất",
                        "Cà phê Đồi Thông Xanh", "Bánh tráng nướng chợ đêm Đà Lạt", "Canyoning thác Datanla")));
    }

    @Test
    void cityMatchesWithOrWithoutDiacriticsAndCase() throws Exception {
        mvc.perform(get("/api/v1/places").param("city", "đà lạt")).andExpect(jsonPath("$.totalItems").value(5));
        mvc.perform(get("/api/v1/places").param("city", "Ha Noi")).andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void filtersByCategoryAndName() throws Exception {
        mvc.perform(get("/api/v1/places").param("category", "eat"))
                .andExpect(jsonPath("$.items[*].name", containsInAnyOrder(
                        "Cà phê Đồi Thông Xanh", "Bánh tráng nướng chợ đêm Đà Lạt")));
        mvc.perform(get("/api/v1/places").param("q", "ho xuan"))
                .andExpect(jsonPath("$.items[*].id", contains(LAKE)));
        mvc.perform(get("/api/v1/places").param("q", "100%"))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void paginates() throws Exception {
        mvc.perform(get("/api/v1/places").param("page", "1").param("size", "2"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalItems").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void searchesNearAPointNearestFirstWithDistance() throws Exception {
        // Centre = Hồ Xuân Hương. Night market is ~0.5 km away; the café is ~3.4 km away.
        mvc.perform(get("/api/v1/places").param("lat", "11.9404").param("lng", "108.4419")
                        .param("radiusMeters", "2000"))
                .andExpect(jsonPath("$.items[*].id", contains(LAKE, STREET_FOOD)))
                .andExpect(jsonPath("$.items[0].distanceMeters").value(0.0))
                .andExpect(jsonPath("$.items[1].distanceMeters").value(org.hamcrest.Matchers.closeTo(530, 40)));
    }

    @Test
    void withoutRadiusEveryPlaceIsReturnedNearestFirst() throws Exception {
        mvc.perform(get("/api/v1/places").param("lat", "11.9404").param("lng", "108.4419"))
                .andExpect(jsonPath("$.totalItems").value(5))
                .andExpect(jsonPath("$.items[0].id").value(LAKE))
                .andExpect(jsonPath("$.items[4].id").value(TEA_HILL));
    }

    @Test
    void onlyMatchedPlacesAreVisible() throws Exception {
        jdbc.update("""
                INSERT INTO place (id, name, category, city, geom, resolve_status)
                VALUES (?::uuid, 'Ambiguous Spot', 'eat', 'Đà Lạt',
                        ST_SetSRID(ST_MakePoint(108.4419, 11.9404), 4326)::geography, 'ambiguous')""", AMBIGUOUS);

        mvc.perform(get("/api/v1/places").param("city", "Da Lat").param("q", "Ambiguous"))
                .andExpect(jsonPath("$.totalItems").value(0));
        mvc.perform(get("/api/v1/places/" + AMBIGUOUS)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/places/" + AMBIGUOUS + "/videos")).andExpect(status().isNotFound());
    }

    @Test
    void detailHasFactsWithEvidenceAndTopVideos() throws Exception {
        mvc.perform(get("/api/v1/places/" + TEA_HILL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.place.name").value("Đồi chè Cầu Đất"))
                .andExpect(jsonPath("$.place.lat").value(11.862))
                .andExpect(jsonPath("$.place.energy").value(0.25))
                .andExpect(jsonPath("$.place.creatorCount").value(2))
                .andExpect(jsonPath("$.facts[?(@.type=='price_median')].value.amount").value(70000))
                .andExpect(jsonPath("$.facts[?(@.type=='price_median')].evidence.videoId").value("7300000000000000002"))
                .andExpect(jsonPath("$.facts[?(@.type=='price_median')].evidence.tStartSec").value(30))
                .andExpect(jsonPath("$.facts[?(@.type=='price_median')].evidence.webVideoUrl")
                        .value("https://www.tiktok.com/@seed/video/7300000000000000002"))
                .andExpect(jsonPath("$.facts[?(@.type=='price_median')].evidence.observedAt").isNotEmpty())
                .andExpect(jsonPath("$.facts[?(@.type=='tip')]").isNotEmpty())
                .andExpect(jsonPath("$.facts[?(@.type=='warning')]").isNotEmpty())
                .andExpect(jsonPath("$.facts[*].evidence.videoId").isNotEmpty())
                .andExpect(jsonPath("$.topVideos[*].tiktokId", contains("7300000000000000002", "7300000000000000001")));
    }

    @Test
    void detailReturnsAtMostThreeTopVideosBestFirst() throws Exception {
        mvc.perform(get("/api/v1/places/" + STREET_FOOD))
                .andExpect(jsonPath("$.topVideos.length()").value(3))
                .andExpect(jsonPath("$.topVideos[*].rank", contains(1, 2, 3)));
    }

    @Test
    void videosEndpointListsVideosBestFirstWithoutAuthorData() throws Exception {
        mvc.perform(get("/api/v1/places/" + LAKE + "/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tiktokId", contains("7300000000000000001", "7300000000000000003")))
                .andExpect(jsonPath("$[0].tStartSec").value(15))
                .andExpect(jsonPath("$[0].webVideoUrl").value("https://www.tiktok.com/@seed/video/7300000000000000001"))
                .andExpect(jsonPath("$[0].embedStatus").value("ok"))
                .andExpect(content().string(not(containsString("author"))));
    }

    @Test
    void unknownPlaceReturnsUnifiedErrorBody() throws Exception {
        mvc.perform(get("/api/v1/places/00000000-0000-4000-8000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/places/00000000-0000-4000-8000-000000000000"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void invalidParametersReturnUnifiedErrorBody() throws Exception {
        mvc.perform(get("/api/v1/places").param("size", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.details[0].field").value("size"));
        mvc.perform(get("/api/v1/places").param("category", "spa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("category"));
        mvc.perform(get("/api/v1/places").param("lat", "11.9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("lat and lng must be provided together"));
        mvc.perform(get("/api/v1/places").param("radiusMeters", "500"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/places/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(get("/api/v1/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void openApiDocumentDescribesTheEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/places']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/places/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/places/{id}/videos']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/recommendations'].post").exists());
    }
}

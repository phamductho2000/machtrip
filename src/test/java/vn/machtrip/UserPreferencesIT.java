package vn.machtrip;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.machtrip.user.UserPreference;
import vn.machtrip.user.UserPreferences;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserPreferencesIT extends AbstractIntegrationTest {

    @Autowired
    UserPreferences preferences;
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void readsStoredPreference() {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO user_preference (user_id, energy, budget_level, tags, party_type) "
                + "VALUES (?, 0.25, 'medium', ARRAY['cafe','view'], 'couple')", id);

        assertThat(preferences.findByUserId(id)).contains(
                new UserPreference(id, 0.25, "medium", java.util.List.of("cafe", "view"), "couple"));
        assertThat(preferences.findByUserId(UUID.randomUUID())).isEmpty();
    }
}

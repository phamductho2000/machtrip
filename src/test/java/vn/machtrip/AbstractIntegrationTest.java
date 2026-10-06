package vn.machtrip;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Boots the full app against throwaway Postgres (PostGIS + pgvector) and Redis containers.
 * Flyway applies V1 + V2, so tests run against the dev seed. Subclasses share one cached context,
 * so the containers start once per test run.
 */
@SpringBootTest(properties = {
        // Required placeholders in application.yml; the containers' own credentials override them.
        "DB_USER=unused", "DB_PASSWORD=unused"})
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
public abstract class AbstractIntegrationTest {
}

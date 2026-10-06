package vn.machtrip;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {

    /** Same Dockerfile as docker-compose: PostGIS + pgvector. Docker's layer cache makes re-builds fast. */
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        String image = new ImageFromDockerfile("machtrip-postgres-it", false)
                .withFileFromPath("Dockerfile", Path.of("docker/postgres/Dockerfile"))
                .get();
        return new PostgreSQLContainer<>(DockerImageName.parse(image).asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("machtrip");
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redis() {
        return new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    }
}

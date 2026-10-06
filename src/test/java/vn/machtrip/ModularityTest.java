package vn.machtrip;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(MachtripApplication.class);

    @Test
    void modulesOnlyUseEachOthersPublicApi() {
        modules.verify();
    }

    @Test
    void expectedModulesExist() {
        var names = modules.stream().map(m -> m.getIdentifier().toString()).toList();
        assertThat(names)
                .containsExactlyInAnyOrder("common", "content", "itinerary", "place", "recommend", "user");
    }
}

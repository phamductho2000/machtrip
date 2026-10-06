package vn.machtrip.user;

import java.util.Optional;
import java.util.UUID;

/** Public API of the user module. No endpoints yet: there is no auth in this iteration. */
public interface UserPreferences {

    Optional<UserPreference> findByUserId(UUID userId);
}

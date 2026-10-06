package vn.machtrip.user.internal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.machtrip.user.UserPreference;
import vn.machtrip.user.UserPreferences;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
class JpaUserPreferences implements UserPreferences {

    private final UserPreferenceRepository repository;

    JpaUserPreferences(UserPreferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserPreference> findByUserId(UUID userId) {
        return repository.findById(userId).map(e -> new UserPreference(e.userId,
                Double.parseDouble(Float.toString(e.energy)), e.budgetLevel, e.tags, e.partyType));
    }
}

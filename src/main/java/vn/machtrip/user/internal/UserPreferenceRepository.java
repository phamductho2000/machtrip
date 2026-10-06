package vn.machtrip.user.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, UUID> {
}

package vn.machtrip.place.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface PlaceFactRepository extends JpaRepository<PlaceFactEntity, UUID> {

    List<PlaceFactEntity> findByPlaceIdOrderByTypeAscObservedAtDesc(UUID placeId);
}

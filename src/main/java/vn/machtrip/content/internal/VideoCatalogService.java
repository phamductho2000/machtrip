package vn.machtrip.content.internal;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.machtrip.content.PlaceVideo;
import vn.machtrip.content.VideoCatalog;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
class VideoCatalogService implements VideoCatalog {

    private final VideoRepository videos;

    VideoCatalogService(VideoRepository videos) {
        this.videos = videos;
    }

    @Override
    public List<PlaceVideo> videosForPlace(UUID placeId, int limit) {
        return videos.findByPlace(placeId, PageRequest.of(0, limit));
    }

    @Override
    public Map<String, String> webVideoUrls(Collection<String> tiktokIds) {
        return videos.findAllById(tiktokIds).stream()
                .collect(Collectors.toMap(v -> v.tiktokId, v -> v.webVideoUrl));
    }
}

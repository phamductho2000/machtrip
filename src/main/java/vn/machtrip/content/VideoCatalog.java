package vn.machtrip.content;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Public read API of the content module. */
public interface VideoCatalog {

    /** Videos mentioning the place, best rank first. */
    List<PlaceVideo> videosForPlace(UUID placeId, int limit);

    /** TikTok id to web URL, for the ids that exist. */
    Map<String, String> webVideoUrls(Collection<String> tiktokIds);
}

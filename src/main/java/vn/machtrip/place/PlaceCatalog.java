package vn.machtrip.place;

import java.util.List;

/** Public read API of the place module. Only places with resolve_status = matched ever leave the module. */
public interface PlaceCatalog {

    /** Matched places whose location is within {@code radiusMeters} of the given point. */
    List<PlaceSummary> findMatchedWithin(double lat, double lng, int radiusMeters);
}

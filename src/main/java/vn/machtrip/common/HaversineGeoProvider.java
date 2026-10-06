package vn.machtrip.common;

import org.springframework.stereotype.Component;

/** Great-circle distance; no network calls. */
@Component
public class HaversineGeoProvider implements GeoProvider {

    private static final double EARTH_RADIUS_METERS = 6_371_008.8;

    @Override
    public double distanceMeters(double fromLat, double fromLng, double toLat, double toLng) {
        double dLat = Math.toRadians(toLat - fromLat);
        double dLng = Math.toRadians(toLng - fromLng);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat)) * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.min(1, Math.sqrt(a)));
    }
}

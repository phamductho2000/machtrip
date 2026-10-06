package vn.machtrip.common;

/** Geo calculations that may later be backed by an external provider (e.g. Goong road distance). */
public interface GeoProvider {

    double distanceMeters(double fromLat, double fromLng, double toLat, double toLng);
}

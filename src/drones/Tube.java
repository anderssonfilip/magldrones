/**
 *
 */
package drones;

/**
 * @author filip
 */
public class Tube {

    private final String _station;
    private final double _latitude, _longitude;

    public Tube(String station, double latitude, double longitude) {
        _station = station;
        _latitude = latitude;
        _longitude = longitude;
    }

    public String getStation() {
        return _station;
    }

    public double getLatitude() {
        return _latitude;
    }

    public double getLongitude() {
        return _longitude;
    }
}

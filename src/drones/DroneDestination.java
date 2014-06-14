/**
 *
 */
package drones;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class DroneDestination {

    private final int _droneId;
    private final double _latitude, _longitude;
    private final LocalDateTime _time;

    public DroneDestination(int droneId, double latitude, double longitude, LocalDateTime time) {
        _droneId = droneId;
        _latitude = latitude;
        _longitude = longitude;
        _time = time;
    }

    public int getDroneId() {return _droneId;}

    public double getLatitude() {
        return _latitude;
    }

    public double getLongitude() {
        return _longitude;
    }

    public LocalTime getTime() {
        return _time.toLocalTime();
    }
}

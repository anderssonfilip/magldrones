package drones;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Drone extends Thread {

    private final int _id;

    private boolean _isTerminated;

    private final List<Tube> _tubes;

    private final Queue<DroneDestination> _destinations = new LinkedList<>();

    public String[] conditions = new String[]{"HEAVY", "LIGHT", "MODERATE"};

    private final Random _random = new Random();

    private final Dispatcher _dispatcher;

    // the current position of the drone
    private double _lat;
    private double _lon;
    private double _bearing;

    private double _speed; // current speed (m/s)
    private double _distanceToDestination; // distance to next destination
    private double _distanceTravelled; // distance since last speed/bearing adjustment

    public Drone(int id, List<Tube> tubes, DroneDestination initialLocation, Dispatcher dispatcher) {
        _id = id;
        _tubes = tubes;

        _lat = initialLocation.getLatitude();
        _lon = initialLocation.getLongitude();

        _dispatcher = dispatcher;
    }


    /**
     * Start the thread (called by Thread.start())
     */
    public void run() {

        while (!_isTerminated) {
            if (!_destinations.isEmpty() && _dispatcher.getTime().isAfter(_destinations.peek().getTime())) {
                DroneDestination destination = _destinations.poll();
                _lat = destination.getLatitude();
                _lon = destination.getLongitude();
                checkNearbyLocations(350);
            }
        }
    }

    /**
     * Start the thread (called by Thread.start())
     */
    public void runTimer() {

        LocalTime t0 = _dispatcher.getTime();
        LocalTime t1 = t0;

        setDestination();

        while (!_isTerminated) {

            long dt = t0.until(t1, ChronoUnit.SECONDS);
            if (dt >= 1) {
                checkNearbyLocations(350); // find stations within distance of 350m
                move(dt);
                t0 = _dispatcher.getTime();
            }
            t1 = _dispatcher.getTime();
        }
    }

    /**
     * Get the drone id
     *
     * @return
     */
    public int getDroneId() {
        return _id;
    }

    /**
     * Get the current speed of the Drone
     *
     * @return
     */
    public double getSpeed() {
        return _speed;
    }


    /**
     * Terminate the drone with a "secret" code
     *
     * @param code
     */
    public void terminate(String code) {
        if (code.equals("SHUTDOWN"))
            _isTerminated = true;
    }

    /**
     * Add a destination to the drones internal _destinations. Since the drone has limited memory
     * this call may return false
     *
     * @param location to store
     * @return true if the drone stored the destination in its _destinations
     */
    public boolean addDestination(DroneDestination location) {

        if (_destinations.size() < 10) {
            return _destinations.offer(location);
        } else {
            return false;
        }
    }

    /**
     * Set the speed required to reach next destination in time
     */
    private void setDestination() {

        DroneDestination destination = _destinations.peek(); // assuming drone is travelling to head of list

        if (destination != null) {

            _distanceToDestination = calcDistance(_lat, destination.getLatitude(), _lon, destination.getLongitude());
            long seconds = _dispatcher.getTime().until(destination.getTime(), ChronoUnit.SECONDS);

            _speed = _distanceToDestination / seconds;
            _bearing = calcBearing(_lat, destination.getLatitude(), _lon, destination.getLongitude());

        } else {
            System.out.println(String.format("Drone %d have no more _destinations...", _id));
            _isTerminated = true;
        }
    }

    /**
     * Find nearby stations and report traffic conditions to dispatcher
     */
    private void checkNearbyLocations(double radius) {

        for (Tube tube : _tubes) {
            if (radius >= calcDistance(_lat, tube.getLatitude(), _lon, tube.getLongitude())) {
                System.out.println(String.format("Drone %d is sending report from %s to Dispatcher", _id, tube.getStation()));

                _dispatcher.reportTraffic(_id, conditions[_random.nextInt(3)]);
            }
        }
    }

    /**
     * Move the drone to its next location.
     * During the move there is a small chance the drone crashes
     */
    private void move(long elapsedTime) {

        if (_random.nextInt(1000000) == 1) {
            System.out.println(String.format("Drone %d crashed into a building", _id));
            _isTerminated = true;
        } else {
            double d = _speed * elapsedTime;

            double delta_latitude = d * Math.sin(_bearing) / 110540;
            double delta_longitude = d * Math.cos(_bearing) / (111320 * Math.cos(_lat));

            _lat += delta_latitude;
            _lon += delta_longitude;
            _distanceTravelled += d;

            if (_distanceTravelled >= _distanceToDestination) {
                _destinations.poll();
                setDestination();
            }
        }
    }

    /**
     * Calculate distance in meters between two points in latitude and longitude
     * Copied from: http://stackoverflow.com/a/16794680/470864
     */
    public static double calcDistance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters
    }

    /**
     * Calculate bearing between two pairs of latitude and longitude
     * Copied from: http://stackoverflow.com/a/9462757/470864
     */
    public static double calcBearing(double lat1, double lat2, double lon1, double lon2) {

        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(lon2 - lon1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }
}

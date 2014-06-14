/**
 *
 */
package drones;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Dispatcher extends Thread {

    private final Map<Integer, Drone> drones = new HashMap<>();
    private final Map<Integer, List<DroneDestination>> _droneLocations;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private LocalTime _time = LocalTime.of(7, 47, 54); // arbitrary time value to start simulation

    public Dispatcher(Map<Integer, List<DroneDestination>> droneLocations, List<Tube> tubes) {

        _droneLocations = droneLocations;

        // for simplicity inject dispatcher in drones (unwanted high-coupling)
        drones.put(5937, new Drone(5937, tubes, _droneLocations.get(5937).get(0), this));
        drones.put(6043, new Drone(6043, tubes, _droneLocations.get(6043).get(0), this));
    }

    /**
     * Start the thread (called by Thread.start())
     */
    public void run() {

        // start the drones
        for (Drone drone : drones.values()) {

            System.out.println(String.format("Sending initial destinations to drone %d", drone.getDroneId()));

            while (drone.addDestination(_droneLocations.get(drone.getDroneId()).get(0))) {
                _droneLocations.get(drone.getDroneId()).remove(0);
            }
            drone.setName("" + drone.getDroneId());
            drone.start();
        }

        long t0 = System.nanoTime();
        long t1 = t0;
        while (_time.isBefore(LocalTime.of(8, 10))) {  // end simulation at 8:10
            if (t1 - t0 >= Program.SampleInterval) {

                readWriteLock.writeLock().lock();
                _time = _time.plusSeconds((t1 - t0) / Program.SampleInterval);
                readWriteLock.writeLock().unlock();

                for (Drone d : drones.values()) {
                    if (!_droneLocations.get(d.getDroneId()).isEmpty()) {
                        if (d.addDestination(_droneLocations.get(d.getDroneId()).get(0))) {
                            _droneLocations.get(d.getDroneId()).remove(0);
                        }
                    } else {
                        System.out.println(String.format("Dispatcher have no more destinations for drone %d", d.getDroneId()));
                    }
                }
                t0 = System.nanoTime();
            }
            t1 = System.nanoTime();
        }

        System.out.println(String.format("Shutting down at %s", _time));

        // terminate drones
        for (Drone drone : drones.values()) {
            drone.terminate("SHUTDOWN");
        }
    }

    /**
     * For simplicity the Dispatcher is keeping track of the actual time
     *
     * @return
     */
    public LocalTime getTime() {
        LocalTime time;
        readWriteLock.readLock().lock();
        time = _time;
        readWriteLock.readLock().unlock();
        return time;
    }

    /**
     * Report a traffic report
     *
     * @param droneId
     * @param condition
     */
    public void reportTraffic(int droneId, String condition) {

        System.out.println(String.format("Dispatcher is reporting traffic: %s, %s, %f, %s", droneId, _time, drones.get(droneId).getSpeed(), condition));
    }
}

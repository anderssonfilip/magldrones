package dronestest;

import drones.Drone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class DroneTest {

    final double error = 0.001;

    @Test
    public void calcDistanceTest() {

        assertEquals(0.0, Drone.calcDistance(0.0, 0.0, 0.0, 0.0), error);
        assertEquals(18.3, Drone.calcDistance(51.476105, 51.475967, -0.100224, -0.100368), error);
    }

    @Test
    public void calcBearingTest() {

        assertEquals(0.0, Drone.calcBearing(0.0, 0.0, 0.0, 0.0), error);
        assertEquals(213.02, Drone.calcBearing(51.476105, 51.475967, -0.100224, -0.100368), error);
    }
}


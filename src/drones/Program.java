/**
 *
 */
package drones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Program {

    public static final long SampleInterval = 10000000;  // time multiplier (seconds) = SampleInterval / 1e9

    /**
     * @param args
     */
    public static void main(String[] args) {

        Program program = new Program();

        try {
            List<Tube> tubes = program.readTubeFile();

            Map<Integer, List<DroneDestination>> droneLocations = new HashMap<>();

            droneLocations.put(5937, program.readLocationFile(5937));
            droneLocations.put(6043, program.readLocationFile(6043));

            Dispatcher dispatcher = new Dispatcher(droneLocations, tubes);

            dispatcher.start();  // start simulation
            dispatcher.join();  // wait for simulation to end

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Read the Tube Stations from file and return a Collection
     */
    public List<Tube> readTubeFile() throws IOException {

        Stream<String> stream = Files.lines(Paths.get("tube.csv"));

        try {
            return stream.map(line -> line.split(","))
                    .map(values -> new Tube(values[0],
                            Double.parseDouble(values[1]),
                            Double.parseDouble(values[2])))
                    .collect(Collectors.toList());
        } finally {
            stream.close();
        }
    }

    /**
     * Read the DroneLocations, specified by it's drone id, from file and return a Collection
     */
    public List<DroneDestination> readLocationFile(int droneId) throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Stream<String> stream = Files.lines(Paths.get(droneId + ".csv"));

        try {
            return stream.map(line -> line.split(",")).map(values ->
                            new DroneDestination(
                                    Integer.parseInt(values[0]),
                                    Double.parseDouble(values[1].replace("\"", "")),  // stripping off "" wrapper
                                    Double.parseDouble(values[2].replace("\"", "")),
                                    LocalDateTime.parse(values[3].replace("\"", ""), formatter))
            ).collect(Collectors.toList());
        } finally {
            stream.close();
        }
    }
}

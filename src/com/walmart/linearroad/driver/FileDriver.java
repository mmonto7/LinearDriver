package com.walmart.linearroad.driver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Sung Kim on 10/19/16.
 * <p>
 * A Java Linear Road driver designed to send 5-15 seconds worth of data in a bursty manner.
 * To keep timings relatively accurate we take into account the amount of time needed to actually send the data before
 * sending the next set. So, if it takes 5 seconds to actually send 15 seconds (assuming 15 was the randomly
 * generated amount for the burst) of data, then if the next random amount was 5, the next set would start sending
 * immediately rather than wait another 5 seconds.
 * Otherwise a lag appears with the final run time +~10-20 min above the default desired end time of 180 min.
 * <p>
 * All of the drivers will have the common core and will be reading from a file. The only difference will be the
 * destination.
 */
public class FileDriver {
    /**
     * To hold the random number of seconds worth of data that will be sent.
     * We could add the ability to create arbitrary bursts.
     * Also, the driver may be arbitrary in that a simple, constant, second-by-second flow of cars may be more
     * desirable.
     * At the moment, send numSeconds worth of data, then wait numSeconds.
     */
    private int numSeconds;
    /**
     * The Random object instance from which to pull random ints [5,15]
     */
    private Random random;
    /**
     * The fileName from which to read the input data.
     */
    private String fileName;
    /**
     * The BufferedReader to actually read the file.
     */
    BufferedReader reader;
    /**
     * The output destination.
     */
    // To be decided. Send to System.out for now.

    /**
     * @param numSeconds
     * @param fileName
     */
    public FileDriver(String fileName) {
        this.fileName = fileName;
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException ex) {
            // We should catch this error early.
            System.err.println(ex);
            System.err.println("The file " + fileName + " could not be found.");
            System.exit(1);
        }
        random = new Random();
    }

    /**
     * Per the paper we initially seek bursts of 5 - 15 seconds.
     *
     * @return the amount of time to wait.
     */
    private int getInterval() {
        return random.nextInt(11) + 5;
    }

    /**
     * Start the driver. All output goes to stdout via System.out.println() at the moment.
     */
    public void run() {

        int currMaxSeconds = 0;
        // Hold each respective line from the file.
        String line = "";
        // Hold how long it took to send the data.
        long timeToSend;

        // Begin reading the file.
        try {
            while ((line = reader.readLine()) != null) {

                // === Get the time field.
                // We have to parse the line to get at the time field.
                int firstComma = line.indexOf(',');
                String timeField = line.substring(firstComma + 1, line.indexOf(',', firstComma + 1));
                int timeFieldInt = Integer.parseInt(timeField);

                // === Try sleeping for the given amount of time.
                // Go ahead and wait numSeconds first, and then send numSeconds worth of data.
                if (timeFieldInt == currMaxSeconds) {

                    // Set a new currMaxSeconds.
                    numSeconds = getInterval();
                    currMaxSeconds += numSeconds;
                    // DEBUG
                    System.out.println(numSeconds);

                    // Wait the numSeconds amount of time.
                    // Time how long it takes to send all the data.
                    // From the next actual wait time, subtract the amount of time it took to send the data.
                    try {
                        // Catch the timeToSend here and substract for the base wait time.
                        Thread.sleep(numSeconds * 1000);
                        // Start the timeToSend here.
                    } catch (InterruptedException ex) {
                        System.err.println(ex);
                        System.err.println("Waiting for new burst of " + numSeconds + " encountered an error.");
                        // Exit or keep on going?
                        // Exit for now.
                        System.exit(1);
                    }
               }
                // === End processing send interval.

                // Print out the line.
                System.out.println(line);
            }
        } catch (IOException ex) {
            System.err.println(ex);
            System.err.println("A problem occurred while reading the input file at line " + line);
            // Go ahead and exit if we have problem reading a line. A true system may need to be okay with some
            // dropped info.
            System.exit(1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    System.err.println("");
                }
            }
        }
    }

    /**
     * Drive
     *
     * @param args
     */
    public static void main(String[] args) {
        // Use a command line argument.
        if (args.length < 1 || args.length > 1) {
            System.err.println("Usage: java FileDriver <input file>");
            System.exit(1);
        }
        String fileName = args[0];
        FileDriver driver = new FileDriver(fileName);
        driver.run();
    }
}

package seng302.utilities;

import seng302.data.RaceVisionFileReader;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created on 6/03/17.
 * A set of configuration options used throughout the software, read from a config file at startup.
 */
public class Config {

    private static final String DEFAULT_CONFIG_PATH = "/defaultFiles/config.txt";
    private static final String EXPECTED_CONFIG_PATH = "config.txt";
    public static int NUM_BOATS_IN_RACE;
    public static int TIME_SCALE_IN_SECONDS;

    /**
     * This function finds a config file located at DEFAULT_CONFIG_PATH and sets any properties it finds in the file.
     * Example format for property-value pair: NUMBOATS=6
     */
    public static void initializeConfig(){
        try {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(EXPECTED_CONFIG_PATH));
            } catch (FileNotFoundException e){
                RaceVisionFileReader.exportResource(DEFAULT_CONFIG_PATH, EXPECTED_CONFIG_PATH);
                br = new BufferedReader(new FileReader(EXPECTED_CONFIG_PATH));
            }
            String line = br.readLine();
            while (line != null){
                StringTokenizer st = new StringTokenizer(line);

                String token = st.nextToken("=");
                switch(token) {
                    case "NUMBOATS":
                        NUM_BOATS_IN_RACE = Integer.parseInt(st.nextToken());
                        break;
                    case "TIMESCALE":
                        TIME_SCALE_IN_SECONDS = (int)TimeUtils.convertMinutesToSeconds(Double.parseDouble(st.nextToken()));
                        break;
                    default:
                        throw new IOException("Invalid Token.");
                }

                line = br.readLine();
            }
        } catch (IOException e) {
            System.err.printf("Error reading config file. Check it is in the correct format: %s", e);
            e.printStackTrace();
        }
    }
}

package kolemannix.com.marauderandroid;

import android.location.Location;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by knix on 9/21/15.
 */
public class Service {

    private static final String API_URL = "http://127.0.0.1:8085/";

    public static Map<MarauderProfile, Location> getLocations() {
        return new HashMap<>();
    }

    public static void setLocation() {
        // TODO use volley
        String url = API_URL + "set";
    }


}

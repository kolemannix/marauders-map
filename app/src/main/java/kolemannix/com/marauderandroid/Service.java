package kolemannix.com.marauderandroid;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by knix on 9/21/15.
 */
public class Service {

    private static final String API_URL = "https://marauder-api.herokuapp.com/";

    public static List<MarauderProfile> update(MarauderProfile profile) throws IOException, JSONException {
        String request = API_URL + "update";
        URL url = null;
        try {
            url = new URL(request);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

        JSONObject jsonProfile = profile.toJSON();
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("profile", jsonProfile);

        wr.writeBytes(jsonParam.toString());

        wr.flush();
        wr.close();

        int code = conn.getResponseCode();
        StringBuilder sb = new StringBuilder();
        if (code == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(),"utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            System.out.println(""+sb.toString());
        }

        JSONObject result = new JSONObject(sb.toString());
        JSONArray jsonProfiles = (JSONArray) result.get("profiles");
        List<MarauderProfile> profiles = new ArrayList<>();
        for (int i = 0; i < jsonProfiles.length(); i++) {
            MarauderProfile p = MarauderProfile.profileFromJSON((JSONObject) jsonProfiles.get(i));
            profiles.add(p);
        }
        return profiles;
    }

}

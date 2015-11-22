package kolemannix.com.marauderandroid;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by knix on 9/21/15.
 */
public class MarauderProfile {
    public final String email;
    public final String nickname;
    public LatLng coordinate;
    public final int icon;

    public MarauderProfile() {
        this("hp@gmail.com", "hp", 0);
    }

    public MarauderProfile(String email, String nickname, int icon) {
        this.email = email;
        this.nickname = nickname;
        this.icon = icon;
    }

    public String[] toStringArray() {
        String[] result = new String[3];
        result[0] = email;
        result[1] = nickname;
        result[2] = Integer.toString(icon);
        return result;
    }

    public static MarauderProfile fromStringArray(String[] s) {
        return new MarauderProfile(s[0], s[1], Integer.parseInt(s[2]));
    }

    public static MarauderProfile profileFromJSON(JSONObject obj) {
        MarauderProfile result = null;
        try {
            result = new MarauderProfile(obj.getString("email"), obj.getString("nickname"), obj.getInt("icon"));
            JSONArray arr = obj.getJSONArray("coordinate");
            result.coordinate = new LatLng((double)arr.get(0), (double)arr.get(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("Profile Parsed:", result.toStringArray().toString());
        return result;
    }


    public JSONObject toJSON() {
        JSONArray latlng = null;
        JSONObject result = null;
        try {
            latlng = new JSONArray().put(0, coordinate.latitude).put(1, coordinate.longitude);
            result = new JSONObject().put("email", email).put("nickname", nickname).put("icon", icon).put("coordinate", latlng);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}

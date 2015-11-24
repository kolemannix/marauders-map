package kolemannix.com.marauderandroid;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final float ZOOM_LEVEL = 18.0f;

    private LocationManager mLocationManager;
    private GoogleMap mMap;

    private MarauderProfile mProfile;

    private Thread mApiThread;
    private ApiRunnable mApiRunnable;

    private List<MarauderProfile> locations;
    private Map<String, Marker> markers;
    private Marker mMyMarker;

    // CONSTANTS
    public static final int REQUEST_NEW_PROFILE = 100;
    private final int[] ICONS = {R.drawable.hallows_64, R.drawable.wolf_64, R.drawable.stag_64, R.drawable.mouse_64};

    private static final int POLL_INTERVAL = 1000;

    private boolean first;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        
        mProfile = MarauderProfile.fromStringArray(intent.getStringArrayExtra("profile"));

        first = true;
        markers = new HashMap<String, Marker>();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocation(location);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
            @Override
            public void onProviderEnabled(String provider) {

            }
            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5.0f, listener);

        mApiRunnable = new ApiRunnable();
        mApiThread = new Thread(mApiRunnable);
        mApiThread.start();
    }

    private class ApiRunnable implements Runnable {
        private volatile boolean running = true;
        public void terminate() {
            Log.i("Stop!", "Stopping!");
            running = false;
        }
        @Override
        public void run() {
            while (running) {
                pollPositions();
                try {
                    Thread.sleep(POLL_INTERVAL);
                } catch (InterruptedException e) {
                    terminate();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_PROFILE) {
            if (resultCode == RESULT_OK) {
                MarauderProfile updated = MarauderProfile.fromStringArray(data.getStringArrayExtra("profile"));
                mProfile.nickname = updated.nickname;
                mProfile.icon = updated.icon;
                mMyMarker.remove();
                mMyMarker = mMap.addMarker(optionsForPair(mProfile));
            }
        }
    }

    public void updateProfile(View view) {
        Intent intent = new Intent(this, UpdateProfileActivity.class);
        startActivityForResult(intent, REQUEST_NEW_PROFILE);
    }

    private void pollPositions() {
        // Make volley request
        Log.i("Location worker thread", "updated positions");

        // Move Harry randomly to test
        try {
            if (mProfile.coordinate == null) {
                return;
            }
            locations = Service.update(mProfile);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                redrawMarkers();
            }
        });
    }

    private void redrawMarkers() {
        if (mMap == null)
            return;

        // Cleanup
        List<String> toBeRemoved = new ArrayList<>();
        for (Map.Entry<String, Marker> pair : markers.entrySet()) {

            String email = pair.getKey();
            boolean contained = false;
            for (MarauderProfile profile : locations) {
                if (profile.email.contentEquals(email)) {
                    contained = true;
                }
            }
            if (!contained)
                toBeRemoved.add(email);
        }

        for (String email : toBeRemoved) {
            markers.remove(email);
        }


        // Update markers
        if (first) {
            for (MarauderProfile profile : locations) {
                Marker m = mMap.addMarker(optionsForPair(profile));
                markers.put(profile.email, m);
            }
            first = false;
        } else {
            for (MarauderProfile profile : locations) {

                if (markers.containsKey(profile.email)) {
                    // Already in: Update marker!
                    markers.get(profile.email).setPosition(profile.coordinate);
                    markers.get(profile.email).setTitle(profile.nickname);
                    markers.get(profile.email).setSnippet(profile.email);
                    markers.get(profile.email).setIcon(BitmapDescriptorFactory.fromResource(ICONS[profile.icon]));
                } else {
                    // Not yet in : Add new marker!
                    Marker m = mMap.addMarker(optionsForPair(profile));
                    markers.put(profile.email, m);
                }
            }
        }
        // Redraw ourself
        mMyMarker.setPosition(mProfile.coordinate);
    }

    private LatLng lastKnownLocation() {
        Location gpsLast = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLast = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location passiveLast = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (gpsLast != null) return new LatLng(gpsLast.getLatitude(), gpsLast.getLongitude());
        if (networkLast != null) return new LatLng(networkLast.getLatitude(), networkLast.getLongitude());
        if (passiveLast != null) return new LatLng(passiveLast.getLatitude(), passiveLast.getLongitude());
        return null;
    }

    private void updateLocation(Location location) {
        mProfile.coordinate = new LatLng(location.getLatitude(), location.getLongitude());
        panToLocation(mProfile.coordinate);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void mischiefManaged(View view) {
        finish();
    }

    private void panToLocation(LatLng location) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, ZOOM_LEVEL);
        mMap.animateCamera(cameraUpdate);
    }

    private MarkerOptions optionsForPair(MarauderProfile profile) {
        return new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(ICONS[profile.icon]))
                .title(profile.nickname)
                .snippet(profile.email)
                .position(profile.coordinate);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Got mah map!
        mMap = googleMap;
        mProfile.coordinate = lastKnownLocation();

        if (mProfile.coordinate != null) {
            panToLocation(mProfile.coordinate);
            mMyMarker = mMap.addMarker(optionsForPair(mProfile).snippet(mProfile.nickname + " (me)"));
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiRunnable.terminate();
        try {
            mApiThread.join();
            Log.i("Joined!", "Joined!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

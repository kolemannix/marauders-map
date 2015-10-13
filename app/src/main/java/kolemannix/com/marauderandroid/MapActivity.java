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
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final float ZOOM_LEVEL = 18.0f;

    private LocationManager mLocationManager;
    private GoogleMap mMap;

    private MarauderProfile mProfile;
    private LatLng mMyLatLng;

    private Thread mApiThread;
    private ApiRunnable mApiRunnable;

    private Map<MarauderProfile, LatLng> locations;
    private Map<MarauderProfile, Marker> markers;
    private Marker mMyMarker;

    // CONSTANTS
    public static final int REQUEST_NEW_PROFILE = 100;
    private final int[] ICONS = {R.drawable.hallows_64, R.drawable.wolf_64, R.drawable.stag_64, R.drawable.mouse_64};
    private static final LatLng ROTUNDA = new LatLng(38.035637, -78.503378);
    private static final LatLng RICE_HALL = new LatLng(38.031713, -78.511050);

    private static final double MOVE_SPEED = 0.000025f;
    private static final int POLL_INTERVAL = 250;

    private static final Map<MarauderProfile, LatLng> STATIC_TEST_LOCATIONS;

    static MarauderProfile harryPotter = new MarauderProfile("harry@hogwarts.com", "Harry", 2);
    static MarauderProfile hermioneGranger = new MarauderProfile("hermione@hogwarts.com", "Hermione", 0);
    static MarauderProfile ronWeasley = new MarauderProfile("beater420@weasleyclan.com", "Ron", 1);
    static MarauderProfile peterPettigrew = new MarauderProfile("peter@deatheaters.org", "Peter", 3);

    private boolean first;

    static {
        STATIC_TEST_LOCATIONS = new HashMap<>();
        STATIC_TEST_LOCATIONS.put(harryPotter, ROTUNDA);
        STATIC_TEST_LOCATIONS.put(hermioneGranger, RICE_HALL);
        STATIC_TEST_LOCATIONS.put(peterPettigrew, ROTUNDA);
        STATIC_TEST_LOCATIONS.put(ronWeasley, RICE_HALL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        
        mProfile = MarauderProfile.fromStringArray(intent.getStringArrayExtra("profile"));


        first = true;
        markers = new HashMap<MarauderProfile, Marker>();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locations = STATIC_TEST_LOCATIONS;

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
                mProfile = MarauderProfile.fromStringArray(data.getStringArrayExtra("profile"));
                Log.i("New profile!", mProfile.toStringArray().toString());
                mMyMarker.remove();
                mMyMarker = mMap.addMarker(optionsForPair(mProfile, mMyLatLng));
            }
        }
    }

    public void updateProfile(View view) {
        Intent intent = new Intent(this, UpdateProfileActivity.class);
        startActivityForResult(intent, REQUEST_NEW_PROFILE);
    }

    private LatLng moveRandomly(LatLng start) {
        Random random = new Random();
        double latDelta = random.nextDouble() * MOVE_SPEED * (random.nextBoolean() ? 1.0 : -1.0);
        double lonDelta = random.nextDouble() * MOVE_SPEED * (random.nextBoolean() ? 1.0 : -1.0);
        double lat = start.latitude + latDelta;
        double lon = start.longitude + lonDelta;
        return new LatLng(lat, lon);
    }

    private void pollPositions() {
        // Make volley request
        Log.i("Location worker thread", "updated positions");

        // Move Harry randomly to test
        locations.put(harryPotter, moveRandomly(locations.get(harryPotter)));
        locations.put(hermioneGranger, moveRandomly(locations.get(hermioneGranger)));
        locations.put(peterPettigrew, moveRandomly(locations.get(peterPettigrew)));
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

        if (first) {
            for (Map.Entry<MarauderProfile, LatLng> pair : locations.entrySet()) {
                MarauderProfile profile = pair.getKey();
                LatLng latLng = pair.getValue();
                Marker m = mMap.addMarker(optionsForPair(profile, latLng));
                markers.put(profile, m);
            }
            first = false;
        } else {
            for (Map.Entry<MarauderProfile, LatLng> pair : locations.entrySet()) {
                MarauderProfile profile = pair.getKey();
                LatLng latLng = pair.getValue();

                if (markers.containsKey(profile)) {
                    // Already in: Update marker!
                    markers.get(profile).setPosition(latLng);
                } else {
                    // Not yet in : Add new marker!
                    Marker m = mMap.addMarker(optionsForPair(profile, latLng));
                    markers.put(profile, m);
                }
            }
        }
        // Redraw ourself
        mMyMarker.setPosition(mMyLatLng);
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
        mMyLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        panToLocation(mMyLatLng);
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

    private MarkerOptions optionsForPair(MarauderProfile profile, LatLng latLng) {
        return new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(ICONS[profile.icon]))
                .title(profile.nickname)
                .snippet(profile.email)
                .position(latLng);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Got mah map!
        mMap = googleMap;


        mMyLatLng = lastKnownLocation();
        panToLocation(mMyLatLng);
        if (mMyLatLng != null) {
            panToLocation(mMyLatLng);
            mMyMarker = mMap.addMarker(optionsForPair(mProfile, mMyLatLng).snippet(mProfile.nickname + " (me)"));
        }
        GroundOverlayOptions parchmentTextureOptions = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.parchment_texture_small))
                .transparency(0.30f)
                .position(ROTUNDA, 2000, 1300);

//        rotundaGroundOverlay = mMap.addGroundOverlay(parchmentTextureOptions);

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

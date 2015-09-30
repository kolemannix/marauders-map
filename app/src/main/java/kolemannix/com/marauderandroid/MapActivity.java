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
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final float ZOOM_LEVEL = 16.0f;

    private LocationManager mLocationManager;
    private GoogleMap mMap;
    private GroundOverlay rotundaGroundOverlay;
    private Marker mPositionMarker;

    LatLng ROTUNDA = new LatLng(38.035637, -78.503378);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        String[] profile = intent.getStringArrayExtra("profile");
        Log.i("profile", "Username: " + profile[0] +  ", Email: " + profile[1] + ", Icon: " + profile[2]);

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

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, listener);

    }

    private Location lastKnownLocation() {
        Location gpsLast = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLast = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location passiveLast = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (gpsLast != null) return gpsLast;
        if (networkLast != null) return networkLast;
        if (passiveLast != null) return passiveLast;
        return null;
    }

    private void updateLocation(Location location) {
        panToLocation(location);
    }

    public void mischiefManaged(View view) {
        finish();
        getApplication().onTerminate();
        System.exit(0);
    }

    private void panToLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Got mah map!
        mMap = googleMap;

        Location lastKnown = lastKnownLocation();
        if (lastKnown != null) {
            panToLocation(lastKnown);
            mPositionMarker = mMap.addMarker(new MarkerOptions()
                    .flat(true)
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.mouse_64))
                    .anchor(0.5f, 0.5f)
                    .position(new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude())));
        }
        LatLng southwest = new LatLng(38.031641, -78.507113);
        LatLng northeast = new LatLng(38.036096, -78.501177);
        LatLngBounds lawnBounds = new LatLngBounds(southwest, northeast);
        GroundOverlayOptions rotundaMapOptions = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.rotunda_overlay))
                .anchor(1, 0)
                .bearing(22.5f)
                .position(northeast, 350);

        GroundOverlayOptions parchmentTextureOptions = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.parchment_texture_small))
                .transparency(0.30f)
                .position(ROTUNDA, 2000, 1300);

//        rotundaGroundOverlay = googleMap.addGroundOverlay(rotundaMapOptions);
        rotundaGroundOverlay = mMap.addGroundOverlay(parchmentTextureOptions);

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }
}

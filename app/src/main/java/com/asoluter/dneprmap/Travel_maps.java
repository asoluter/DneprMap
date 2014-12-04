package com.asoluter.dneprmap;

import android.content.Intent;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

public class Travel_maps extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call  once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                Intent intent=getIntent();
                setUpMap(intent.getStringExtra("position"),intent.getStringExtra("destination"));
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(String position,String destination) {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        RouteApi routeService = restAdapter.create(RouteApi.class);
        RouteResponse routeResponse = routeService.getRoute(position, destination, true, "ru");

        List<LatLng> mPoints= PolyUtil.decode(routeResponse.getPoints());

        PolylineOptions line = new PolylineOptions();
        line.width(4f).color(R.color.material_blue_grey_900);
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < mPoints.size(); i++) {
            if (i == 0) {
                MarkerOptions startMarkerOptions = new MarkerOptions()
                        .position(mPoints.get(i))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
                mMap.addMarker(startMarkerOptions);
            } else if (i == mPoints.size() - 1) {
                MarkerOptions endMarkerOptions = new MarkerOptions()
                        .position(mPoints.get(i))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
                mMap.addMarker(endMarkerOptions);
            }
            line.add(mPoints.get(i));
            latLngBuilder.include(mPoints.get(i));
        }
        mMap.addPolyline(line);
        int size = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds latLngBounds = latLngBuilder.build();
        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
        mMap.moveCamera(track);
    }

    public interface RouteApi {
        @GET("/maps/api/directions/json")
        RouteResponse getRoute(
                @Query(value = "origin", encodeValue = false) String position,
                @Query(value = "destination", encodeValue = false) String destination,
                @Query("sensor") boolean sensor,
                @Query("language") String language);
    }

    public class RouteResponse {

        public List<Route> routes;

        public String getPoints() {
            return this.routes.get(0).overview_polyline.points;
        }

        class Route {
            OverviewPolyline overview_polyline;
        }

        class OverviewPolyline {
            String points;
        }
    }
}

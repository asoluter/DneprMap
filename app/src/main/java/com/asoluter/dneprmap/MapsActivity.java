package com.asoluter.dneprmap;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class MapsActivity extends FragmentActivity implements LocationListener {


    public static SQLiteDatabase database;
    public static Cursor cursor;

    public static final String DATABASE_NAME="places.sqlite3";
    public static final String TABLE_NAME="places";
    public static final String TABLE_TITLE="place";
    public static final String TABLE_PIC="background";
    public static final String TABLE_TEXT="description";
    public static final String TABLE_CHECK="checked";

    private LatLngBounds latlngBounds;
    private Marker PositionMarker;

    ProgressDialog progressDialog;
    AlertDialog dialog;
    LatLng from;
    public Location loc;

    private Polyline newPolyline;
    private boolean isTravelingToParis = false;
    private int width, height;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    SharedPreferences preferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getScreenDimensions();

        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        progressDialog=new ProgressDialog(getApplicationContext());
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,1,this);
        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER,0,1,this);
        preferences=getSharedPreferences("MyPref",MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,1,this);
        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER,0,1,this);

        setUpMapIfNeeded();

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call  once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {

        try{

                database=new ExternalDbOpenHelper(this,DATABASE_NAME).openDataBase();
                cursor=OpenData.cursor(this);

        }catch (SQLException e){
            Log.wtf("info","FailDatabase");
        }

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                GetPos getPos=new GetPos();
                getPos.execute();
            }
        }
    }

    /**
     * GetPos
     */

    private Location getLastBestLocation() {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    class GetPos extends AsyncTask<Integer,String,Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            from=new LatLng(loc.getLatitude(),loc.getLongitude());
            Log.d(getResources().getString(R.string.debug), String.valueOf(from));

            try {
                setUpMap(from);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            do {
                loc=getLastBestLocation();
            }while (loc==null);
            return null;
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(LatLng position) throws SQLException {

        PositionMarker=mMap.addMarker(new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_history_black)));

        Cursor cursor=OpenData.cursor(getApplicationContext());
        cursor.moveToFirst();
        double rad=preferences.getInt("sradius",0)*100;
        while (!cursor.isAfterLast()){
            double x=cursor.getDouble(2);
            if(x!=0)
            if(rad==0||checkRadius(position.latitude,position.longitude,cursor.getDouble(2),cursor.getDouble(3))){
                mMap.addMarker(new MarkerOptions().position(new LatLng(cursor.getDouble(2),cursor.getDouble(3))).title(cursor.getString(0)));} cursor.moveToNext();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,10));
        mMap.setOnInfoWindowClickListener(onMarker);
    }

    public boolean checkRadius(double lat1,double long1,double lat2,double long2){
        double rad=preferences.getInt("sradius",0);
        if(rad==0)return true;
        //радиус Земли
        long R = 6372795;

        //перевод коордитат в радианы
        lat1 *= Math.PI / 180;
        lat2 *= Math.PI / 180;
        long1 *= Math.PI / 180;
        long2 *= Math.PI / 180;

        //вычисление косинусов и синусов широт и разницы долгот
        double cl1 = Math.cos(lat1);
        double cl2 = Math.cos(lat2);
        double sl1 = Math.sin(lat1);
        double sl2 = Math.sin(lat2);
        double delta = long2 - long1;
        double cdelta = Math.cos(delta);
        double sdelta = Math.sin(delta);

        //вычисления длины большого круга
        double y = Math.sqrt(Math.pow(cl2 * sdelta, 2) + Math.pow(cl1 * sl2 - sl1 * cl2 * cdelta, 2));
        double x = sl1 * sl2 + cl1 * cl2 * cdelta;
        double ad = Math.atan2(y, x);
        double dist = ad * R; //расстояние между двумя координатами в метрах

        if(dist<=rad*100)return true;
        else return false;
    }


    public GoogleMap.OnInfoWindowClickListener onMarker=new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            Intent intent=new Intent(getApplicationContext(),PlaceActivity.class);
            intent.putExtra("title",marker.getTitle());
            startActivity(intent);
        }
    };

    /**
     * Location
     */

    LocationManager locationManager;

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            //from=new LatLng(location.getLatitude(),location.getLongitude());
            if(PositionMarker!=null)moveMarker(PositionMarker,location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast toast=Toast.makeText(getApplicationContext(),"Bad connection",Toast.LENGTH_SHORT);
        if(status== LocationProvider.TEMPORARILY_UNAVAILABLE) toast.show();
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        showAlert();
    }

    public void showAlert(){
        dialog=new AlertDialog.Builder(this).create();

        dialog.setTitle("Warning");
        dialog.setMessage("Не включены службы геолокации");
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,"Включить",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                finish();
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Get Directions
     */


    private void moveMarker(Marker marker,Location location){
        LatLng position=new LatLng(location.getLatitude(),location.getLongitude());
        marker.setPosition(position);
    }



    private void getScreenDimensions()
    {
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
    }


}

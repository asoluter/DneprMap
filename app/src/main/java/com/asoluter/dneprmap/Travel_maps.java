package com.asoluter.dneprmap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.view.SubMenu;
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

public class Travel_maps extends FragmentActivity implements LocationListener {

    private LatLngBounds latlngBounds;
    private Marker PositionMarker;
    private Marker DestinationMarker;
    ProgressDialog progressDialog;
    AlertDialog dialog;
    LatLng from;
    public Location loc;

    private Polyline newPolyline;
    private ArrayList<Polyline> polylines;
    private boolean isTravelingToParis = false;
    private int width, height;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_maps);
        getScreenDimensions();

        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        progressDialog=new ProgressDialog(getApplicationContext());
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,1,this);
        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER,0,1,this);
        preferences=getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=preferences.edit();
        polylines=new ArrayList<>();
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

                GetPos getPos=new GetPos();
                getPos.execute();

            }
        }
    }

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

    class GetPos extends AsyncTask<Integer,String,Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            Intent intent=getIntent();
            String s=getIntent().getStringExtra("travel");
            from=new LatLng(loc.getLatitude(),loc.getLongitude());
            if(s==null){
                Bundle bundle=intent.getBundleExtra("bundle");
                LatLng to=bundle.getParcelable("destination");

                latlngBounds = createLatLngBoundsObject(from,to);
                Log.d(getResources().getString(R.string.debug), String.valueOf(from));

                setUpMap(from, to);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds,width,height,300));
            }
            else
            {
                Cursor tcursor=OpenData.tcursor(getApplicationContext());
                tcursor.moveToFirst();
                while (!s.equals(tcursor.getString(0))){
                    tcursor.moveToNext();
                    if(tcursor.isAfterLast())break;
                }
                //Toast.makeText(getApplicationContext(), String.valueOf(tcursor.getPosition()), Toast.LENGTH_SHORT);
                tsetUpMap(from, tcursor.getString(1));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(from,10));
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
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(LatLng position,LatLng destination) {
        PositionMarker=mMap.addMarker(new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_history_black)));
        DestinationMarker=mMap.addMarker(new MarkerOptions().position(destination));

        if(preferences.getBoolean("walking",true))findDirections(position.latitude,position.longitude,destination.latitude,destination.longitude,GMapV2Direction.MODE_WALKING);
        else findDirections(position.latitude,position.longitude,destination.latitude,destination.longitude,GMapV2Direction.MODE_DRIVING);
    }

    private void tsetUpMap(LatLng position,String places){
        PositionMarker=mMap.addMarker(new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_history_black)));
        String[] ss=places.split(","); int k=ss.length;
        LatLng last=position;
        LatLng prev;
        try {
            Cursor cursor=OpenData.cursor(getApplicationContext());
            cursor.moveToPosition(Integer.valueOf(ss[0])-1);
            prev=new LatLng(cursor.getDouble(2),cursor.getDouble(3));
            if(preferences.getBoolean("walking",true))findDirections(last.latitude,last.longitude,prev.latitude,prev.longitude,GMapV2Direction.MODE_WALKING);
            else findDirections(last.latitude,last.longitude,prev.latitude,prev.longitude,GMapV2Direction.MODE_DRIVING);
            mMap.addMarker(new MarkerOptions().position(prev).title(cursor.getString(0)));
            for(int i=1;i<k;i++){
                cursor.moveToPosition(Integer.valueOf(ss[i])-1);
                last=prev;
                prev=new LatLng(cursor.getDouble(2),cursor.getDouble(3));
                if(preferences.getBoolean("walking",true))findDirections(last.latitude,last.longitude,prev.latitude,prev.longitude,GMapV2Direction.MODE_WALKING);
                else findDirections(last.latitude,last.longitude,prev.latitude,prev.longitude,GMapV2Direction.MODE_DRIVING);
                mMap.addMarker(new MarkerOptions().position(prev).title(cursor.getString(0)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * This if where we get Our Position
     */

    LocationManager locationManager;

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!location.equals(null)){
            from=new LatLng(location.getLatitude(),location.getLongitude());
            if(PositionMarker!=null)moveMarker(PositionMarker,location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast toast=Toast.makeText(getApplicationContext(),"Bad connection",Toast.LENGTH_SHORT);
        if(status==LocationProvider.TEMPORARILY_UNAVAILABLE) toast.show();
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.ic_settings)
        {
            SubMenu subMenu=item.getSubMenu();

            if(preferences.getBoolean("walking",true))subMenu.getItem(0).setChecked(true);
            else subMenu.getItem(1).setChecked(true);
        }
        else{
            for(int i=0;i<polylines.size();i++){
                polylines.get(i).remove();
            }
            polylines.clear();
            Boolean bt,b=preferences.getBoolean("walking",true); bt=b;
            if(item.getItemId()==R.id.mode_walking_bimg)b=true;
            if(item.getItemId()==R.id.mode_driving_bimg)b=false;
            editor.putBoolean("walking",b);
            editor.apply();
            item.setChecked(true);
            if(bt!=b){
                GetPos getPos=new GetPos();
                getPos.execute();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_travel_maps,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onProviderDisabled(String provider) {
        showAlert();
    }

    public void showAlert(){
        dialog=new AlertDialog.Builder(this).create();

        dialog.setTitle("Внимание");
        dialog.setMessage("Отключены службы геолокации");
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
     * This is where we find direction to the Destination Position
     */

    private void moveMarker(Marker marker,Location location){
        LatLng position=new LatLng(location.getLatitude(),location.getLongitude());
        marker.setPosition(position);
    }

    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {
        PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);

        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add(directionPoints.get(i));
        }

        newPolyline=mMap.addPolyline(rectLine);
        polylines.add(newPolyline);

    }

    private void getScreenDimensions()
    {
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
    }

    private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
    {
        if (firstLocation != null && secondLocation != null)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(firstLocation).include(secondLocation);

            return builder.build();
        }
        return null;
    }

    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

        GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
        asyncTask.execute(map);
    }
}

package com.mathiascraeghs.foodtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Handler;

import static android.widget.Toast.*;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Cursor mCursor;

    private String googleSearchResults;


    private double latitude;
    private double longitude;
    private double radius = 5000;

    private RestaurantAdapter adapter;
    private RecyclerView mNumberList;
    private NetworkUtils mNetworkUtils;

    private LocationManager mLocationMangager;
    private LocationListener mLocationListener;

    /**
     * the methods that is called when the activity starts
     * @param savedInstanceState savedInstanceState
     */
    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNumberList = (RecyclerView) findViewById(R.id.rv_numbers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNumberList.setLayoutManager(layoutManager);

        getLocation();
        setupSeekBarPreferences();

        Log.i("coord", Double.toString(getLatitude()));
        Log.i("coord", Double.toString(getLongitude()));

        Log.i("radius", String.valueOf(radius));
        updateURL();

    }

    /**
     *  update the URL and does a request
     */
    public void updateURL() {
        String URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+getLatitude()+","+getLongitude()+"&radius="+getRadius()+"&type=restaurant&key=AIzaSyAWp6MXdRMNjutTPL1qr-8EPX6UgEaU4ac";
        Log.i("URL", URL);
        try {
            new GoogleQueryTask().execute(new URL(Uri.parse(URL).toString()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * methods that does a location request and than update the longitude and latitude
     */
    public void getLocation(){
        mLocationMangager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setLatitude(location.getLatitude());
                setLongitude(location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
                return;
            }
        } else {
            mLocationMangager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 60000, 0, mLocationListener);
        }
        mLocationMangager.requestLocationUpdates("gps", 1000000, 0, mLocationListener);
        Location loc =mLocationMangager.getLastKnownLocation("gps");
        if(loc != null) {
            setLatitude(loc.getLatitude());
            setLongitude(loc.getLongitude());
        }
        else{
            Location locNet =mLocationMangager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (locNet !=null){
                setLatitude(locNet.getLatitude());
                setLongitude(locNet.getLongitude());

            }
        }
    }

    /**
     * checks if the permissions are granted
     * @param requestCode requestCode
     * @param permissions permissions
     * @param grantResults grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
        }
    }

    /**
     * method that sets the latitude
     * @param latitude the latitude that you want to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * method that set the longitude
     * @param longitude the latitude that you want to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * receives the new radius that you want to set
     */
    private void setupSeekBarPreferences(){
        SharedPreferences seekBarPreference = PreferenceManager.getDefaultSharedPreferences(this);

        String radiusString = String.valueOf(seekBarPreference.getInt(this.getString(R.string.pref_Distance_key), 2));
        radius= Integer.valueOf(radiusString)*1000;
        Log.i("radius",String.valueOf(radius));
        if(radius <= 1000) radius =2000;
        Log.i("radius",String.valueOf(radius));

        seekBarPreference.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * build a json cursor from a string
     * @param response the string that you want to build into a json cursor
     * @return a json cursor from the string
     */
    private Cursor getJSONCursor(String response){
        try{
            Log.i("main", response);
            JSONArray array = new JSONArray(response);
            return new JSONArrayCursor(array);
        } catch(JSONException exception)
        {
            String ex = exception.getMessage();
        }
        return null;
    }

    /**
     * ask the current latitude that has been saved
     * @return the latitude that has been saved
     */
    public double getLatitude(){
        return latitude;
    }

    /**
     * ask the current longitude that has been saved
     * @return the longitude that has been saved
     */
    public double getLongitude(){
        return longitude;
    }

    /**
     * ask the current radius that has been saved
     * @return the radius that has been saved
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Destroys the application
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     *  changes and updates: the location, the radius, URL after the settings is changed
     * @param sharedPreferences the preference that was selected
     * @param s
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        String radiusString = String.valueOf(sharedPreferences.getInt(this.getString(R.string.pref_Distance_key), 2));

        radius= Integer.valueOf(radiusString)*1000;
        Log.i("radius" , String.valueOf(radius));
        if(radius <= 1000) radius =2000;
        Log.i("radius",String.valueOf(radius));
        getLocation();
        updateURL();

    }


    public class GoogleQueryTask extends AsyncTask<URL, Void, String> {
        /**
         * call to the api
         * @param params the URL that you want the results from
         * @return a string with the result from the response
         */
        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];

            try {
                HttpURLConnection conn = (HttpURLConnection) params[0].openConnection();
                InputStream in = conn.getInputStream();
                String line, text;
                text = "";
                byte[] byteArray = new byte[1024];
                BufferedReader reader = new BufferedReader(new InputStreamReader(in), byteArray.length);
                while((line = reader.readLine()) != null){
                    text = text + line;
                }
                googleSearchResults = text;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Log.i("main", googleSearchResults);
            int start, end;
            start = googleSearchResults.indexOf("[      {");
            end = googleSearchResults.indexOf("}   ]");
            Log.i("main", start +" "+ end);
            if(start == -1 && end == -1) {
                start = googleSearchResults.indexOf("[{");
                end = googleSearchResults.indexOf("}]");
                Log.i("main", start +" "+ end);
            }
            googleSearchResults = googleSearchResults.substring(start,end)+"}]";

            return googleSearchResults;
        }

        /**
         * sets the cursor and adapter after the result from the response
         * @param googleSearchResults the result from the api request in a string
         */
        @Override
        protected void onPostExecute(String googleSearchResults) {
            if (googleSearchResults != null && !googleSearchResults.equals("")) {
                mCursor = getJSONCursor(googleSearchResults);
                adapter =new RestaurantAdapter(MainActivity.this, mCursor);
                mNumberList.setAdapter(adapter);
            }
        }

    }

    /**
     * inflate a menu
     * @param menu menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return true;
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        if (id == R.id.activity_settings){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);

        }
        return super.onOptionsItemSelected(item);
    }
*/

    /**
     * start the settings activity
     * @param item item
     */
    public void share(MenuItem item){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(intent);
    }

}

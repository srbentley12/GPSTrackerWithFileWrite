package com.example.GpsTrackerWithFileWrite;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


// Basic Rundown: Takes coordinates, displays them in text views, and adds them to a list
// which writes to a JSON file every three seconds.

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationListener locationListener;
    private String lon;
    private String lat;
    private TextView latTv;
    private TextView lonTv;
    private TextView timeTv;
    Thread thread;

    // SubSample class models JSON object
    // with lat, lon, and time properties
    List<SubSample> subSamples;


    String file; //file name
    String toWrite; //temporary holder for JSON object to write to file
    String currentTime;
    FileOutputStream fos;

    Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lat = "Not Set"; //updates in onLocationChange
        lon = "Not Set";

        //Initializing text views
        latTv = findViewById(R.id.lat_tv);
        lonTv = findViewById(R.id.lon_tv);
        timeTv = findViewById(R.id.time_tv);

        //For file writing and handling JSON objects
        subSamples = new ArrayList<SubSample>();
        file = "coordinates.json";
        fos = null;
        //GsonBuilder is used to convert the JSON string to the correct format.
        gson = new GsonBuilder().setPrettyPrinting().create();

        //Check for permissions and request if they are not allowed already
        //TODO: Stop program from executing unless permission is granted. Currently crashes
        //      at GoogleApiClient call.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }

        //Initialize google API, this is needed for FusedLocationAPI.
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();

        //Looping thread to keep updating from location every 3 seconds and update the JSON file.
        //This thread runs as long as the program is running, mimics "Looper".
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    SystemClock.sleep(3000);
                    currentTime = String.valueOf(System.currentTimeMillis());
                    // Formatted time stamp to be more human readable.
                    //currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    try {
                        subSamples.add(new SubSample(lon, lat, currentTime));
                        fos = openFileOutput(file, MODE_PRIVATE);
                        toWrite = gson.toJson(subSamples);
                        fos.write(toWrite.getBytes());
                        // Writes to /data/data/<Java Package>/files/coordinates.json
                        // on device.
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("File Not Found");
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // Post is needed because UI cannot be updated from a thread other than the
                    // main(UI) thread.
                    timeTv.post(new Runnable() {
                        @Override
                        public void run() {
                            timeTv.setText(currentTime);
                            latTv.setText(lat);
                            lonTv.setText(lon);
                        }
                    });
                }
            }
        });
        thread.start();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (location != null) {
                    lat = String.valueOf(location.getLatitude());
                    lon = String.valueOf(location.getLongitude());
                }
            }
        };
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
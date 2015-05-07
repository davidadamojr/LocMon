package edu.unt.sell.locmon;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.unt.sell.locmon.contentprovider.LocationContentProvider;
import edu.unt.sell.locmon.db.LocationTable;

public class LocationMonitorService extends Service
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;

    private static final String TAG = LocationMonitorService.class.getSimpleName();

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 5000;

    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public LocationMonitorService() {
    }

    public void onCreate() {
        super.onCreate();

        // check that Google Play Services is available
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // verify Google play services on the device
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Toast.makeText(getApplicationContext(), "Could not connect to Google Play Services", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // save last known location in database
        // start update requests
        Log.i(TAG, "Google play services is connected...");

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            saveLocation();
        }

        startLocationUpdates();
    }

    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
        saveLocation();
    }

    public void saveLocation() {
        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();

        // add location info to the database
        ContentValues values = new ContentValues();
        values.put(LocationTable.COLUMN_LAT, Double.toString(latitude));
        values.put(LocationTable.COLUMN_LONG, Double.toString(longitude));
        values.put(LocationTable.COLUMN_TIMESTAMP, dateFormat.format(Calendar.getInstance().getTime()));
        getContentResolver().insert(LocationContentProvider.CONTENT_URI, values);

        Log.i(TAG, "Saved location data, latitude: " + Double.toString(latitude) + ", longitude: " + Double.toString(longitude));
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Location monitor service has started.");

        return Service.START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        // stop location updates
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d(TAG, "stopping location requests.");
        }

        Log.d(TAG, "Destroying service... ");
        super.onDestroy();
    }
}

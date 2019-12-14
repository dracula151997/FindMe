package com.project.semicolon.findme;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationTracker extends Service implements LocationListener {
    private static final int MIN_TIME_UPDATED = 1000 * 60;
    private static final int MIN_DISTANCE_CHANGE = 10;
    private static final String TAG = LocationTracker.class.getSimpleName();
    protected LocationManager locationManager;
    private Context context;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private boolean canGetLocation;
    private Location location;
    private double lat, lon;

    public LocationTracker(Context context) {
        this.context = context;
        getLocation();
    }

    @SuppressLint("MissingPermission")
    private Location getLocation() {
        try {
            locationManager = (LocationManager)
                    context.getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isNetworkEnabled && !isGPSEnabled) {
                    Toast.makeText(context,
                            "You must enable network or GPS to get your live location",
                            Toast.LENGTH_SHORT).show();
                } else {
                    this.canGetLocation = true;

                    if (isNetworkEnabled) {
                        Log.d(TAG, "getLocation: Network provider enabled");
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_UPDATED,
                                MIN_DISTANCE_CHANGE,
                                this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            updateLocation();


                        }
                    }

                    if (isGPSEnabled) {
                        if (location == null) {
                            if (locationManager != null) {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                        MIN_TIME_UPDATED,
                                        MIN_DISTANCE_CHANGE,
                                        this);

                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                updateLocation();
                            }

                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return location;

    }

    private void updateLocation() {
        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public boolean canGetLocation() {
        return canGetLocation;
    }

    public String getAddress(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);
            return addressLine;
        } else {
            return null;
        }
    }

    public String getLocality(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            return address.getLocality();
        }

        return null;
    }

    public String getCountry(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            return address.getCountryName();
        }

        return null;
    }

    private List<Address> getGeocoderAddress(Context context) {
        if (location != null) {
            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
            try {
                return geocoder.getFromLocation(lat, lon, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}

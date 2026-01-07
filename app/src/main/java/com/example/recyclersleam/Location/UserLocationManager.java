package com.example.recyclersleam.Location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

public class UserLocationManager {

    private final Context context;
    private final android.location.LocationManager locationManager;
    private LocationCallback callback;
    private Location currentLocation;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            if (callback != null) {
                callback.onLocationUpdate(location);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public UserLocationManager(Context context) {
        this.context = context;
        this.locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startLocationUpdates(LocationCallback callback) {
        this.callback = callback;

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Permissions refusées");
            return;
        }

        try {
            // Essayer d'utiliser le GPS en priorité
            if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        1000, // Mises à jour toutes les 1 seconde
                        5, // Mises à jour si le changement dépasse 5 mètres
                        locationListener);
            }

            // Fallback sur le réseau mobile
            if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        1000,
                        5,
                        locationListener);
            }

            // Obtenir la dernière position connue
            Location lastKnownGPS = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            Location lastKnownNetwork = locationManager
                    .getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);

            Location bestLocation = null;
            if (lastKnownGPS != null && lastKnownNetwork != null) {
                bestLocation = lastKnownGPS.getAccuracy() < lastKnownNetwork.getAccuracy() ? lastKnownGPS
                        : lastKnownNetwork;
            } else if (lastKnownGPS != null) {
                bestLocation = lastKnownGPS;
            } else if (lastKnownNetwork != null) {
                bestLocation = lastKnownNetwork;
            }

            if (bestLocation != null) {
                currentLocation = bestLocation;
                callback.onLocationUpdate(bestLocation);
            }

        } catch (SecurityException e) {
            callback.onLocationError("Erreur d'accès aux permissions: " + e.getMessage());
        }
    }

    public void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public interface LocationCallback {
        void onLocationUpdate(Location location);

        void onLocationError(String error);
    }
}

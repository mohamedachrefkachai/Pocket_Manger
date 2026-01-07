package com.example.recyclersleam.controller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.recyclersleam.Entity.Expense;
import com.example.recyclersleam.Location.HotSpot;
import com.example.recyclersleam.Location.HotSpotOverlay;
import com.example.recyclersleam.Location.LocationEntity;
import com.example.recyclersleam.Location.UserLocationManager;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class GlobalMapActivity extends AppCompatActivity {

    private MapView map;
    private MyDataBase db;
    private EditText etSearchMap;
    private FloatingActionButton fabMyLocation;
    private FloatingActionButton fabToggleHotSpots;
    private UserLocationManager userLocationManager;
    private Marker myLocationMarker;
    private HotSpotOverlay hotSpotOverlay;
    private boolean showHotSpots = true;

    private static final int REQUEST_LOCATION_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(getFilesDir());
        Configuration.getInstance().setOsmdroidTileCache(new java.io.File(getFilesDir(), "osmdroid/tiles"));

        setContentView(R.layout.activity_global_map);

        map = findViewById(R.id.globalMap);
        etSearchMap = findViewById(R.id.etSearchMap);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        fabToggleHotSpots = findViewById(R.id.fabToggleHotSpots);

        if (map == null) {
            Toast.makeText(this, "Erreur : Map non trouvée", Toast.LENGTH_SHORT).show();
            return;
        }

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        map.getController().setZoom(13.0);
        map.setExpectedCenter(new GeoPoint(36.8065, 10.1686));
        map.invalidate();

        db = MyDataBase.getAppDataBase(this);

        displayExpenses("");

        userLocationManager = new UserLocationManager(this);

        fabMyLocation.setOnClickListener(v -> {
            requestLocationPermissionsIfNeeded();
        });

        fabToggleHotSpots.setOnClickListener(v -> {
            showHotSpots = !showHotSpots;
            displayExpenses(etSearchMap.getText().toString().trim().toLowerCase());
            Toast.makeText(this,
                    showHotSpots ? "Hot Spots activés" : "Hot Spots désactivés",
                    Toast.LENGTH_SHORT).show();
        });

        etSearchMap.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                displayExpenses(s.toString().trim().toLowerCase());
            }
        });
    }

    private void displayExpenses(String query) {
        if (map == null)
            return;

        new Thread(() -> {
            List<LocationEntity> locations = db.LocationDao().getAllLocations();
            List<HotSpot> hotSpots = new ArrayList<>();
            List<Marker> markersToAdd = new ArrayList<>();
            GeoPoint firstPointFound = null;

            // 1. Calculate HotSpots
            if (showHotSpots && !locations.isEmpty()) {
                hotSpots = calculateHotSpots(locations, query);
            }

            // 2. Create Markers
            for (LocationEntity loc : locations) {
                Expense expense = db.ExpenseDao().getExpenseById(loc.expenseId);
                if (expense == null)
                    continue;

                String titre = expense.getTitle().toLowerCase();
                String prix = String.valueOf(expense.getAmount());
                String adresse = (loc.adresse != null) ? loc.adresse.toLowerCase() : "";

                if (query.isEmpty() || titre.contains(query) || prix.contains(query) || adresse.contains(query)) {
                    GeoPoint point = new GeoPoint(loc.latitude, loc.longitude);
                    if (firstPointFound == null) {
                        firstPointFound = point;
                    }

                    final double destLat = loc.latitude;
                    final double destLon = loc.longitude;
                    final String destName = expense.getTitle();
                    final String destAddress = loc.adresse != null ? loc.adresse : "Destination";

                    Marker marker = new Marker(map) {
                        @Override
                        public boolean onLongPress(android.view.MotionEvent event, MapView mapView) {
                            if (hitTest(event, mapView)) {
                                showNavigationDialog(destLat, destLon, destName, destAddress);
                                return true;
                            }
                            return super.onLongPress(event, mapView);
                        }
                    };

                    marker.setPosition(point);
                    marker.setTitle(expense.getTitle() + " (" + expense.getAmount() + " DT)");
                    marker.setSnippet(loc.adresse != null ? loc.adresse : "Pas d'adresse");

                    // Note: We cannot set Drawable from background thread easily if it relies on
                    // Resources being ready?
                    // actually ContextCompat is fine.
                    // But we will set icon on UI thread just to be safe or pre-load it?
                    // Let's keep it here, it should be fine.
                    Drawable markerDrawable = ContextCompat.getDrawable(GlobalMapActivity.this,
                            R.drawable.ic_map_marker);
                    if (markerDrawable != null) {
                        marker.setIcon(markerDrawable);
                    }

                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
                        clickedMarker.showInfoWindow();
                        return true;
                    });

                    markersToAdd.add(marker);
                }
            }

            final GeoPoint finalFirstPoint = firstPointFound;
            final List<HotSpot> finalHotSpots = hotSpots;
            final List<Marker> finalMarkers = markersToAdd;

            runOnUiThread(() -> {
                map.getOverlays().clear();

                if (myLocationMarker != null) {
                    map.getOverlays().add(myLocationMarker);
                }

                if (locations.isEmpty() && query.isEmpty()) {
                    Toast.makeText(GlobalMapActivity.this, "Aucune dépense avec position enregistrée",
                            Toast.LENGTH_SHORT).show();
                }

                if (showHotSpots && !finalHotSpots.isEmpty()) {
                    if (hotSpotOverlay == null) {
                        hotSpotOverlay = new HotSpotOverlay(map, finalHotSpots);
                    } else {
                        hotSpotOverlay.updateHotSpots(finalHotSpots);
                    }
                    map.getOverlays().add(hotSpotOverlay);
                }

                map.getOverlays().addAll(finalMarkers);

                if (finalFirstPoint != null) {
                    map.getController().setCenter(finalFirstPoint);
                }

                map.invalidate();
            });
        }).start();
    }

    private List<HotSpot> calculateHotSpots(List<LocationEntity> locations, String query) {
        List<HotSpot> hotSpots = new ArrayList<>();
        double clusterRadius = 0.003;

        for (LocationEntity loc : locations) {
            Expense expense = db.ExpenseDao().getExpenseById(loc.expenseId);
            if (expense == null)
                continue;

            String titre = expense.getTitle().toLowerCase();
            String prix = String.valueOf(expense.getAmount());
            String adresse = (loc.adresse != null) ? loc.adresse.toLowerCase() : "";

            if (!query.isEmpty() && !titre.contains(query) && !prix.contains(query) && !adresse.contains(query)) {
                continue;
            }

            boolean addedToCluster = false;

            for (HotSpot spot : hotSpots) {
                double distance = calculateDistance(spot.latitude, spot.longitude, loc.latitude, loc.longitude);
                if (distance < clusterRadius) {
                    spot.addExpense(expense.getAmount());

                    double totalCount = spot.count;
                    spot.latitude = (spot.latitude * (totalCount - 1) + loc.latitude) / totalCount;
                    spot.longitude = (spot.longitude * (totalCount - 1) + loc.longitude) / totalCount;

                    addedToCluster = true;
                    break;
                }
            }

            if (!addedToCluster) {
                HotSpot newSpot = new HotSpot(loc.latitude, loc.longitude);
                newSpot.addExpense(expense.getAmount());
                hotSpots.add(newSpot);
            }
        }

        if (!hotSpots.isEmpty()) {
            double maxAmount = 0;
            for (HotSpot spot : hotSpots) {
                if (spot.totalAmount > maxAmount) {
                    maxAmount = spot.totalAmount;
                }
            }

            for (HotSpot spot : hotSpots) {
                float intensity = (float) (spot.totalAmount / maxAmount);
                spot.color = interpolateColor(android.graphics.Color.BLUE,
                        android.graphics.Color.RED,
                        intensity);
            }
        }

        return hotSpots;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDiff = lat1 - lat2;
        double lonDiff = lon1 - lon2;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }

    private int interpolateColor(int colorStart, int colorEnd, float ratio) {
        int r = (int) (android.graphics.Color.red(colorStart) +
                ratio * (android.graphics.Color.red(colorEnd) - android.graphics.Color.red(colorStart)));
        int g = (int) (android.graphics.Color.green(colorStart) +
                ratio * (android.graphics.Color.green(colorEnd) - android.graphics.Color.green(colorStart)));
        int b = (int) (android.graphics.Color.blue(colorStart) +
                ratio * (android.graphics.Color.blue(colorEnd) - android.graphics.Color.blue(colorStart)));
        return android.graphics.Color.rgb(r, g, b);
    }

    private void requestLocationPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        REQUEST_LOCATION_PERMISSION);
            } else {
                startLocating();
            }
        } else {
            startLocating();
        }
    }

    private void startLocating() {
        userLocationManager.startLocationUpdates(new UserLocationManager.LocationCallback() {
            @Override
            public void onLocationUpdate(Location location) {
                updateMapWithUserLocation(location);
            }

            @Override
            public void onLocationError(String error) {
                Toast.makeText(GlobalMapActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapWithUserLocation(Location location) {
        GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (myLocationMarker != null) {
            map.getOverlays().remove(myLocationMarker);
        }

        myLocationMarker = new Marker(map);
        myLocationMarker.setPosition(userLocation);
        myLocationMarker.setTitle("Ma position");
        myLocationMarker.setSnippet("Précision: " + Math.round(location.getAccuracy()) + "m");

        Drawable myLocationIcon = ContextCompat.getDrawable(this, R.drawable.ic_my_location);
        if (myLocationIcon != null) {
            myLocationMarker.setIcon(myLocationIcon);
        }

        myLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(myLocationMarker);

        map.getController().setCenter(userLocation);
        map.getController().setZoom(15.0);
        map.invalidate();

        Toast.makeText(this, "Position trouvée ✓", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocating();
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showNavigationDialog(double destLat, double destLon, String destName, String destAddress) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Navigate to " + destName);
        builder.setMessage(destAddress + "\n\nChoose navigation method:");

        builder.setPositiveButton("Google Maps", (dialog, which) -> {
            openGoogleMapsNavigation(destLat, destLon, destName);
        });

        builder.setNeutralButton("Other Apps", (dialog, which) -> {
            openGenericNavigation(destLat, destLon, destName);
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void openGoogleMapsNavigation(double destLat, double destLon, String destName) {
        try {
            String uri = String.format("google.navigation:q=%f,%f", destLat, destLon);
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            String uri = String.format("https://www.google.com/maps/dir/?api=1&destination=%f,%f",
                    destLat, destLon);
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(uri));
            startActivity(intent);
        }
    }

    private void openGenericNavigation(double destLat, double destLon, String destName) {
        String uri = String.format("geo:%f,%f?q=%f,%f(%s)",
                destLat, destLon, destLat, destLon,
                android.net.Uri.encode(destName));

        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(uri));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No navigation app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null)
            map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
            userLocationManager.stopLocationUpdates();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.onDetach();
        }
        userLocationManager.stopLocationUpdates();
    }
}

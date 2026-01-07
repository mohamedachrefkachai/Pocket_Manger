package com.example.recyclersleam.controller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.BitmapFactory; // Added for loading image

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // For Delete Confirmation
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.recyclersleam.Entity.Expense;
import com.example.recyclersleam.Location.GeocodingApi;
import com.example.recyclersleam.Location.GeocodingResponse;
import com.example.recyclersleam.Location.LocationEntity;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class AddExpenseActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private GeocodingApi geocodingApi;

    private EditText etTitle;
    private EditText etAmount;
    private Button btnGetLocation;
    private Button btnSaveExpense;
    private Button btnPhoto;
    private Button btnAnalyzeReceipt;
    private Button btnDeleteExpense;
    private ImageView imgReceipt;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private TextView tvAddress;

    private MapView map;
    private Marker marker;
    private LocationManager locationManager;
    private MyDataBase db;
    private int userId;
    private int expenseId = -1; // -1 means new expense

    private Bitmap receiptBitmap;
    private String imagePath = "";

    private boolean isGpsCaptured = false;

    private ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        receiptBitmap = (Bitmap) extras.get("data");
                        imgReceipt.setImageBitmap(receiptBitmap);
                        imagePath = saveImage(receiptBitmap);
                        Toast.makeText(this, "Photo capturée !", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(getFilesDir());
        Configuration.getInstance().setOsmdroidTileCache(new java.io.File(getFilesDir(), "osmdroid/tiles"));

        setContentView(R.layout.activity_add_expense);

        userId = getIntent().getIntExtra("userId", -1);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle); // Need this for Edit Mode

        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnAnalyzeReceipt = findViewById(R.id.btnAnalyzeReceipt);
        btnDeleteExpense = findViewById(R.id.btnDeleteExpense);
        imgReceipt = findViewById(R.id.imgReceipt);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAddress = findViewById(R.id.tvAddress);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.invalidate();

        marker = new Marker(map);
        marker.setTitle("Lieu de la dépense");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                if (isGpsCaptured) {
                    updateLocationAndAddress(p);
                } else {
                    Toast.makeText(AddExpenseActivity.this, "Position GPS d'abord !", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        map.getOverlays().add(mapEventsOverlay);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geocodingApi = retrofit.create(GeocodingApi.class);

        db = MyDataBase.getAppDataBase(this);

        btnGetLocation.setOnClickListener(view -> checkPermissionAndRequestLocation());
        btnSaveExpense.setOnClickListener(view -> saveExpenseWithLocation());
        btnPhoto.setOnClickListener(view -> checkCameraPermissionAndOpenCamera());

        btnAnalyzeReceipt.setOnClickListener(v -> {
            if (receiptBitmap == null) {
                Toast.makeText(this, "Take a photo first", Toast.LENGTH_SHORT).show();
                return;
            }
            analyzeReceiptWithOCR(receiptBitmap);
        });

        // Edit Mode Logic
        expenseId = getIntent().getIntExtra("expenseId", -1);
        if (expenseId != -1) {
            tvHeaderTitle.setText("Modifier Dépense");
            btnSaveExpense.setText("Mettre à jour");
            btnDeleteExpense.setVisibility(android.view.View.VISIBLE);
            loadExpenseData(expenseId);
        }

        btnDeleteExpense.setOnClickListener(v -> deleteExpense());

        // Handle Cancel button
        findViewById(R.id.tvCancel).setOnClickListener(v -> finish());
    }

    private void checkPermissionAndRequestLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_REQUEST_CODE);
        } else {
            requestGpsLocation();
        }
    }

    private void requestGpsLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Activez le GPS", Toast.LENGTH_SHORT).show();
            return;
        }
        tvAddress.setText("Recherche GPS...");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                isGpsCaptured = true;
                GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                map.getController().animateTo(point);
                map.getController().setZoom(18.0);
                updateLocationAndAddress(point);
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        });
    }

    private void updateLocationAndAddress(GeoPoint point) {
        tvLatitude.setText("Latitude : " + point.getLatitude());
        tvLongitude.setText("Longitude : " + point.getLongitude());
        marker.setPosition(point);
        map.invalidate();

        tvAddress.setText("Recherche de l'adresse...");

        geocodingApi.reverseGeocode(point.getLatitude(), point.getLongitude(), "json", "PocketManager/1.0", "fr,ar")
                .enqueue(new Callback<GeocodingResponse>() {
                    @Override
                    public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String finalAddr = response.body().displayName;
                            tvAddress.setText("Adresse : " + finalAddr);
                        } else {
                            tvAddress.setText("Adresse : Introuvable");
                        }
                    }

                    @Override
                    public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                        tvAddress.setText("Erreur réseau : " + t.getMessage());
                    }
                });
    }

    private void loadExpenseData(int id) {
        Expense expense = db.ExpenseDao().getExpenseById(id);
        if (expense != null) {
            etTitle.setText(expense.getTitle());
            etAmount.setText(String.valueOf(expense.getAmount()));
            imagePath = expense.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                receiptBitmap = BitmapFactory.decodeFile(imagePath);
                if (receiptBitmap != null) {
                    imgReceipt.setImageBitmap(receiptBitmap);
                }
            }
            // Note: Location loading is omitted for brevity/complexity,
            // but you could load it from LocationDao using expense.id
        }
    }

    private void deleteExpense() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Voulez-vous vraiment supprimer cette dépense ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    new Thread(() -> {
                        Expense expense = db.ExpenseDao().getExpenseById(expenseId);
                        if (expense != null) {
                            db.ExpenseDao().deleteExpense(expense);
                            // Also delete location stats? cascade? existing DAO doesn't seem to enforce it
                        }
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Dépense supprimée", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }).start();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void saveExpenseWithLocation() {
        String inputTitle = etTitle.getText().toString().trim();
        String inputAmount = etAmount.getText().toString().trim();

        if (inputTitle.isEmpty() || inputAmount.isEmpty()) {
            Toast.makeText(this, "Saisir titre et montant", Toast.LENGTH_SHORT).show();
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(inputAmount);
        } catch (Exception e) {
            return;
        }

        if (userId == -1) {
            // Fallback or error
            // Toast.makeText(this, "Erreur utilisateur", Toast.LENGTH_SHORT).show();
            userId = 1; // Default for safety
        }

        new Thread(() -> {
            Expense expense = new Expense();
            expense.userId = userId;
            expense.setTitle(inputTitle);
            expense.setAmount(montant);
            expense.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (!imagePath.isEmpty()) {
                expense.setImagePath(imagePath);
            }

            long idToUse = expenseId;
            if (expenseId == -1) {
                idToUse = db.ExpenseDao().insertExpense(expense);
            } else {
                expense.setId(expenseId);
                db.ExpenseDao().updateExpense(expense);
            }

            if (marker.getPosition() != null && isGpsCaptured) {
                // Simplified: Delete old location and insert new one for simplicity
                // In a real app, you might want to update or check if it changed
                // Here we just insert new location data linked to this expense
                // Note: You should handle cleaning up old location data if necessary
                LocationEntity locEntity = new LocationEntity();
                locEntity.expenseId = (int) idToUse;
                locEntity.latitude = marker.getPosition().getLatitude();
                locEntity.longitude = marker.getPosition().getLongitude();
                locEntity.adresse = tvAddress.getText().toString().replace("Adresse :", "").trim();
                db.LocationDao().insertLocation(locEntity);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, expenseId == -1 ? "Dépense enregistrée !" : "Dépense mise à jour !",
                        Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private String saveImage(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "receipt_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur sauvegarde image", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestGpsLocation();
            } else {
                Toast.makeText(this, "Permission localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ================= OCR =================

    private void analyzeReceiptWithOCR(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(result -> {
                    String receiptText = result.getText();

                    if (receiptText.isEmpty()) {
                        Toast.makeText(this,
                                "No text detected",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    extractExpenseDetails(receiptText);
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "OCR Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void extractExpenseDetails(String receiptText) {
        String[] lines = receiptText.split("\n");

        if (lines.length > 0) {
            // First line as title
            etTitle.setText(lines[0]);

            // Try to find a valid amount in subsequent lines
            // This is a naive heuristic; for real apps, regex matching specific currency
            // formats is better
            // Here we just look for the first valid double found
            boolean amountFound = false;
            for (String line : lines) {
                // Try to find numbers in the line
                String potentialAmount = line.replaceAll("[^\\d.]", "");
                if (!potentialAmount.isEmpty()) {
                    try {
                        // Check if it has a decimal point to be more likely a price
                        if (potentialAmount.contains(".")) {
                            double amount = Double.parseDouble(potentialAmount);
                            etAmount.setText(String.valueOf(amount));
                            amountFound = true;
                            break; // Stop after first potential price found
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }

            if (!amountFound && lines.length > 1) {
                // Fallback: Try second line if no decimal number found anywhere
                try {
                    double amount = Double.parseDouble(lines[1].replaceAll("[^\\d.]", ""));
                    etAmount.setText(String.valueOf(amount));
                } catch (Exception e) {
                }
            }
        }

        Toast.makeText(this, "Analysis Complete", Toast.LENGTH_SHORT).show();
    }
}

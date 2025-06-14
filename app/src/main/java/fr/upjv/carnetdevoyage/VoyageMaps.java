package fr.upjv.carnetdevoyage;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import fr.upjv.carnetdevoyage.databinding.ActivityVoyageMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.app.AlertDialog;
import android.widget.Toast;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class VoyageMaps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityVoyageMapsBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable locationRunnable;
    private FusedLocationProviderClient fusedLocationClient;
    private String nom_voyage;
    private long intervalMillis = 2 * 60 * 1000;

    private boolean isTracking = true;

    private List<LatLng> pointList = new ArrayList<>();
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVoyageMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nom_voyage = getIntent().getStringExtra("nom_voyage");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        FirebaseFirestore.getInstance()
                .collection("voyages")
                .document(nom_voyage)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long frequenceMinutes = documentSnapshot.getLong("periodicite");
                        if (frequenceMinutes != null && frequenceMinutes > 0) {
                            intervalMillis = frequenceMinutes * 60 * 1000;
                        }
                    }
                    startLocationTracking();
                })
                .addOnFailureListener(e -> {
                    startLocationTracking();
                });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.8566, 2.3522), 12f));
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 12f));
                    }
                });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    private void enregistrerPosition() {
        if (!isTracking) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        LatLng latLng = new LatLng(latitude, longitude);
                        pointList.add(latLng);

                        runOnUiThread(() -> {
                            mMap.addMarker(new MarkerOptions().position(latLng).title("📍"));
                            if (polyline == null) {
                                polyline = mMap.addPolyline(new PolylineOptions().addAll(pointList));
                            } else {
                                polyline.setPoints(pointList);
                            }
                        });

                        Map<String, Object> point = new HashMap<>();
                        point.put("latitude", latitude);
                        point.put("longitude", longitude);
                        point.put("timestamp", System.currentTimeMillis());

                        FirebaseFirestore.getInstance()
                                .collection("voyages")
                                .document(nom_voyage)
                                .collection("points")
                                .add(point)
                                .addOnSuccessListener(d -> Log.d("TRACK", "📍 Point enregistré"))
                                .addOnFailureListener(e -> Log.e("TRACK", "❌ Échec d'enregistrement", e));
                    }
                });
    }


    private void startLocationTracking() {
        enregistrerPosition(); // première position
        locationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    enregistrerPosition();
                    handler.postDelayed(this, intervalMillis);
                }
            }
        };
        handler.postDelayed(locationRunnable, intervalMillis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationTracking();
    }

    private void stopLocationTracking() {
        isTracking = false;
        handler.removeCallbacks(locationRunnable);
    }

    public void onClickEndVoyage(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Terminer le voyage")
                .setMessage("Voulez-vous vraiment arrêter l’enregistrement du voyage ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    stopLocationTracking();
                    Toast.makeText(this, "Voyage terminé.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    public void onClickExportGPX(View view) {
        FirebaseFirestore.getInstance()
                .collection("voyages")
                .document(nom_voyage)
                .collection("points")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder gpx = new StringBuilder();
                    gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    gpx.append("<gpx version=\"1.1\" creator=\"CarnetDeVoyage\">\n");

                    for (var doc : queryDocumentSnapshots) {
                        double lat = doc.getDouble("latitude");
                        double lon = doc.getDouble("longitude");
                        gpx.append(String.format("  <wpt lat=\"%f\" lon=\"%f\"></wpt>\n", lat, lon));
                    }

                    gpx.append("</gpx>");

                    try {
                        File file = new File(getExternalFilesDir(null), nom_voyage + ".gpx");
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(gpx.toString().getBytes());
                        }

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/xml");
                        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                this,
                                getPackageName() + ".provider",
                                file));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Fichier GPX");
                        intent.putExtra(Intent.EXTRA_TEXT, "Voici le fichier GPX du voyage " + nom_voyage);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(intent, "Envoyer le fichier par email"));

                    } catch (IOException e) {
                        Log.e("ExportGPX", "Erreur écriture GPX", e);
                    }

                })
                .addOnFailureListener(e -> Log.e("ExportGPX", "Erreur Firestore", e));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }
}

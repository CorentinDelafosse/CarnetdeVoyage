package fr.upjv.carnetdevoyage;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ObserverVoyagesActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String emailCible;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observer_voyages);

        emailCible = getIntent().getStringExtra("email_cible");
        db = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        chargerVoyagesDe(emailCible);
    }

    private void chargerVoyagesDe(String email) {
        db.collection("users")
                .document(email)
                .collection("voyages")
                .get()
                .addOnSuccessListener(voyages -> {
                    for (QueryDocumentSnapshot voyageDoc : voyages) {
                        String nomVoyage = voyageDoc.getId();

                        db.collection("users")
                                .document(email)
                                .collection("voyages")
                                .document(nomVoyage)
                                .collection("points")
                                .get()
                                .addOnSuccessListener(points -> {
                                    for (QueryDocumentSnapshot point : points) {
                                        Double lat = point.getDouble("latitude");
                                        Double lon = point.getDouble("longitude");
                                        if (lat != null && lon != null) {
                                            LatLng pos = new LatLng(lat, lon);
                                            mMap.addMarker(new MarkerOptions().position(pos).title(nomVoyage));
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10f));
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
                    Log.e("Observer", "Erreur Firestore", e);
                });
    }
}

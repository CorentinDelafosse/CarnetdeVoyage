package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class VoyagesUtilisateurActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VoyageAdapter adapter;
    private ArrayList<Voyage> voyageList = new ArrayList<>();
    private FirebaseFirestore db;
    private String emailUtilisateur;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voyages_utilisateur);

        emailUtilisateur = getIntent().getStringExtra("email_utilisateur");
        if (emailUtilisateur == null || emailUtilisateur.isEmpty()) {
            Toast.makeText(this, "Email utilisateur manquant", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView_voyages_utilisateur);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VoyageAdapter(voyageList, voyage -> {
            Intent intent = new Intent(this, VoyageMaps.class);
            intent.putExtra("nom_voyage", voyage.getNom_voyage()); // <== À ajouter
            intent.putExtra("email_utilisateur", emailUtilisateur); // utilisateur à observer
            intent.putExtra("mode_observation", true); // pour cacher les boutons
            startActivity(intent);
        }, this);

        recyclerView.setAdapter(adapter);

        chargerVoyages();
    }

    private void chargerVoyages() {
        db.collection("users")
                .document(emailUtilisateur)
                .collection("voyages")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    voyageList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Voyage v = doc.toObject(Voyage.class);
                        voyageList.add(v);
                    }
                    adapter.setVoyages(voyageList);
                })
                .addOnFailureListener(e -> {
                    Log.e("VOYAGES_UTILISATEUR", "Erreur récupération des voyages", e);
                    Toast.makeText(this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
                });
    }
}


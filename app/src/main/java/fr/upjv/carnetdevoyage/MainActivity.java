package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private VoyageAdapter adapter;
    private ArrayList<Voyage> voyageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // connexion à la base de données Firestore
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerVoyages_list_voyages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VoyageAdapter(voyageList, voyage -> {
            Intent intent = new Intent(MainActivity.this, VoyageMaps.class);
            intent.putExtra("nom_voyage", voyage.getNom_voyage());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // lance l'écoute en temps réel pour rafraîchir la liste automatiquement
        updateVoyages();
    }

    public void on_click_create_voyage(View view) {
        Log.i("MainActivity", "Bouton créer voyage cliqué");
        Intent intent = new Intent(this, EditVoyage.class);
        startActivity(intent);
    }

    public void updateVoyages() {
        db.collection("voyages").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w("MainActivity", "Erreur écoute Firestore : ", error);
                return;
            }
            if (value != null) {
                voyageList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Voyage v = doc.toObject(Voyage.class);
                    voyageList.add(v);
                }
                adapter.setVoyages(voyageList);
            }
        });
    }
}

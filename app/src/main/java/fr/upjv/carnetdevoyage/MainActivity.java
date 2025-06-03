package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button button_creer_voyage;
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
            Intent intent = new Intent(MainActivity.this, EditVoyage.class);
            intent.putExtra("nom_voyage", voyage.getNom_voyage());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        updateVoyages();
    }


    public void on_click_create_voyage(View view) {
        // ouvrir l'activité EditVoyage sans données
        Intent intent = new Intent(this, EditVoyage.class);
        startActivity(intent);
    }

    public void updateVoyages() {
        db.collection("voyages").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                voyageList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Voyage v = doc.toObject(Voyage.class);
                    voyageList.add(v);
                }
                adapter.setVoyages(voyageList);
            } else {
                Log.w("MainActivity", "Error getting documents.", task.getException());
            }
        });
    }
}
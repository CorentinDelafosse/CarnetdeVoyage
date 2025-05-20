package fr.upjv.carnetdevoyage;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

private EditText etNomVoyage;
private Button button_Creer_Voyage;
private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNomVoyage = findViewById(R.id.etNomVoyage);
        button_Creer_Voyage = findViewById(R.id.id_button_create_voyage);
        db = FirebaseFirestore.getInstance();

        button_Creer_Voyage.setOnClickListener(v -> {;
            String nom = etNomVoyage.getText().toString().trim();

            if (!nom.isEmpty()) {
                Map<String, Object> voyage = new HashMap<>();
                voyage.put("nom", nom);
                voyage.put("dateDebut", String.valueOf(System.currentTimeMillis()));
                voyage.put("dateFin", null);

                db.collection("voyages")
                        .add(voyage)
                        .addOnSuccessListener(documentReference ->
                                Log.d("Firestore", "Voyage créé avec ID: " + documentReference.getId()))
                        .addOnFailureListener(e ->
                                Log.e("Firestore", "Erreur Firestore", e));
        };
        });
    }


}
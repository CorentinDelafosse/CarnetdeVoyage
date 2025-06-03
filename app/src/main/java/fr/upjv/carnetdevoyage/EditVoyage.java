package fr.upjv.carnetdevoyage;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditVoyage extends AppCompatActivity {
    private String nom_voyage;
    private int periodicite;
    private FirebaseFirestore db;

    private EditText editTextNomVoyage;
    private SeekBar seekBarPeriodicite;
    private TextView textViewPeriodicite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_voyage);
        db = FirebaseFirestore.getInstance();
        // Récupérer les données passées par l'activité précédente
        nom_voyage = getIntent().getStringExtra("nom_voyage");
        periodicite = getIntent().getIntExtra("periodicite", 10);
        // Afficher les données dans les champs de texte
        editTextNomVoyage = findViewById(R.id.text_nom_voyage);
        editTextNomVoyage.setText(nom_voyage);
        textViewPeriodicite = findViewById(R.id.textView_seekbar);
        // Configurer le SeekBar pour afficher la valeur actuelle
        seekBarPeriodicite = findViewById(R.id.seekBar_periodicite);
        seekBarPeriodicite.setProgress(periodicite);
        seekBarPeriodicite.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                periodicite = progress;
                // Mettre à jour le TextView avec la valeur actuelle du SeekBar
                textViewPeriodicite.setText(periodicite + " minutes");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //
            }
        });
    }


    public void on_click_save_voyage(View view) {
        // Récupérer les valeurs des champs de texte
        String nom_voyage = editTextNomVoyage.getText().toString();
        int periodicite = seekBarPeriodicite.getProgress();

        // Créer un document dans Firestore avec collection "voyages" et document $nom_voyage
        // Si le document existe déjà, il sera mis à jour
        if( nom_voyage.isEmpty()) {
            // Afficher un message d'erreur si le nom du voyage est vide
            editTextNomVoyage.setError("Le nom du voyage ne peut pas être vide");
            return;
        }
        Map<String, Object> voyage = new HashMap<>();
        voyage.put("nom_voyage", nom_voyage);
        voyage.put("periodicite", periodicite);
        db.collection("voyages").document(nom_voyage)
                .set(voyage)
                .addOnSuccessListener(aVoid -> {
                    // Document successfully written
                    Log.d("EditVoyage", "Voyage correctement inscrit!");
                    // close the activity and return to the previous one
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Error writing document
                    Log.w("EditVoyage", "Erreur lors de l'enregistrement", e);
                });
    }
}
package fr.upjv.carnetdevoyage;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
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

        // Récupération de l'intent
        nom_voyage = getIntent().getStringExtra("nom_voyage");
        periodicite = getIntent().getIntExtra("periodicite", 10);

        editTextNomVoyage = findViewById(R.id.text_nom_voyage);
        textViewPeriodicite = findViewById(R.id.textView_seekbar);
        seekBarPeriodicite = findViewById(R.id.seekBar_periodicite);

        editTextNomVoyage.setText(nom_voyage);
        seekBarPeriodicite.setProgress(periodicite);
        textViewPeriodicite.setText(periodicite + " minutes");

        seekBarPeriodicite.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                periodicite = progress;
                textViewPeriodicite.setText(progress + " minutes");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void on_click_save_voyage(View view) {
        String nom_voyage = editTextNomVoyage.getText().toString().trim();
        int periodicite = seekBarPeriodicite.getProgress();

        if (nom_voyage.isEmpty()) {
            editTextNomVoyage.setError("Le nom du voyage ne peut pas être vide");
            return;
        }

        // Récupérer l'utilisateur connecté
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Créer la structure du document
        Map<String, Object> voyage = new HashMap<>();
        voyage.put("nom_voyage", nom_voyage);
        voyage.put("periodicite", periodicite);

        db.collection("users")
                .document(userEmail)
                .collection("voyages")
                .document(nom_voyage)
                .set(voyage)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EditVoyage", "Voyage bien enregistré dans Firestore");

                    // Optionnel : notify MainActivity
                    finish();
                })
                .addOnFailureListener(e -> Log.w("EditVoyage", "Erreur d'enregistrement", e));
    }
}

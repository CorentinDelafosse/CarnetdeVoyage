package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SearchUserActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button btnObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        emailInput = findViewById(R.id.emailInput);
        btnObserver = findViewById(R.id.btnObserver);

        btnObserver.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer une adresse e-mail", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, VoyagesUtilisateurActivity.class);
            intent.putExtra("email_utilisateur", email);
            startActivity(intent);
        });
    }
}

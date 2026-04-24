package com.med.locationvoiture.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.med.locationvoiture.R;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Utilisateur;
import com.med.locationvoiture.utils.SessionManager;

public class PortalActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            redirectToDashboard();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            Utilisateur utilisateur = dbHelper.getUtilisateur(username, password);
            if (utilisateur != null) {
                sessionManager.createSession(utilisateur.getId(), utilisateur.getUsername(), utilisateur.getRole());
                redirectToDashboard();
            } else {
                Toast.makeText(this, "Identifiants invalides", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(PortalActivity.this, RegisterActivity.class));
        });
    }

    private void redirectToDashboard() {
        Intent intent;
        if ("admin".equals(sessionManager.getRole())) {
            intent = new Intent(PortalActivity.this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(PortalActivity.this, DashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
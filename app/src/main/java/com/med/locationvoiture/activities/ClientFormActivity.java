package com.med.locationvoiture.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.med.locationvoiture.R;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Client;
import java.util.List;

public class ClientFormActivity extends AppCompatActivity {
    private EditText etNom, etPrenom, etEmail, etTelephone, etCin, etAdresse;
    private Button btnSave, btnDelete;
    private DatabaseHelper dbHelper;
    private int clientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_form);

        dbHelper = new DatabaseHelper(this);
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etEmail = findViewById(R.id.etEmail);
        etTelephone = findViewById(R.id.etTelephone);
        etCin = findViewById(R.id.etCin);
        etAdresse = findViewById(R.id.etAdresse);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("client_id")) {
            clientId = extras.getInt("client_id");
            loadClient(clientId);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(v -> saveClient());
        btnDelete.setOnClickListener(v -> deleteClient());
    }

    private void loadClient(int id) {
        List<Client> clients = dbHelper.getAllClients();
        for (Client c : clients) {
            if (c.getId() == id) {
                etNom.setText(c.getNom());
                etPrenom.setText(c.getPrenom());
                etEmail.setText(c.getEmail());
                etTelephone.setText(c.getTelephone());
                etCin.setText(c.getCin());
                etAdresse.setText(c.getAdresse());
                break;
            }
        }
    }

    private void saveClient() {
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telephone = etTelephone.getText().toString().trim();
        String cin = etCin.getText().toString().trim();
        String adresse = etAdresse.getText().toString().trim();

        if (nom.isEmpty() || email.isEmpty() || cin.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email invalide", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!cin.matches("[A-Za-z0-9]+")) {
            Toast.makeText(this, "CIN invalide (lettres et chiffres uniquement)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!telephone.isEmpty() && !telephone.matches("\\d+")) {
            Toast.makeText(this, "Téléphone invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Client c = new Client();
        if (clientId > 0) c.setId(clientId);
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setEmail(email);
        c.setTelephone(telephone);
        c.setCin(cin);
        c.setAdresse(adresse);

        long id;
        if (clientId > 0) {
            id = dbHelper.updateClient(c);
        } else {
            id = dbHelper.insertClient(c);
        }

        if (id > 0) {
            Toast.makeText(this, "Client enregistré", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteClient() {
        if (dbHelper.deleteClient(clientId) > 0) {
            Toast.makeText(this, "Client supprimé", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
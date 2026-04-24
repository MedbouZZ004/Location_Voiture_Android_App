package com.med.locationvoiture.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.med.locationvoiture.R;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Voiture;
import com.med.locationvoiture.models.Reservation;
import com.med.locationvoiture.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReservationFormClientActivity extends AppCompatActivity {
    private TextView tvVoiture, tvPrixJour, tvPrixTotal;
    private EditText etDateDebut, etDateFin;
    private Button btnCalculer, btnReserver;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private Voiture voiture;
    private int voitureId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_form_client);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        voitureId = getIntent().getIntExtra("voiture_id", -1);

        if (voitureId == -1 || !sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadVoiture();

        etDateDebut.setOnClickListener(v -> showDatePicker(etDateDebut));
        etDateFin.setOnClickListener(v -> showDatePicker(etDateFin));
        btnCalculer.setOnClickListener(v -> calculerPrix());
        btnReserver.setOnClickListener(v -> saveReservation());
    }

    private void initViews() {
        tvVoiture = findViewById(R.id.tvVoiture);
        tvPrixJour = findViewById(R.id.tvPrixJour);
        tvPrixTotal = findViewById(R.id.tvPrixTotal);
        etDateDebut = findViewById(R.id.etDateDebut);
        etDateFin = findViewById(R.id.etDateFin);
        btnCalculer = findViewById(R.id.btnCalculer);
        btnReserver = findViewById(R.id.btnReserver);
    }

    private void loadVoiture() {
        voiture = dbHelper.getVoitureById(voitureId);
        if (voiture == null) {
            Toast.makeText(this, "Voiture non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvVoiture.setText(voiture.getMarque() + " " + voiture.getModele());
        tvPrixJour.setText(voiture.getPrix_jour() + " €/jour");
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.FRANCE, "%02d/%02d/%d", day, month + 1, year);
            editText.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void calculerPrix() {
        try {
            if (etDateDebut.getText().toString().isEmpty() || etDateFin.getText().toString().isEmpty()) {
                Toast.makeText(this, "Sélectionnez les dates", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            java.util.Date debut = sdf.parse(etDateDebut.getText().toString());
            java.util.Date fin = sdf.parse(etDateFin.getText().toString());

            if (debut != null && fin != null) {
                long diffMillis = fin.getTime() - debut.getTime();
                int jours = (int) (diffMillis / (1000 * 60 * 60 * 24));
                if (jours < 1) {
                    Toast.makeText(this, "La durée minimum est 1 jour", Toast.LENGTH_SHORT).show();
                    return;
                }
                double prixParJour = voiture.getPrix_jour();
                double total = jours * prixParJour;
                tvPrixTotal.setText(String.format(Locale.FRANCE, "%.2f €", total));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur dans le calcul", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveReservation() {
        try {
            if (etDateDebut.getText().toString().isEmpty() || etDateFin.getText().toString().isEmpty()) {
                Toast.makeText(this, "Sélectionnez les dates", Toast.LENGTH_SHORT).show();
                return;
            }

            String prixText = tvPrixTotal.getText().toString();
            if (prixText.isEmpty()) {
                Toast.makeText(this, "Calculer le prix d'abord", Toast.LENGTH_SHORT).show();
                return;
            }

            double prixTotal;
            try {
                String prixStr = prixText.replace(" €", "").replace(",", ".").trim();
                prixTotal = Double.parseDouble(prixStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Prix invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            if (prixTotal <= 0) {
                Toast.makeText(this, "Prix invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            int clientId = dbHelper.getOrCreateClientId(sessionManager.getUserId(), sessionManager.getUsername());

            Reservation r = new Reservation();
            r.setClient_id(clientId);
            r.setVoiture_id(voitureId);
            r.setDate_debut(etDateDebut.getText().toString());
            r.setDate_fin(etDateFin.getText().toString());
            r.setPrix_total(prixTotal);
            r.setStatut("en_attente");

            long id = dbHelper.insertReservation(r);

            if (id > 0) {
                Toast.makeText(this, "Réservation créée! En attente de validation.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(ReservationFormClientActivity.this, ReservationListActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Erreur lors de la réservation", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
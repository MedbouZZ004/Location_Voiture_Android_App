package com.med.locationvoiture.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.med.locationvoiture.R;
import com.med.locationvoiture.adapters.ReservationAdapter;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Paiement;
import com.med.locationvoiture.models.Reservation;
import com.med.locationvoiture.utils.NotificationHelper;
import com.med.locationvoiture.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class ValidationListActivity extends AppCompatActivity {
    private ListView lvReservations, lvPaiements;
    private Spinner spType;
    private DatabaseHelper dbHelper;
    private List<Reservation> reservations;
    private List<Paiement> paiements;
    private TextView tvTitre, tvPaiementsCount;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        
        tvTitre = findViewById(R.id.tvTitre);
        tvPaiementsCount = findViewById(R.id.tvPaiementsCount);
        lvReservations = findViewById(R.id.lvReservations);
        lvPaiements = findViewById(R.id.lvPaiements);
        spType = findViewById(R.id.spType);

        String[] types = {"Réservations en attente", "Paiements en attente"};
        spType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    lvReservations.setVisibility(View.VISIBLE);
                    lvPaiements.setVisibility(View.GONE);
                    loadReservationsEnAttente();
                } else {
                    lvReservations.setVisibility(View.GONE);
                    lvPaiements.setVisibility(View.VISIBLE);
                    loadPaiementsEnAttente();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadReservationsEnAttente();
    }

    private void loadReservationsEnAttente() {
        reservations = dbHelper.getAllReservations();
        List<Reservation> enAttente = new ArrayList<>();
        for (Reservation r : reservations) {
            if ("en_attente".equals(r.getStatut())) {
                enAttente.add(r);
            }
        }
        lvReservations.setAdapter(new ReservationAdapter(this, enAttente));
        
        lvReservations.setOnItemClickListener((parent, view, position, id) -> {
            Reservation r = enAttente.get(position);
            showReservationDialog(r);
        });
    }

    private void loadPaiementsEnAttente() {
        List<Paiement> allPaiements = dbHelper.getAllPaiements();
        List<Paiement> paiementsEnAttente = new ArrayList<>();
        
        for (Paiement p : allPaiements) {
            Reservation r = dbHelper.getReservationById(p.getReservation_id());
            if (r != null && "payee".equals(r.getStatut())) {
                p.setClient_nom(r.getClient_nom());
                paiementsEnAttente.add(p);
            }
        }
        
        if (paiementsEnAttente.isEmpty()) {
            tvPaiementsCount.setText("Aucun paiement en attente");
            tvPaiementsCount.setTextColor(getResources().getColor(R.color.text_secondary));
            tvPaiementsCount.setVisibility(View.VISIBLE);
        } else {
            tvPaiementsCount.setText(paiementsEnAttente.size() + " paiement(s) en attente de validation");
            tvPaiementsCount.setTextColor(getResources().getColor(R.color.warning));
            tvPaiementsCount.setVisibility(View.VISIBLE);
        }
        
        lvPaiements.setAdapter(new com.med.locationvoiture.adapters.PaiementAdapter(this, paiementsEnAttente));
        
        lvPaiements.setOnItemClickListener((parent, view, position, id) -> {
            Paiement p = paiementsEnAttente.get(position);
            showPaiementDialog(p);
        });
    }

    private void showReservationDialog(Reservation r) {
        String voitureNom = r.getVoiture_nom() != null ? r.getVoiture_nom() : "Voiture #" + r.getVoiture_id();
        new AlertDialog.Builder(this)
            .setTitle("Valider Réservation #" + r.getId())
            .setMessage("Voiture: " + voitureNom + "\nDates: " + r.getDate_debut() + " -> " + r.getDate_fin() +
                "\nPrix: " + String.format("%.2f €", r.getPrix_total()) + "\n\nVoulez-vous valider cette réservation?")
            .setPositiveButton("Valider", (dialog, which) -> {
                r.setStatut("en_cours");
                dbHelper.updateReservation(r);
                NotificationHelper.notifyReservationValidated(this);
                Toast.makeText(this, "Réservation validée - Le client peut maintenant payer", Toast.LENGTH_LONG).show();
                loadReservationsEnAttente();
            })
            .setNegativeButton("Refuser", (dialog, which) -> {
                r.setStatut("annulee");
                dbHelper.updateReservation(r);
                ContentValues cv = new ContentValues();
                cv.put("disponible", 1);
                dbHelper.getWritableDatabase().update("voitures", cv, "id = ?", new String[]{String.valueOf(r.getVoiture_id())});
                Toast.makeText(this, "Réservation refusée - Voiture maintenant disponible", Toast.LENGTH_SHORT).show();
                loadReservationsEnAttente();
            })
            .setNeutralButton("Annuler", null)
            .show();
    }

    private void showPaiementDialog(Paiement p) {
        Reservation r = dbHelper.getReservationById(p.getReservation_id());
        String clientName = r != null && r.getClient_nom() != null ? r.getClient_nom() : "Client #" + p.getReservation_id();
        
        new AlertDialog.Builder(this)
            .setTitle("Valider Paiement #" + p.getId())
            .setMessage("Client: " + clientName + "\nMontant: " + String.format("%.2f €", p.getMontant()) + 
                "\nDate: " + p.getDate_paiement() + "\n\nVoulez-vous valider ce paiement?")
            .setPositiveButton("Valider", (dialog, which) -> {
                if (r != null) {
                    r.setStatut("confirmee");
                    dbHelper.updateReservation(r);
                    
                    ContentValues cv = new ContentValues();
                    cv.put("disponible", 0);
                    dbHelper.getWritableDatabase().update("voitures", cv, "id = ?", new String[]{String.valueOf(r.getVoiture_id())});
                }
                NotificationHelper.notifyPaymentValidated(this);
                Toast.makeText(this, "Paiement validé - Location confirmée!", Toast.LENGTH_LONG).show();
                loadPaiementsEnAttente();
            })
            .setNegativeButton("Annuler", null)
            .show();
    }
}
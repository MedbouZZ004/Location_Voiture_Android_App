package com.med.locationvoiture.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.med.locationvoiture.R;
import com.med.locationvoiture.adapters.PaiementAdapter;
import com.med.locationvoiture.adapters.PaiementAdapter.OnPaiementClickListener;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Paiement;
import com.med.locationvoiture.models.Reservation;
import com.med.locationvoiture.utils.NotificationHelper;
import com.med.locationvoiture.utils.SessionManager;
import java.util.List;

public class PaiementActivity extends AppCompatActivity {
    private ListView lvPaiements;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int reservationId = -1;
    private TextView tvTitre;
    private Button btnNouveauPaiement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paiement_list);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        reservationId = getIntent().getIntExtra("reservation_id", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        lvPaiements = findViewById(R.id.lvPaiements);
        tvTitre = findViewById(R.id.tvTitre);
        btnNouveauPaiement = findViewById(R.id.btnNouveauPaiement);

        boolean isAdmin = "admin".equals(sessionManager.getRole());

        if (isAdmin) {
            tvTitre.setText("Gestion Paiements");
        } else {
            if (reservationId > 0) {
                showPaiementForm(reservationId);
            } else {
                tvTitre.setText("Mes Paiements");
                btnNouveauPaiement.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void showPaiementForm(int resId) {
        setContentView(R.layout.activity_paiement_rib);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Paiement");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> {
                startActivity(new Intent(PaiementActivity.this, DashboardActivity.class));
                finish();
            });
        }

        TextView tvMontant = findViewById(R.id.tvMontant);
        TextView tvStatut = findViewById(R.id.tvStatut);
        Button btnPayer = findViewById(R.id.btnPayer);
        EditText etCodeBanque = findViewById(R.id.etCodeBanque);
        EditText etCodeGuichet = findViewById(R.id.etCodeGuichet);
        EditText etNumeroCompte = findViewById(R.id.etNumeroCompte);
        EditText etCleRib = findViewById(R.id.etCleRib);

        Reservation r = dbHelper.getReservationById(resId);
        
        if (r == null) {
            Toast.makeText(this, "Réservation non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        tvMontant.setText(String.format("%.2f €", r.getPrix_total()));
        
        switch (r.getStatut()) {
            case "en_cours":
                tvStatut.setText("Réservation validée!\nEntrez vos coordonnées bancaires");
                tvStatut.setTextColor(getResources().getColor(R.color.uber_green));
                btnPayer.setEnabled(true);
                btnPayer.setText("Confirmer le paiement");
                break;
            case "payee":
                tvStatut.setText("Paiement en attente de validation");
                tvStatut.setTextColor(getResources().getColor(R.color.warning));
                btnPayer.setEnabled(false);
                btnPayer.setText("Paiement effectué");
                etCodeBanque.setEnabled(false);
                etCodeGuichet.setEnabled(false);
                etNumeroCompte.setEnabled(false);
                etCleRib.setEnabled(false);
                break;
            case "confirmee":
                tvStatut.setText("Paiement validé - Location confirmée!");
                tvStatut.setTextColor(getResources().getColor(R.color.uber_green));
                btnPayer.setEnabled(false);
                btnPayer.setText("Payé");
                etCodeBanque.setEnabled(false);
                etCodeGuichet.setEnabled(false);
                etNumeroCompte.setEnabled(false);
                etCleRib.setEnabled(false);
                break;
            case "annulee":
                tvStatut.setText("Réservation annulée");
                tvStatut.setTextColor(getResources().getColor(R.color.error));
                btnPayer.setEnabled(false);
                btnPayer.setText("Annulé");
                break;
            default:
                tvStatut.setText("En attente de validation par l'admin");
                tvStatut.setTextColor(getResources().getColor(R.color.text_secondary));
                btnPayer.setEnabled(false);
                btnPayer.setText("En attente de validation");
                break;
        }

        final Reservation finalR = r;
        btnPayer.setOnClickListener(v -> {
            String codeBanque = etCodeBanque.getText().toString().trim();
            String codeGuichet = etCodeGuichet.getText().toString().trim();
            String numeroCompte = etNumeroCompte.getText().toString().trim();
            String cleRib = etCleRib.getText().toString().trim();
            
            if (codeBanque.isEmpty() || codeGuichet.isEmpty() || numeroCompte.isEmpty() || cleRib.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs RIB", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String rib = codeBanque + " " + codeGuichet + " " + numeroCompte + " " + cleRib;
            
            Paiement p = new Paiement();
            p.setReservation_id(resId);
            p.setMontant(finalR.getPrix_total());
            p.setDate_paiement(new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(new java.util.Date()));
            p.setMode("Virement bancaire - RIB: " + rib);

            long id = dbHelper.insertPaiement(p);
            if (id > 0) {
                finalR.setStatut("payee");
                dbHelper.updateReservation(finalR);
                NotificationHelper.notifyPaymentReceived(this, finalR.getClient_nom() != null ? finalR.getClient_nom() : "Client", String.format("%.2f €", finalR.getPrix_total()));
                Toast.makeText(this, "Paiement effectué! En attente de validation admin.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(PaiementActivity.this, DashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Erreur de paiement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPaiementDialog(Paiement p) {
        Reservation r = dbHelper.getReservationById(p.getReservation_id());
        String clientName = p.getClient_nom() != null ? p.getClient_nom() : "Client #" + p.getReservation_id();
        
        new AlertDialog.Builder(this)
            .setTitle("Valider Paiement #" + p.getId())
            .setMessage("Client: " + clientName + "\nMontant: " + String.format("%.2f €", p.getMontant()) + 
                "\nDate: " + p.getDate_paiement() + "\n\nVoulez-vous valider ce paiement?")
            .setPositiveButton("Valider", (dialog, which) -> {
                if (r != null) {
                    r.setStatut("confirmee");
                    dbHelper.updateReservation(r);
                    android.content.ContentValues cv = new android.content.ContentValues();
                    cv.put("disponible", 0);
                    dbHelper.getWritableDatabase().update("voitures", cv, "id = ?", new String[]{String.valueOf(r.getVoiture_id())});
                }
                Toast.makeText(this, "Paiement validé!", Toast.LENGTH_SHORT).show();
                onResume();
            })
            .setNegativeButton("Annuler", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lvPaiements == null) {
            lvPaiements = findViewById(R.id.lvPaiements);
        }
        if (lvPaiements == null) {
            lvPaiements = new android.widget.ListView(this);
        }
        if (reservationId <= 0) {
            try {
                boolean isAdmin = "admin".equals(sessionManager.getRole());
                List<Paiement> paiements;
                if (isAdmin) {
                    paiements = dbHelper.getAllPaiements();
                } else {
                    paiements = dbHelper.getPaiementsByClientId(sessionManager.getUserId());
                }
                if (paiements == null) {
                    paiements = new java.util.ArrayList<>();
                }
final PaiementAdapter adapter = new PaiementAdapter(this, paiements, isAdmin);
                
                adapter.setOnPaiementClickListener(new OnPaiementClickListener() {
                    @Override
                    public void onValiderClicked(Paiement p) {
                        showPaiementDialog(p);
                    }
                    @Override
                    public void onTelechargerClicked(Paiement p) {
                    }
                });
                
                lvPaiements.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
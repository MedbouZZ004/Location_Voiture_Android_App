package com.med.locationvoiture.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.med.locationvoiture.R;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Voiture;
import com.med.locationvoiture.utils.SessionManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VoitureDetailActivity extends AppCompatActivity {
    private TextView tvMarque, tvPrix, tvAnnee, tvCouleur, tvKilometrage, tvAdresse, tvStatut, tvDateDispo;
    private ImageView ivPhoto;
    private Button btnReserver, btnVoirCarte;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private Voiture voiture;
    private int voitureId;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voiture_detail);

        sessionManager = new SessionManager(this);
        isAdmin = "admin".equals(sessionManager.getRole());

        dbHelper = new DatabaseHelper(this);
        voitureId = getIntent().getIntExtra("voiture_id", -1);

        if (voitureId == -1) {
            Toast.makeText(this, "Voiture non trouvée", Toast.LENGTH_SHORT).show();
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
    }

    private void initViews() {
        ivPhoto = findViewById(R.id.ivPhoto);
        tvMarque = findViewById(R.id.tvMarque);
        tvPrix = findViewById(R.id.tvPrix);
        tvAnnee = findViewById(R.id.tvAnnee);
        tvCouleur = findViewById(R.id.tvCouleur);
        tvKilometrage = findViewById(R.id.tvKilometrage);
        tvAdresse = findViewById(R.id.tvAdresse);
        tvStatut = findViewById(R.id.tvStatut);
        tvDateDispo = findViewById(R.id.tvDateDispo);
        btnReserver = findViewById(R.id.btnReserver);
        btnVoirCarte = findViewById(R.id.btnVoirCarte);
    }

    private void loadVoiture() {
        voiture = dbHelper.getVoitureById(voitureId);
        if (voiture == null) {
            Toast.makeText(this, "Voiture non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (voiture.getImage_path() != null && !voiture.getImage_path().isEmpty()) {
            ivPhoto.setImageURI(Uri.parse(voiture.getImage_path()));
        }

        tvMarque.setText(voiture.getMarque() + " " + voiture.getModele());
        tvPrix.setText(voiture.getPrix_jour() + " €/jour");
        tvAnnee.setText("Année: " + voiture.getAnnee());
        tvCouleur.setText("Couleur: " + (voiture.getCouleur() != null ? voiture.getCouleur() : "N/A"));
        tvKilometrage.setText("Kilométrage: " + voiture.getKilometrage() + " km");

        String address = voiture.getAdresse();
        if (address != null && !address.isEmpty()) {
            tvAdresse.setText(address);
            btnVoirCarte.setVisibility(View.VISIBLE);
            btnVoirCarte.setOnClickListener(v -> {
                if (voiture.getLatitude() != 0 && voiture.getLongitude() != 0) {
                    String uri = "geo:" + voiture.getLatitude() + "," + voiture.getLongitude() + 
                        "?q=" + voiture.getLatitude() + "," + voiture.getLongitude() + 
                        "(" + voiture.getMarque() + " " + voiture.getModele() + ")";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                } else {
                    Toast.makeText(this, "Localisation non disponible", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            tvAdresse.setText("Non disponible");
            btnVoirCarte.setVisibility(View.GONE);
        }

        if (voiture.isDisponible()) {
            tvStatut.setText("✓ Disponible");
            tvStatut.setTextColor(getResources().getColor(R.color.status_available));
            tvDateDispo.setVisibility(View.GONE);
            btnReserver.setText("Réserver maintenant");
            btnReserver.setEnabled(true);
            btnReserver.setOnClickListener(v -> {
                if (isAdmin) {
                    Toast.makeText(this, "Les admins ne peuvent pas réserver", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(VoitureDetailActivity.this, ReservationFormClientActivity.class);
                    intent.putExtra("voiture_id", voiture.getId());
                    startActivity(intent);
                }
            });
        } else {
            String dateFin = dbHelper.getDateFinLocation(voitureId);
            if (dateFin != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                    Date fin = sdf.parse(dateFin);
                    Date now = new Date();
                    if (fin != null) {
                        long diffMillis = fin.getTime() - now.getTime();
                        long joursRestants = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
                        if (joursRestants > 0) {
                            tvStatut.setText("✗ Louée");
                            tvStatut.setTextColor(getResources().getColor(R.color.status_rented));
                            tvDateDispo.setText("Revient dans " + joursRestants + " jour(s) (" + dateFin + ")");
                            tvDateDispo.setVisibility(View.VISIBLE);
                        } else if (joursRestants == 0) {
                            tvStatut.setText("✗ Retour aujourd'hui");
                            tvStatut.setTextColor(getResources().getColor(R.color.warning));
                            tvDateDispo.setText("Revient aujourd'hui");
                            tvDateDispo.setVisibility(View.VISIBLE);
                        } else {
                            tvStatut.setText("✗ En retard");
                            tvStatut.setTextColor(getResources().getColor(R.color.error));
                            tvDateDispo.setText("Retard - devez contacter l admin");
                            tvDateDispo.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (ParseException e) {
                    tvStatut.setText("✗ Louée");
                    tvStatut.setTextColor(getResources().getColor(R.color.status_rented));
                    tvDateDispo.setText("Date de retour: " + dateFin);
                    tvDateDispo.setVisibility(View.VISIBLE);
                }
            }
            btnReserver.setText(" currently unavailable");
            btnReserver.setEnabled(false);
        }
    }
}
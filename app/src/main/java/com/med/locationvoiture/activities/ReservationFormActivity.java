package com.med.locationvoiture.activities;

import android.os.Bundle;
import android.app.DatePickerDialog;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.med.locationvoiture.R;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Client;
import com.med.locationvoiture.models.Reservation;
import com.med.locationvoiture.models.Voiture;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReservationFormActivity extends AppCompatActivity {
    private Spinner spClient, spVoiture;
    private EditText etDateDebut, etDateFin, etPrixTotal;
    private Button btnCalculer, btnSave;
    private TextView tvTitre;
    private DatabaseHelper dbHelper;
    private List<Client> clients;
    private List<Voiture> voitures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_form);

        dbHelper = new DatabaseHelper(this);
        
        spClient = findViewById(R.id.spClient);
        spVoiture = findViewById(R.id.spVoiture);
        etDateDebut = findViewById(R.id.etDateDebut);
        etDateFin = findViewById(R.id.etDateFin);
        etPrixTotal = findViewById(R.id.etPrixTotal);
        btnCalculer = findViewById(R.id.btnCalculer);
        btnSave = findViewById(R.id.btnSave);
        tvTitre = findViewById(R.id.tvTitre);

        loadClients();
        loadVoitures();

        etDateDebut.setOnClickListener(v -> showDatePicker(etDateDebut));
        etDateFin.setOnClickListener(v -> showDatePicker(etDateFin));

        btnCalculer.setOnClickListener(v -> calculerPrix());
        btnSave.setOnClickListener(v -> saveReservation());
    }

    private void loadClients() {
        clients = dbHelper.getAllClients();
        String[] clientNames = new String[clients.size()];
        for (int i = 0; i < clients.size(); i++) {
            clientNames[i] = clients.get(i).getNom() + " " + clients.get(i).getPrenom();
        }
        spClient.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clientNames));
    }

    private void loadVoitures() {
        voitures = dbHelper.getVoituresDisponibles();
        String[] voitureNames = new String[voitures.size()];
        for (int i = 0; i < voitures.size(); i++) {
            voitureNames[i] = voitures.get(i).getMarque() + " " + voitures.get(i).getModele() + " - " + voitures.get(i).getPrix_jour() + "€/jour";
        }
        spVoiture.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, voitureNames));
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
            int selectedVoiture = spVoiture.getSelectedItemPosition();
            if (selectedVoiture < 0 || etDateDebut.getText().toString().isEmpty() || etDateFin.getText().toString().isEmpty()) {
                Toast.makeText(this, "Sélectionnez une voiture et les dates", Toast.LENGTH_SHORT).show();
                return;
            }

            Voiture v = voitures.get(selectedVoiture);
            double prixJour = v.getPrix_jour();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            java.util.Date debut = sdf.parse(etDateDebut.getText().toString());
            java.util.Date fin = sdf.parse(etDateFin.getText().toString());

            if (debut != null && fin != null) {
                long diffMillis = fin.getTime() - debut.getTime();
                int jours = (int) (diffMillis / (1000 * 60 * 60 * 24));
                if (jours <= 0) {
                    Toast.makeText(this, "La date de fin doit être après la date de début", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (jours > 365) {
                    Toast.makeText(this, "La durée maximale est de 365 jours", Toast.LENGTH_SHORT).show();
                    return;
                }
                double total = jours * prixJour;
                etPrixTotal.setText(String.format("%.2f", total));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur dans le calcul", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveReservation() {
        if (spClient.getSelectedItemPosition() < 0 || spVoiture.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Sélectionnez un client et une voiture", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etDateDebut.getText().toString().isEmpty() || etDateFin.getText().toString().isEmpty() || etPrixTotal.getText().toString().isEmpty()) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Client client = clients.get(spClient.getSelectedItemPosition());
        Voiture voiture = voitures.get(spVoiture.getSelectedItemPosition());

        Reservation r = new Reservation();
        r.setClient_id(client.getId());
        r.setVoiture_id(voiture.getId());
        r.setDate_debut(etDateDebut.getText().toString());
        r.setDate_fin(etDateFin.getText().toString());
        r.setPrix_total(Double.parseDouble(etPrixTotal.getText().toString()));
        r.setStatut("en_attente");

        long id = dbHelper.insertReservation(r);

        if (id > 0) {
            Toast.makeText(this, "Réservation créée! Le changement automatique de disponibilité de la voiture.", Toast.LENGTH_LONG).show();
            
            String pdfPath = com.med.locationvoiture.utils.PdfGenerator.generateReservationContract(
                this,
                (int) id,
                client.getNom() + " " + client.getPrenom(),
                client.getEmail(),
                client.getCin(),
                voiture.getMarque(),
                voiture.getModele(),
                etDateDebut.getText().toString(),
                etDateFin.getText().toString(),
                r.getPrix_total()
            );

            if (pdfPath != null) {
                Toast.makeText(this, "Contrat PDF généré: " + pdfPath, Toast.LENGTH_LONG).show();
            }

            finish();
        } else {
            Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
        }
    }
}
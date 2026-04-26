package com.med.locationvoiture.activities;

import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.med.locationvoiture.R;
import com.med.locationvoiture.adapters.ReservationAdapter;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Reservation;
import com.med.locationvoiture.models.Voiture;
import com.med.locationvoiture.utils.SessionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationListActivity extends AppCompatActivity {
    private ListView lvReservations;
    private Spinner spFiltre;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Reservation> reservations;
    private FloatingActionButton fabAjouter;
    private boolean isAdmin;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        sessionManager = new SessionManager(this);
        isAdmin = "admin".equals(sessionManager.getRole());

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(isAdmin ? "Réservations" : "Mes Réservations");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        dbHelper = new DatabaseHelper(this);
        lvReservations = findViewById(R.id.lvReservations);
        spFiltre = findViewById(R.id.spFiltre);
        fabAjouter = findViewById(R.id.fabAjouter);
        tvEmpty = findViewById(R.id.tvEmpty);

        if (!isAdmin) {
            spFiltre.setVisibility(View.GONE);
        }

        fabAjouter.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        String[] filtres = {"Tous", "En attente", "Validée", "Payée", "Confirmée", "Annulée"};
        spFiltre.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filtres));

        spFiltre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterReservations(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        fabAjouter.setOnClickListener(v -> {
            startActivity(new Intent(ReservationListActivity.this, ReservationFormActivity.class));
        });

        loadReservations();
        
        lvReservations.setOnItemClickListener((parent, view, position, id) -> {
            if (reservations != null && position >= 0 && position < reservations.size()) {
                Reservation r = reservations.get(position);
                if (!isAdmin) {
                    if ("en_cours".equals(r.getStatut())) {
                        Intent intent = new Intent(ReservationListActivity.this, PaiementActivity.class);
                        intent.putExtra("reservation_id", r.getId());
                        startActivity(intent);
                    } else if ("confirmee".equals(r.getStatut())) {
                        showPdfDialog(r);
                    }
                }
            }
        });
    }

    private void showPdfDialog(Reservation r) {
        String contractPath = r.getContract_path();
        
        if (contractPath != null && !contractPath.isEmpty()) {
            File contractFile = new File(contractPath);
            if (contractFile.exists()) {
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Contrat de Location")
                    .setMessage("Le contrat est déjà généré. Voulez-vous le télécharger?")
                    .setPositiveButton("Télécharger", (dialog, which) -> {
                        openPdf(contractPath);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
                return;
            }
        }
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Contrat de Location")
            .setMessage("Voulez-vous télécharger le contrat de location en PDF?")
            .setPositiveButton("Télécharger", (dialog, which) -> {
                generatePdf(r);
            })
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void openPdf(String filePath) {
        try {
            File file = new File(filePath);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Aucune application pour ouvrir les PDF", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void generatePdf(Reservation r) {
        try {
            Voiture v = dbHelper.getVoitureById(r.getVoiture_id());
            String clientNom = r.getClient_nom() != null ? r.getClient_nom() : "Client";
            
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            
            android.graphics.Canvas canvas = page.getCanvas();
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setTextSize(12);
            
            int y = 50;
            int lineHeight = 25;
            int margin = 50;
            
            paint.setFakeBoldText(true);
            paint.setTextSize(18);
            paint.setColor(android.graphics.Color.parseColor("#06C167"));
            canvas.drawText("CONTRAT DE LOCATION DE VOITURE", 80, y, paint);
            y += 40;
            
            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            paint.setColor(android.graphics.Color.BLACK);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
            String dateContrat = sdf.format(new Date());
            
            canvas.drawText("N° Réservation: " + r.getId(), margin, y, paint);
            y += lineHeight;
            canvas.drawText("Date: " + dateContrat, margin, y, paint);
            y += lineHeight * 2;
            
            paint.setFakeBoldText(true);
            canvas.drawText("INFORMATIONS CLIENT", margin, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);
            canvas.drawText("Nom: " + clientNom, margin, y, paint);
            y += lineHeight * 2;
            
            paint.setFakeBoldText(true);
            canvas.drawText("INFORMATIONS DU VÉHICULE", margin, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);
            if (v != null) {
                canvas.drawText("Marque: " + v.getMarque(), margin, y, paint);
                y += lineHeight;
                canvas.drawText("Modèle: " + v.getModele(), margin, y, paint);
                y += lineHeight;
                canvas.drawText("Année: " + v.getAnnee(), margin, y, paint);
                y += lineHeight;
                canvas.drawText("Couleur: " + (v.getCouleur() != null ? v.getCouleur() : "N/A"), margin, y, paint);
                y += lineHeight;
                canvas.drawText("Prix journalier: " + String.format("%.2f €", v.getPrix_jour()), margin, y, paint);
                y += lineHeight;
            }
            y += lineHeight;
            
            paint.setFakeBoldText(true);
            canvas.drawText("DÉTAILS DE LA LOCATION", margin, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);
            canvas.drawText("Date de début: " + r.getDate_debut(), margin, y, paint);
            y += lineHeight;
            canvas.drawText("Date de fin: " + r.getDate_fin(), margin, y, paint);
            y += lineHeight;
            
            paint.setFakeBoldText(true);
            paint.setTextSize(14);
            paint.setColor(android.graphics.Color.parseColor("#06C167"));
            canvas.drawText("PRIX TOTAL: " + String.format("%.2f €", r.getPrix_total()), margin, y, paint);
            y += lineHeight * 2;
            
            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            paint.setColor(android.graphics.Color.BLACK);
            
            canvas.drawText("STATUT: " + r.getStatut().toUpperCase(), margin, y, paint);
            y += lineHeight * 2;
            
            paint.setFakeBoldText(true);
            canvas.drawText("SIGNATURES", margin, y, paint);
            y += lineHeight * 2;
            
            paint.setFakeBoldText(false);
            canvas.drawText("_________________________", margin, y, paint);
            y += lineHeight;
            canvas.drawText("Signature du client", margin, y, paint);
            y += lineHeight * 3;
            
            canvas.drawText("_________________________", 300, y, paint);
            y += lineHeight;
            canvas.drawText("Signature du gérant", 300, y, paint);
            
            y += lineHeight * 2;
            paint.setTextSize(10);
            canvas.drawText("Document généré par AutoLocation", margin, y, paint);
            
            document.finishPage(page);
            
            String fileName = "Contrat_Location_" + r.getId() + ".pdf";
            File folder = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (folder != null && !folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            
            Toast.makeText(this, "Contrat enregistré: " + file.getName(), Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReservations();
    }

    private void loadReservations() {
        try {
            reservations = dbHelper.getAllReservations();
            if (reservations == null) {
                reservations = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            reservations = new ArrayList<>();
        }
        
        if (!isAdmin) {
            int clientId = dbHelper.getOrCreateClientId(sessionManager.getUserId(), sessionManager.getUsername());
            List<Reservation> clientReservations = new ArrayList<>();
            for (Reservation r : reservations) {
                if (r.getClient_id() == clientId) {
                    clientReservations.add(r);
                }
            }
            reservations = clientReservations;
        }
        
        if (reservations.isEmpty()) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            if (lvReservations != null) lvReservations.setVisibility(View.GONE);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            if (lvReservations != null) {
                lvReservations.setVisibility(View.VISIBLE);
                lvReservations.setAdapter(new ReservationAdapter(this, reservations));
            }
        }
    }

    private void filterReservations(int position) {
        if (lvReservations == null) {
            lvReservations = findViewById(R.id.lvReservations);
        }
        List<Reservation> allReservations = dbHelper.getAllReservations();
        
        if (!isAdmin) {
            int clientId = dbHelper.getOrCreateClientId(sessionManager.getUserId(), sessionManager.getUsername());
            List<Reservation> clientReservations = new ArrayList<>();
            for (Reservation r : allReservations) {
                if (r.getClient_id() == clientId) {
                    clientReservations.add(r);
                }
            }
            allReservations = clientReservations;
        }
        
        if (position == 0) {
            if (lvReservations != null) lvReservations.setAdapter(new ReservationAdapter(this, allReservations));
        } else {
            String[] statuts = {"en_attente", "en_cours", "payee", "confirmee", "annulee"};
            List<Reservation> filtered = new ArrayList<>();
            for (Reservation r : allReservations) {
                if (r.getStatut().equals(statuts[position - 1])) {
                    filtered.add(r);
                }
            }
            if (lvReservations != null) lvReservations.setAdapter(new ReservationAdapter(this, filtered));
        }
    }
}
package com.med.locationvoiture.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.med.locationvoiture.R;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.FesLocation;
import com.med.locationvoiture.models.Voiture;
import com.med.locationvoiture.utils.ImageUtils;
import java.util.List;

public class VoitureFormActivity extends AppCompatActivity {
    private EditText etMarque, etModele, etAnnee, etPrix, etKilometrage, etCouleur;
    private CheckBox cbDisponible;
    private Button btnSave, btnDelete;
    private ImageView ivPhoto;
    private FloatingActionButton fabPickPhoto;
    private TextView tvTitre;
    private Spinner spinnerLocation;
    private TextView tvSelectedLocation;
    private DatabaseHelper dbHelper;
    private int voitureId = -1;
    private String imagePath = null;
    private static final int PICK_IMAGE = 100;
    private FesLocation selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voiture_form);

        dbHelper = new DatabaseHelper(this);
        
        tvTitre = findViewById(R.id.tvTitre);
        etMarque = findViewById(R.id.etMarque);
        etModele = findViewById(R.id.etModele);
        etAnnee = findViewById(R.id.etAnnee);
        etPrix = findViewById(R.id.etPrix);
        etKilometrage = findViewById(R.id.etKilometrage);
        etCouleur = findViewById(R.id.etCouleur);
        cbDisponible = findViewById(R.id.cbDisponible);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        ivPhoto = findViewById(R.id.ivPhoto);
        fabPickPhoto = findViewById(R.id.fabPickPhoto);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);

        setupLocationSpinner();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("voiture_id")) {
            voitureId = extras.getInt("voiture_id");
            loadVoiture(voitureId);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        fabPickPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> saveVoiture());
        btnDelete.setOnClickListener(v -> deleteVoiture());
    }

    private void setupLocationSpinner() {
        List<FesLocation> locations = FesLocation.getFesLocations();
        ArrayAdapter<FesLocation> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter);

        spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = (FesLocation) parent.getSelectedItem();
                if (selectedLocation != null) {
                    tvSelectedLocation.setText("📍 " + selectedLocation.getName() + 
                        String.format("\nLat: %.4f, Lng: %.4f", 
                            selectedLocation.getLatitude(), selectedLocation.getLongitude()));
                    tvSelectedLocation.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLocation = null;
                tvSelectedLocation.setVisibility(View.GONE);
            }
        });
    }

    private void loadVoiture(int id) {
        List<Voiture> voitures = dbHelper.getAllVoitures();
        for (Voiture v : voitures) {
            if (v.getId() == id) {
                etMarque.setText(v.getMarque());
                etModele.setText(v.getModele());
                etAnnee.setText(String.valueOf(v.getAnnee()));
                etPrix.setText(String.valueOf(v.getPrix_jour()));
                etKilometrage.setText(String.valueOf(v.getKilometrage()));
                etCouleur.setText(v.getCouleur());
                cbDisponible.setChecked(v.isDisponible());
                imagePath = v.getImage_path();
                
                if (imagePath != null && !imagePath.isEmpty()) {
                    ivPhoto.setImageURI(Uri.parse(imagePath));
                }
                
                for (int i = 0; i < spinnerLocation.getCount(); i++) {
                    FesLocation loc = (FesLocation) spinnerLocation.getItemAtPosition(i);
                    if (loc.getLatitude() == v.getLatitude() && loc.getLongitude() == v.getLongitude()) {
                        spinnerLocation.setSelection(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            imagePath = ImageUtils.saveImageToInternalStorage(this, imageUri);
            if (imagePath != null) {
                ivPhoto.setImageURI(Uri.parse(imagePath));
            }
        }
    }

    private void saveVoiture() {
        String marque = etMarque.getText().toString().trim();
        String modele = etModele.getText().toString().trim();
        String anneeStr = etAnnee.getText().toString().trim();
        String prixStr = etPrix.getText().toString().trim();
        String kmStr = etKilometrage.getText().toString().trim();
        String couleur = etCouleur.getText().toString().trim();

        if (marque.isEmpty() || modele.isEmpty() || anneeStr.isEmpty() || prixStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLocation == null) {
            Toast.makeText(this, "Veuillez sélectionner un emplacement à Fès", Toast.LENGTH_SHORT).show();
            return;
        }

        Voiture v = new Voiture();
        if (voitureId > 0) v.setId(voitureId);
        v.setMarque(marque);
        v.setModele(modele);
        v.setAnnee(Integer.parseInt(anneeStr));
        v.setPrix_jour(Double.parseDouble(prixStr));
        v.setKilometrage(kmStr.isEmpty() ? 0 : Integer.parseInt(kmStr));
        v.setCouleur(couleur);
        v.setDisponible(cbDisponible.isChecked());
        v.setImage_path(imagePath);
        v.setLatitude(selectedLocation.getLatitude());
        v.setLongitude(selectedLocation.getLongitude());
        v.setAdresse(selectedLocation.getName());

        long id;
        if (voitureId > 0) {
            id = dbHelper.updateVoiture(v);
        } else {
            id = dbHelper.insertVoiture(v);
        }

        if (id > 0) {
            Toast.makeText(this, "Voiture enregistrée", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteVoiture() {
        if (dbHelper.deleteVoiture(voitureId) > 0) {
            Toast.makeText(this, "Voiture supprimée", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
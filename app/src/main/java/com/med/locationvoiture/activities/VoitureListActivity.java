package com.med.locationvoiture.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.med.locationvoiture.R;
import com.med.locationvoiture.adapters.VoitureAdapter;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Voiture;
import com.med.locationvoiture.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class VoitureListActivity extends AppCompatActivity {
    private static final String TAG = "VoitureListActivity";
    private ListView lvVoitures;
    private Spinner spFiltre;
    private ExtendedFloatingActionButton fabAjouter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Voiture> allVoitures;
    private List<Voiture> filteredVoitures;
    private boolean isAdmin;
    private com.google.android.material.textfield.TextInputEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        sessionManager = new SessionManager(this);
        isAdmin = "admin".equals(sessionManager.getRole());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isAdmin ? "Gestion Voitures" : "Nos Voitures");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        lvVoitures = findViewById(R.id.lvItems);
        spFiltre = findViewById(R.id.spFiltre);
        fabAjouter = findViewById(R.id.fabAjouter);
        etSearch = findViewById(R.id.etSearch);

        fabAjouter.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        spFiltre.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        setupFiltre();
        setupSearch();
        loadVoitures();

        fabAjouter.setOnClickListener(v -> {
            startActivity(new Intent(VoitureListActivity.this, VoitureFormActivity.class));
        });

        lvVoitures.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, "Item clicked: " + position);
            handleCarClick(position);
        });
    }

    private void handleCarClick(int position) {
        Toast.makeText(this, "Position: " + position, Toast.LENGTH_SHORT).show();
        List<Voiture> listToUse = (filteredVoitures != null && !filteredVoitures.isEmpty()) ? filteredVoitures : allVoitures;
        if (listToUse != null && position >= 0 && position < listToUse.size()) {
            Voiture v = listToUse.get(position);
            Intent intent;
            if (isAdmin) {
                intent = new Intent(this, VoitureFormActivity.class);
            } else {
                intent = new Intent(this, VoitureDetailActivity.class);
            }
            intent.putExtra("voiture_id", v.getId());
            startActivity(intent);
        }
    }

    private void loadVoitures() {
        try {
            allVoitures = dbHelper.getAllVoitures();
            if (allVoitures == null) {
                allVoitures = new ArrayList<>();
            }
            filteredVoitures = new ArrayList<>(allVoitures);
            updateListView();
        } catch (Exception e) {
            Log.e(TAG, "Error loading voitures", e);
            allVoitures = new ArrayList<>();
            filteredVoitures = new ArrayList<>();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVoitures();
    }

    private void setupFiltre() {
        String[] filtreOptions = isAdmin ? 
            new String[]{"Tous", "Disponibles", "Louées"} :
            new String[]{"Tous", "Disponibles"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filtreOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltre.setAdapter(adapter);
        spFiltre.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterVoitures();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterVoitures();
            }
        });
    }

    private void filterVoitures() {
        if (allVoitures == null) return;
        
        String query = etSearch != null ? etSearch.getText().toString().trim().toLowerCase() : "";
        int filtreIndex = spFiltre != null ? spFiltre.getSelectedItemPosition() : 0;
        
        filteredVoitures = new ArrayList<>();
        for (Voiture v : allVoitures) {
            boolean matchesSearch = query.isEmpty() || 
                (v.getMarque() != null && v.getMarque().toLowerCase().contains(query)) ||
                (v.getModele() != null && v.getModele().toLowerCase().contains(query)) ||
                (v.getCouleur() != null && v.getCouleur().toLowerCase().contains(query));
            
            boolean matchesFiltre = true;
            if (filtreIndex == 1) {
                matchesFiltre = v.isDisponible();
            } else if (filtreIndex == 2) {
                matchesFiltre = !v.isDisponible();
            }
            
            if (matchesSearch && matchesFiltre) {
                filteredVoitures.add(v);
            }
        }
        
        updateListView();
    }

    private void updateListView() {
        if (lvVoitures != null) {
            lvVoitures.setAdapter(new VoitureAdapter(this, filteredVoitures));
        }
    }
}
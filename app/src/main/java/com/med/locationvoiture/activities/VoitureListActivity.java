package com.med.locationvoiture.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private boolean isAdmin;

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

        fabAjouter.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        spFiltre.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

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
        if (allVoitures != null && position >= 0 && position < allVoitures.size()) {
            Voiture v = allVoitures.get(position);
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
            if (lvVoitures != null) {
                lvVoitures.setAdapter(new VoitureAdapter(this, allVoitures));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading voitures", e);
            allVoitures = new ArrayList<>();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVoitures();
    }
}
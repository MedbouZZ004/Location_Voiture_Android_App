package com.med.locationvoiture.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.med.locationvoiture.R;
import com.med.locationvoiture.adapters.VoitureAdapter;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Voiture;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private ListView lvVoitures;
    private TextView tvTitre;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = new DatabaseHelper(this);
        lvVoitures = findViewById(R.id.lvVoitures);
        tvTitre = findViewById(R.id.tvTitre);

        loadVoitures();
    }

    private void loadVoitures() {
        List<Voiture> voitures = dbHelper.getAllVoitures();
        
        final List<Voiture> avecPosition = new java.util.ArrayList<>();
        for (Voiture v : voitures) {
            if (v.getLatitude() != 0 || v.getLongitude() != 0) {
                avecPosition.add(v);
            }
        }

        if (avecPosition.isEmpty()) {
            tvTitre.setText("Aucune voiture avec géolocalisation");
        } else {
            tvTitre.setText("Voitures géolocalisées (" + avecPosition.size() + ") - Cliquez pour ouvrir");
        }

        lvVoitures.setAdapter(new VoitureAdapter(this, avecPosition));
        
        lvVoitures.setOnItemClickListener((parent, view, position, id) -> {
            Voiture v = avecPosition.get(position);
            String uri = "geo:" + v.getLatitude() + "," + v.getLongitude() + "?q=" + 
                v.getLatitude() + "," + v.getLongitude() + "(" + v.getMarque() + " " + v.getModele() + ")";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        });
    }
}
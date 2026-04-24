package com.med.locationvoiture.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.google.android.material.appbar.AppBarLayout;
import com.med.locationvoiture.R;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.utils.SessionManager;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvWelcome, tvStats;
    private CardView cvVoitures, cvClients, cvReservations, cvPaiements, cvMap;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || "admin".equals(sessionManager.getRole())) {
            startActivity(new Intent(DashboardActivity.this, PortalActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_dashboard);

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Client");
            }
        }

        dbHelper = new DatabaseHelper(this);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStats = findViewById(R.id.tvStats);
        cvVoitures = findViewById(R.id.cvVoitures);
        cvClients = findViewById(R.id.cvClients);
        cvReservations = findViewById(R.id.cvReservations);
        cvPaiements = findViewById(R.id.cvPaiements);
        cvMap = findViewById(R.id.cvMap);

        tvWelcome.setText("Bienvenue");

        cvClients.setVisibility(View.GONE);

        try {
            int[] stats = dbHelper.getStatistiques();
            tvStats.setText("Voitures disponibles: " + stats[0] + " / " + stats[1]);
        } catch (Exception e) {
            tvStats.setText("Voitures disponibles: 0 / 0");
        }

        cvVoitures.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, VoitureListActivity.class)));
        cvReservations.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ReservationListActivity.class)));
        cvPaiements.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, PaiementActivity.class)));
        cvMap.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MapViewActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logout();
            startActivity(new Intent(DashboardActivity.this, PortalActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
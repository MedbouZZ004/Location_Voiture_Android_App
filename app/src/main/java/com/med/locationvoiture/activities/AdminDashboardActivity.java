package com.med.locationvoiture.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.med.locationvoiture.R;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.utils.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome, tvStats, tvStatVoitures, tvStatClients, tvStatReservations, tvStatRevenus;
    private CardView cvVoitures, cvClients, cvReservations, cvPaiements, cvMap, cvValidations;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || !"admin".equals(sessionManager.getRole())) {
            startActivity(new Intent(AdminDashboardActivity.this, PortalActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_admin_dashboard);

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Admin");
            }
        }

        dbHelper = new DatabaseHelper(this);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStats = findViewById(R.id.tvStats);
        tvStatVoitures = findViewById(R.id.tvStatVoitures);
        tvStatClients = findViewById(R.id.tvStatClients);
        tvStatReservations = findViewById(R.id.tvStatReservations);
        tvStatRevenus = findViewById(R.id.tvStatRevenus);
        cvVoitures = findViewById(R.id.cvVoitures);
        cvClients = findViewById(R.id.cvClients);
        cvReservations = findViewById(R.id.cvReservations);
        cvPaiements = findViewById(R.id.cvPaiements);
        cvMap = findViewById(R.id.cvMap);
        cvValidations = findViewById(R.id.cvValidations);

        tvWelcome.setText("Bienvenue Admin");

        int[] stats = dbHelper.getStatistiques();
        tvStatVoitures.setText(stats[0] + "/" + stats[1]);
        tvStatClients.setText(String.valueOf(stats[4]));
        tvStatReservations.setText(String.valueOf(stats[2]));
        tvStatRevenus.setText(stats[3] + " €");

        cvVoitures.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, VoitureListActivity.class)));
        cvClients.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ClientListActivity.class)));
        cvReservations.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ReservationListActivity.class);
            intent.putExtra("admin_mode", true);
            startActivity(intent);
        });
        cvPaiements.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, PaiementActivity.class);
            intent.putExtra("admin_mode", true);
            startActivity(intent);
        });
        cvMap.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, MapViewActivity.class)));
        cvValidations.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ValidationListActivity.class)));
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
            startActivity(new Intent(AdminDashboardActivity.this, PortalActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
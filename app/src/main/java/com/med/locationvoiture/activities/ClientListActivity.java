package com.med.locationvoiture.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.med.locationvoiture.R;
import com.med.locationvoiture.adapters.ClientAdapter;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Client;
import com.med.locationvoiture.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class ClientListActivity extends AppCompatActivity {
    private ListView lvClients;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Client> clients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);

        sessionManager = new SessionManager(this);
        boolean isAdmin = "admin".equals(sessionManager.getRole());

        if (!isAdmin) {
            Toast.makeText(this, "Accès réservé aux administrateurs", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        lvClients = findViewById(R.id.lvClients);

        lvClients.setOnItemClickListener((parent, view, position, id) -> {
            if (clients != null && position >= 0 && position < clients.size()) {
                Client c = clients.get(position);
                Intent intent = new Intent(ClientListActivity.this, ClientFormActivity.class);
                intent.putExtra("client_id", c.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            clients = dbHelper.getAllClients();
            if (clients == null) {
                clients = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            clients = new ArrayList<>();
        }
        lvClients.setAdapter(new ClientAdapter(this, clients));
    }
}
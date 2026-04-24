package com.med.locationvoiture.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.med.locationvoiture.R;
import com.med.locationvoiture.database.DatabaseHelper;
import com.med.locationvoiture.models.Voiture;
import com.med.locationvoiture.utils.SessionManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.util.List;

public class MapViewActivity extends AppCompatActivity {
    private MapView mapView;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private MyLocationNewOverlay myLocationOverlay;
    private FloatingActionButton fabMyLocation;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final GeoPoint FES = new GeoPoint(34.0181, -4.9981);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_map_view);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Carte des Voitures");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(FES);

        fabMyLocation = findViewById(R.id.fabMyLocation);
        fabMyLocation.setOnClickListener(v -> centerOnMyLocation());

        loadCarLocations();
        setupMyLocation();
    }

    private void centerOnMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }
        
        if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
            mapView.getController().animateTo(myLocationOverlay.getMyLocation());
            mapView.getController().setZoom(15.0);
        } else {
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
            myLocationOverlay.enableMyLocation();
            mapView.getOverlays().add(myLocationOverlay);
            Toast.makeText(this, "Activation de la localisation...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
            myLocationOverlay.enableMyLocation();
            mapView.getOverlays().add(myLocationOverlay);
        }
    }

    private void loadCarLocations() {
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);

        List<Voiture> voitures = dbHelper.getAllVoitures();
        int availableCount = 0;
        int rentedCount = 0;

        for (Voiture v : voitures) {
            if (v.getLatitude() != 0 || v.getLongitude() != 0) {
                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(v.getLatitude(), v.getLongitude()));
                marker.setTitle(v.getMarque() + " " + v.getModele());
                
                String status = v.isDisponible() ? "✅ Disponible" : "❌ Loué(e)";
                String info = v.getPrix_jour() + " €/jour\n" + status;
                if (v.getAdresse() != null && !v.getAdresse().isEmpty()) {
                    info += "\n📍 " + v.getAdresse();
                }
                marker.setSnippet(info);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                if (v.isDisponible()) {
                    Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_car_available);
                    if (icon != null) {
                        marker.setIcon(icon);
                    }
                    availableCount++;
                } else {
                    Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_car_rented);
                    if (icon != null) {
                        marker.setIcon(icon);
                    }
                    rentedCount++;
                }

                final boolean isDispo = v.isDisponible();
                marker.setOnMarkerClickListener((m, mv) -> {
                    Toast.makeText(this, isDispo ? "Voiture disponible" : "Voiture actuellement louée", Toast.LENGTH_SHORT).show();
                    return false;
                });

                mapView.getOverlays().add(marker);
            }
        }

        if (availableCount == 0 && rentedCount == 0) {
            Toast.makeText(this, "Aucune voiture géolocalisée", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "✅ " + availableCount + " dispo | ❌ " + rentedCount + " loué(es)", Toast.LENGTH_LONG).show();
        }
        
        mapView.invalidate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMyLocation();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
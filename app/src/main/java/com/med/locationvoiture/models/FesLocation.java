package com.med.locationvoiture.models;

import java.util.ArrayList;
import java.util.List;

public class FesLocation {
    private String name;
    private double latitude;
    private double longitude;

    public FesLocation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString() { return name; }

    public static List<FesLocation> getFesLocations() {
        List<FesLocation> locations = new ArrayList<>();
        locations.add(new FesLocation("Avenue Hassan II, Fès", 34.0335, -4.9997));
        locations.add(new FesLocation("Boulevard Mohammed V, Fès", 34.0262, -5.0006));
        locations.add(new FesLocation("Place Florence, Fès", 34.0181, -4.9981));
        locations.add(new FesLocation("Gare ONCF, Fès", 34.0384, -5.0009));
        locations.add(new FesLocation("Avenue Imam Malik, Fès", 34.0098, -5.0172));
        locations.add(new FesLocation("Bab Boujloud, Fès", 34.0187, -5.0063));
        locations.add(new FesLocation("Bab Rcif, Fès", 34.0236, -5.0032));
        locations.add(new FesLocation("Avenue Sidi Brahim, Fès", 34.0282, -4.9946));
        locations.add(new FesLocation("Quartier Ville Nouvelle, Fès", 34.0257, -4.9909));
        locations.add(new FesLocation("Route d'Immouzer, Fès", 34.0444, -4.9821));
        locations.add(new FesLocation("Avenue Allal Al Fassi, Fès", 34.0311, -5.0125));
        locations.add(new FesLocation("Route de Taza, Fès", 34.0512, -5.0254));
        return locations;
    }
}
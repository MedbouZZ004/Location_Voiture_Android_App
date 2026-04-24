package com.med.locationvoiture.models;

public class Voiture {
    private int id;
    private String marque;
    private String modele;
    private int annee;
    private double prix_jour;
    private boolean disponible;
    private String image_path;
    private int kilometrage;
    private String couleur;
    private double latitude;
    private double longitude;
    private String adresse;

    public Voiture() {}

    public Voiture(String marque, String modele, int annee, double prix_jour, String couleur) {
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.prix_jour = prix_jour;
        this.couleur = couleur;
        this.disponible = true;
        this.kilometrage = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    public double getPrix_jour() { return prix_jour; }
    public void setPrix_jour(double prix_jour) { this.prix_jour = prix_jour; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public String getImage_path() { return image_path; }
    public void setImage_path(String image_path) { this.image_path = image_path; }
    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int kilometrage) { this.kilometrage = kilometrage; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
}
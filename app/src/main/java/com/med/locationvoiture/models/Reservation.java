package com.med.locationvoiture.models;

public class Reservation {
    private int id;
    private int client_id;
    private int voiture_id;
    private String date_debut;
    private String date_fin;
    private double prix_total;
    private String statut;
    private String created_at;
    private String client_nom;
    private String voiture_nom;

    public Reservation() {}

    public Reservation(int client_id, int voiture_id, String date_debut, String date_fin, double prix_total) {
        this.client_id = client_id;
        this.voiture_id = voiture_id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.prix_total = prix_total;
        this.statut = "en_attente";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClient_id() { return client_id; }
    public void setClient_id(int client_id) { this.client_id = client_id; }
    public int getVoiture_id() { return voiture_id; }
    public void setVoiture_id(int voiture_id) { this.voiture_id = voiture_id; }
    public String getDate_debut() { return date_debut; }
    public void setDate_debut(String date_debut) { this.date_debut = date_debut; }
    public String getDate_fin() { return date_fin; }
    public void setDate_fin(String date_fin) { this.date_fin = date_fin; }
    public double getPrix_total() { return prix_total; }
    public void setPrix_total(double prix_total) { this.prix_total = prix_total; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public String getClient_nom() { return client_nom; }
    public void setClient_nom(String client_nom) { this.client_nom = client_nom; }
    public String getVoiture_nom() { return voiture_nom; }
    public void setVoiture_nom(String voiture_nom) { this.voiture_nom = voiture_nom; }
}
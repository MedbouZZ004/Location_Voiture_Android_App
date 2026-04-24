package com.med.locationvoiture.models;

public class Paiement {
    private int id;
    private int reservation_id;
    private double montant;
    private String date_paiement;
    private String mode;
    private String created_at;
    private String client_nom;

    public Paiement() {}

    public Paiement(int reservation_id, double montant, String date_paiement, String mode) {
        this.reservation_id = reservation_id;
        this.montant = montant;
        this.date_paiement = date_paiement;
        this.mode = mode;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getReservation_id() { return reservation_id; }
    public void setReservation_id(int reservation_id) { this.reservation_id = reservation_id; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
    public String getDate_paiement() { return date_paiement; }
    public void setDate_paiement(String date_paiement) { this.date_paiement = date_paiement; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public String getClient_nom() { return client_nom; }
    public void setClient_nom(String client_nom) { this.client_nom = client_nom; }
}
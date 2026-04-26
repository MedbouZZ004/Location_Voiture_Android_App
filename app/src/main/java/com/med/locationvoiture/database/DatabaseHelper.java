package com.med.locationvoiture.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.med.locationvoiture.models.Utilisateur;
import com.med.locationvoiture.models.Voiture;
import com.med.locationvoiture.models.Client;
import com.med.locationvoiture.models.Reservation;
import com.med.locationvoiture.models.Paiement;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "location_voiture.db";
    private static final int DATABASE_VERSION = 2;
    private static final String SALT = "LocationVoiture2024_SaltSecret!";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(SALT.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return hashPassword(plainPassword).equals(hashedPassword);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE utilisateurs (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, role TEXT)");
        db.execSQL("CREATE TABLE voitures (id INTEGER PRIMARY KEY AUTOINCREMENT, marque TEXT, modele TEXT, annee INTEGER, prix_jour REAL, disponible INTEGER DEFAULT 1, image_path TEXT, kilometrage INTEGER DEFAULT 0, couleur TEXT, latitude REAL DEFAULT 0, longitude REAL DEFAULT 0, adresse TEXT)");
        db.execSQL("CREATE TABLE clients (id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT, prenom TEXT, email TEXT UNIQUE, telephone TEXT, cin TEXT UNIQUE, adresse TEXT, date_naissance TEXT)");
        db.execSQL("CREATE TABLE reservations (id INTEGER PRIMARY KEY AUTOINCREMENT, client_id INTEGER, voiture_id INTEGER, date_debut TEXT, date_fin TEXT, prix_total REAL, statut TEXT DEFAULT 'en_attente', created_at TEXT, contract_path TEXT)");
        db.execSQL("CREATE TABLE paiements (id INTEGER PRIMARY KEY AUTOINCREMENT, reservation_id INTEGER, montant REAL, date_paiement TEXT, mode TEXT, created_at TEXT)");
        db.execSQL("INSERT INTO utilisateurs (username, password, role) VALUES ('admin', '" + hashPassword("admin") + "', 'admin')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS paiements");
        db.execSQL("DROP TABLE IF EXISTS reservations");
        db.execSQL("DROP TABLE IF EXISTS clients");
        db.execSQL("DROP TABLE IF EXISTS voitures");
        db.execSQL("DROP TABLE IF EXISTS utilisateurs");
        onCreate(db);
    }

    public long insertUtilisateur(Utilisateur utilisateur) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", utilisateur.getUsername());
        values.put("password", utilisateur.getPassword());
        values.put("role", utilisateur.getRole());
        return db.insert("utilisateurs", null, values);
    }

    public Utilisateur getUtilisateur(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Utilisateur utilisateur = null;
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM utilisateurs WHERE username = ?", new String[]{username});
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String storedHash = cursor.getString(2);
                    if (verifyPassword(password, storedHash)) {
                        utilisateur = new Utilisateur();
                        utilisateur.setId(cursor.getInt(0));
                        utilisateur.setUsername(cursor.getString(1));
                        utilisateur.setPassword(storedHash);
                        utilisateur.setRole(cursor.getString(3));
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return utilisateur;
    }

    public Client getClientById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM clients WHERE id = ?", new String[]{String.valueOf(id)});
        Client client = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    client = new Client();
                    client.setId(cursor.getInt(0));
                    client.setNom(cursor.getString(1));
                    client.setPrenom(cursor.getString(2));
                    client.setEmail(cursor.getString(3));
                    client.setTelephone(cursor.getString(4));
                    client.setCin(cursor.getString(5));
                    client.setAdresse(cursor.getString(6));
                    client.setDate_naissance(cursor.getString(7));
                }
            } finally {
                cursor.close();
            }
        }
        return client;
    }

    public Voiture getVoitureById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM voitures WHERE id = ?", new String[]{String.valueOf(id)});
        Voiture v = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    v = new Voiture();
                    v.setId(cursor.getInt(0));
                    v.setMarque(cursor.getString(1));
                    v.setModele(cursor.getString(2));
                    v.setAnnee(cursor.getInt(3));
                    v.setPrix_jour(cursor.getDouble(4));
                    v.setDisponible(cursor.getInt(5) == 1);
                    v.setImage_path(cursor.getString(6));
                    v.setKilometrage(cursor.getInt(7));
                    v.setCouleur(cursor.getString(8));
                    v.setLatitude(cursor.getDouble(9));
                    v.setLongitude(cursor.getDouble(10));
                    v.setAdresse(cursor.getString(11));
                }
            } finally {
                cursor.close();
            }
        }
        return v;
    }

    public String getDateFinLocation(int voitureId) {
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = sdf.format(new Date());
        android.database.Cursor cursor = db.rawQuery(
            "SELECT date_fin FROM reservations WHERE voiture_id = ? AND date_fin >= ? ORDER BY date_fin ASC LIMIT 1",
            new String[]{String.valueOf(voitureId), today});
        String dateFin = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    dateFin = cursor.getString(0);
                }
            } finally {
                cursor.close();
            }
        }
        return dateFin;
    }

    public int getOrCreateClientId(int userId, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT id FROM clients WHERE cin = ?", new String[]{"USER_" + userId});
        if (cursor.moveToFirst()) {
            int clientId = cursor.getInt(0);
            cursor.close();
            return clientId;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("nom", username != null ? username : "Client");
        values.put("prenom", "");
        values.put("email", (username != null ? username : "user") + userId + "@local.com");
        values.put("telephone", "");
        values.put("cin", "USER_" + userId);
        values.put("adresse", "");
        values.put("date_naissance", "");
        long id = db.insert("clients", null, values);
        if (id == -1) {
            cursor = db.rawQuery("SELECT id FROM clients WHERE cin = ?", new String[]{"USER_" + userId});
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
            cursor.close();
        }
        return (int) id;
    }

    public Reservation getReservationById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM reservations WHERE id = ?", new String[]{String.valueOf(id)});
        Reservation r = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    r = new Reservation();
                    r.setId(cursor.getInt(0));
                    r.setClient_id(cursor.getInt(1));
                    r.setVoiture_id(cursor.getInt(2));
                    r.setDate_debut(cursor.getString(3));
                    r.setDate_fin(cursor.getString(4));
                    r.setPrix_total(cursor.getDouble(5));
                    r.setStatut(cursor.getString(6));
                }
            } finally {
                cursor.close();
            }
        }
        return r;
    }

    public List<Paiement> getPaiementsByClientId(int userId) {
        List<Paiement> paiements = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        int clientId = getOrCreateClientId(userId, "");
        android.database.Cursor cursor = db.rawQuery(
            "SELECT p.* FROM paiements p INNER JOIN reservations r ON p.reservation_id = r.id WHERE r.client_id = ?",
            new String[]{String.valueOf(clientId)});
        while (cursor.moveToNext()) {
            Paiement p = new Paiement();
            p.setId(cursor.getInt(0));
            p.setReservation_id(cursor.getInt(1));
            p.setMontant(cursor.getDouble(2));
            p.setDate_paiement(cursor.getString(3));
            p.setMode(cursor.getString(4));
            paiements.add(p);
        }
        cursor.close();
        return paiements;
    }

    public long insertClient(Client client) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nom", client.getNom());
        values.put("prenom", client.getPrenom());
        values.put("email", client.getEmail());
        values.put("telephone", client.getTelephone());
        values.put("cin", client.getCin());
        values.put("adresse", client.getAdresse());
        values.put("date_naissance", client.getDate_naissance());
        return db.insert("clients", null, values);
    }

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM clients", null);
        while (cursor.moveToNext()) {
            Client c = new Client();
            c.setId(cursor.getInt(0));
            c.setNom(cursor.getString(1));
            c.setPrenom(cursor.getString(2));
            c.setEmail(cursor.getString(3));
            c.setTelephone(cursor.getString(4));
            c.setCin(cursor.getString(5));
            c.setAdresse(cursor.getString(6));
            c.setDate_naissance(cursor.getString(7));
            clients.add(c);
        }
        cursor.close();
        return clients;
    }

    public int updateClient(Client client) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nom", client.getNom());
        values.put("prenom", client.getPrenom());
        values.put("email", client.getEmail());
        values.put("telephone", client.getTelephone());
        values.put("cin", client.getCin());
        values.put("adresse", client.getAdresse());
        values.put("date_naissance", client.getDate_naissance());
        return db.update("clients", values, "id = ?", new String[]{String.valueOf(client.getId())});
    }

    public int deleteClient(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("clients", "id = ?", new String[]{String.valueOf(id)});
    }

    public long insertVoiture(Voiture voiture) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("marque", voiture.getMarque());
        values.put("modele", voiture.getModele());
        values.put("annee", voiture.getAnnee());
        values.put("prix_jour", voiture.getPrix_jour());
        values.put("disponible", voiture.isDisponible() ? 1 : 0);
        values.put("image_path", voiture.getImage_path());
        values.put("kilometrage", voiture.getKilometrage());
        values.put("couleur", voiture.getCouleur());
        values.put("latitude", voiture.getLatitude());
        values.put("longitude", voiture.getLongitude());
        values.put("adresse", voiture.getAdresse());
        return db.insert("voitures", null, values);
    }

    public int updateVoiture(Voiture voiture) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("marque", voiture.getMarque());
        values.put("modele", voiture.getModele());
        values.put("annee", voiture.getAnnee());
        values.put("prix_jour", voiture.getPrix_jour());
        values.put("disponible", voiture.isDisponible() ? 1 : 0);
        values.put("image_path", voiture.getImage_path());
        values.put("kilometrage", voiture.getKilometrage());
        values.put("couleur", voiture.getCouleur());
        values.put("latitude", voiture.getLatitude());
        values.put("longitude", voiture.getLongitude());
        values.put("adresse", voiture.getAdresse());
        return db.update("voitures", values, "id = ?", new String[]{String.valueOf(voiture.getId())});
    }

    public int deleteVoiture(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("voitures", "id = ?", new String[]{String.valueOf(id)});
    }

    public long insertReservation(Reservation reservation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("client_id", reservation.getClient_id());
        values.put("voiture_id", reservation.getVoiture_id());
        values.put("date_debut", reservation.getDate_debut());
        values.put("date_fin", reservation.getDate_fin());
        values.put("prix_total", reservation.getPrix_total());
        values.put("statut", reservation.getStatut());
        values.put("created_at", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(new java.util.Date()));
        long id = db.insert("reservations", null, values);
        ContentValues cv = new ContentValues();
        cv.put("disponible", 0);
        db.update("voitures", cv, "id = ?", new String[]{String.valueOf(reservation.getVoiture_id())});
        return id;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery(
            "SELECT r.*, c.nom, c.prenom, v.marque, v.modele FROM reservations r " +
            "LEFT JOIN clients c ON r.client_id = c.id " +
            "LEFT JOIN voitures v ON r.voiture_id = v.id " +
            "ORDER BY r.id DESC", null);
        if (cursor != null) {
            try {
while (cursor.moveToNext()) {
                    Reservation r = new Reservation();
                    r.setId(cursor.getInt(0));
                    r.setClient_id(cursor.getInt(1));
                    r.setVoiture_id(cursor.getInt(2));
                    r.setDate_debut(cursor.getString(3));
                    r.setDate_fin(cursor.getString(4));
                    r.setPrix_total(cursor.getDouble(5));
                    r.setStatut(cursor.getString(6));
                    r.setCreated_at(cursor.getString(7));
                    String contractPath = cursor.getString(12);
                    r.setContract_path(contractPath);
                    String clientName = cursor.getString(8);
                    String clientPrenom = cursor.getString(9);
                    r.setClient_nom(clientName + (clientPrenom != null && !clientPrenom.isEmpty() ? " " + clientPrenom : ""));
                    String marque = cursor.getString(10);
                    String modele = cursor.getString(11);
                    r.setVoiture_nom(marque + " " + modele);
                    reservations.add(r);
                }
            } finally {
                cursor.close();
            }
        }
        return reservations;
    }

    public int updateReservation(Reservation reservation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("client_id", reservation.getClient_id());
        values.put("voiture_id", reservation.getVoiture_id());
        values.put("date_debut", reservation.getDate_debut());
        values.put("date_fin", reservation.getDate_fin());
        values.put("prix_total", reservation.getPrix_total());
        values.put("statut", reservation.getStatut());
        if (reservation.getContract_path() != null) {
            values.put("contract_path", reservation.getContract_path());
        }
        return db.update("reservations", values, "id = ?", new String[]{String.valueOf(reservation.getId())});
    }

    public long insertPaiement(Paiement paiement) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("reservation_id", paiement.getReservation_id());
        values.put("montant", paiement.getMontant());
        values.put("date_paiement", paiement.getDate_paiement());
        values.put("mode", paiement.getMode());
        values.put("created_at", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(new java.util.Date()));
        return db.insert("paiements", null, values);
    }

    public List<Paiement> getAllPaiements() {
        List<Paiement> paiements = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery(
            "SELECT p.*, c.nom, c.prenom FROM paiements p " +
            "LEFT JOIN reservations r ON p.reservation_id = r.id " +
            "LEFT JOIN clients c ON r.client_id = c.id " +
            "ORDER BY p.id DESC", null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    Paiement p = new Paiement();
                    p.setId(cursor.getInt(0));
                    p.setReservation_id(cursor.getInt(1));
                    p.setMontant(cursor.getDouble(2));
                    p.setDate_paiement(cursor.getString(3));
                    p.setMode(cursor.getString(4));
                    p.setCreated_at(cursor.getString(5));
                    String clientName = cursor.getString(6);
                    String clientPrenom = cursor.getString(7);
                    p.setClient_nom(clientName + (clientPrenom != null && !clientPrenom.isEmpty() ? " " + clientPrenom : ""));
                    paiements.add(p);
                }
            } finally {
                cursor.close();
            }
        }
        return paiements;
    }

    public int[] getStatistiques() {
        SQLiteDatabase db = this.getReadableDatabase();
        int[] stats = new int[5];
        android.database.Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM voitures WHERE disponible = 1", null);
        if (c1.moveToFirst()) stats[0] = c1.getInt(0);
        c1.close();
        android.database.Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM voitures", null);
        if (c2.moveToFirst()) stats[1] = c2.getInt(0);
        c2.close();
        android.database.Cursor c3 = db.rawQuery("SELECT COUNT(*) FROM reservations WHERE statut IN ('en_attente', 'en_cours')", null);
        if (c3.moveToFirst()) stats[2] = c3.getInt(0);
        c3.close();
        android.database.Cursor c4 = db.rawQuery("SELECT COALESCE(SUM(montant), 0) FROM paiements", null);
        if (c4.moveToFirst()) stats[3] = c4.getInt(0);
        c4.close();
        android.database.Cursor c5 = db.rawQuery("SELECT COUNT(*) FROM clients", null);
        if (c5.moveToFirst()) stats[4] = c5.getInt(0);
        c5.close();
        return stats;
    }

    public List<Voiture> getAllVoitures() {
        List<Voiture> voitures = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM voitures", null);
        while (cursor.moveToNext()) {
            Voiture v = new Voiture();
            v.setId(cursor.getInt(0));
            v.setMarque(cursor.getString(1));
            v.setModele(cursor.getString(2));
            v.setAnnee(cursor.getInt(3));
            v.setPrix_jour(cursor.getDouble(4));
            v.setDisponible(cursor.getInt(5) == 1);
            v.setImage_path(cursor.getString(6));
            v.setKilometrage(cursor.getInt(7));
            v.setCouleur(cursor.getString(8));
            v.setLatitude(cursor.getDouble(9));
            v.setLongitude(cursor.getDouble(10));
            v.setAdresse(cursor.getString(11));
            voitures.add(v);
        }
        cursor.close();
        return voitures;
    }

    public List<Voiture> getVoituresDisponibles() {
        List<Voiture> voitures = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM voitures WHERE disponible = 1", null);
        while (cursor.moveToNext()) {
            Voiture v = new Voiture();
            v.setId(cursor.getInt(0));
            v.setMarque(cursor.getString(1));
            v.setModele(cursor.getString(2));
            v.setAnnee(cursor.getInt(3));
            v.setPrix_jour(cursor.getDouble(4));
            v.setDisponible(cursor.getInt(5) == 1);
            v.setImage_path(cursor.getString(6));
            v.setKilometrage(cursor.getInt(7));
            v.setCouleur(cursor.getString(8));
            v.setLatitude(cursor.getDouble(9));
            v.setLongitude(cursor.getDouble(10));
            v.setAdresse(cursor.getString(11));
            voitures.add(v);
        }
        cursor.close();
        return voitures;
    }
}
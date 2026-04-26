package com.med.locationvoiture.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfGenerator {

    private static File getWrapperFolder() {
        File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File wrapperDir = new File(docsDir, "Wrapper");
        if (!wrapperDir.exists()) {
            wrapperDir.mkdirs();
        }
        return wrapperDir;
    }

    public static byte[] generateReservationContractBytes(int reservationId, String clientName, String clientEmail, String clientCin,
                                                      String marque, String modele, String dateDebut, String dateFin, double prixTotal) {
        try {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            
            android.graphics.Canvas canvas = page.getCanvas();
            
            int y = 50;
            int lineaHeight = 25;
            
            canvas.drawText("CONTRAT DE LOCATION DE VOITURE", 150, y, new android.graphics.Paint());
            y += 50;
            
            canvas.drawText("Agence Location Voitures", 250, y, new android.graphics.Paint());
            y += lineaHeight;
            canvas.drawText("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(new Date()), 50, y, new android.graphics.Paint());
            y += 50;
            
            canvas.drawText("═══════════════════════════════════════════════════════", 50, y, new android.graphics.Paint());
            y += 40;
            
            canvas.drawText("RÉSERVATION #" + reservationId, 250, y, new android.graphics.Paint());
            y += 40;
            
            canvas.drawText("INFORMATIONS CLIENT", 50, y, new android.graphics.Paint());
            y += 30;
            canvas.drawText("Nom: " + clientName, 50, y, new android.graphics.Paint());
            y += lineaHeight;
            canvas.drawText("Email: " + clientEmail, 50, y, new android.graphics.Paint());
            y += lineaHeight;
            canvas.drawText("CIN: " + clientCin, 50, y, new android.graphics.Paint());
            y += 40;
            
            canvas.drawText("INFORMATIONS VOITURE", 50, y, new android.graphics.Paint());
            y += 30;
            canvas.drawText("Véhicule: " + marque + " " + modele, 50, y, new android.graphics.Paint());
            y += 40;
            
            canvas.drawText("DATES DE LOCATION", 50, y, new android.graphics.Paint());
            y += 30;
            canvas.drawText("Date de début: " + dateDebut, 50, y, new android.graphics.Paint());
            y += lineaHeight;
            canvas.drawText("Date de fin: " + dateFin, 50, y, new android.graphics.Paint());
            y += 40;
            
            canvas.drawText("TARIF", 50, y, new android.graphics.Paint());
            y += 30;
            canvas.drawText("Prix total: " + String.format("%.2f", prixTotal) + " €", 50, y, new android.graphics.Paint());
            y += 50;
            
            canvas.drawText("═══════════════════════════════════════════════════════", 50, y, new android.graphics.Paint());
            y += 40;
            
            canvas.drawText(" Signature du client: _______________________", 50, y, new android.graphics.Paint());
            y += 60;
            canvas.drawText(" Signature de l'agence: _______________________", 350, y, new android.graphics.Paint());
            
            pdfDocument.finishPage(page);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            pdfDocument.writeTo(outputStream);
            pdfDocument.close();
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

public static byte[] generateFacturePDFBytes(int reservationId, String clientName, String clientEmail, String datePaiement, double montant) {
        try {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            
            android.graphics.Canvas canvas = page.getCanvas();
            android.graphics.Paint paint = new android.graphics.Paint();
            android.graphics.Paint boldPaint = new android.graphics.Paint();
            boldPaint.setFakeBoldText(true);
            
            int y = 50;
            int lineHeight = 25;
            
            canvas.drawText("FACTURE", 270, y, boldPaint);
            y += 40;
            
            canvas.drawText("Agence Location Voitures", 250, y, paint);
            y += lineHeight;
            canvas.drawText("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(new Date()), 50, y, paint);
            y += 50;
            
            canvas.drawText("═══════════════════════════════════════════════════════", 50, y, paint);
            y += 40;
            
            canvas.drawText("FACTURE #" + reservationId, 250, y, boldPaint);
            y += 40;
            
            canvas.drawText("INFORMATIONS CLIENT", 50, y, boldPaint);
            y += 30;
            canvas.drawText("Nom: " + clientName, 50, y, paint);
            y += lineHeight;
            canvas.drawText("Email: " + (clientEmail != null ? clientEmail : "N/A"), 50, y, paint);
            y += 40;
            
            canvas.drawText("DÉTAILS PAIEMENT", 50, y, boldPaint);
            y += 30;
            canvas.drawText("Réservation #: " + reservationId, 50, y, paint);
            y += lineHeight;
            canvas.drawText("Date paiement: " + datePaiement, 50, y, paint);
            y += 40;
            
            canvas.drawText("───────────────────────────────────────────────────────", 50, y, paint);
            y += 40;
            
            canvas.drawText("MONTANT TOTAL: " + String.format("%.2f", montant) + " €", 200, y, boldPaint);
            y += 50;
            
            canvas.drawText("═══════════════════════════════════════════════════════", 50, y, paint);
            y += 40;
            
            canvas.drawText("Merci pour votre confiance!", 200, y, paint);
            
            pdfDocument.finishPage(page);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            pdfDocument.writeTo(outputStream);
            pdfDocument.close();
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void shareFacture(Context context, int reservationId, String clientName, String clientEmail, String datePaiement, double montant) {
        byte[] pdfBytes = generateFacturePDFBytes(reservationId, clientName, clientEmail, datePaiement, montant);
        if (pdfBytes == null) {
            Toast.makeText(context, "Erreur génération PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            File cacheDir = context.getCacheDir();
            File pdfFile = new File(cacheDir, "facture_" + reservationId + ".pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(pdfBytes);
            fos.close();
            
            android.net.Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Facture - Réservation #" + reservationId);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Facture pour la réservation #" + reservationId + "\nClient: " + clientName);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(Intent.createChooser(shareIntent, "Enregistrer/Télécharger la facture"));
            
        } catch (Exception e) {
            Toast.makeText(context, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareContrat(Context context, int reservationId, String clientName, String clientEmail, String clientCin,
                             String marque, String modele, String dateDebut, String dateFin, double prixTotal) {
        byte[] pdfBytes = generateReservationContractBytes(reservationId, clientName, clientEmail, clientCin, marque, modele, dateDebut, dateFin, prixTotal);
        if (pdfBytes == null) {
            Toast.makeText(context, "Erreur génération PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            File cacheDir = context.getCacheDir();
            File pdfFile = new File(cacheDir, "contrat_" + reservationId + ".pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(pdfBytes);
            fos.close();
            
            android.net.Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Contrat de location - Réservation #" + reservationId);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Contrat de location pour la réservation #" + reservationId + "\nClient: " + clientName);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(Intent.createChooser(shareIntent, "Enregistrer/Télécharger le contrat"));
            
        } catch (Exception e) {
            Toast.makeText(context, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Deprecated
    public static String generateReservationContract(Context context, int reservationId, String clientName, String clientEmail, String clientCin, 
                                                      String marque, String modele, String dateDebut, String dateFin, double prixTotal) {
        byte[] bytes = generateReservationContractBytes(reservationId, clientName, clientEmail, clientCin, marque, modele, dateDebut, dateFin, prixTotal);
        if (bytes == null) return null;
        
        try {
            String fileName = "contrat_location_" + reservationId + "_" + System.currentTimeMillis() + ".pdf";
            File folder = getWrapperFolder();
            File file = new File(folder, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Deprecated
    public static String generateFacturePDF(Context context, int reservationId, String clientName, 
                                             String clientEmail, String datePaiement, double montant) {
        byte[] bytes = generateFacturePDFBytes(reservationId, clientName, clientEmail, datePaiement, montant);
        if (bytes == null) return null;
        
        try {
            String fileName = "facture_" + reservationId + "_" + System.currentTimeMillis() + ".pdf";
            File folder = getWrapperFolder();
            File file = new File(folder, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
package com.med.locationvoiture.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfGenerator {

    public static String generateReservationContract(Context context, int reservationId, String clientName, String clientEmail, String clientCin, 
                                                      String marque, String modele, String dateDebut, String dateFin, double prixTotal) {
        try {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
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
            
            String fileName = "contrat_location_" + reservationId + "_" + System.currentTimeMillis() + ".pdf";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
            
            return file.getAbsolutePath();
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
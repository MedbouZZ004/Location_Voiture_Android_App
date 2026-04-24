package com.med.locationvoiture.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.med.locationvoiture.R;
import com.med.locationvoiture.activities.LoginActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "location_voiture_channel";
    private static final String CHANNEL_NAME = "Notifications Location";

    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications pour les réservations et paiements");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void notifyReservationCreated(Context context, String clientName, String voiture) {
        showNotification(context, "Nouvelle réservation", "Client: " + clientName + "\nVoiture: " + voiture);
    }

    public static void notifyReservationValidated(Context context) {
        showNotification(context, "Réservation validée", "Votre réservation a été validée! Vous pouvez procéder au paiement.");
    }

    public static void notifyPaymentReceived(Context context, String clientName, String montant) {
        showNotification(context, "Paiement reçu", "Client: " + clientName + "\nMontant: " + montant);
    }

    public static void notifyPaymentValidated(Context context) {
        showNotification(context, "Paiement confirmé", "Votre paiement a été validé! Votre location est confirmée.");
    }
}
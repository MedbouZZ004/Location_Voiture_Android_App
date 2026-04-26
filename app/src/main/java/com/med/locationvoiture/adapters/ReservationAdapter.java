package com.med.locationvoiture.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import com.med.locationvoiture.R;
import com.med.locationvoiture.models.Reservation;
import java.io.File;
import java.util.List;

public class ReservationAdapter extends BaseAdapter {
    private Context context;
    private List<Reservation> reservations;

    public ReservationAdapter(Context context, List<Reservation> reservations) {
        this.context = context;
        this.reservations = reservations != null ? reservations : new java.util.ArrayList<>();
    }

    @Override
    public int getCount() { return reservations != null ? reservations.size() : 0; }
    @Override
    public Object getItem(int position) { return reservations != null ? reservations.get(position) : null; }
    @Override
    public long getItemId(int position) { return reservations != null && position < reservations.size() ? reservations.get(position).getId() : 0; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (reservations == null || position >= reservations.size()) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false);
        }
        Reservation r = reservations.get(position);
        if (r == null) return convertView;
        
        String clientName = r.getClient_nom() != null ? r.getClient_nom() : "Client #" + r.getClient_id();
        String voitureName = r.getVoiture_nom() != null ? r.getVoiture_nom() : "Voiture #" + r.getVoiture_id();
        
        TextView tvDetails = convertView.findViewById(R.id.tvDetails);
        TextView tvDates = convertView.findViewById(R.id.tvDates);
        TextView tvPrix = convertView.findViewById(R.id.tvPrix);
        TextView tvStatut = convertView.findViewById(R.id.tvStatut);
        
        if (tvDetails != null) tvDetails.setText(clientName + " - " + voitureName);
        if (tvDates != null) tvDates.setText(r.getDate_debut() + " -> " + r.getDate_fin());
        if (tvPrix != null) tvPrix.setText(String.format("%.2f €", r.getPrix_total()));
        if (tvStatut != null) tvStatut.setText(r.getStatut() + (r.getCreated_at() != null ? "\n" + r.getCreated_at() : ""));
        
        Button btnDownload = convertView.findViewById(R.id.btnDownloadContract);
        
        if ("confirmee".equals(r.getStatut()) && r.getContract_path() != null && !r.getContract_path().isEmpty()) {
            if (btnDownload != null) {
                btnDownload.setVisibility(View.VISIBLE);
                btnDownload.setOnClickListener(v -> {
                    try {
                        File file = new File(r.getContract_path());
                        if (file.exists()) {
                            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/pdf");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            if (btnDownload != null) btnDownload.setVisibility(View.GONE);
        }
        
        return convertView;
    }
}
package com.med.locationvoiture.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.med.locationvoiture.R;
import com.med.locationvoiture.models.Reservation;
import java.util.List;

public class ReservationAdapter extends BaseAdapter {
    private Context context;
    private List<Reservation> reservations;

    public ReservationAdapter(Context context, List<Reservation> reservations) {
        this.context = context;
        this.reservations = reservations;
    }

    @Override
    public int getCount() { return reservations.size(); }
    @Override
    public Object getItem(int position) { return reservations.get(position); }
    @Override
    public long getItemId(int position) { return reservations.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false);
        }
        Reservation r = reservations.get(position);
        String clientName = r.getClient_nom() != null ? r.getClient_nom() : "Client #" + r.getClient_id();
        String voitureName = r.getVoiture_nom() != null ? r.getVoiture_nom() : "Voiture #" + r.getVoiture_id();
        ((TextView) convertView.findViewById(R.id.tvDetails)).setText(clientName + " - " + voitureName);
        ((TextView) convertView.findViewById(R.id.tvDates)).setText(r.getDate_debut() + " -> " + r.getDate_fin());
        ((TextView) convertView.findViewById(R.id.tvPrix)).setText(String.format("%.2f €", r.getPrix_total()));
        ((TextView) convertView.findViewById(R.id.tvStatut)).setText(r.getStatut() + (r.getCreated_at() != null ? "\n" + r.getCreated_at() : ""));
        return convertView;
    }
}
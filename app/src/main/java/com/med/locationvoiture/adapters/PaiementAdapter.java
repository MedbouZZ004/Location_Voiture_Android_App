package com.med.locationvoiture.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.med.locationvoiture.R;
import com.med.locationvoiture.models.Paiement;
import java.util.List;

public class PaiementAdapter extends BaseAdapter {
    private Context context;
    private List<Paiement> paiements;

    public PaiementAdapter(Context context, List<Paiement> paiements) {
        this.context = context;
        this.paiements = paiements;
    }

    @Override
    public int getCount() { return paiements.size(); }
    @Override
    public Object getItem(int position) { return paiements.get(position); }
    @Override
    public long getItemId(int position) { return paiements.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_paiement, parent, false);
        }
        Paiement p = paiements.get(position);
        String clientName = p.getClient_nom() != null ? p.getClient_nom() : "Inconnu";
        ((TextView) convertView.findViewById(R.id.tvDetails)).setText(clientName + " - Réservation #" + p.getReservation_id());
        ((TextView) convertView.findViewById(R.id.tvMontant)).setText(String.format("%.2f €", p.getMontant()));
        String dateInfo = p.getCreated_at() != null ? p.getCreated_at() : p.getDate_paiement();
        ((TextView) convertView.findViewById(R.id.tvDate)).setText(dateInfo + " - " + p.getMode());
        return convertView;
    }
}
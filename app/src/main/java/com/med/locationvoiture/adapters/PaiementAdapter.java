package com.med.locationvoiture.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.med.locationvoiture.R;
import com.med.locationvoiture.models.Paiement;
import com.med.locationvoiture.utils.PdfGenerator;
import java.util.List;

public class PaiementAdapter extends BaseAdapter {
    private Context context;
    private List<Paiement> paiements;
    private OnPaiementClickListener listener;
    private boolean isAdmin;
    private static final String TAG = "PaiementAdapter";

    public interface OnPaiementClickListener {
        void onValiderClicked(Paiement paiement);
        void onTelechargerClicked(Paiement paiement);
    }

    public PaiementAdapter(Context context, List<Paiement> paiements, boolean isAdmin) {
        this.context = context;
        this.paiements = paiements != null ? paiements : new java.util.ArrayList<>();
        this.isAdmin = isAdmin;
    }

    public void setOnPaiementClickListener(OnPaiementClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() { return paiements != null ? paiements.size() : 0; }
    @Override
    public Object getItem(int position) { return paiements != null ? paiements.get(position) : null; }
    @Override
    public long getItemId(int position) { return paiements != null && position < paiements.size() ? paiements.get(position).getId() : 0; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (paiements == null || position >= paiements.size()) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_paiement, parent, false);
        }
        Paiement p = paiements.get(position);
        if (p == null) return convertView;
        
        TextView tvDetails = convertView.findViewById(R.id.tvDetails);
        TextView tvMontant = convertView.findViewById(R.id.tvMontant);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        Button btnValider = convertView.findViewById(R.id.btnValider);
        Button btnTelecharger = convertView.findViewById(R.id.btnTelechargerFacture);
        
        if (tvDetails != null) {
            String clientName = p.getClient_nom() != null ? p.getClient_nom() : "Inconnu";
            tvDetails.setText(clientName + " - Réservation #" + p.getReservation_id());
        }
        if (tvMontant != null) {
            tvMontant.setText(String.format("%.2f €", p.getMontant()));
        }
        if (tvDate != null) {
            String dateInfo = p.getCreated_at() != null ? p.getCreated_at() : p.getDate_paiement();
            tvDate.setText(dateInfo + " - " + p.getMode());
        }
        
        final Paiement paiement = p;
        
        if (btnValider != null) {
            btnValider.setTag(p);
            if (isAdmin) {
                btnValider.setVisibility(View.VISIBLE);
                btnValider.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Valider clicked for paiement: " + p.getId());
                        if (listener != null) {
                            listener.onValiderClicked(p);
                        }
                    }
                });
            } else {
                btnValider.setVisibility(View.GONE);
            }
        }
        
        if (btnTelecharger != null) {
            btnTelecharger.setTag(p);
            btnTelecharger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Telecharger clicked for paiement: " + p.getId());
                    PdfGenerator.shareFacture(
                        context,
                        p.getReservation_id(),
                        p.getClient_nom() != null ? p.getClient_nom() : "Client",
                        "",
                        p.getDate_paiement(),
                        p.getMontant()
                    );
                }
            });
        }
        return convertView;
    }
}
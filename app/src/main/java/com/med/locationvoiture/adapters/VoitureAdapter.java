package com.med.locationvoiture.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.med.locationvoiture.R;
import com.med.locationvoiture.models.Voiture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VoitureAdapter extends BaseAdapter {
    private static final String TAG = "VoitureAdapter";
    private Context context;
    private List<Voiture> items;
    private HashMap<Integer, String> rentedEndDates;

    public VoitureAdapter(Context context, List<Voiture> voitures) {
        this.context = context;
        this.items = voitures != null ? voitures : new ArrayList<>();
        this.rentedEndDates = new HashMap<>();
    }

    public VoitureAdapter(Context context, List<Voiture> list, HashMap<Integer, String> dates) {
        this.context = context;
        this.items = list != null ? list : new ArrayList<>();
        this.rentedEndDates = dates != null ? dates : new HashMap<>();
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            return items.get(position).getId();
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (items == null || position < 0 || position >= items.size()) {
            return convertView != null ? convertView : new View(context);
        }
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_voiture, parent, false);
        }
        
        try {
            Voiture v = items.get(position);
            
            ImageView ivPhoto = convertView.findViewById(R.id.ivVoiturePhoto);
            if (ivPhoto != null) {
                if (v.getImage_path() != null && !v.getImage_path().isEmpty()) {
                    ivPhoto.setImageURI(Uri.parse(v.getImage_path()));
                } else {
                    ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
            
            TextView tvMarque = convertView.findViewById(R.id.tvMarque);
            if (tvMarque != null) {
                tvMarque.setText(v.getMarque() + " " + v.getModele());
            }
            
            TextView tvDetails = convertView.findViewById(R.id.tvDetails);
            if (tvDetails != null) {
                tvDetails.setText(v.getAnnee() + " - " + v.getPrix_jour() + " €/jour");
            }
            
            TextView tvStatut = convertView.findViewById(R.id.tvStatut);
            
            if (tvStatut != null) {
                if (v.isDisponible()) {
                    tvStatut.setText("Disponible");
                    tvStatut.setTextColor(ContextCompat.getColor(context, R.color.status_available));
                } else {
                    tvStatut.setText("Loué(e)");
                    tvStatut.setTextColor(ContextCompat.getColor(context, R.color.status_rented));
                }
            }
            
            TextView tvAdresse = convertView.findViewById(R.id.tvAdresse);
            if (tvAdresse != null) {
                String address = v.getAdresse();
                if (address != null && !address.isEmpty()) {
                    tvAdresse.setText("📍 " + address);
                    tvAdresse.setVisibility(View.VISIBLE);
                } else {
                    tvAdresse.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getView", e);
        }
        
        return convertView;
    }
}
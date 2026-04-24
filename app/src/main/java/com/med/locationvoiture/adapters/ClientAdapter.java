package com.med.locationvoiture.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.med.locationvoiture.R;
import com.med.locationvoiture.models.Client;
import java.util.List;

public class ClientAdapter extends BaseAdapter {
    private Context context;
    private List<Client> clients;

    public ClientAdapter(Context context, List<Client> clients) {
        this.context = context;
        this.clients = clients;
    }

    @Override
    public int getCount() { return clients.size(); }
    @Override
    public Object getItem(int position) { return clients.get(position); }
    @Override
    public long getItemId(int position) { return clients.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_client, parent, false);
        }
        Client c = clients.get(position);
        ((TextView) convertView.findViewById(R.id.tvNom)).setText(c.getNom() + " " + c.getPrenom());
        ((TextView) convertView.findViewById(R.id.tvDetails)).setText(c.getEmail() + " - " + c.getTelephone());
        return convertView;
    }
}
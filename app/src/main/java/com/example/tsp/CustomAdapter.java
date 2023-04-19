package com.example.tsp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    Context context;
    List<Lugar> lstLugares;

    public CustomAdapter(Context context, List<Lugar> lstLugares) {
        this.context = context;
        this.lstLugares = lstLugares;
    }

    @Override
    public int getCount() {
        return lstLugares.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        TextView txtLugar;
        Lugar lugar = lstLugares.get(position);

        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.option_place_layout, null);

        txtLugar = view.findViewById(R.id.txtLugar);
        txtLugar.setText(lugar.getName());

        return null;
    }
}

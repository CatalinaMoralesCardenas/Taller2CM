package com.example.taller2.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.taller2.R;

public class ContactsAdapter extends CursorAdapter {
    public ContactsAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.contacts_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){
        int idNum = cursor.getInt(0);
        String name = cursor.getString(1);
        TextView tvId = view.findViewById(R.id.idContacto);
        TextView tvName = view.findViewById(R.id.nombreContacto);
        tvId.setText(String.valueOf(idNum));
        tvName.setText(String.valueOf(name));

    }
}

package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taller2.adapters.ContactsAdapter;

public class ContactsActivity extends AppCompatActivity {

    ListView listContacts;
    String[] projection;
    Cursor cursor;
    ContactsAdapter contactsAdapter;

    String permContacts = Manifest.permission.READ_CONTACTS;
    private static final int CONTACTS_PERMISSION_ID=5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        listContacts = findViewById(R.id.listContacts);
        contactsAdapter = new ContactsAdapter(this, null,0);
        listContacts.setAdapter(contactsAdapter);

        requestPermission(
                this,
                permContacts,
                "Se Necesita el Permiso para Mostrar los Contactos Disponibles",
                CONTACTS_PERMISSION_ID
        );
        initView();

    }

    private void initView(){
        if(ContextCompat.checkSelfPermission(this, permContacts)== PackageManager.PERMISSION_GRANTED) {
            projection = new String[]{
                    ContactsContract.Profile._ID, ContactsContract.Profile.DISPLAY_NAME_PRIMARY
            };
            cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,projection, null, null, null);
            contactsAdapter.changeCursor(cursor);
        }
        else{

        }
    }

    private void requestPermission(Activity context, String permission, String justification, int id){
        if(ContextCompat.checkSelfPermission(context, permission)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(context, permission)){
                Toast.makeText(context, justification, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(context, new String[]{permission}, id);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CONTACTS_PERMISSION_ID){
            initView();
        }
    }
}
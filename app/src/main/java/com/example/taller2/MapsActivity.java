package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.taller2.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Marker locationM;
    private Marker searchM;
    FusedLocationProviderClient clientLocation;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    //Permisos
    String permLocation = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_ID = 15;

    //Sensores
    SensorManager sensorManager;
    Sensor lightSensor;
    SensorEventListener lightSensorListener;

    //Settings
    private static final int SETTINGS_GPS = 20;

    //views
    EditText address;
    //Geocoder
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Sensores de Luminosidad
        initLightSensor();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationRequest = createLocationRequest();
        clientLocation = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.i(" LOCATION ", "Longitud: " + location.getLongitude());
                    Log.i(" LOCATION ", "Latitud: " + location.getLatitude());

                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    locationM = mMap.addMarker(new MarkerOptions().position(userLocation).title("Tu Ubicación"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

                }

            }

        };

        requestPermission(this, permLocation, "Needed", LOCATION_PERMISSION_ID);
        initView();

        geocoder = new Geocoder(this);
        address = findViewById(R.id.busqueda);
        address.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String direccion = address.getText().toString();
                    if (!direccion.isEmpty()) {
                        try {
                            List<Address> addresses = geocoder.getFromLocationName(direccion, 2);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address addressResult = addresses.get(0);
                                if (mMap != null) {
                                    if(searchM != null) {
                                        searchM.remove();
                                    }
                                    LatLng location = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                                    searchM = mMap.addMarker(new MarkerOptions().
                                            position(location).
                                            title(addressResult.getAddressLine(0)).
                                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

                                    Double latitude1 = searchM.getPosition().latitude;
                                    Double latitude2 = locationM.getPosition().latitude;
                                    Double longitude1 = searchM.getPosition().longitude;
                                    Double longitude2 = locationM.getPosition().longitude;
                                    Toast.makeText(MapsActivity.this, "La distancia es: " + distance(latitude1, longitude1, latitude2, longitude2) + " Km", Toast.LENGTH_SHORT).show();

                                }
                            } else {
                                Toast.makeText(v.getContext(), "Dirección No Encontrada", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });

    }

    private void initLightSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (mMap != null) {
                    if (event.values[0] < 5000) {
                        Log.i("MAPS", "DARK MAP " + event.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.darkmap));
                    }
                    else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.lightmap));
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    private void initView(){
        if(ContextCompat.checkSelfPermission(this, permLocation)== PackageManager.PERMISSION_GRANTED) {
            checkSettingsLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.lightmap));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(searchM != null)
                {
                    searchM.remove();
                }
                searchM=mMap.addMarker(new MarkerOptions().position(latLng).title(geoCoderSearchLatLang(latLng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                Double latitude1 = searchM.getPosition().latitude;
                Double latitude2 = locationM.getPosition().latitude;
                Double longitude1 = searchM.getPosition().longitude;
                Double longitude2 = locationM.getPosition().longitude;
                Toast.makeText(MapsActivity.this, "La distancia es: " + distance(latitude1, longitude1, latitude2, longitude2) + " Km", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(300000);
        locationRequest.setFastestInterval(200000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;

    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            clientLocation.requestLocationUpdates(locationRequest, locationCallback, null); }
    }

    private void stopLocationUpdates(){
        clientLocation.removeLocationUpdates(locationCallback);

    }

    //Subscripciones
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(lightSensorListener, lightSensor, sensorManager.SENSOR_DELAY_NORMAL);
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(lightSensorListener);
        stopLocationUpdates();
    }

    //Check de Setting de Ubicacion
    private void checkSettingsLocation(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                int statusCode = ((ApiException)e).getStatusCode();
                switch (statusCode){
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapsActivity.this,SETTINGS_GPS );
                        } catch (IntentSender.SendIntentException sendEx) {
                        } break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTINGS_GPS){
            startLocationUpdates();
        }
    }


    //Permisos
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
        if(requestCode == LOCATION_PERMISSION_ID){
            initView();
        }
    }

    private String geoCoderSearchLatLang(LatLng latLng) {
        String finalName="";
        try {
            Address name = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
            finalName = name.getAddressLine(0);
        }catch (IOException e){
            e.printStackTrace();
        }
        return finalName;
    }

    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = 6371 * c;
        return Math.round(result*100.0)/100.0;
    }
}
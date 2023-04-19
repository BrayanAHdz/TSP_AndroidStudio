package com.example.tsp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int AUTOCOMPLETE_REQUEST_CODE_ORIGIN = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;
    private boolean setOrigin = false;
    private EditText txtOrigen, txtLugar;
    private TextView tvDistancia, tvTiempo;
    private GoogleMap mMap;

    private Lugar origen, lugar;

    private HashMap<String, Marker> mMarkerMap = new HashMap<>();
    private List<Lugar> lstLugares = new ArrayList<>();

    private long disntacia = 0, tiempo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        origen = new Lugar();
        lugar = new Lugar();

        txtOrigen = findViewById(R.id.etBuscar);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvTiempo = findViewById(R.id.tvTiempo);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Places.initialize(getApplicationContext(), getString(R.string.API_KEY));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public void showBottomDialog(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showCenterDialog(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.center_sheet_layout, null);
        txtLugar = mView.findViewById(R.id.etLugar);
        final Button btnAddLugar = mView.findViewById(R.id.btnAddLugar);
        dialog.setView(mView);
        AlertDialog card = dialog.create();
        card.show();

        card.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        card.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        card.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        card.getWindow().setGravity(Gravity.CENTER);

        btnAddLugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lugar.getName() != null){
                    addMarker(lugar,2);
                    card.cancel();
                }
                else{
                    Toast.makeText(getApplicationContext(), "No se ha encontrado la ubicación.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng mexico = new LatLng(20.268445, -98.943285);

        Marker marker = mMap.addMarker(new MarkerOptions().position(mexico).title("México"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mexico));
        mMarkerMap.put("México", marker);

        removeMarker("México");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        FusedLocationProviderClient mFusedLocationProviderClient;

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                }
            });
        }
    }

    public void startAutocompleteOrigin(View view){
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_ORIGIN);
    }
    public void startAutocomplete(View view){
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_ORIGIN) {
            autocompleteRequest(requestCode,resultCode, data, txtOrigen);
            return;
        }
        else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            autocompleteRequest(requestCode,resultCode, data, txtLugar);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void autocompleteRequest(int requestCode,int resultCode, @Nullable Intent data, EditText txtPlace){
        if (resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            txtPlace.setText(place.getName());

            if(requestCode == AUTOCOMPLETE_REQUEST_CODE_ORIGIN){
                origen.setName(place.getName());
                origen.setLatitude(place.getLatLng().latitude);
                origen.setLongitude(place.getLatLng().longitude);
                origen.setLatLng(place.getLatLng());
                addMarker(origen, 1);
            }
            else{
                lugar = new Lugar();
                lugar.setName(place.getName());
                lugar.setLatitude(place.getLatLng().latitude);
                lugar.setLongitude(place.getLatLng().longitude);
                lugar.setLatLng(place.getLatLng());
            }
        }
        else if (resultCode == AutocompleteActivity.RESULT_ERROR) {}
        else if (resultCode == RESULT_CANCELED) {}
    }

    private void addMarker(Lugar ubicacion, int tipo){

        String name = ubicacion.getName();
        LatLng place = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());
        BitmapDescriptor bitmapDescriptor;

        if(tipo == 1)
            bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        else
            bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(place)
                .title(name)
                .icon(bitmapDescriptor));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(place));

        if(tipo == 1){
            if(setOrigin){
                replaceMarker("origen");
                mMarkerMap.replace("origen",marker);
            }
            else{
                mMarkerMap.put("origen", marker);
                setOrigin = true;
            }
        }
        else{
            mMarkerMap.put(name, marker);
            lstLugares.add(ubicacion);
        }

        generateRute();
    }

    private void removeMarker(String markerId){
        Marker marker = mMarkerMap.get(markerId);
        if (marker != null) {
            marker.remove();
            mMarkerMap.remove(markerId);
        }
    }

    private void replaceMarker(String markerId){
        Marker marker = mMarkerMap.get(markerId);
        if (marker != null)
            marker.remove();
    }

    private void generateRute(){
        if(origen.getName() == null || lstLugares.size() == 0) return;

        mMap.clear();
        disntacia = 0;
        tiempo = 0;

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        mMap.addMarker(new MarkerOptions()
                .position(origen.getLatLng())
                .title(origen.getName())
                .icon(bitmapDescriptor));

        bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);

        for (int i = 0; i < lstLugares.size(); i++){
            mMap.addMarker(new MarkerOptions()
                    .position(lstLugares.get(i).getLatLng())
                    .title(lstLugares.get(i).getName())
                    .icon(bitmapDescriptor));
        }

        if(lstLugares.size() == 1){
            GetDirectionsTask getDirectionsTask = new GetDirectionsTask( origen.getLatLng(), lstLugares.get(0).getLatLng(), mMap, this);
            getDirectionsTask.execute();

            getDirectionsTask = new GetDirectionsTask( lstLugares.get(0).getLatLng(), origen.getLatLng(), mMap, this);
            getDirectionsTask.execute();

        }
        else if(lstLugares.size() == 2){
            GetDirectionsTask getDirectionsTask = new GetDirectionsTask(origen.getLatLng(), lstLugares.get(0).getLatLng(), mMap, this);
            getDirectionsTask.execute();

            getDirectionsTask = new GetDirectionsTask( lstLugares.get(0).getLatLng(), lstLugares.get(1).getLatLng(), mMap, this);
            getDirectionsTask.execute();

            getDirectionsTask = new GetDirectionsTask( lstLugares.get(1).getLatLng(), origen.getLatLng(), mMap, this);
            getDirectionsTask.execute();
        }
        else{

            TSP tsp = new TSP();
            LatLng ruta[] = tsp.getTSP(origen, lstLugares);

            GetDirectionsTask getDirectionsTask;
            for (int i = 0; i < ruta.length - 1; i++){
                getDirectionsTask = new GetDirectionsTask(ruta[i], ruta[i+1], mMap, this);
                getDirectionsTask.execute();
            }
            getDirectionsTask = new GetDirectionsTask(ruta[ruta.length - 1], ruta[0], mMap, this);
            getDirectionsTask.execute();
        }
    }

    public void showDistanceAndTime(long distance, long time){
        disntacia += distance;
        tiempo += time;

        tvDistancia.setText(disntacia/1000 + " km");
        tvTiempo.setText(tiempo / 60 + " min");
    }
}

class GetDirectionsTask extends AsyncTask<Void, Void, DirectionsResult> {

    private LatLng origin;
    private LatLng destination;
    private MainActivity mainActivity;
    GoogleMap mMap;

    public GetDirectionsTask(LatLng origin, LatLng destination, GoogleMap mMap, MainActivity mainActivity) {
        this.origin = origin;
        this.destination = destination;
        this.mMap = mMap;
        this.mainActivity = mainActivity;
    }

    @Override
    protected DirectionsResult doInBackground(Void... params) {
        try {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBDyw4G9UJTW_BYGFi8VtlQQGXq1rmglqQ")
                    .build();

            DirectionsApiRequest request = DirectionsApi.newRequest(context)
                    .origin(cLatLong(origin))
                    .destination(cLatLong(destination));
            return request.await();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(DirectionsResult result) {
        if (result != null) {
            PolylineOptions polylineOptions = new PolylineOptions();
            for (DirectionsStep step : result.routes[0].legs[0].steps) {
                for (com.google.maps.model.LatLng latLng : step.polyline.decodePath()) {
                    polylineOptions.add(new LatLng(latLng.lat,latLng.lng));
                }
            }
            mMap.addPolyline(polylineOptions);

            long distance = result.routes[0].legs[0].distance.inMeters;
            long time = result.routes[0].legs[0].duration.inSeconds;
            mainActivity.showDistanceAndTime(distance,time);
        }
    }

    private com.google.maps.model.LatLng cLatLong(LatLng latLng){
        return new com.google.maps.model.LatLng(latLng.latitude, latLng.longitude);
    }
}


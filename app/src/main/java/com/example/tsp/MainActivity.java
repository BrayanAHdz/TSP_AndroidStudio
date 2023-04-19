package com.example.tsp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int AUTOCOMPLETE_REQUEST_CODE_ORIGIN = 1;
    private TextView tvDistancia, tvTiempo;
    private GoogleMap mMap;

    private final HashMap<String, Marker> mMarkerMap = new HashMap<>();
    private final List<Lugar> lstLugares = new ArrayList<>();

    private long disntacia = 0, tiempo = 0;
    private  AlertDialog card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDistancia = findViewById(R.id.tvDistancia);
        tvTiempo = findViewById(R.id.tvTiempo);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Places.initialize(getApplicationContext(), getString(R.string.API_KEY));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public void showBottomDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);

        ListView listView = mView.findViewById(R.id.lvLugares);
        List<String> dataList = new ArrayList<>();
        for(int i = 0; i < lstLugares.size(); i++)
            dataList.add(lstLugares.get(i).getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        builder.setView(mView);
        AlertDialog dialog = builder.create();

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        listView.setOnItemLongClickListener((parent, view1, position, id) -> {
            dialog.cancel();
            showDeleteDialog(position);
            return false;
        });
    }

    private void showDeleteDialog(int position){
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setMessage("¿Desea eliminar la ubicación '"+lstLugares.get(position).getName()+"' de su lista de lugares?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean origin = lstLugares.get(position).isOrigen();

                        lstLugares.remove(position);
                        if(origin && lstLugares.size() > 0)
                            lstLugares.get(0).setOrigen(true);
                        generateMarkers();
                        generateRute();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog titulo = alerta.create();
        titulo.setTitle("Eliminar ubicación");
        titulo.show();
    }

    private void showCenterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.center_sheet_layout, null);

        dialog.setView(mView);
        card = dialog.create();
        card.show();

        card.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        card.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        card.getWindow().setGravity(Gravity.CENTER);

        ImageView imgLoad = mView.findViewById(R.id.imgLoad);

        Animation _animation = AnimationUtils.loadAnimation(this, R.anim.rotate_load);
        imgLoad.setAnimation(_animation);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_ORIGIN) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                Lugar lugar = new Lugar();
                lugar.setName(place.getName());
                lugar.setLatitude(place.getLatLng().latitude);
                lugar.setLongitude(place.getLatLng().longitude);
                lugar.setLatLng(place.getLatLng());

                if(lstLugares.size() == 0){
                    lugar.setOrigen(true);
                    addMarker(lugar, 1);
                }
                else{
                    addMarker(lugar, 2);
                }
            }
            else if (resultCode == AutocompleteActivity.RESULT_ERROR) {}
            else if (resultCode == RESULT_CANCELED) {}
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        if(tipo == 1)
            mMarkerMap.put("origen", marker);
        else
            mMarkerMap.put(name, marker);

        lstLugares.add(ubicacion);
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

    private void generateMarkers(){
        mMap.clear();
        BitmapDescriptor bitmapDescriptor;

        for (int i = 0; i < lstLugares.size(); i++){
            bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);

            if(lstLugares.get(i).isOrigen())
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

            mMap.addMarker(new MarkerOptions()
                    .position(lstLugares.get(i).getLatLng())
                    .title(lstLugares.get(i).getName())
                    .icon(bitmapDescriptor));
        }
    }

    private void generateRute(){
        if( lstLugares.size() < 2) return;

        mMap.clear();
        disntacia = 0;
        tiempo = 0;

        BitmapDescriptor bitmapDescriptor;

        for (int i = 0; i < lstLugares.size(); i++){
            bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);

            if(lstLugares.get(i).isOrigen())
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

            mMap.addMarker(new MarkerOptions()
                    .position(lstLugares.get(i).getLatLng())
                    .title(lstLugares.get(i).getName())
                    .icon(bitmapDescriptor));
        }

        if(lstLugares.size() == 2){
            GetDirectionsTask getDirectionsTask = new GetDirectionsTask( lstLugares.get(0).getLatLng(), lstLugares.get(1).getLatLng(), mMap, this);
            getDirectionsTask.execute();

            getDirectionsTask = new GetDirectionsTask( lstLugares.get(1).getLatLng(), lstLugares.get(0).getLatLng(), mMap, this);
            getDirectionsTask.execute();

        }
        else if(lstLugares.size() == 3){
            GetDirectionsTask getDirectionsTask = new GetDirectionsTask(lstLugares.get(0).getLatLng(), lstLugares.get(1).getLatLng(), mMap, this);
            getDirectionsTask.execute();

            getDirectionsTask = new GetDirectionsTask( lstLugares.get(1).getLatLng(), lstLugares.get(2).getLatLng(), mMap, this);
            getDirectionsTask.execute();

            getDirectionsTask = new GetDirectionsTask( lstLugares.get(2).getLatLng(), lstLugares.get(0).getLatLng(), mMap, this);
            getDirectionsTask.execute();
        }
        else{
            showCenterDialog();
            GetRuteTask getRuteTask = new GetRuteTask(lstLugares, this);
            getRuteTask.execute();
        }
    }

    public void paintRuta(List<Lugar> ruta){
        card.cancel();
        lstLugares.clear();
        for (int i = 0; i < ruta.size(); i++)
            lstLugares.add(ruta.get(i));

        GetDirectionsTask getDirectionsTask;
        for (int i = 0; i < ruta.size() - 1; i++){
            getDirectionsTask = new GetDirectionsTask(ruta.get(i).getLatLng(), ruta.get(i+1).getLatLng(), mMap, this);
            getDirectionsTask.execute();
        }
        getDirectionsTask = new GetDirectionsTask(ruta.get(ruta.size()-1).getLatLng(), ruta.get(0).getLatLng(), mMap, this);
        getDirectionsTask.execute();
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



class GetRuteTask extends AsyncTask<Void, Void, List<Lugar>> {
    private List<Lugar> lstLugares;
    private MainActivity mainActivity;

    public GetRuteTask(List<Lugar> lstLugares, MainActivity mainActivity) {
        this.lstLugares = lstLugares;
        this.mainActivity = mainActivity;
    }
    @Override
    protected List<Lugar> doInBackground(Void... voids) {
        TSP tsp = new TSP();
        return tsp.getTSP(lstLugares);
    }

    @Override
    protected void onPostExecute(List<Lugar> ruta) {
        super.onPostExecute(ruta);
        mainActivity.paintRuta(ruta);
    }
}


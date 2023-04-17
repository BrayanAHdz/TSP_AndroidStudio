package com.example.tsp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;

import java.io.Console;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private Button btnMostrarLugares;
    EditText txtLat, txtLong;
    GoogleMap mMap;

    private HashMap<String, Marker> mMarkerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLong = findViewById(R.id.txtLong);
        txtLat = findViewById(R.id.txtLat);
        btnMostrarLugares =  findViewById(R.id.btnMostrarLugares);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Places.initialize(getApplicationContext(), getString(R.string.API_KEY));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        btnMostrarLugares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void startAutocomplete(View view){
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        this.mMap.setOnMapClickListener(this);
        this.mMap.setOnMapLongClickListener(this);

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

    private void removeMarker(String markerId){
        Marker marker = mMarkerMap.get(markerId);
        if (marker != null) {
            marker.remove();
            mMarkerMap.remove(markerId);
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        txtLong.setText(""+latLng.longitude);
        txtLat.setText(""+latLng.latitude);
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        txtLong.setText(""+latLng.longitude);
        txtLat.setText(""+latLng.latitude);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                System.out.println("////////////////////////////////////////////////////////");
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());

                LatLng mexico = new LatLng(20.268445, -98.943285);
                Location locationA = new Location("Locacion A");
                locationA.setLatitude(mexico.latitude);
                locationA.setLongitude(mexico.longitude);

                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("México"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                Location locationB = new Location("Locacion A");
                locationB.setLatitude(place.getLatLng().latitude);
                locationB.setLongitude(place.getLatLng().longitude);

                GetDirectionsTask getDirectionsTask = new GetDirectionsTask(mexico, place.getLatLng(), mMap);
                getDirectionsTask.execute();


                float distance = locationA.distanceTo(locationB);

                txtLong.setText(""+place.getName());
                txtLat.setText(""+distance);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

class GetDirectionsTask extends AsyncTask<Void, Void, DirectionsResult> {

    private LatLng origin;
    private LatLng destination;
    GoogleMap mMap;

    public GetDirectionsTask(LatLng origin, LatLng destination, GoogleMap mMap) {
        this.origin = origin;
        this.destination = destination;
        this.mMap = mMap;
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
            System.out.println("///////////////////////////");
            System.out.println(result.routes[0].legs[0].distance.inMeters);
        }
    }

    private com.google.maps.model.LatLng cLatLong(LatLng latLng){
        return new com.google.maps.model.LatLng(latLng.latitude, latLng.longitude);
    }
}
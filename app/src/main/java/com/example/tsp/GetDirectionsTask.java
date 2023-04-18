package com.example.tsp;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;

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

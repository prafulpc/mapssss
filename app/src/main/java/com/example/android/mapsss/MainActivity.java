package com.example.android.mapsss;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/directions";
    //     private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyCMHreDsdYsGffXyFBHe6Q6KMps-QMj_aA";
    HttpURLConnection conn = null;
    StringBuilder jsonResults = new StringBuilder();
    ArrayList<String> resultList = null;
    final String TAG = PlaceAPI.class.getSimpleName();
    PolylineOptions lineOptions = null;

    Double lat1, lng1, lat2, lng2;

    private boolean mPermissionDenied = false;
    Button button;
    GoogleMap mMap;

    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("onCreate", "onCreate");

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));

        final AutoCompleteTextView autocompleteView1 = (AutoCompleteTextView) findViewById(R.id.autocomplete1);
        final AutoCompleteTextView autocompleteView2 = (AutoCompleteTextView) findViewById(R.id.autocomplete2);
        autocompleteView1.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
        autocompleteView2.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));

        autocompleteView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String description = (String) parent.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, description, Toast.LENGTH_LONG).show();
                String place1 = autocompleteView1.getText().toString();
                try {
                    String abc = URLEncoder.encode(place1, "utf-8");
                    Log.i("abc", abc);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }
        });

        autocompleteView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String description = (String) parent.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, description, Toast.LENGTH_LONG).show();
                String place2 = autocompleteView1.getText().toString();

            }
        });

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String place1 = null, place2 = null;

                try {
                    place1 = URLEncoder.encode(autocompleteView1.getText().toString(), "utf8");
                    place2 = URLEncoder.encode(autocompleteView2.getText().toString(), "utf8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                ;
////                String place2 = autocompleteView2.getText().toString();
//
//                Log.i("place1", place1);
//                Log.i("place2", place2);

                if ((place1 != "")&&(place2 != ""))
                    {
                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        runner.execute(place1, place2);
                    }
                else {
                    Toast.makeText(MainActivity.this, "Enter From and To Address", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
//        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
//            progressDialog= ProgressDialog.show(MainActivity.this, "ProgressDialog", "wait for "+ time.getText().toString()+ " Seconds");
        }

        @Override
        protected String doInBackground(String... params) {

            publishProgress("Fetching........");

            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE + OUT_JSON);
                sb.append("?key=" + API_KEY);
                sb.append("&origin=" + params[0]);
                sb.append("&destination=" + params[1]);
//                    sb.append("&location=12.9716,77.5946&radius=100000");
//                    sb.append("&input=" + URLEncoder.encode(input, "utf8"));

                URL url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Error processing Places API URL", e);
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to Places API", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return String.valueOf(jsonResults);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        map.setMyLocationEnabled(true);
        Log.i("onMapReady", "onMapReady");
//        mMap. enableMyLocation();
        if (lat1 != null) {
            mMap.setOnMyLocationButtonClickListener(this);
            MarkerOptions options1 = new MarkerOptions();
            MarkerOptions options2 = new MarkerOptions();
            options1.position(new LatLng(lat1, lng1));
            options2.position(new LatLng(lat2, lng2));
            mMap.addMarker(options1);
            mMap.addMarker(options2);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat1, lng1),
                    12));
            enableMyLocation();
            mMap.addPolyline(lineOptions);
        } else {
//            mMap.setOnMyLocationButtonClickListener(this);
//            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            super.onPostExecute(result);

            GoogleMap mGMap;
            lat1 = Double.valueOf(result.get(0).get(0).get("lat"));
            lng1 = Double.valueOf(result.get(0).get(0).get("lng"));

            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(MainActivity.this);

            int size = ((result.get(0).size()) - 1);
            lat2 = Double.parseDouble(result.get(0).get(size).get("lat"));
            lng2 = Double.parseDouble(result.get(0).get(size).get("lng"));

            ArrayList<LatLng> points = null;


            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                lineOptions.addAll(points);

            }
//            mMap.addPolyline(lineOptions);

        }
    }
}

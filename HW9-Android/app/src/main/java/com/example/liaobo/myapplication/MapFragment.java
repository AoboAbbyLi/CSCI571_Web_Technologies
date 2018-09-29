package com.example.liaobo.myapplication;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "MapFragment";
    private static final int GOOGLE_API_CLIENT_ID = 1;

    private static View rootView;
    private Bundle savedInstanceState;
    private Spinner spinner;
    MapView mMapView;
    private GoogleMap googleMap;
    private JSONObject details;
    private String name;
    private String lat;
    private String lng;
    private AutoCompleteTextView mAutocompleteTextView;
    private TextView mNameView;
    private String mode;
    private String fromPlaceId;
    private String toPlaceId;
    private Place fromPlace;
    private LatLng fromPoint;
    private LatLng toPoint;

    ArrayList<LatLng> markerPoints;

    private GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteAdapter mPlaceArrayAdapter;
//    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
//            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    private static final LatLngBounds BOUNDS = new LatLngBounds(
            new LatLng(-90, -180), new LatLng(90, 180));


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_map, container, false);
        this.savedInstanceState = savedInstanceState;

        mAutocompleteTextView = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteMapFrom);
        mAutocompleteTextView.setThreshold(3);
        mNameView = (TextView) rootView.findViewById(R.id.name);

        createSpinner();
        createAutoComplete();
        createMap();

        fromPlaceId = null;
        toPlaceId = getArguments().getString("place_id");
        mode = "DRIVING";

        return rootView;
    }

    public void createSpinner() {
        spinner = (Spinner) rootView.findViewById(R.id.spinnerMode);
        List<SpinnerData> list = new ArrayList<SpinnerData>(){
            {
                add(new SpinnerData("Driving", "driving"));
                add(new SpinnerData("Walking", "walking"));
                add(new SpinnerData("Bicycling", "bicycling"));
                add(new SpinnerData("Transit", "transit"));
            }};
        ArrayAdapter<SpinnerData> Adapter = new ArrayAdapter<SpinnerData>(getActivity(), android.R.layout.simple_spinner_item, list);
        Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(Adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

                mode =  ((SpinnerData)adapter.getItemAtPosition(i)).getValue();
                if (fromPlaceId != null) drawRoute();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
    }

    public void createAutoComplete() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(getActivity(), GOOGLE_API_CLIENT_ID, this)
                    .addConnectionCallbacks(this)
                    .build();

            mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
            mPlaceArrayAdapter = new PlaceAutoCompleteAdapter(getActivity(), android.R.layout.simple_list_item_1,
                    BOUNDS, null);
            mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);
        }
    }

    public void createMap() {
        String detailsStr = getArguments().getString("details");
        try {
            details = new JSONObject(detailsStr);
            name = details.getString("name");
            JSONObject location = details.getJSONObject("geometry").getJSONObject("location");
            lat = location.getString("lat");
            lng = location.getString("lng");
        }
        catch (JSONException e) {
            Log.i("map", "JSONException", e);
        }

        mMapView= (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;


                if (checkLocationPermission()) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        googleMap.setMyLocationEnabled(true);
                    }
                }

                markerPoints = new ArrayList<LatLng>();

                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setRotateGesturesEnabled(true);
                // For dropping a marker at a point on the Map
                // lat lng and title
                toPoint = new LatLng(Float.parseFloat(lat),Float.parseFloat(lng));

                googleMap.addMarker(new MarkerOptions().position(toPoint).title(name)).showInfoWindow();

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(toPoint).zoom(15).build();
//                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition
//                        (cameraPosition ));
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition
                        (cameraPosition ));
            }
        });

    }

    public void drawRoute() {
        googleMap.clear();
        fromPoint = fromPlace.getLatLng();
        String fromName = String.valueOf(fromPlace.getName());
        Marker origin = googleMap.addMarker(new MarkerOptions().position(fromPoint).title(fromName));
        origin.showInfoWindow();
        Marker destination = googleMap.addMarker(new MarkerOptions().position(toPoint).title(name));

        // Getting URL to the Google Directions API
        String url = "http://aobolihw9-env.us-east-2.elasticbeanstalk.com/directions?origin="
                + fromPoint.latitude + "," + fromPoint.longitude + "&destination=" + toPoint.latitude + "," + toPoint.longitude
                + "&mode=" + mode;
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //move map camera
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
//            fromPlaceId = String.valueOf(item.placeId);
            fromPlaceId = String.valueOf(item.placeId);
            Log.i(TAG, "from place ID: " + item.placeId);

            //use placeId to get latlng
            Places.GeoDataApi.getPlaceById(mGoogleApiClient, fromPlaceId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                fromPlace = places.get(0);
                                //request direction
                                drawRoute();
                                Log.i(TAG, "from Place found: " + fromPlace.getName());
                            } else {
                                Log.e(TAG, "from Place not found");
                            }
//                            places.release();
                        }
                    });
//            Log.i(TAG, "Selected: " + item.description);
//            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
//                    .getPlaceById(mGoogleApiClient, placeId);
//            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
//            Log.i(TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

            mNameView.setText(Html.fromHtml(place.getAddress() + ""));
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),new String[]
                                        {Manifest.permission.ACCESS_FINE_LOCATION},1);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                        googleMap.setMyLocationEnabled(true);
                    }

                } else {



                }
                return;
            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(getActivity(),
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();

    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                PathParser parser = new PathParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);
                lineOptions.color(Color.BLUE);

                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                for (LatLng latLngPoint : points)
                    boundsBuilder.include(latLngPoint);

                LatLngBounds bounds = boundsBuilder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                //int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen
                int padding = 200;

                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                googleMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}


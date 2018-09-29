package com.example.liaobo.myapplication;

//import android.app.Fragment;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.net.Uri;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class SearchFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private Spinner sp;
    private static View rootView;
    private EditText keyword;
    private EditText distance;
    private EditText location;
    private RadioGroup fromGroup;
    private Button radioButton1;
    private Button radioButton2;
    private Button searchButton;
    private Button clearButton;
    private AutoCompleteTextView mAutocompleteTextView;
    private ProgressDialog dialog;
    private double lat;
    private double lng;

    private boolean mLocationPermissionGranted;
    private boolean mLastKnownLocation;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteAdapter mPlaceArrayAdapter;

    private static final String TAG = "SearchFragment";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private static final LatLngBounds BOUNDS = new LatLngBounds(
            new LatLng(-90, -180), new LatLng(90, 180));

    public static final String SEARCH_RESULTS_EXTRA = "com.example.myapplication.MESSAGE";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//    public void onCreate(Bundle savedInstanceState) {
//        if (view != null) {
//            ViewGroup parent = (ViewGroup) view.getParent();
//            if (parent != null)
//                parent.removeView(view);
//        }
//        view = inflater.inflate(R.layout.fragment_search, container, false);

        super.onCreate(savedInstanceState);
//        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        sp = (Spinner) rootView.findViewById(R.id.spinnerCategory);
        bind();
        keyword = (EditText) rootView.findViewById(R.id.editTextKeyword);
        distance = (EditText) rootView.findViewById(R.id.editTextDistance);
//        location = (EditText) rootView.findViewById(R.id.editTextLocation);
        mAutocompleteTextView = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteFrom);
        mAutocompleteTextView.setThreshold(3);
        mAutocompleteTextView.setEnabled(false);
        fromGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);
        radioButton1 = (Button) rootView.findViewById(R.id.radioButton1);
        radioButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioClick(v);
            }
        });
        radioButton2 = (Button) rootView.findViewById(R.id.radioButton2);
        radioButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioClick(v);
            }
        });
        searchButton = (Button) rootView.findViewById(R.id.formSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFormClick(v);
            }
        });
        clearButton = (Button) rootView.findViewById(R.id.formClearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFormClick(v);
            }
        });

        createAutoComplete();

        getDeviceLocation();

        return rootView;

    }

    public void createAutoComplete() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(getActivity(), GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceAutoCompleteAdapter(getActivity(), android.R.layout.simple_list_item_1,
                BOUNDS, null);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(TAG, "Fetching details for ID: " + item.placeId);
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

//            mNameView.setText(Html.fromHtml(place.getAddress() + ""));


        }
    };

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

    private void bind() {
        List<SpinnerData> list = new ArrayList<SpinnerData>(){{
            add(new SpinnerData("Default", "default"));
            add(new SpinnerData("Airport", "airport"));
            add(new SpinnerData("Amusement Park", "amusement_park"));
            add(new SpinnerData("Aquarium", "aquarium"));
            add(new SpinnerData("Art Gallery", "art_gallery"));
            add(new SpinnerData("Bakery", "bakery"));
            add(new SpinnerData("Bar", "bar"));
            add(new SpinnerData("Beauty Salon", "beauty_salon"));
            add(new SpinnerData("Bowling Alley", "bowling_alley"));
            add(new SpinnerData("Bus Station", "bus_station"));
            add(new SpinnerData("Cafe", "cafe"));
            add(new SpinnerData("Campground", "campground"));
            add(new SpinnerData("Car Rental", "car_rental"));
            add(new SpinnerData("Casino", "casino"));
            add(new SpinnerData("Lodging", "lodging"));
            add(new SpinnerData("Movie Theater", "movie_theater"));
            add(new SpinnerData("Museum", "museum"));
            add(new SpinnerData("Night Club", "night_club"));
            add(new SpinnerData("Park", "park"));
            add(new SpinnerData("Parking", "parking"));
            add(new SpinnerData("Restaurant", "restaurant"));
            add(new SpinnerData("Shopping Mall", "shopping_mall"));
            add(new SpinnerData("Stadium", "stadium"));
            add(new SpinnerData("Subway Station", "subway_station"));
            add(new SpinnerData("Taxi Stand", "taxi_stand"));
            add(new SpinnerData("Train Station", "train_station"));
            add(new SpinnerData("Trainsit Station", "trainsit_station"));
            add(new SpinnerData("Travel Agency", "travel_station"));
            add(new SpinnerData("Zoo", "zoo"));
        }};

//        ArrayAdapter<SpinnerData> Adapter = new ArrayAdapter<SpinnerData>(SearchFragment.this, android.R.layout.simple_spinner_item, list);
        ArrayAdapter<SpinnerData> Adapter = new ArrayAdapter<SpinnerData>(getActivity(), android.R.layout.simple_spinner_item, list);
        Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(Adapter);
    }

    public void onRadioClick(View v) {
        int id = v.getId();
        if (id == R.id.radioButton1) {
            Log.i("radio1", "click");
            mAutocompleteTextView.setEnabled(false);
//            Toast.makeText(MainActivity.this, ((Button) v).getText(), Toast.LENGTH_SHORT).show();
        } else {
            Log.i("radio2", "click");
            mAutocompleteTextView.setEnabled(true);
        }
    }

    public void onFormClick(View v) {

        int id = v.getId();
        boolean valid = true;
        if (id == R.id.formSearchButton) {
            Log.i("search","click");
//            Toast.makeText(MainActivity.this, ((Button) v).getText(), Toast.LENGTH_SHORT).show();

            String categoryValue = ((SpinnerData)sp.getSelectedItem()).getValue();
            int checkedRadioId = fromGroup.getCheckedRadioButtonId();
    //        RadioButton choice = (RadioButton) rootView.findViewById(checkedRadioId);
    //        String fromValue = choice.getText().toString();
            String fromValue;
    //        if (fromValue == "Current location") { fromValue = "here"; }"
    //          @id/radioButton1"
            if (checkedRadioId == R.id.radioButton1) { fromValue = "here"; }
            else { fromValue = "other"; }
            if (!mLocationPermissionGranted && fromValue == "here") {
                Toast.makeText(getActivity(), "Cannot get current location. Search Failed.", Toast.LENGTH_LONG).show();
                return;
            }
            String keywordValue = keyword.getText().toString();
            if ("".equals(keywordValue)) {
                valid = false;
                rootView.findViewById(R.id.textViewPrompt1).setVisibility(LinearLayout.VISIBLE);
            }
            else {
                rootView.findViewById(R.id.textViewPrompt1).setVisibility(LinearLayout.GONE);
            }
            String distanceValue = distance.getText().toString();
            if ("".equals(distanceValue)) { distanceValue = "10"; }
    //        String locationValue = location.getText().toString();
            String locationValue = mAutocompleteTextView.getText().toString();
            if ("other".equals(fromValue) && "".equals(locationValue)) {
                valid = false;
                rootView.findViewById(R.id.textViewPrompt2).setVisibility(LinearLayout.VISIBLE);
            }
            else {
                rootView.findViewById(R.id.textViewPrompt2).setVisibility(LinearLayout.GONE);
            }
            Log.i("locationValue", locationValue);
    //        final TextView mTextView = (TextView) rootView.findViewById(R.id.textViewResult);

            if (!valid) {
                return;
            }

            RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

            String url = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com/submit";


            if (keywordValue.length()>0 && (fromValue=="here" || (fromValue=="other" && locationValue.length()>0))) {
                Log.i("form","valid");
                Log.i("keyword",keywordValue);
                Log.i("category",categoryValue);
                Log.i("distance",distanceValue);
                Log.i("from",fromValue);
    //            HashMap<String, String> parrams = new HashMap<String, String>();
    //            parrams.put("keyword", Uri.encode(keywordValue));
    //            parrams.put("category", Uri.encode(categoryValue));
    //            parrams.put("distance", Uri.encode(distanceValue));
    //            parrams.put("from", Uri.encode(fromValue));
    //            parrams.put("lat", Uri.encode("34.0266"));
    //            parrams.put("lng", Uri.encode("-118.2831"));
    //            parrams.put("location", Uri.encode(locationValue));
    //            new JSONObject(parrams);
                dialog = new ProgressDialog(getActivity());
                dialog.show(getActivity(), "", "Fetching results");
                String params = "?keyword=" + Uri.encode(keywordValue) + "&category=" + Uri.encode(categoryValue) + "&distance=" + Uri.encode(distanceValue)
                        + "&from=" + Uri.encode(fromValue) + "&lat=" + Uri.encode(String.valueOf(lat)) + "&lng=" + Uri.encode(String.valueOf(lng)) + "&location=" + Uri.encode(locationValue);
                url += params;
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest (Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.i("http","success");
                                JSONArray results = response.getJSONArray("results");
    //                            Log.i("results", results.toString());
                                sendFormResults(response);
                            } catch (JSONException e) {
                                Log.i("http","JSONException", e);
                                e.printStackTrace();
    //                            Toast.makeText(getApplicationContext(),
    //                                    "Error: " + e.getMessage(),
    //                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    } , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.i("http",error.getMessage());
                        dialog.dismiss();
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getActivity(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(jsonObjectRequest);
            }
            else {
                Log.i("form","invalid");
                Log.i("keyword",keywordValue);
                Log.i("from",fromValue);
                Log.i("distance",distanceValue);
                Log.i("from",fromValue);
            }
        }
        else {
            Log.i("clear","click");
            keyword.setText("");
            sp.setSelection(0);
            distance.setText("");
            mAutocompleteTextView.setText("");
            mAutocompleteTextView.setEnabled(false);
            fromGroup.check(R.id.radioButton1);
        }

        // Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
//                        mTextView.setText("Response is: "+ response.substring(0,500));
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                mTextView.setText("That didn't work!");
//            }
//        });

        // Access the RequestQueue through your singleton class.
//        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        //check whether the msg empty or not
//        if (keywordValue.length()>0 && (fromValue=="here" || (fromValue=="other" && locationValue.length()>0))) {
//            HttpClient httpclient = new DefaultHttpClient();
//            HttpPost httppost = new HttpPost("http://www.yourdomain.com/serverside-script.php");
//
//            try {
//                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//                nameValuePairs.add(new BasicNameValuePair("id", "01"));
//                nameValuePairs.add(new BasicNameValuePair("message", msg));
//                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                httpclient.execute(httppost);
//                msgTextField.setText(""); //reset the message text field
//                Toast.makeText(getBaseContext(),"Sent",Toast.LENGTH_SHORT).show();
//            } catch (ClientProtocolException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            //display message if text field is empty
//            Toast.makeText(getBaseContext(),"All fields are required",Toast.LENGTH_SHORT).show();
//        }
    }

    public void sendFormResults(JSONObject jsonObject) {
        dialog.dismiss();
        Intent intent = new Intent(getActivity(), ResultsActivity.class);
        intent.putExtra(SEARCH_RESULTS_EXTRA, jsonObject.toString());
        startActivity(intent);
    }

//    public boolean checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(getActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                new AlertDialog.Builder(getActivity())
//                        .setTitle("")
//                        .setMessage("")
//                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                //Prompt the user once explanation has been shown
//                                ActivityCompat.requestPermissions(getActivity(),new String[]
//                                        {Manifest.permission.ACCESS_FINE_LOCATION},1);
//                            }
//                        })
//                        .create()
//                        .show();
//
//
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        1);
//            }
//            return false;
//        } else {
//            return true;
//        }
//    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private void getDeviceLocation() {
        /*
         * Before getting the device location, you must check location
         * permission, as described earlier in the tutorial. Then:
         * Get the best and most recent location of the device, which may be
         * null in rare cases when a location is not available.
         */
        checkLocationPermission();
        if (mLocationPermissionGranted) {
//            mLastKnownLocation = LocationServices.FusedLocationApi
//                    .getLastLocation(mGoogleApiClient);
//            String lat =  mLastKnownLocation.getLatitude();
//            String lng =  mLastKnownLocation.getLongitude();
            LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lng = location.getLongitude();
            lat = location.getLatitude();

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            lng = location.getLongitude();
            lat = location.getLatitude();
        }
        //@Override
        public void onProviderDisabled(String provider) {}

        //@Override
        public void onProviderEnabled(String provider) {}

        //@Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
//
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                else {
                    Toast.makeText(getActivity(),
                            "Location Permission Denied. Please enable location permission",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if(dialog != null) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if(dialog != null) {
            dialog.dismiss();
        }
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        if(dialog != null) {
            dialog.dismiss();
        }
        super.onPause();
    }

}

//


//    String s="value="+((SpinnerData)sp.getSelectedItem()).getValue() +" && Text="+((SpinnerData)sp.getSelectedItem()).getText();
package com.example.liaobo.myapplication;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class ReviewsFragment extends Fragment {

    private static View rootView;
    private JSONArray googleReviews;
    private JSONArray yelpReviews;
    private JSONObject info;
    private ArrayList<Review> googleDefault;
    private ArrayList<Review> googleRatingAsc;
    private ArrayList<Review> googleRatingDesc;
    private ArrayList<Review> googleDateAsc;
    private ArrayList<Review> googleDateDesc;
    private ArrayList<Review> yelpDefault;
    private ArrayList<Review> yelpRatingAsc;
    private ArrayList<Review> yelpRatingDesc;
    private ArrayList<Review> yelpDateAsc;
    private ArrayList<Review> yelpDateDesc;
    private ArrayList<Review> currList;
    private String source;
    private String order;
    private RecyclerView mRecyclerView;
    //    private RecyclerView.Adapter mAdapter;
    private ReviewsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean hasGoogleReview;
    private boolean hasYelpReview;
    private Spinner sourceSpinner;
    private Spinner orderSpinner;
    private RequestQueue requestQueue;
    private TextView noReviewTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        noReviewTextView = rootView.findViewById(R.id.noReviewTextView);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.review_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        String reviewStr = getArguments().getString("reviews");
        String infoStr = getArguments().getString("info");
        createSpinner();
        source = "google";
        order = "default";
        try {
            if (reviewStr == null || reviewStr.equals("")) {
                hasGoogleReview = false;
            }
            else {
                hasGoogleReview = true;
                googleReviews = new JSONArray(reviewStr);
//            TextView tv = rootView.findViewById(R.id.info_textview);
//            tv.setText(info.toString());
                sortGoogleReviews();
            }
            info = new JSONObject(infoStr);
        }
        catch (JSONException e) {
            Log.i("info","JSONException", e);
        }
        getYelpMatch();
        drawReviews();

        return rootView;
    }

    public void createSpinner() {
        sourceSpinner = (Spinner) rootView.findViewById(R.id.spinnerSource);
        List<SpinnerData> list = new ArrayList<SpinnerData>(){
            {
                add(new SpinnerData("Google reviews", "google"));
                add(new SpinnerData("Yelp reviews", "yelp"));
            }};
        ArrayAdapter<SpinnerData> Adapter = new ArrayAdapter<SpinnerData>(getActivity(), android.R.layout.simple_spinner_item, list);
        Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(Adapter);
        sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

//                String selectedItem =  adapter.getItemAtPosition(i).toString();
                switch (i) {
                    case 0: source = "google"; break;
                    case 1: source = "yelp"; break;
                }
                drawReviews();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });

        orderSpinner = (Spinner) rootView.findViewById(R.id.spinnerOrder);
        List<SpinnerData> list2 = new ArrayList<SpinnerData>(){
            {
                add(new SpinnerData("Default order", "default"));
                add(new SpinnerData("Highest rating", "ratingDesc"));
                add(new SpinnerData("Lowest rating", "ratingAsc"));
                add(new SpinnerData("Most recent", "dateDesc"));
                add(new SpinnerData("Least recent", "dateAsc"));
            }};
        ArrayAdapter<SpinnerData> Adapter2 = new ArrayAdapter<SpinnerData>(getActivity(), android.R.layout.simple_spinner_item, list2);
        Adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(Adapter2);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

//                String selectedItem =  adapter.getItemAtPosition(i).toString();
                switch (i) {
                    case 0: order = "default"; break;
                    case 1: order = "ratingDesc"; break;
                    case 2: order = "ratingAsc"; break;
                    case 3: order = "dateDesc"; break;
                    case 4: order = "dateAsc"; break;
                }
                drawReviews();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
    }

    public void drawReviews() {
        if (source.equals("google") && !hasGoogleReview || source.equals("yelp") && !hasYelpReview) {
//            TextView tv = new TextView(getActivity());
//            tv.setText("No Reviews");
//            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            tv.setLayoutParams(parms);
//            tv.setGravity(Gravity.CENTER);
//            LinearLayout no_photo = rootView.findViewById(R.id.no_review);
//            no_photo.addView(tv);
            noReviewTextView.setVisibility(View.VISIBLE);
            currList = new ArrayList<>();
            mAdapter = new ReviewsAdapter(currList);
            mRecyclerView.setAdapter(mAdapter);
        }
        else {
            noReviewTextView.setVisibility(View.GONE);
            if (source.equals("google")) {
                if (order.equals("default")) currList = googleDefault;
                else if (order.equals("dateAsc")) currList = googleDateAsc;
                else if (order.equals("dateDesc")) currList = googleDateDesc;
                else if (order.equals("ratingAsc")) currList = googleRatingAsc;
                else currList = googleRatingDesc;
            }
            else {
                if (order.equals("default")) currList = yelpDefault;
                else if (order.equals("dateAsc")) currList = yelpDateAsc;
                else if (order.equals("dateDesc")) currList = yelpDateDesc;
                else if (order.equals("ratingAsc")) currList = yelpRatingAsc;
                else currList = yelpRatingDesc;
            }

            // specify an adapter (see also next example)
            mAdapter = new ReviewsAdapter(currList);

            mAdapter.setOnItemClickListener(new ReviewsAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
//                    Toast.makeText(getActivity(), position + " click",
//                            Toast.LENGTH_SHORT).show();
                    Uri uri = Uri.parse(currList.get(position).url); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
//                try {
//                    String place_id = places.getJSONObject(position).getString("place_id");
//                    Intent intent = new Intent(ResultsActivity.this, DetailsActivity.class);
//                    intent.putExtra(PLACE_ID_EXTRA, place_id);
//                    startActivity(intent);
//                } catch (JSONException e) {
//                    Log.i("Result item", "click", e);
//                }

                }

            });

            mRecyclerView.setAdapter(mAdapter);
        }
    }

    public void sortGoogleReviews() {
        try {
            googleDefault = new ArrayList<>();
            for (int i = 0; i < googleReviews.length(); i++) {
                String name = googleReviews.getJSONObject(i).getString("author_name");
                String img = googleReviews.getJSONObject(i).getString("profile_photo_url");
                String url = googleReviews.getJSONObject(i).getString("author_url");
                int rating = Integer.parseInt(googleReviews.getJSONObject(i).getString("rating"));
                String timeStamp = googleReviews.getJSONObject(i).getString("time");
                Date mydate = new java.util.Date(Long.parseLong(timeStamp) * 1000);
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(mydate);
//                Calendar cal = Calendar.getInstance();
//                cal.setTime(mydate);
//                int year = cal.get(Calendar.YEAR);
//                int month = cal.get(Calendar.MONTH);
//                int day = cal.get(Calendar.DAY_OF_MONTH);
//                int hour = cal.get(Calendar.HOUR_OF_DAY);
//                int minute = cal.get(Calendar.MINUTE);
//                int second = cal.get(Calendar.SECOND);
//                String date = year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second;
                String content = googleReviews.getJSONObject(i).getString("text");
                Review review = new Review(name, img, url, rating, date, content);
                googleDefault.add(review);
            }
        }
        catch (JSONException e) {
            Log.i("reviewFragment", "JSONException", e);
        }
        googleRatingAsc = new ArrayList<>(googleDefault);
//        Collections.sort(googleRatingAsc, (Review r1, Review r2)-> {return r2.rating - r1.rating;});
        Collections.sort(googleRatingAsc, new Comparator<Review>() {
            @Override
            public int compare(Review r1, Review r2) {
                return r1.rating - r2.rating;
            }
        });
        googleRatingDesc = new ArrayList<>(googleDefault);
        Collections.sort(googleRatingDesc, new Comparator<Review>() {
            @Override
            public int compare(Review r1, Review r2) {
                return r2.rating - r1.rating;
            }
        });
        googleDateAsc = new ArrayList<>(googleDefault);
        Collections.sort(googleDateAsc, new Comparator<Review>() {
            @Override
            public int compare(Review r1, Review r2) {
                return r1.date.compareTo(r2.date);
            }
        });
        googleDateDesc = new ArrayList<>(googleDefault);
        Collections.sort(googleDateDesc, new Comparator<Review>() {
            @Override
            public int compare(Review r1, Review r2) {
                return r2.date.compareTo(r1.date);
            }
        });
    }

    public void getYelpMatch() {
        try {
            requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

            String name = info.getString("name");
            String formatted_address = info.getString("address");

            String[] arr = formatted_address.split(", ");
            String url = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com/yelpMatch";
            String params = "?name=" + Uri.encode(name) + "&city=" + Uri.encode(arr[arr.length - 3]) + "&state=" + Uri.encode(arr[arr.length - 2].split(" ")[0])
                    + "&country=" + Uri.encode("US");
            url += params;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest (Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.i("http","success");
                                String id = response.getString("id");
                                getYelpReviews(id);
                            } catch (JSONException e) {
                                Log.i("http","JSONException", e);
                                e.printStackTrace();
                                hasYelpReview = false;
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
                    VolleyLog.d("ReviewsFragment", "Error: " + error.getMessage());
                    Toast.makeText(getActivity(),
                            "Failed to get Yelp Reviews", Toast.LENGTH_SHORT).show();
                    hasYelpReview = false;
                }
            });
            requestQueue.add(jsonObjectRequest);
        }
        catch (Exception e) {
            hasYelpReview = false;
            Log.i("yelpReview", "Exception", e);
        }
    }

    public void getYelpReviews(String id) {
        String url = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com/yelpReviews?id=" + Uri.encode(id);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest (Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
//                        try {
                            Log.i("http","success");
//                            yelpReviews = response.getJSONArray("reviews");
                            yelpReviews = response;
                            Log.i("yelpReviews return value",yelpReviews.toString());
                            sortYelpReviews();
//                        } catch (JSONException e) {
//                            Log.i("http","JSONException", e);
//                            e.printStackTrace();
//                            hasYelpReview = false;
                            //                            Toast.makeText(getApplicationContext(),
                            //                                    "Error: " + e.getMessage(),
                            //                                    Toast.LENGTH_LONG).show();
//                        }
                    }
                } , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.i("http",error.getMessage());
                VolleyLog.d("ReviewsFragment", "Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        "Failed to get Yelp Reviews", Toast.LENGTH_SHORT).show();
                hasYelpReview = false;
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    public void sortYelpReviews() {
        if (yelpReviews == null || yelpReviews.length() == 0) {
            Log.i("sort yelpReviews","false");
            hasYelpReview = false;
        }
        else {
            try {
                yelpDefault = new ArrayList<>();
                for (int i = 0; i < yelpReviews.length(); i++) {
                    String name = yelpReviews.getJSONObject(i).getJSONObject("user").getString("name");
                    String img = yelpReviews.getJSONObject(i).getJSONObject("user").getString("image_url");
                    String url = yelpReviews.getJSONObject(i).getString("url");
                    int rating = Integer.parseInt(yelpReviews.getJSONObject(i).getString("rating"));
                    String date = yelpReviews.getJSONObject(i).getString("time_created");
                    String content = yelpReviews.getJSONObject(i).getString("text");
                    Review review = new Review(name, img, url, rating, date, content);
                    yelpDefault.add(review);
                }
                hasYelpReview = true;
            } catch (JSONException e) {
                Log.i("reviewFragment", "JSONException", e);
            }
            yelpRatingAsc = new ArrayList<>(yelpDefault);
//        Collections.sort(yelpRatingAsc, (Review r1, Review r2)-> {return r2.rating - r1.rating;});
            Collections.sort(yelpRatingAsc, new Comparator<Review>() {
                @Override
                public int compare(Review r1, Review r2) {
                    return r1.rating - r2.rating;
                }
            });
            yelpRatingDesc = new ArrayList<>(yelpDefault);
            Collections.sort(yelpRatingDesc, new Comparator<Review>() {
                @Override
                public int compare(Review r1, Review r2) {
                    return r2.rating - r1.rating;
                }
            });
            yelpDateAsc = new ArrayList<>(yelpDefault);
            Collections.sort(yelpDateAsc, new Comparator<Review>() {
                @Override
                public int compare(Review r1, Review r2) {
                    return r1.date.compareTo(r2.date);
                }
            });
            yelpDateDesc = new ArrayList<>(yelpDefault);
            Collections.sort(yelpDateDesc, new Comparator<Review>() {
                @Override
                public int compare(Review r1, Review r2) {
                    return r2.date.compareTo(r1.date);
                }
            });
        }
    }

}

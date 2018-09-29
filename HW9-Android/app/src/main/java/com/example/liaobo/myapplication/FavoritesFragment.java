package com.example.liaobo.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class FavoritesFragment extends Fragment {

    View rootView;

    private RecyclerView mRecyclerView;
    //    private RecyclerView.Adapter mAdapter;
    private ResultsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
//    private JSONArray places;
    private JSONArray results;
    private JSONObject details;
    private String place_id;
    private ProgressDialog dialog;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Map<String, ?> allEntries;


    public static final String PLACE_ID_EXTRA = "com.example.myapplication.PLACE_ID";
    public static final String PLACE_DETAIL_EXTRA = "com.example.myapplication.PLACE_DETAIL_EXTRA";
    public static final String  MY_PREFS_NAME = "favorites";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);

        prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        sortResults();
        updateView(results);

        return rootView;
    }

    public void sortResults() {
        allEntries = prefs.getAll();
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        try {
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                jsonValues.add(new JSONObject(entry.getValue().toString()));
            }
        }
        catch (JSONException e) { }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                try {
                    String timeA = a.getString("timestamp");
                    String timeB = b.getString("timestamp");
                    return timeA.compareTo(timeB);
                }
                catch (JSONException e) {}
                return 0;
            }
        });
        results = new JSONArray();
        for (int i = 0; i < jsonValues.size(); i++) {
            results.put(jsonValues.get(i));
        }
    }

    public void updateView (final JSONArray results) {

        if (results == null || results.length() == 0) {
            LinearLayout has_favorites = rootView.findViewById(R.id.has_favorites);
            has_favorites.setVisibility(LinearLayout.GONE);
//            TextView tv = new TextView(getActivity());
//            tv.setText("No Favorites");
//            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            tv.setLayoutParams(parms);
//            tv.setGravity(Gravity.CENTER);
            LinearLayout no_favorites = rootView.findViewById(R.id.no_favorites);
            no_favorites.setVisibility(LinearLayout.VISIBLE);
//            no_favorites.addView(tv);
            mAdapter = new ResultsAdapter(getActivity(), new JSONArray());
        }
        else {
            LinearLayout has_favorites = rootView.findViewById(R.id.has_favorites);
            has_favorites.setVisibility(LinearLayout.VISIBLE);
            LinearLayout no_favorites = rootView.findViewById(R.id.no_favorites);
            no_favorites.setVisibility(LinearLayout.GONE);

            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.fav_recycler_view);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new ResultsAdapter(getActivity(), results);

            mAdapter.setOnItemClickLitener(new ResultsAdapter.OnItemClickLitener() {

                @Override
                public void onItemClick(View view, int position) {
//                    Toast.makeText(ResultsActivity.this, position + " click",
//                            Toast.LENGTH_SHORT).show();
                    try {
                        place_id = results.getJSONObject(position).getString("place_id");
                        getDetails();
                    } catch (JSONException e) {
                        Log.i("Result item", "click", e);
                    }
                }
            });
            mAdapter.setOnFavClickLitener(new ResultsAdapter.OnFavClickLitener() {

                @Override
                public void onFavClick(View view, int position) {
                    try {
                        String place_id = results.getJSONObject(position).getString("place_id");
//                        JSONObject place = results.getJSONObject(position);
//                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//                        place.put("timestamp", timestamp);
//                        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
//                        if (!prefs.contains(place_id)) {
//                            SharedPreferences.Editor editor = prefs.edit();
//                            editor.putString(place_id, place.toString());
//                            editor.commit();
//                        }
                        ImageButton fav = (ImageButton) view.findViewById(R.id.fav_num);
                        fav.setImageResource(R.drawable.heart_outline_black);
                        editor.remove(place_id);
                        editor.apply();
                        results.remove(position);
                        if (results.length() == 0) {
                            updateView(results);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    catch (JSONException e) {
                        Toast.makeText(getActivity(), "click fav JSONException",
                                Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getActivity(), position + " click fav",
                            Toast.LENGTH_SHORT).show();

                }

            });

            mRecyclerView.setAdapter(mAdapter);
        }

    }

    public void getDetails() {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

//        String url = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com/details";
//        String url = "http://localhost:8080/details?place_id="+place_id;
        String url = "http://aobolihw9-env.us-east-2.elasticbeanstalk.com/details?place_id="+place_id;

//        dialog = new ProgressDialog(this);
//        dialog.show(this, "", "Fetching results");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest (Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i("http","success");
                            details = response.getJSONObject("result");
                            Log.i("results", details.toString());
//                            tv.setText(details.toString());

                            Intent intent = new Intent(getActivity(), DetailsActivity.class);
                            intent.putExtra(PLACE_ID_EXTRA, place_id);
                            intent.putExtra(PLACE_DETAIL_EXTRA, details.toString());
//                            dialog.dismiss();
                            startActivity(intent);
                        } catch (JSONException e) {
                            Log.i("details http","JSONException");
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
                Log.i("http","error", error);
//                    VolleyLog.d(TAG, "Error: " + error.getMessage());
////                Toast.makeText(getApplicationContext(),
////                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        sortResults();
        updateView(results);
        mAdapter.notifyDataSetChanged();

    }
//
//    public void updateButton() {
//        String token = next_page_token;
//        if ("".equals(token)) {
//            Log.i("updateButton","noNextPage");
//            rootView.findViewById(R.id.next).setEnabled(false);
//        }
//        else {
//            Log.i("updateButton","hasNextPage");
//            rootView.findViewById(R.id.next).setEnabled(true);
//        }
//    }
//
//    public void onClick(View v) {
//        if (v.getId() == R.id.previous) {
//            try {
//                currPage--;
//                places = allPlaces.getJSONArray(currPage);
//                updateView(places);
//                if (currPage == 0) rootView.findViewById(R.id.previous).setEnabled(false);
//                else rootView.findViewById(R.id.previous).setEnabled(true);
//                rootView.findViewById(R.id.next).setEnabled(true);
//            }
//            catch (Exception e) {
//                Log.i("clickprevious", "JSONException");
//            }
//        }
//        else if (v.getId() == R.id.next) {
//            if (currPage < allPlaces.length() - 1 ) {
//                try {
//                    currPage++;
//                    places = allPlaces.getJSONArray(currPage);
//                    updateView(places);
//                    rootView.findViewById(R.id.previous).setEnabled(true);
//                }
//                catch (Exception e) {
//                    Log.i("clicknext", "JSONException");
//                }
//            }
//            else {
//                RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
//
//                String url = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com/next_page?next_page_token=" + next_page_token;
//                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                try {
//                                    Log.i("http", "success");
//                                    JSONArray results = response.getJSONArray("results");
//                                    currPage++;
//                                    places = results;
//                                    allPlaces.put(places);
//                                    rootView.findViewById(R.id.previous).setEnabled(true);
//
//                                    updateView(places);
//
//                                    if (response.has("next_page_token")) {
//                                        next_page_token = response.getString("next_page_token");
//                                        updateButton();
//                                    }
//                                    else {
//                                        next_page_token = "";
//                                        updateButton();
//                                    }
//                                    //                            Log.i("results", results.toString());
//                                } catch (JSONException e) {
//                                    Log.i("http", "JSONException");
//                                    e.printStackTrace();
//                                    //                            Toast.makeText(getApplicationContext(),
//                                    //                                    "Error: " + e.getMessage(),
//                                    //                                    Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO: Handle error
//                        Log.i("http", "error");
//                        //                    VolleyLog.d(TAG, "Error: " + error.getMessage());
//                        ////                Toast.makeText(getApplicationContext(),
//                        ////                        error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                requestQueue.add(jsonObjectRequest);
//
//            }
//        }
//    }
//
//    public void onFavClick(View v) {
//        Log.i("favBtn","Click");
//    }


}

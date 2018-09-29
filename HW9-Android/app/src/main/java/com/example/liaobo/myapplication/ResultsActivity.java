package com.example.liaobo.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.Image;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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

import java.net.URL;
import java.sql.Timestamp;

import static android.view.View.generateViewId;

public class ResultsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    //    private RecyclerView.Adapter mAdapter;
    private ResultsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private JSONArray places;
    private String next_page_token;
    private JSONArray allPlaces;
    private int currPage;
    public static final String PLACE_ID_EXTRA = "com.example.myapplication.PLACE_ID";
    public static final String PLACE_DETAIL_EXTRA = "com.example.myapplication.PLACE_DETAIL_EXTRA";
    public static final String  MY_PREFS_NAME = "favorites";
    private JSONObject details;
    private String place_id;
    private ProgressDialog dialog;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

        }

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(SearchFragment.SEARCH_RESULTS_EXTRA);

        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        // Capture the layout's TextView and set the string as its text
//        TextView textView = findViewById(R.id.textViewDisplay);
//        textView.setText(message);

//        displayMessage(message);

        try {
            JSONObject data = new JSONObject(message);
            JSONArray results = data.getJSONArray("results");
            places = results;
            currPage = 0;
            allPlaces = new JSONArray();
            allPlaces.put(places);
            findViewById(R.id.previous).setEnabled(false);

            updateView(results);

            if (data.has("next_page_token")) {
                next_page_token = data.getString("next_page_token");
//                updateButton();
                findViewById(R.id.next).setEnabled(true);
            }
            else {
                next_page_token = "";
                findViewById(R.id.next).setEnabled(false);
//                updateButton();
            }

        }
        catch (Exception e) {
            Log.i("ResultsActivity", "JSONException", e);
        }

    }

    public void updateView (JSONArray results) {

        if (results == null || results.length() == 0) {
            LinearLayout has_results = findViewById(R.id.has_results);
            has_results.setVisibility(LinearLayout.GONE);
            TextView tv = new TextView(this);
            tv.setText("No Results");
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            tv.setLayoutParams(parms);
            tv.setGravity(Gravity.CENTER);
            LinearLayout no_photo = findViewById(R.id.no_results);
            no_photo.addView(tv);
        }
        else {

            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new ResultsAdapter(this, results);

            mAdapter.setOnItemClickLitener(new ResultsAdapter.OnItemClickLitener() {

                @Override
                public void onItemClick(View view, int position) {
//                    Toast.makeText(ResultsActivity.this, position + " click",
//                            Toast.LENGTH_SHORT).show();
                    try {
                        place_id = places.getJSONObject(position).getString("place_id");
//                        Intent intent = new Intent(ResultsActivity.this, DetailsActivity.class);
//                        intent.putExtra(PLACE_ID_EXTRA, place_id);
//                        startActivity(intent);
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
                        place_id = places.getJSONObject(position).getString("place_id");
                        ImageButton fav = (ImageButton) view.findViewById(R.id.fav_num);
                        if (prefs.contains(place_id)) {
                            fav.setImageResource(R.drawable.heart_outline_black);
                            editor.remove(place_id);
                            editor.apply();
                        }
                        else {
                            fav.setImageResource(R.drawable.heart_fill_red);
                            JSONObject place = places.getJSONObject(position);
                            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                            place.put("timestamp", timestamp);
                            if (!prefs.contains(place_id)) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(place_id, place.toString());
                                editor.commit();
                            }
                            Toast.makeText(ResultsActivity.this, position + " was added to favorites",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (JSONException e) {
                        Toast.makeText(ResultsActivity.this, "click fav JSONException",
                                Toast.LENGTH_SHORT).show();
                    }

                }

            });

            mRecyclerView.setAdapter(mAdapter);
        }

    }

//    public void updateButton() {
//        String token = next_page_token;
//        if ("".equals(token)) {
//            Log.i("updateButton","noNextPage");
//            findViewById(R.id.next).setEnabled(false);
//        }
//        else {
//            Log.i("updateButton","hasNextPage");
//            findViewById(R.id.next).setEnabled(true);
//        }
//    }

    public void onClick(View v) {
        if (v.getId() == R.id.previous) {
            try {
                currPage--;
                places = allPlaces.getJSONArray(currPage);
                updateView(places);
                if (currPage == 0) findViewById(R.id.previous).setEnabled(false);
                else findViewById(R.id.previous).setEnabled(true);
                findViewById(R.id.next).setEnabled(true);
//                Log.i("currentpage",String.valueOf(currPage));
//                Log.i("allpagelength",String.valueOf(allPlaces.length()));
            }
            catch (Exception e) {
                Log.i("clickprevious", "JSONException");
            }
        }
        else if (v.getId() == R.id.next) {
            if (currPage < allPlaces.length() - 1 ) {
                try {
                    currPage++;
                    places = allPlaces.getJSONArray(currPage);
                    updateView(places);
                    findViewById(R.id.previous).setEnabled(true);
                    if (currPage == allPlaces.length() - 1 && next_page_token.equals("")) {
                        findViewById(R.id.next).setEnabled(false);
                    }
//                    Log.i("currentpage",String.valueOf(currPage));
//                    Log.i("allpagelength",String.valueOf(allPlaces.length()));
                }
                catch (Exception e) {
                    Log.i("clicknext", "JSONException");
                }
            }
            else {
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                String url = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com/next_page?next_page_token=" + next_page_token;
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.i("http", "success");
                                    JSONArray results = response.getJSONArray("results");
                                    currPage++;
                                    places = results;
                                    allPlaces.put(places);
                                    findViewById(R.id.previous).setEnabled(true);
//                                    Log.i("currentpage",String.valueOf(currPage));
//                                    Log.i("allpagelength",String.valueOf(allPlaces.length()));

                                    updateView(places);

                                    if (response.has("next_page_token")) {
                                        next_page_token = response.getString("next_page_token");
//                                        Log.i("clickNextandFetch has",next_page_token);
//                                        Log.i("updateButton","hasNextPage");
                                        findViewById(R.id.next).setEnabled(true);
//                                        updateButton();
                                    }
                                    else {
                                        next_page_token = "";
//                                        Log.i("clickNextandFetch doesn't have","empty");
//                                        Log.i("updateButton","noNextPage");
                                        findViewById(R.id.next).setEnabled(false);
//                                        updateButton();
                                    }
                                    //                            Log.i("results", results.toString());
                                } catch (JSONException e) {
                                    Log.i("http", "JSONException");
                                    e.printStackTrace();
                                    //                            Toast.makeText(getApplicationContext(),
                                    //                                    "Error: " + e.getMessage(),
                                    //                                    Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.i("http", "error");
                        //                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                        ////                Toast.makeText(getApplicationContext(),
                        ////                        error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(jsonObjectRequest);

            }
        }
    }

//    public void onFavClick(View v) {
//        Log.i("favBtn","Click");
//    }

    public void getDetails() {

        dialog = new ProgressDialog(this);
        dialog.show(this, "", "Fetching results",false,true);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

//        String url = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com/details";
        String url = "http://aobolihw9-env.us-east-2.elasticbeanstalk.com/details?place_id="+place_id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest (Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            Log.i("http","success");
                            details = response.getJSONObject("result");
                            Log.i("results", details.toString());
//                            tv.setText(details.toString());
//                            dialog.hide();
                            Intent intent = new Intent(ResultsActivity.this, DetailsActivity.class);
                            intent.putExtra(PLACE_ID_EXTRA, place_id);
                            intent.putExtra(PLACE_DETAIL_EXTRA, details.toString());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            dialog.dismiss();

                            startActivity(intent);

//                            if (dialog.isShowing())
//                                dialog.dismiss();

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
                dialog.dismiss();
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
        if(dialog != null) {
            dialog.dismiss();
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(dialog != null) {
            dialog.dismiss();
        }
    }

}

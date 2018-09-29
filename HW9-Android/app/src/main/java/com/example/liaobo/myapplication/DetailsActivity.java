package com.example.liaobo.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

    private String place_id;
    private JSONObject details;
    TextView tv;

    private ActionBar actionBar;
    private ViewPager mViewPager;
    private DetailsFragmentPagerAdapter mAdapter;
    private ArrayList<View> mViews;
    private ArrayList<ActionBar.Tab> mTabs;
    private String shareText;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private static final String SELECTED_ITEM = "selected_item";
    public static final String  MY_PREFS_NAME = "favorites";

    public Bundle bundle;

//    public static final String INFO_EXTRA = "com.example.myapplication.INFO";
//    public static final String PHOTOS_EXTRA = "com.example.myapplication.PHOTOS";
//    public static final String MAP_EXTRA = "com.example.myapplication.MAP";
//    public static final String REVIEWS_EXTRA = "com.example.myapplication.REVIEWS";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
//        place_id = intent.getStringExtra(ResultsActivity.PLACE_ID_EXTRA);
        place_id = intent.getStringExtra(ResultsActivity.PLACE_ID_EXTRA);
        String detailsStr = intent.getStringExtra(ResultsActivity.PLACE_DETAIL_EXTRA);
        Log.i("detailsStr", detailsStr);

//        tv = (TextView)findViewById(R.id.textView001);
//        tv.setText(place_id);

        bundle = new Bundle();
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();
//        getDetails();
        try {
            details = new JSONObject(detailsStr);
            actionBar.setTitle(details.getString("name"));
        }
        catch (JSONException e) {

        }
        updateInfo();
        updatePhotos();
        updateMap();
        updateReviews();
        initialize();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void initialize() {
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mTabs=new ArrayList<ActionBar.Tab>();
        // Three tab to display in actionbar
        ActionBar.Tab tab0 = actionBar.newTab().setText("INFO").setIcon(R.drawable.info_outline).setTabListener(this);
        ActionBar.Tab tab1 = actionBar.newTab().setText("PHOTOS").setIcon(R.drawable.photos).setTabListener(this);
        ActionBar.Tab tab2 = actionBar.newTab().setText("MAP").setIcon(R.drawable.maps).setTabListener(this);
        ActionBar.Tab tab3 = actionBar.newTab().setText("REVIEWS").setIcon(R.drawable.review).setTabListener(this);
        actionBar.addTab(tab0);
        actionBar.addTab(tab1);
        actionBar.addTab(tab2);
        actionBar.addTab(tab3);
        mTabs.add(tab0);
        mTabs.add(tab1);
        mTabs.add(tab2);
        mTabs.add(tab3);

        //获取ViewPager
        mViewPager=(ViewPager)findViewById(R.id.detailsViewPager);
        //初始化mViews
        mViews=new ArrayList<View>();
        mViews.add(LayoutInflater.from(this).inflate(R.layout.fragment_info, null));
        mViews.add(LayoutInflater.from(this).inflate(R.layout.fragment_photos, null));
        mViews.add(LayoutInflater.from(this).inflate(R.layout.fragment_map, null));
        mViews.add(LayoutInflater.from(this).inflate(R.layout.fragment_reviews, null));
        //初始化mAdapter
//        mAdapter=new MainFragmentPagerAdapter(mViews);
        mAdapter = new DetailsFragmentPagerAdapter(getSupportFragmentManager(), bundle);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        //默认显示第零项
        mViewPager.setCurrentItem(0);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details_activity_actions, menu);
        if (prefs.contains(place_id)) {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.heart_fill_white);
        }
        else {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.heart_outline_white);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
                this.finish();
                break;
            case R.id.action_share:
                Uri uri = Uri.parse("https://twitter.com/intent/tweet?text="+shareText);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
//                loadUrl("www.google.com");
                break;
            case R.id.action_favorite:
//                ImageButton fav = (ImageButton) findViewById(R.id.action_favorite);
                if (prefs.contains(place_id)) {
                    item.setIcon(R.drawable.heart_outline_white);
                    editor.remove(place_id);
                    editor.apply();
                }
                else {
                    try {
                        item.setIcon(R.drawable.heart_fill_white);
                        String id = details.getString("place_id");
                        String icon = details.getString("icon");
                        String name = details.getString("name");
                        String address = details.getString("vicinity");
//                    Place place = new Place(id, icon, name, address, false);
                        JSONObject place = new JSONObject();
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        place.put("place_id", place_id);
                        place.put("icon", icon);
                        place.put("name", name);
                        place.put("vicinity", address);
                        place.put("timestamp", timestamp);
                        if (!prefs.contains(place_id)) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(place_id, place.toString());
                            editor.commit();
                        }
                    }
                    catch (JSONException e) {}
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        if (savedInstanceState.containsKey(SELECTED_ITEM))
        {
            if (getActionBar() != null) {
                // 选中前面保存的索引对应的Fragment页
                getActionBar().setSelectedNavigationItem(
                        savedInstanceState.getInt(SELECTED_ITEM));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // 将当前选中的Fragment页的索引保存到Bundle中
        if (getActionBar() != null) {
            outState.putInt(SELECTED_ITEM,
                    getActionBar().getSelectedNavigationIndex());
//            overriding method should call super.onsaveinstancestate
            super.onSaveInstanceState(outState);
        }

    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
        Log.i(String.valueOf("tab"+tab.getPosition()),"click");
        if(mViewPager!=null)
        {
            mViewPager.setCurrentItem(tab.getPosition());
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0)
    {

    }


    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2)
    {

    }


    @Override
    public void onPageSelected(int Index)
    {
        //设置当前要显示的View
        mViewPager.setCurrentItem(Index);
        //选中对应的Tab
        actionBar.selectTab(mTabs.get(Index));
    }

//    public void getDetails() {
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//
////        String url = "http://aobolihw8-env.us-west-1.elasticbeanstalk.com/details";
////        String url = "http://localhost:8080/details?place_id="+place_id;
//        String url = "http://aobolihw9-env.us-east-2.elasticbeanstalk.com/details?place_id="+place_id;
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest (Request.Method.GET, url, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            Log.i("http","success");
//                            details = response.getJSONObject("result");
////                            Log.i("results", results.toString());
////                            tv.setText(details.toString());
//                            actionBar.setTitle(details.getString("name"));
//                            updateInfo();
//                            updatePhotos();
//                            updateMap();
//                            updateReviews();
//                            initialize();
//                        } catch (JSONException e) {
//                            Log.i("details http","JSONException");
//                            e.printStackTrace();
////                            Toast.makeText(getApplicationContext(),
////                                    "Error: " + e.getMessage(),
////                                    Toast.LENGTH_LONG).show();
//                        }
//                    }
//                } , new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                // TODO: Handle error
//                Log.i("http","error", error);
////                    VolleyLog.d(TAG, "Error: " + error.getMessage());
//////                Toast.makeText(getApplicationContext(),
//////                        error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//        requestQueue.add(jsonObjectRequest);
//    }
    public void updateInfo() {
        JSONObject info = new JSONObject();
        try {
            if (details.has("name")) info.put("name", details.getString("name"));
            if (details.has("formatted_address")) info.put("address", details.getString("formatted_address"));
            if (details.has("formatted_phone_number")) info.put("phone", details.getString("formatted_phone_number"));
            if (details.has("price_level")) info.put("price_level", details.getString("price_level"));
            if (details.has("rating")) info.put("rating", details.getString("rating"));
            if (details.has("url")) info.put("url", details.getString("url"));
            if (details.has("website")) info.put("website", details.getString("website"));
            String url;
            if (details.has("website")) {
                url = details.getString("website");
            }
            else {
                url = details.getString("url");
            }
            shareText = "Check out "+ URLEncoder.encode(details.getString("name"))+" located at "+details.getString("formatted_address")+". "+"Website:"+
                    "&url="+URLEncoder.encode(url)+"&hashtags=TravelAndEntertainmentSearch";
//            Intent intent = new Intent(this, InfoFragment.class);
//            intent.putExtra(INFO_EXTRA, info.toString());
            bundle.putString("info", info.toString());
//            InfoFragment fragobj = new InfoFragment();
//            fragobj.setArguments(bundle);
        }
        catch (JSONException e) {
            Log.i("updateinfo", "JSONException", e);
        }

    }
    public void updatePhotos() {
//        try {
//            JSONArray photos = details.getJSONArray("photos");
//            bundle.putString("photos", photos.toString());
            bundle.putString("place_id", place_id);
            PhotosFragment fragobj = new PhotosFragment();
            fragobj.setArguments(bundle);
//        }
//        catch (JSONException e) {
//            Log.i("updatephotos","JSONException",e);
//        }
    }
    public void updateMap() {
//        Intent intent = new Intent(this, MapFragment.class);
//        intent.putExtra(MAP_EXTRA, place_id);
        bundle.putString("details", details.toString());
        MapFragment fragobj = new MapFragment();
        fragobj.setArguments(bundle);
    }
    public void updateReviews() {
        try {
            JSONArray reviews = details.getJSONArray("reviews");
            bundle.putString("reviews", reviews.toString());
//            JSONArray address = details.getJSONArray("formatted_address");
//            bundle.putString("formatted_address", address.toString());
        }
        catch (JSONException e) {
            Log.i("updatereviews","JSONException",e);
            bundle.putString("reviews", "");
        }
            ReviewsFragment fragobj = new ReviewsFragment();
            fragobj.setArguments(bundle);

    }
}

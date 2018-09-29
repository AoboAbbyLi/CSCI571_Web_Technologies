package com.example.liaobo.myapplication;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


public class InfoFragment extends Fragment {

    private static View rootView;
    private JSONObject info;
//    private ClipData.Item shareIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_info, container, false);

        String infoStr = getArguments().getString("info");

        try {
            info = new JSONObject(infoStr);
//            TextView tv = rootView.findViewById(R.id.info_textview);
//            tv.setText(info.toString());
            drawTable();
        }
        catch (JSONException e) {
            Log.i("info","JSONException", e);
        }
        // Inflate the layout for this fragment

        return rootView;
    }

    public void drawTable() {
        TableLayout tl = (TableLayout)rootView.findViewById(R.id.info_table);
        try {
            if (info.has("address")) {
                TableRow tr = drawRow("Address", info.getString("address"));
                tl.addView(tr);
            }
            if (info.has("phone")) {
                TableRow tr = drawRow("Phone Number", info.getString("phone"));
                tl.addView(tr);
            }
            if (info.has("price_level")) {
                String str = info.getString("price_level");
                String price = "";
                if (!"".equals(str)) {
                    int n = Integer.parseInt(str);
                    for (int i = 0; i < n; i++) price += "$";
                }
                TableRow tr = drawRow("Price Level", price);
                tl.addView(tr);
            }
            if (info.has("rating")) {
                TableRow tr = drawRow("Rating", info.getString("rating"));
                tl.addView(tr);
            }
            if (info.has("url")) {
                TableRow tr = drawRow("Google Page", info.getString("url"));
                tl.addView(tr);
            }
            if (info.has("website")) {
                TableRow tr = drawRow("Website", info.getString("website"));
                tl.addView(tr);
            }
        }
        catch (JSONException e) {
            Log.i("drawTable","JSONException", e);
        }

    }

    public TableRow drawRow(String key, String value) {
        TableRow tr = new TableRow(getActivity());
        TableLayout.LayoutParams tableRowParams;
//        if (!key.equals("Rating")) {
            tableRowParams = new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
//        }
//        else {
//            tableRowParams = new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 50);
//        }
        if (!key.equals("Rating")) {
            tableRowParams.setMargins(0, 0, 0, 30);
        }
        else {
            tableRowParams.setMargins(0,0,0,-50);
        }
        tr.setLayoutParams(tableRowParams);
//        tr.setGravity(Gravity.CENTER);
//        tr.setWeightSum(4); //total row weight

        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f);;
//        lp.weight = 1; //column weight
        TextView tv1 = new TextView(getActivity());
//        lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.6f);
        lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.3f);
        tv1.setText(key);
        tv1.setLayoutParams(lp);
        tv1.setTypeface(null, Typeface.BOLD);
        tr.addView(tv1);

        if (key.equals("Rating")) {
            LinearLayout ll = new LinearLayout(getActivity());
            lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f);
            lp.setMargins(0,0,0,0);
            ll.setLayoutParams(lp);
//            RatingBar ratingBar = new RatingBar(getActivity(), null, android.R.attr.ratingBarStyleSmall);

            RatingBar ratingBar = new RatingBar(getActivity());
            ratingBar.setNumStars(5);
//            ratingBar.setMax(5);
            ratingBar.setStepSize(0.1f);
            ratingBar.setRating(Float.parseFloat(value));
            ratingBar.setIsIndicator(true);
            ratingBar.setSecondaryProgress(0);
            ratingBar.setScaleX(0.5f);
            ratingBar.setScaleY(0.5f);
            ratingBar.setPivotX(0);
            ratingBar.setPivotY(0);
//            lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f);
////            lp.setMargins(0,0,0,0);
//            lp.gravity = Gravity.LEFT;
//            ratingBar.setLayoutParams(lp);
//            RatingBar ratingBar = new RatingBar(getActivity(), null, R.style.CustomRatingBar);
//            RatingBar ratingBar = new RatingBar(getActivity(), null, android.R.attr.ratingBarStyleSmall);
//            ratingBar.setRating(Float.parseFloat(value));
//            ratingBar.setIsIndicator(true);
            lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
//            lp.setMargins(0,0,0,0);
//            lp.gravity = Gravity.LEFT;
            ratingBar.setLayoutParams(lp);

            ll.addView(ratingBar);

            tr.addView(ll);
        }
        else {
            TextView tv2 = new TextView(getActivity());
//        lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.4f);
            lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f);
            tv2.setText(value);
            tv2.setLayoutParams(lp);
            if (key.equals("Phone Number") || key.equals("Google Page") || key.equals("Website")) {
                Linkify.addLinks(tv2, Linkify.ALL);
            }
            tr.addView(tv2);
        }

        return tr;
    }

}

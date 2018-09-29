package com.example.liaobo.myapplication;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.telecom.Call;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotosFragment extends Fragment {

    private static View rootView;
//    private JSONArray photos;
    private String place_id;
    LinearLayout linearLayout;

    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_photos, container, false);

//        String photosStr = getArguments().getString("photos");
//
//        try {
//            photos = new JSONArray(photosStr);
////            TextView tv = rootView.findViewById(R.id.info_textview);
////            tv.setText(info.toString());
//            drawPhotos();
//        }
//        catch (JSONException e) {
//            Log.i("info","JSONException", e);
//        }

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);

        place_id = getArguments().getString("place_id");

        linearLayout = (LinearLayout)rootView.findViewById(R.id.photo_linear);

        getPhotos();

        return rootView;
    }

    // Request photos and metadata for the specified place.
    private void getPhotos() {
        final String placeId = place_id;
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);

        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() == 0) {
                    if (getActivity() != null) {
                        TextView tv = new TextView(getActivity());
                        tv.setText("No Photos");
                        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        tv.setLayoutParams(parms);
                        tv.setGravity(Gravity.CENTER);
                        LinearLayout no_photo = rootView.findViewById(R.id.no_photo);
                        no_photo.addView(tv);
                    }
                } else {
                    for (int i = 0; i < photoMetadataBuffer.getCount(); i++) {
                        // Get the first photo in the list.
                        PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);
                        // Get the attribution text.
                        CharSequence attribution = photoMetadata.getAttributions();
                        // Get a full-size bitmap for the photo.
                        Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                                PlacePhotoResponse photo = task.getResult();
                                Bitmap bitmap = photo.getBitmap();
                                if (getActivity() != null) {
                                    ImageView image = new ImageView(getActivity());
                                    image.setImageBitmap(bitmap);
                                    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    parms.setMargins(0, 25, 0, 25);
                                    image.setLayoutParams(parms);
                                    image.setAdjustViewBounds(true);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    linearLayout.addView(image);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

}

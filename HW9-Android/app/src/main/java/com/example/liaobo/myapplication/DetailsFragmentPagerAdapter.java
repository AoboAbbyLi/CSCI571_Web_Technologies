package com.example.liaobo.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DetailsFragmentPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 4;
    private Bundle args;

    /** Constructor of the class */
    public DetailsFragmentPagerAdapter(FragmentManager fm, Bundle args) {
        super(fm);
        this.args = args;
    }

    /** This method will be invoked when a page is requested to create */
    @Override
    public Fragment getItem(int index) {
//        Bundle data = new Bundle();
//        Fragment fragment = null;
        switch(index){
            /** Search tab is selected */
            case 0:
                InfoFragment infoFragment = new InfoFragment();
                infoFragment.setArguments(args);
                return infoFragment;
//                fragment = new InfoFragment();
//                break;

            /** Favorites tab is selected */
            case 1:
                PhotosFragment photosFragment = new PhotosFragment();
                photosFragment.setArguments(args);
                return photosFragment;
//                fragment = new PhotosFragment();
//                break;
            case 2:
                MapFragment mapFragment = new MapFragment();
                mapFragment.setArguments(args);
                return mapFragment;
//                fragment = new MapFragment();
//                break;
            case 3:
                ReviewsFragment reviewsFragment = new ReviewsFragment();
                reviewsFragment.setArguments(args);
                return reviewsFragment;
//                fragment = new ReviewsFragment();
//                break;
        }
//        return fragment;
        return null;
    }

    /** Returns the number of pages */
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}

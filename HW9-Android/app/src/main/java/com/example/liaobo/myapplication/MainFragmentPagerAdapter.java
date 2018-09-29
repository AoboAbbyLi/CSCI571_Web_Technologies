package com.example.liaobo.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter{

    final int PAGE_COUNT = 2;

    /** Constructor of the class */
    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /** This method will be invoked when a page is requested to create */
    @Override
    public Fragment getItem(int arg0) {
//        Bundle data = new Bundle();
        Fragment fragment = null;
        switch(arg0){
            /** Search tab is selected */
            case 0:
//                AndroidFragment androidFragment = new AndroidFragment();
//                data.putInt("current_page", arg0+1);
//                androidFragment.setArguments(data);
//                return androidFragment;
                fragment = new SearchFragment();
                break;

            /** Favorites tab is selected */
            case 1:
//                AppleFragment appleFragment = new AppleFragment();
//                data.putInt("current_page", arg0+1);
//                appleFragment.setArguments(data);
//                return appleFragment;
                fragment = new FavoritesFragment();
                break;
        }
        return fragment;
    }

    /** Returns the number of pages */
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}

package com.example.liaobo.myapplication;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.*;
import android.view.View;
import android.view.LayoutInflater;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

//    Button searchButton;
//    Button favoritesButton;
    EditText input;

    private ActionBar actionBar;
    private ViewPager mViewPager;
    private MainFragmentPagerAdapter mAdapter;
    private ArrayList<View> mViews;
    private ArrayList<ActionBar.Tab> mTabs;
    private static final String SELECTED_ITEM = "selected_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mTabs=new ArrayList<ActionBar.Tab>();
        // Three tab to display in actionbar
        ActionBar.Tab tab0 = actionBar.newTab().setText("Search").setIcon(R.drawable.search).setTabListener(this);
        ActionBar.Tab tab1 = actionBar.newTab().setText("Favorites").setIcon(R.drawable.heart_fill_white).setTabListener(this);
        actionBar.addTab(tab0);
        actionBar.addTab(tab1);
        mTabs.add(tab0);
        mTabs.add(tab1);

        //获取ViewPager
        mViewPager=(ViewPager)findViewById(R.id.mainViewPager);
        //初始化mViews
        mViews=new ArrayList<View>();
        mViews.add(LayoutInflater.from(this).inflate(R.layout.fragment_search, null));
        mViews.add(LayoutInflater.from(this).inflate(R.layout.fragment_favorites, null));
        //初始化mAdapter
//        mAdapter=new MainFragmentPagerAdapter(mViews);
        mAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        //默认显示第零项
        mViewPager.setCurrentItem(0);
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

//    public void onClick(View v) {
        // TODO Auto-generated method stub
//        int id = v.getId();
//        if (id == R.id.searchButton) {
//            Log.i("search","click");
////            Toast.makeText(MainActivity.this, ((Button) v).getText(), Toast.LENGTH_SHORT).show();
//        }
//        else if (id == R.id.favoritesButton) {
//            Log.i("favorites","click");
////            Toast.makeText(MainActivity.this, ((Button) v).getText(), Toast.LENGTH_SHORT).show();
//        }
//    }
}

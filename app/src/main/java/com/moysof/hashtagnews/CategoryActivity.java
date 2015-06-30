package com.moysof.hashtagnews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.moysof.hashtagnews.util.DBHelper;
import com.moysof.hashtagnews.util.Util;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;

public class CategoryActivity extends AppCompatActivity {

    private android.support.v4.view.ViewPager pager;
    private MyPagerAdapter adapter;
    private int totalCount;
    public static Activity activity;
    private ArrayList<String> ids = new ArrayList<String>();
    private ArrayList<String> titles = new ArrayList<String>();
    private ArrayList<String> colors = new ArrayList<String>();
    private ArrayList<String> lastIdsTwitter = new ArrayList<String>();
    private ArrayList<String> lastIdsInstagram = new ArrayList<String>();
    private ArrayList<String> lastTimes = new ArrayList<String>();
    private SharedPreferences preferences;
    public static MenuItem menu_edit;
    public static int currentPosition = 0;
    public static View imageProgressBar;
    public static PhotoViewAttacher mAttacher;
    public static ImageView fullScreenImage;
    public static TabLayout tabs;
    private final static Handler handler = new Handler();
    public static ActionBar actionBar;

    public static Drawable oldBackground = null;
    public static int currentColor = 0xFF666666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);

        setContentView(R.layout.activity_category);

        activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setElevation(0);

        fullScreenImage = (ImageView) findViewById(R.id.fullScreenImage);
        imageProgressBar = findViewById(R.id.imageProgressBar);
        mAttacher = new PhotoViewAttacher(fullScreenImage);
        mAttacher.setMaximumScale(20f);

        SQLiteDatabase db = new DBHelper(CategoryActivity.this)
                .getReadableDatabase();
        Cursor c = db.query("categories", null, null, null, null, null, null);
        totalCount = 0;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        while (c.moveToNext()) {
            if (c.getString(c.getColumnIndex("id")).equals("1")) {
                if (preferences.getBoolean("news_category", true)) {
                    ids.add(c.getString(c.getColumnIndex("id")));
                    titles.add(c.getString(c.getColumnIndex("name")));
                    colors.add(c.getString(c.getColumnIndex("color")));
                    lastIdsTwitter.add(c.getString(c
                            .getColumnIndex("last_id_twitter")));
                    lastIdsInstagram.add(c.getString(c
                            .getColumnIndex("last_id_instagram")));
                    lastTimes.add(c.getString(c.getColumnIndex("last_time")));
                    totalCount++;
                }
            } else if (c.getString(c.getColumnIndex("id")).equals("2")) {
                if (preferences.getBoolean("loc_category", true)) {
                    ids.add(c.getString(c.getColumnIndex("id")));
                    titles.add(c.getString(c.getColumnIndex("name")));
                    colors.add(c.getString(c.getColumnIndex("color")));
                    lastIdsTwitter.add(c.getString(c
                            .getColumnIndex("last_id_twitter")));
                    lastIdsInstagram.add(c.getString(c
                            .getColumnIndex("last_id_instagram")));
                    lastTimes.add(c.getString(c.getColumnIndex("last_time")));
                    totalCount++;
                }
            } else {
                ids.add(c.getString(c.getColumnIndex("id")));
                titles.add(c.getString(c.getColumnIndex("name")));
                colors.add(c.getString(c.getColumnIndex("color")));
                lastIdsTwitter.add(c.getString(c
                        .getColumnIndex("last_id_twitter")));
                lastIdsInstagram.add(c.getString(c
                        .getColumnIndex("last_id_instagram")));
                lastTimes.add(c.getString(c.getColumnIndex("last_time")));
                totalCount++;
            }

        }
        c.close();
        db.close();

        tabs = (TabLayout) findViewById(R.id.tabs);
        pager = (android.support.v4.view.ViewPager) findViewById(R.id.pager);
        adapter = new MyPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);
        pager.setCurrentItem(getIntent().getIntExtra(
                "position", 0), false);

        tabs.setupWithViewPager(pager);

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        currentPosition = getIntent().getIntExtra("position", 0);

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        changeColor(currentColor);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onConfigurationChanged(Configuration config) {
        Log(config.orientation);
        if (config.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            ((TextView) getSupportActionBar().getCustomView().findViewById(
                    R.id.actionBar)).setPadding(toDP(5), toDP(5), 0, 0);
            Log("set portrait");
        } else {
            ((TextView) getSupportActionBar().getCustomView().findViewById(
                    R.id.actionBar)).setPadding(toDP(5), toDP(2), 0, 0);
            Log("set landscape");
        }
        super.onConfigurationChanged(config);
    }

    private int toDP(int size) {
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (size * scale + 0.5f);
        return dpAsPixels;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.category, menu);
        menu_edit = menu.findItem(R.id.menu_edit);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit:
                Log("currentPosition = " + currentPosition);
                if (ids.get(currentPosition).equals("2")) {
                    startActivity(new Intent(CategoryActivity.this,
                            CategoryLocationActivity.class).putExtra("position",
                            ids.get(currentPosition)));
                } else {
                    startActivity(new Intent(CategoryActivity.this,
                            CategoryEditActivity.class).putExtra("position",
                            ids.get(currentPosition)));
                    Log("sending pos = " + ids.get(currentPosition));
                }
                finish();
                return false;
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    private void changeColor(int newColor) {
        if (Util.isLollipop()) {
            float[] hsv = new float[3];
            Color.colorToHSV(newColor, hsv);
            hsv[2] *= 0.8f; // value component
            int darkerColor = Color.HSVToColor(hsv);
            getWindow().setStatusBarColor(darkerColor);
        }

        // change ActionBar color just if an ActionBar is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            Drawable colorDrawable = new ColorDrawable(newColor);

            if (oldBackground == null) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    colorDrawable.setCallback(drawableCallback);
                } else {
                    // getSupportActionBar().setBackgroundDrawable(colorDrawable);
                    findViewById(R.id.tabsWrapper).setBackgroundDrawable(
                            colorDrawable);
                }
            } else {

                TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                        oldBackground, colorDrawable});

                // workaround for broken ActionBarContainer drawable handling on
                // pre-API 17 builds
                // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    td.setCallback(drawableCallback);
                } else {
                    // getSupportActionBar().setBackgroundDrawable(td);
                    findViewById(R.id.tabsWrapper).setBackgroundDrawable(td);
                }

                td.startTransition(100);
            }

            oldBackground = colorDrawable;

        }

        Log("set color = " + currentColor);
        currentColor = newColor;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentColor", currentColor);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentColor = savedInstanceState.getInt("currentColor");
        changeColor(currentColor);
    }

    static Drawable.Callback drawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            actionBar.setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };

    public void onColorClicked(View v) {

        int color = Color.parseColor(v.getTag().toString());
        changeColor(color);

    }

    @Override
    public void onBackPressed() {
        if (fullScreenImage.getVisibility() == View.VISIBLE) {
            fullScreenImage.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }

    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return totalCount;
        }

        @Override
        public Fragment getItem(int position) {
            FragmentCategory f = FragmentCategory.newInstance();
            f.position = position;
            f.id = ids.get(position);
            f.title = titles.get(position);
            f.color = colors.get(position);
            f.lastIdTwitter = lastIdsTwitter.get(position);
            f.lastIdInstagram = lastIdsInstagram.get(position);
            f.lastTime = lastTimes.get(position);

            return f;
        }

    }

    public void switchDrawer(View v) {
        finish();
    }

    public static void Log(Object text) {
        Log.d("Log", text + "");
    }

}

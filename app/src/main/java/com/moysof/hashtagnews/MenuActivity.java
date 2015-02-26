package com.moysof.hashtagnews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MenuActivity extends ActionBarActivity {

    private GridView menuGrid;
    private ArrayList<String> ids = new ArrayList<String>();
    private ArrayList<String> titles = new ArrayList<String>();
    private ArrayList<String> hashtags = new ArrayList<String>();
    private ArrayList<String> counters = new ArrayList<String>();
    private ArrayList<String> colors = new ArrayList<String>();
    private ArrayList<String> lastIdsInstagram = new ArrayList<String>();
    private ArrayList<String> lastIdsTwitter = new ArrayList<String>();
    private ArrayList<String> lastTimes = new ArrayList<String>();
    private String countersUrl = "http://hashtagnews.net/app/counters.php";
    private AdapterMenu adapter;
    private SharedPreferences preferences;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    public static Drawable oldBackground = null;
    public static int currentColor = 0xFFff8b2f;
    private ActionBarDrawerToggle mDrawerToggle;
    private int selectedItem = 1;
    private AdapterDrawer mDrawerAdapter;
    private String gpsProvider;
    private String networkProvider;
    private AdView adView;
    private AlertDialog dialog;
    private int deletedPosition;
    private boolean undoIsHiden = true;
    private View undoLayout;
    public static MenuActivity activity;
    private String selectedId = "";
    private SwipeRefreshLayout refreshLayout;
    private FrameLayout mDrawerFrameLayout;
    private SharedPreferences mPrefs;
    private int mContextPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        toolbar.setContentInsetsAbsolute(
                CommonUtilities.convertDpToPixel(72, this), 0);
        setSupportActionBar(toolbar);

        overridePendingTransition(0, 0);

        activity = this;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerFrameLayout = (FrameLayout) findViewById(R.id.navigation_drawer);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(
                R.color.primary_dark));
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerAdapter = new AdapterDrawer(this, selectedItem);
        mDrawerList.setAdapter(mDrawerAdapter);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        View headerView = getLayoutInflater().inflate(
                R.layout.item_drawer_header, null);
        ((TextView) headerView.findViewById(R.id.drawerNameTxt)).setText(mPrefs
                .getString("name", ""));
        ((TextView) headerView.findViewById(R.id.drawerEmailTxt))
                .setText(mPrefs.getString("email", ""));

        if (mPrefs.getString("avatar", "").equals("")) {
            ((ImageView) headerView.findViewById(R.id.drawerAvatarImg))
                    .setImageResource(R.drawable.avatar_placeholder);
        } else {
            ImageLoader.getInstance()
                    .displayImage(
                            mPrefs.getString("avatar", "").toString(),
                            ((ImageView) headerView
                                    .findViewById(R.id.drawerAvatarImg)));
        }
        mDrawerList.addHeaderView(headerView);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.app_name, R.string.app_name) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setElevation(8f);

        menuGrid = (GridView) findViewById(R.id.menuGrid);

        deletedPosition = getIntent().getIntExtra("position", -1);

        adapter = new AdapterMenu(getApplicationContext(), titles, counters,
                colors, hashtags);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        menuGrid.setAdapter(adapter);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        refreshLayout.setColorSchemeResources(new int[]{R.color.primary,
                R.color.primary_dark, R.color.accent});

        LocationManager lm = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        gpsProvider = LocationManager.GPS_PROVIDER;
        networkProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocationGPS = lm.getLastKnownLocation(gpsProvider);
        Location lastKnownLocationWiFi = lm
                .getLastKnownLocation(networkProvider);
        if (lastKnownLocationGPS != null) {
            saveLocation(lastKnownLocationGPS.getLatitude(),
                    lastKnownLocationGPS.getLongitude());
        } else if (lastKnownLocationWiFi != null) {
            saveLocation(lastKnownLocationWiFi.getLatitude(),
                    lastKnownLocationWiFi.getLongitude());
        }

        if (lm.isProviderEnabled(networkProvider)) {
            lm.requestLocationUpdates(networkProvider, 0, 0,
                    new mylocationlistenerWifi());
        }

        adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        menuGrid.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    delete();
                    hideUndo();
                }
                return false;
            }
        });

        undoLayout = findViewById(R.id.undoLayout);

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        if (prefs.getBoolean("promtRate", true)) {
            int launchCount = prefs.getInt("launchCount", 0);
            launchCount = launchCount + 1;
            if (launchCount >= 15) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Rate HashtagNews");
                builder.setCancelable(false);
                builder.setMessage("If you enjoy using HashtagNews, please take a moment to rate the app. Thank you for your support!");
                builder.setPositiveButton("Rate", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (BaseApplication.isAmazonPhone()) {
                            Intent browserIntent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://www.amazon.com/MoyerSoftware-HashtagNews/dp/B00M8R7RC2"));
                            startActivity(browserIntent);
                        } else {
                            Intent browserIntent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.moysof.hashtagnews"));
                            startActivity(browserIntent);
                        }
                        prefs.edit().putBoolean("promtRate", false).commit();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Don't ask again",
                        new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                prefs.edit().putBoolean("promtRate", false)
                                        .commit();
                                dialog.dismiss();
                            }
                        });
                builder.setNeutralButton("Later", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
                dialog.show();
                launchCount = 0;
            }
            prefs.edit().putInt("launchCount", launchCount).commit();
        }
    }

    public void logOut(View v) {
        preferences.edit().clear().commit();
        startActivity(new Intent(MenuActivity.this, SplashActivity.class));
        finish();
    }

    private class mylocationlistenerWifi implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            saveLocation(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    public void showInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_internet,
                null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setNegativeButton("Exit", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
            }
        });
        builder.setPositiveButton("Retry", new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                getCounters();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    private void saveLocation(double latitude, double longitude) {
        preferences.edit().putString("lat", latitude + "")
                .putString("lng", longitude + "").commit();
        Log(latitude + ", " + longitude);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        mContextPos = Integer.parseInt(v.getTag() + "");

        if (ids.get(mContextPos).equals("2")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list_loc, menu);
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                if (ids.get(mContextPos).equals("1")) {
                    preferences.edit().putBoolean("news_category", false).commit();
                } else {
                    SQLiteDatabase db = new DBHelper(this).getWritableDatabase();
                    db.delete("categories", "id='" + ids.get(mContextPos)
                            + "'", null);
                    db.close();
                }
                Toast.makeText(this,
                        titles.get(mContextPos) + " category was removed",
                        Toast.LENGTH_SHORT).show();
                refreshList();
                return true;
            case R.id.hide:
                preferences.edit().putBoolean("loc_category", false).commit();
                refreshList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            if (mDrawerLayout.isDrawerOpen(mDrawerFrameLayout))
                mDrawerLayout.closeDrawer(mDrawerFrameLayout);
            else
                mDrawerLayout.openDrawer(mDrawerFrameLayout);
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                final int position, long id) {
            Log("position drawer clicked = " + position);

            mDrawerLayout.closeDrawer(mDrawerFrameLayout);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    MenuActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (position == 3) {
                                AdapterDrawer.selectedPos = position;
                                mDrawerAdapter.notifyDataSetChanged();
                                startActivity(new Intent(MenuActivity.this,
                                        CategoryAddActivity.class));
                            } else if (position == 4) {
                                AdapterDrawer.selectedPos = position;
                                mDrawerAdapter.notifyDataSetChanged();
                                startActivity(new Intent(MenuActivity.this,
                                        MenuDeleteActivity.class));
                            } else if (position == 6) {
                                startActivity(new Intent(MenuActivity.this,
                                        SettingsActivity.class));
                            } else if (position == 7) {
                                startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://www.hashtagnews.net/support.php")));
                            } else if (position == 8) {
                                String appPackageName = getPackageName();
                                if (BaseApplication.isAmazonPhone()) {
                                    Intent goToAppstore = new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("http://www.amazon.com/gp/mas/dl/andro"
                                                    + "id?p=" + appPackageName));
                                    goToAppstore
                                            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                                    | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                                                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                    startActivity(goToAppstore);
                                } else {
                                    Intent marketIntent = new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id="
                                                    + appPackageName));
                                    marketIntent
                                            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                                    | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                                                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                    startActivity(marketIntent);
                                }
                            }
                        }

                        ;
                    });
                }
            }, 500);
        }
    }

    public void switchDrawer(View v) {
        if (mDrawerLayout.isDrawerOpen(mDrawerFrameLayout))
            mDrawerLayout.closeDrawer(mDrawerFrameLayout);
        else
            mDrawerLayout.openDrawer(mDrawerFrameLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshList();

        AdapterDrawer.selectedPos = 1;
        mDrawerAdapter.notifyDataSetChanged();
    }

    private void refreshList() {
        ids.clear();
        titles.clear();
        colors.clear();
        counters.clear();
        hashtags.clear();
        SQLiteDatabase db = new DBHelper(this).getReadableDatabase();
        Cursor c = db.query("categories", null, null, null, null, null, null);
        while (c.moveToNext()) {
            if (c.getString(c.getColumnIndex("id")).equals("1")) {
                if (preferences.getBoolean("news_category", true)) {
                    ids.add(c.getString(c.getColumnIndex("id")));
                    titles.add(c.getString(c.getColumnIndex("name")));
                    colors.add(c.getString(c.getColumnIndex("color")));
                    hashtags.add(c.getString(c.getColumnIndex("hashtags")));
                    counters.add("");
                }
            } else if (c.getString(c.getColumnIndex("id")).equals("2")) {
                if (preferences.getBoolean("loc_category", true)) {
                    ids.add(c.getString(c.getColumnIndex("id")));
                    titles.add(c.getString(c.getColumnIndex("name")));
                    colors.add(c.getString(c.getColumnIndex("color")));
                    hashtags.add(c.getString(c.getColumnIndex("hashtags")));
                    counters.add("");
                }
            } else {
                ids.add(c.getString(c.getColumnIndex("id")));
                titles.add(c.getString(c.getColumnIndex("name")));
                colors.add(c.getString(c.getColumnIndex("color")));
                hashtags.add(c.getString(c.getColumnIndex("hashtags")));
                counters.add("");
            }
        }

        if (deletedPosition != -1 & ids.size() != 0) {

            selectedId = ids.get(deletedPosition);

            ids.remove(deletedPosition);
            titles.remove(deletedPosition);
            counters.remove(deletedPosition);
            hashtags.remove(deletedPosition);
            colors.remove(deletedPosition);

            if (selectedId.equals("2")) {
                ((TextView) findViewById(R.id.undoTitleTxt))
                        .setText("Category was hided");
                findViewById(R.id.undoDescTxt).setVisibility(View.VISIBLE);
            } else {
                ((TextView) findViewById(R.id.undoTitleTxt))
                        .setText("Category was removed");
                findViewById(R.id.undoDescTxt).setVisibility(View.GONE);
            }

            showUndo();
        }

        if (ids.size() == 0) {
            findViewById(R.id.emptyLayout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.emptyLayout).setVisibility(View.GONE);
            getCounters();
        }

        adapter.notifyDataSetChanged();

    }

    // Change alpha of a view from 0% to 100%
    public void showUndo() {
        if (undoIsHiden) {
            AlphaAnimation aa = new AlphaAnimation(0f, 1f);

            aa.setDuration(500);
            aa.setFillAfter(true);
            undoLayout.setVisibility(View.VISIBLE);
            undoLayout.startAnimation(aa);
            undoIsHiden = false;
        }
    }

    // Change alpha of a view from 100% to 0%
    public void hideUndo() {
        if (!undoIsHiden) {
            AlphaAnimation aa = new AlphaAnimation(1f, 0f);
            aa.setDuration(500);
            aa.setFillAfter(true);
            undoLayout.startAnimation(aa);
            undoLayout.setEnabled(false);
            undoIsHiden = true;
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    try {
                        MenuActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                undoLayout.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            }, 300);
        }
    }

    private void delete() {
        Log("delete1, id = " + selectedId);
        if (!selectedId.equals("")) {
            if (selectedId.equals("2")) {
                preferences.edit().putBoolean("loc_category", false).commit();
            } else {
                Log("delete2");
                SQLiteDatabase db = new DBHelper(
                        BaseApplication.getAppContext()).getWritableDatabase();
                db.delete("categories", "id='" + selectedId + "'", null);
                db.close();
            }
        }
        deletedPosition = -1;
        selectedId = "";
    }

    @Override
    public void onPause() {
        delete();
        hideUndo();
        super.onPause();
    }

    public void undo(View v) {
        if (selectedId.equals("2")) {
            preferences.edit().putBoolean("loc_category", true).commit();
        }
        deletedPosition = -1;
        refreshList();
        hideUndo();
        selectedId = "";
    }

    private void getCounters() {
        if (!isNetworkConnected()) {
            if (dialog == null) {
                showInternetDialog();
            } else if (!dialog.isShowing()) {
                showInternetDialog();
            } else {
                dialog.cancel();
                showInternetDialog();
            }
        } else {
            new getCountersTask().execute();
        }
    }

    class getCountersTask extends AsyncTask<String, Void, Void> {

        private JSONArray countersArray;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log("log1");
        }

        protected Void doInBackground(String... files) {
            Log("log2");
            try {
                SQLiteDatabase db = new DBHelper(MenuActivity.this)
                        .getReadableDatabase();
                Cursor c = db.query("categories", null, null, null, null, null,
                        null);
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put(
                        "radius",
                        PreferenceManager.getDefaultSharedPreferences(
                                MenuActivity.this).getInt("radius", 10));
                jsonRequest.put("id", PreferenceManager
                        .getDefaultSharedPreferences(MenuActivity.this)
                        .getString("id", ""));
                jsonRequest.put("lat", PreferenceManager
                        .getDefaultSharedPreferences(MenuActivity.this)
                        .getString("lat", ""));
                jsonRequest.put("lng", PreferenceManager
                        .getDefaultSharedPreferences(MenuActivity.this)
                        .getString("lng", ""));

                jsonRequest.put("twitter", PreferenceManager
                        .getDefaultSharedPreferences(MenuActivity.this)
                        .getBoolean("twitter", true));
                jsonRequest.put("instagram", PreferenceManager
                        .getDefaultSharedPreferences(MenuActivity.this)
                        .getBoolean("instagram", true));
                JSONObject preferencesJSON = new JSONObject();
                preferencesJSON.put("loc_category", PreferenceManager
                        .getDefaultSharedPreferences(MenuActivity.this)
                        .getBoolean("loc_category", true));
                preferencesJSON.put("news_category", PreferenceManager
                        .getDefaultSharedPreferences(MenuActivity.this)
                        .getBoolean("news_category", true));
                jsonRequest.put("preferences", preferencesJSON.toString());
                JSONArray jsonArray = new JSONArray();
                lastIdsInstagram.clear();
                lastIdsTwitter.clear();
                lastTimes.clear();
                while (c.moveToNext()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", c.getString(c.getColumnIndex("id")));
                    jsonObject.put("name",
                            c.getString(c.getColumnIndex("name")));
                    jsonObject.put(
                            "hashtags",
                            new JSONArray(c.getString(c
                                    .getColumnIndex("hashtags"))));
                    jsonObject.put("color",
                            c.getString(c.getColumnIndex("color")));
                    jsonObject.put("last_time",
                            c.getString(c.getColumnIndex("last_time")));
                    jsonObject.put("last_id_instagram",
                            c.getString(c.getColumnIndex("last_id_instagram")));
                    Log("get couters with instagram id = "+c.getString(c.getColumnIndex("last_id_instagram")));

                    jsonObject.put("last_id_twitter",
                            c.getString(c.getColumnIndex("last_id_twitter")));
                    jsonArray.put(jsonObject);
                    lastIdsInstagram.add(c.getString(c
                            .getColumnIndex("last_id_instagram")));
                    lastIdsTwitter.add(c.getString(c
                            .getColumnIndex("last_id_twitter")));
                    lastTimes.add(c.getString(c.getColumnIndex("last_time")));
                }

                Log.d("idhash",
                        "since id = "
                                + lastIdsTwitter.get(lastIdsTwitter.size() - 1));
                jsonRequest.put("categories", jsonArray);
                DefaultHttpClient client = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(countersUrl);
                try {
                    StringEntity s = new StringEntity(jsonRequest.toString(),
                            "UTF-8");

                    s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                            "application/json"));
                    httpPost.setEntity(s);
                    HttpResponse response = client.execute(httpPost);
                    String responseString = EntityUtils.toString(response
                            .getEntity());
                    Log("response = "+responseString);
                    countersArray = new JSONArray(responseString);
                    counters.clear();

                    for (int i = 0; i < countersArray.length(); i++) {
                        counters.add(countersArray.getJSONObject(i).getString(
                                "number"));
                    }
                } catch (ClientProtocolException e) {
                    Log(e);
                } catch (IOException e) {
                    Log(e);
                }
            } catch (JSONException e) {
                Log(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log("log3");
            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        }
    }

    public void addCategory(View v) {
        startActivity(new Intent(MenuActivity.this, CategoryAddActivity.class));
    }

    public static void Log(Object text) {
        Log.d("Log", text + "");
    }

}

package com.moysof.hashtagnews;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Locale;

public class CategoryAddActivity extends ActionBarActivity {

    private String color = "#F44336";
    private ActionBar actionBar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private AlertDialog dialog;
    private ArrayList<String> hashtags = new ArrayList<String>();
    private AdapterHashtags hashtagAdapter;
    private AdapterDrawer mDrawerAdapter;
    private int selectedItem = 2;
    private SharedPreferences mPrefs;
    private FrameLayout mDrawerFrameLayout;
    public static ListView hashtagsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
        toolbar.setContentInsetsAbsolute(
                CommonUtilities.convertDpToPixel(72, this), 0);
        setSupportActionBar(toolbar);

        overridePendingTransition(0, 0);

        color = "#F44336";
        ((CardView) findViewById(R.id.categoryColor))
                .setCardBackgroundColor(Color.parseColor(color));

        actionBar = getSupportActionBar();
        actionBar.setElevation(0f);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerFrameLayout = (FrameLayout) findViewById(R.id.navigation_drawer);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(
                R.color.primary_dark));
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

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

        // Set the adapter for the list view
        mDrawerAdapter = new AdapterDrawer(this, selectedItem);
        mDrawerList.setAdapter(mDrawerAdapter);
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        hashtagAdapter = new AdapterHashtags(this, hashtags);
        hashtagsList = (ListView) findViewById(R.id.hashtagsList);
        hashtagsList.setAdapter(hashtagAdapter);

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public void switchDrawer(View v) {
        if (mDrawerLayout.isDrawerOpen(mDrawerFrameLayout))
            mDrawerLayout.closeDrawer(mDrawerFrameLayout);
        else
            mDrawerLayout.openDrawer(mDrawerFrameLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.category_add, menu);
        return super.onCreateOptionsMenu(menu);

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
        } else {
            create();
        }

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
            OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                final int position, long id) {

            mDrawerLayout.closeDrawer(mDrawerFrameLayout);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    CategoryAddActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (position == 2) {
                                AdapterDrawer.selectedPos = position;
                                mDrawerAdapter.notifyDataSetChanged();
                                MenuActivity.activity.finish();
                                startActivity(new Intent(
                                        CategoryAddActivity.this,
                                        MenuActivity.class));
                                finish();
                            } else if (position == 4) {
                                AdapterDrawer.selectedPos = position;
                                mDrawerAdapter.notifyDataSetChanged();
                                startActivity(new Intent(
                                        CategoryAddActivity.this,
                                        MenuDeleteActivity.class));
                            } else if (position == 6) {
                                AdapterDrawer.selectedPos = position;
                                mDrawerAdapter.notifyDataSetChanged();
                                startActivity(new Intent(
                                        CategoryAddActivity.this,
                                        SettingsActivity.class));
                                finish();
                            } else if (position == 7) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://www.hashtagnews.net")));
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
                    });
                }
            }, 500);
        }
    }

    public void openCategory(View v) {
        hidePopup(v);
        Intent categoryIntent = new Intent(CategoryAddActivity.this,
                CategoryEditActivity.class);
        startActivity(categoryIntent);
    }

    public void create() {
        if (((EditText) findViewById(R.id.categoryName)).getText().toString()
                .equals("")) {
            Toast.makeText(this, "Enter category name", Toast.LENGTH_LONG)
                    .show();
        } else if (hashtags.size() == 0) {
            Toast.makeText(this, "Add at least one hashtag", Toast.LENGTH_LONG)
                    .show();
        } else {
            SQLiteDatabase db = new DBHelper(this).getWritableDatabase();
            Cursor c = db
                    .query("categories",
                            null,
                            "name=? COLLATE NOCASE",
                            new String[]{(((EditText) findViewById(R.id.categoryName))
                                    .getText().toString())}, null, null, null);
            if (!c.moveToFirst()) {
                JSONArray hashtagsJSON = new JSONArray();
                for (String hashtag : hashtags) {
                    hashtagsJSON.put(hashtag);
                }
                ContentValues cv = new ContentValues();
                cv.put("name", ((EditText) findViewById(R.id.categoryName))
                        .getText().toString());
                cv.put("hashtags", hashtagsJSON.toString());
                cv.put("color", color);
                cv.put("last_id_twitter", "");
                cv.put("last_id_instagram", "");
                cv.put("last_time", "");
                db.insert("categories", null, cv);
                db.close();
                startActivity(new Intent(CategoryAddActivity.this,
                        MenuActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Category already exists",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void hidePopup(View v) {
        findViewById(R.id.popup).setVisibility(View.GONE);
    }

    public static void Log(Object text) {
        Log.d("Log", text + "");
    }

    public void pickColor(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_colors,
                null);
        ((GridView) dialogView.findViewById(R.id.colorsGrid))
                .setAdapter(new AdapterColors(this));
        ((GridView) dialogView.findViewById(R.id.colorsGrid))
                .setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int pos, long arg3) {
                        color = AdapterColors.colors[pos];
                        ((CardView) findViewById(R.id.categoryColor))
                                .setCardBackgroundColor(Color.parseColor(color));
                        dialog.dismiss();
                    }
                });
        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", null);
        dialog = builder.create();
        dialog.show();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
                MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth,
                        LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hashtagsList = null;
    }

    public void addHashtag(View v) {
        String hashtag = ((EditText) findViewById(R.id.categoryHashtags))
                .getText().toString().replaceAll("#", "").replaceAll(" ", "")
                .toLowerCase(Locale.getDefault());
        if (hashtag.equals("")) {
            Toast.makeText(this, "Hashtag is empty", Toast.LENGTH_LONG).show();
        } else {
            hashtags.add(((EditText) findViewById(R.id.categoryHashtags))
                    .getText().toString().replaceAll("#", "")
                    .replaceAll(" ", "").toLowerCase(Locale.getDefault()));
            hashtagAdapter.notifyDataSetChanged();
            findViewById(R.id.hashtagsList).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.categoryHashtags)).setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(
                    ((EditText) findViewById(R.id.categoryHashtags))
                            .getWindowToken(), 0);

            setListViewHeightBasedOnChildren(hashtagsList);
        }
    }

}

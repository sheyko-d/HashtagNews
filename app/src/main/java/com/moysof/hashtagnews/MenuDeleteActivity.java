package com.moysof.hashtagnews;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MenuDeleteActivity extends ActionBarActivity {

	private ListView menuList;
	private ArrayList<String> ids = new ArrayList<String>();
	private ArrayList<String> titles = new ArrayList<String>();
	private ArrayList<String> counters = new ArrayList<String>();
	private ArrayList<String> colors = new ArrayList<String>();
	private AdapterMenuDelete adapter;
	private SharedPreferences preferences;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	public static Drawable oldBackground = null;
	public static int currentColor = 0xFFff8b2f;
	private ActionBarDrawerToggle mDrawerToggle;
	private int selectedItem = 3;
	private AdapterDrawer mDrawerAdapter;
	private String gpsProvider;
	private String networkProvider;
	private AdView adView;
	private SharedPreferences mPrefs;
	private FrameLayout mDrawerFrameLayout;
	public static MenuDeleteActivity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_delete);

		Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
		toolbar.setContentInsetsAbsolute(
				CommonUtilities.convertDpToPixel(72, this), 0);
		setSupportActionBar(toolbar);

		getSupportActionBar().setElevation(0f);
		overridePendingTransition(0, 0);

		activity = this;

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

		menuList = (ListView) findViewById(R.id.menuList);
		adapter = new AdapterMenuDelete(getApplicationContext(), ids, titles,
				colors);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		menuList.setAdapter(adapter);

		adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

	}

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
			OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				final int position, long id) {

			mDrawerLayout.closeDrawer(mDrawerFrameLayout);

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					MenuDeleteActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (position == 2) {
								MenuActivity.activity.finish();
								AdapterDrawer.selectedPos = position;
								mDrawerAdapter.notifyDataSetChanged();
								startActivity(new Intent(
										MenuDeleteActivity.this,
										MenuActivity.class));
								finish();
							} else if (position == 3) {
								AdapterDrawer.selectedPos = position;
								mDrawerAdapter.notifyDataSetChanged();
								startActivity(new Intent(
										MenuDeleteActivity.this,
										CategoryAddActivity.class));
							} else if (position == 6) {
								AdapterDrawer.selectedPos = position;
								mDrawerAdapter.notifyDataSetChanged();
								startActivity(new Intent(
										MenuDeleteActivity.this,
										SettingsActivity.class));
							} else if (position == 7) {
								startActivity(new Intent(
										Intent.ACTION_VIEW,
										Uri.parse("http://www.moyersoftware.com/hashtagnews/support.php")));
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

		AdapterDrawer.selectedPos = 3;
		mDrawerAdapter.notifyDataSetChanged();
	}

	private void refreshList() {
		ids.clear();
		titles.clear();
		colors.clear();
		counters.clear();
		SQLiteDatabase db = new DBHelper(this).getReadableDatabase();
		Cursor c = db.query("categories", null, null, null, null, null, null);
		while (c.moveToNext()) {
			if (c.getString(c.getColumnIndex("id")).equals("1")) {
				if (preferences.getBoolean("news_category", true)) {
					ids.add(c.getString(c.getColumnIndex("id")));
					titles.add(c.getString(c.getColumnIndex("name")));
					colors.add(c.getString(c.getColumnIndex("color")));
					counters.add("");
				}
			} else if (c.getString(c.getColumnIndex("id")).equals("2")) {
				if (preferences.getBoolean("loc_category", true)) {
					ids.add(c.getString(c.getColumnIndex("id")));
					titles.add(c.getString(c.getColumnIndex("name")));
					colors.add(c.getString(c.getColumnIndex("color")));
					counters.add("");
				}
			} else {
				ids.add(c.getString(c.getColumnIndex("id")));
				titles.add(c.getString(c.getColumnIndex("name")));
				colors.add(c.getString(c.getColumnIndex("color")));
				counters.add("");
			}
		}
		if (ids.size() == 0) {
			findViewById(R.id.emptyLayout).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.emptyLayout).setVisibility(View.GONE);
		}
		adapter.notifyDataSetChanged();
	}

	public static void Log(Object text) {
		Log.d("Log", text + "");
	}

	public void addCategory(View v) {
		startActivity(new Intent(MenuDeleteActivity.this,
				CategoryAddActivity.class));
		finish();
	}

}

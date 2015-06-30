package com.moysof.hashtagnews;

import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.moysof.hashtagnews.util.DBHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class CategoryLocationActivity extends AppCompatActivity {

	private ActionBar actionBar;
	private ArrayList<String> hashtags = new ArrayList<String>();
	private String position = "0";
	private JSONArray hashtagsJSON;
	private String id;
	private SharedPreferences preferences;
	private SeekBar categoryRadius;
	private TextView categoryProgress;
	private Circle circle;
	private GoogleMap googleMap;
	private LatLng ME;
	public static ListView hashtagsList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category_location);

		Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setElevation(0f);
		actionBar = getSupportActionBar();

		SQLiteDatabase db = new DBHelper(this).getReadableDatabase();
		Cursor c = db.query("categories", null, null, null, null, null, null);
		position = getIntent().getStringExtra("position");

		Log("edit pos = " + position);
		while (c.moveToNext()) {
			if (c.getString(c.getColumnIndex("id")).equals(position)) {
				((TextView) findViewById(R.id.categoryName)).setText(c
						.getString(c.getColumnIndex("name")));
				id = c.getString(c.getColumnIndex("id"));
				try {
					hashtagsJSON = new JSONArray(c.getString(c
							.getColumnIndex("hashtags")));
				} catch (JSONException e) {
					Log(e);
				}
			}
		}
		for (int i = 0; i < hashtagsJSON.length(); i++) {
			try {
				hashtags.add(hashtagsJSON.getString(i));
			} catch (JSONException e) {
				Log(e);
			}
		}
		c.close();
		db.close();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		categoryRadius = (SeekBar) findViewById(R.id.categoryRadius);
		categoryProgress = (TextView) findViewById(R.id.categoryProgress);
		categoryProgress.setText(preferences.getInt("radius", 10) + " mi");
		categoryRadius.setProgress(preferences.getInt("radius", 10) - 1);
		categoryRadius
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					private int seekBarProgress;

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						if (!BaseApplication.isAmazonPhone()) {
							addCircle(seekBarProgress);
						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						seekBarProgress = progress + 1;
						categoryProgress.setText(seekBarProgress + " mi");
						preferences.edit().putInt("radius", seekBarProgress)
								.commit();
					}
				});

		Log(googleMap);

	}

	@Override
	public void onStart() {
		super.onStart();
		if (!BaseApplication.isAmazonPhone()) {
			MapFragment mapFragment = MapFragment.newInstance();
			FragmentTransaction fragmentTransaction = getFragmentManager()
					.beginTransaction();
			fragmentTransaction.add(R.id.mapLayout, mapFragment);
			fragmentTransaction.commit();
			getFragmentManager().executePendingTransactions();
			googleMap = mapFragment.getMap();
			MapsInitializer.initialize(BaseApplication.getAppContext());
			ME = new LatLng(Double.parseDouble(preferences
					.getString("lat", "0")), Double.parseDouble(preferences
					.getString("lng", "0")));
			googleMap.addMarker(new MarkerOptions().position(ME)
					.title("My position")
					.icon(BitmapDescriptorFactory.defaultMarker()));
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ME, 10));
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(7), 2000, null);

			addCircle(preferences.getInt("radius", 10));

		}
	}

	private void addCircle(int radius) {
		if (circle != null) {
			circle.remove();
		}
		circle = googleMap.addCircle(new CircleOptions().center(ME)
				.radius(radius * 1609.34)
				.strokeColor(Color.parseColor("#55ff0000")).strokeWidth(2f)
				.fillColor(Color.parseColor("#33ff0000")));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.category_add, menu);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		case R.id.menu_save:
			create();
		}
		return super.onOptionsItemSelected(item);
	}

	public static void Log(Object text) {
		Log.d("Log", text + "");
	}

	public void create() {
		JSONArray hashtagsJSON = new JSONArray();
		for (String hashtag : hashtags) {
			hashtagsJSON.put(hashtag);
		}
		SQLiteDatabase db = new DBHelper(this).getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("name", ((EditText) findViewById(R.id.categoryName)).getText()
				.toString());
		db.update("categories", cv, "id='" + id + "'", null);
		Log("id='" + id + "'");
		db.close();

		startActivity(new Intent(CategoryLocationActivity.this,
				MenuActivity.class).putExtra("position", position));
		finish();
	}

	public void switchDrawer(View v) {

		startActivity(new Intent(CategoryLocationActivity.this,
				CategoryActivity.class).putExtra("position", position));
		finish();
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(CategoryLocationActivity.this,
				CategoryActivity.class).putExtra("position", position));
		super.onBackPressed();
	}

}

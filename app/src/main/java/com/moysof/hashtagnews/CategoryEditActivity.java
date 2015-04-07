package com.moysof.hashtagnews;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Locale;

public class CategoryEditActivity extends ActionBarActivity {

	private String color = "#F44336";
	private ActionBar actionBar;
	private AlertDialog dialog;
	private ArrayList<String> hashtags = new ArrayList<String>();
	private AdapterHashtags hashtagAdapter;
	private String position = "0";
	private JSONArray hashtagsJSON;
	private String id;
	public static ListView hashtagsList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category_edit);

		Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
		toolbar.setContentInsetsAbsolute(
				CommonUtilities.convertDpToPixel(72, this), 0);
		setSupportActionBar(toolbar);

		actionBar = getSupportActionBar();

		SQLiteDatabase db = new DBHelper(this).getReadableDatabase();
		Cursor c = db.query("categories", null, null, null, null, null, null);
		position = getIntent().getStringExtra("position");
		Log("edit pos = " + position);
		while (c.moveToNext()) {
			if (c.getString(c.getColumnIndex("id")).equals(position)) {
				((TextView) findViewById(R.id.categoryName)).setText(c
						.getString(c.getColumnIndex("name")));
				((CardView) findViewById(R.id.categoryColor))
						.setCardBackgroundColor(Color.parseColor(c.getString(c
								.getColumnIndex("color"))));
				color = c.getString(c.getColumnIndex("color"));
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

		hashtagAdapter = new AdapterHashtags(this, hashtags);
		hashtagsList = (ListView) findViewById(R.id.hashtagsList);
		hashtagsList.setAdapter(hashtagAdapter);
		if (hashtags.size() > 0) {
			hashtagsList.setVisibility(View.VISIBLE);
		}
		setListViewHeightBasedOnChildren(hashtagsList);

		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.category_add, menu);
		return super.onCreateOptionsMenu(menu);

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
			Cursor c = db.query("categories", null, "name='"
					+ ((EditText) findViewById(R.id.categoryName)).getText()
							.toString() + "' COLLATE NOCASE and id <> '" + id
					+ "'", null, null, null, null);
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
				db.update("categories", cv, "id='" + id + "'", null);
				Log("id='" + id + "'");
				db.close();
				CategoryActivity.activity.finish();
				startActivity(new Intent(CategoryEditActivity.this,
						MenuActivity.class));
				finish();
			} else {
				Toast.makeText(this, "Category already exists",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void switchDrawer(View v) {
		CategoryActivity.activity.finish();
		startActivity(new Intent(CategoryEditActivity.this,
				CategoryActivity.class).putExtra("position", position));
		finish();
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

	@Override
	public void onBackPressed() {
		startActivity(new Intent(CategoryEditActivity.this,
				CategoryActivity.class).putExtra("position", position));
		super.onBackPressed();
	}

}

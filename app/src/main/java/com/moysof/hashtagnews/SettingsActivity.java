package com.moysof.hashtagnews;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

	private ActionBar actionBar;
	private SharedPreferences preferences;
	private ImageView switcherTwitter;
	private ImageView switcherInstagram;
	private AlertDialog dialog;
	private EditText passwordOld;
	private EditText passwordRepeat;
	private EditText passwordNew;
	private String changePasswordUrl = "http://www.moyersoftware.com/hashtagnews/app/change_password.php";
	private ImageView switcherLocation;
	private ImageView switcherShade;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(false);
		overridePendingTransition(0, 0);


		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		actionBar = getSupportActionBar();
		actionBar.setElevation(8f);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		((TextView) findViewById(R.id.settingsNameText)).setText(preferences
				.getString("name", ""));
		((TextView) findViewById(R.id.settingsEmailText)).setText(preferences
				.getString("email", ""));
		((TextView) findViewById(R.id.settingsLoginText)).setText(preferences
				.getString("login", ""));

		try {
			PackageInfo manager = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			((TextView) findViewById(R.id.settingsAboutText))
					.setText("version " + manager.versionName
							+ getString(R.string.copyright));
		} catch (NameNotFoundException e) {
		}

		switcherTwitter = (ImageView) findViewById(R.id.switcherTwitter);
		switcherInstagram = (ImageView) findViewById(R.id.switcherInstagram);
		switcherLocation = (ImageView) findViewById(R.id.switcherLocation);
		switcherShade = (ImageView) findViewById(R.id.switcherShade);

		if (!preferences.getBoolean("twitter", true)) {
			switcherTwitter.setImageResource(R.drawable.switch_off);
		}
		if (!preferences.getBoolean("instagram", true)) {
			switcherInstagram.setImageResource(R.drawable.switch_off);
		}
		if (!preferences.getBoolean("loc_category", true)) {
			switcherLocation.setImageResource(R.drawable.switch_off);
		}
		if (!preferences.getBoolean("shade", true)) {
			switcherShade.setImageResource(R.drawable.switch_off);
		}

		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	public void switchTwitter(View v) {
		if (preferences.getBoolean("twitter", true)) {
			switcherTwitter.setImageResource(R.drawable.switch_off);
			preferences.edit().putBoolean("twitter", false).commit();
		} else {
			switcherTwitter.setImageResource(R.drawable.switch_on);
			preferences.edit().putBoolean("twitter", true).commit();
		}
	}

	public void switchInstagram(View v) {
		if (preferences.getBoolean("instagram", true)) {
			switcherInstagram.setImageResource(R.drawable.switch_off);
			preferences.edit().putBoolean("instagram", false).commit();
		} else {
			switcherInstagram.setImageResource(R.drawable.switch_on);
			preferences.edit().putBoolean("instagram", true).commit();
		}
	}

	public void switchLocation(View v) {
		if (preferences.getBoolean("loc_category", true)) {
			switcherLocation.setImageResource(R.drawable.switch_off);
			preferences.edit().putBoolean("loc_category", false).commit();
		} else {
			switcherLocation.setImageResource(R.drawable.switch_on);
			preferences.edit().putBoolean("loc_category", true).commit();
		}
	}

	public void switchShade(View v) {
		if (preferences.getBoolean("shade", true)) {
			switcherShade.setImageResource(R.drawable.switch_off);
			preferences.edit().putBoolean("shade", false).commit();
		} else {
			switcherShade.setImageResource(R.drawable.switch_on);
			preferences.edit().putBoolean("shade", true).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}

	public void openPasswordDialog(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View dialogView = getLayoutInflater().inflate(R.layout.dialog_password,
				null);
		builder.setView(dialogView);
		passwordOld = (EditText) dialogView.findViewById(R.id.passwordOld);
		passwordNew = (EditText) dialogView.findViewById(R.id.passwordNew);
		passwordRepeat = (EditText) dialogView
				.findViewById(R.id.passwordRepeat);
		builder.setNegativeButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setPositiveButton("Save", null);
		dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(final DialogInterface dialog) {

				Button b = ((AlertDialog) dialog)
						.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						if (passwordOld.getText().toString().equals("")
								|| passwordNew.getText().toString().equals("")
								|| passwordRepeat.getText().toString()
										.equals("")) {
							Toast.makeText(SettingsActivity.this,
									"Some fields are empty", Toast.LENGTH_LONG)
									.show();
						} else if (!passwordNew.getText().toString()
								.equals(passwordRepeat.getText().toString())) {
							Toast.makeText(SettingsActivity.this,
									"New passwords don't match",
									Toast.LENGTH_LONG).show();
						} else {
							new changePasswordTask().execute();
						}
					}
				});
			}
		});

		dialog.show();
	}

	class changePasswordTask extends AsyncTask<String, Void, Void> {

		private ProgressDialog loadingDialog;
		private int errorCode = 0;
		private String password_new;
		private String password_old;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loadingDialog = new ProgressDialog(SettingsActivity.this);
			loadingDialog.setMessage("Loading");
			loadingDialog.show();
			password_new = passwordNew.getText().toString();
			password_old = passwordOld.getText().toString();
		}

		protected Void doInBackground(String... files) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("password_new", password_new);
				jsonObject.put("password_old", password_old);
				jsonObject.put("id", preferences.getString("id", ""));
				DefaultHttpClient client = new DefaultHttpClient();

				HttpPost httpPost = new HttpPost(changePasswordUrl);
				try {
					StringEntity s = new StringEntity(jsonObject.toString(),
							"UTF-8");

					s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
							"application/json"));
					httpPost.setEntity(s);
					HttpResponse response = client.execute(httpPost);
					String responseString = EntityUtils.toString(response
							.getEntity());
					Log(responseString);
					JSONObject jsonResponse = new JSONObject(responseString);
					if (jsonResponse.has("result")) {
						if (jsonResponse.getString("result").equals("success")) {
							errorCode = -1;
							dialog.dismiss();
						} else if (jsonResponse.getString("result").equals(
								"empty")) {
							errorCode = 1;
						} else if (jsonResponse.getString("result").equals(
								"exists")) {
							errorCode = 2;
						} else if (jsonResponse.getString("result").equals(
								"password")) {
							errorCode = 3;
						} else if (jsonResponse.getString("result").equals(
								"fail")) {
							errorCode = 4;
						} else {
							errorCode = 0;
						}
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
			loadingDialog.dismiss();
			if (errorCode == -1) {
				Toast.makeText(SettingsActivity.this,
						"Success, password changed", Toast.LENGTH_LONG).show();
			} else if (errorCode == 0) {
				Toast.makeText(SettingsActivity.this, "Unknown error",
						Toast.LENGTH_LONG).show();
			} else if (errorCode == 1) {
				Toast.makeText(SettingsActivity.this, "Some fields are empty",
						Toast.LENGTH_LONG).show();
			} else if (errorCode == 2) {
				Toast.makeText(SettingsActivity.this, "User already exists",
						Toast.LENGTH_LONG).show();
			} else if (errorCode == 3) {
				Toast.makeText(SettingsActivity.this, "Incorrect old password",
						Toast.LENGTH_LONG).show();
			} else if (errorCode == 4) {
				Toast.makeText(SettingsActivity.this, "Server error",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public static void Log(Object text) {
		Log.d("Log", text + "");
	}
}

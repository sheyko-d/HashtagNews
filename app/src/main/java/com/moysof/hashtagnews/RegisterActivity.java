package com.moysof.hashtagnews;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class RegisterActivity extends ActionBarActivity {

	private EditText registerLoginEditText;
	private EditText registerPasswordEditText;
	private EditText registerRepeatPasswordEditText;
	private EditText registerEmailEditText;
	private EditText registerNameEditText;
	private String registerUrl = "http://www.moyersoftware.com/hashtagnews/app/register.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
		toolbar.setContentInsetsAbsolute(
				CommonUtilities.convertDpToPixel(72, this), 0);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setElevation(0);

		registerLoginEditText = (EditText) findViewById(R.id.registerLoginEditText);
		registerPasswordEditText = (EditText) findViewById(R.id.registerPasswordEditText);
		registerRepeatPasswordEditText = (EditText) findViewById(R.id.registerRepeatPasswordEditText);
		registerEmailEditText = (EditText) findViewById(R.id.registerEmailEditText);
		registerNameEditText = (EditText) findViewById(R.id.registerNameEditText);

		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}

	class registerTask extends AsyncTask<String, Void, Void> {

		private String login;
		private String password;
		private String passwordSecret;
		private String email;
		private String name;
		private ProgressDialog loadingDialog;
		private int errorCode = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loadingDialog = new ProgressDialog(RegisterActivity.this);
			loadingDialog.setMessage("Loading");
			loadingDialog.show();
			login = registerLoginEditText.getText().toString();
			password = registerPasswordEditText.getText().toString();
			passwordSecret = random();
			email = registerEmailEditText.getText().toString();
			name = registerNameEditText.getText().toString();
		}

		protected Void doInBackground(String... files) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("login", login);
				jsonObject.put("password", md5(password + passwordSecret));
				jsonObject.put("password_secret", passwordSecret);
				jsonObject.put("email", email);
				jsonObject.put("name", name);
				DefaultHttpClient client = new DefaultHttpClient();

				HttpPost httpPost = new HttpPost(registerUrl);
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
							PreferenceManager
									.getDefaultSharedPreferences(
											RegisterActivity.this)
									.edit()
									.putString("id",
											jsonResponse.getString("id"))
									.putString("login",
											jsonResponse.getString("login"))
									.putString("email",
											jsonResponse.getString("email"))
									.putString("name",
											jsonResponse.getString("name"))
									.commit();
							SQLiteDatabase db = new DBHelper(
									RegisterActivity.this)
									.getWritableDatabase();
							db.delete("categories", null, null);
							db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'categories'");
							ContentValues cv = new ContentValues();
							cv.put("name", "HashtagNews");
							cv.put("hashtags", "[\"hashtagnews\"]");
							cv.put("color", "#FF9800");
							cv.put("last_id_instagram", "");
							cv.put("last_id_twitter", "");
							cv.put("last_time", "");
							db.insert("categories", null, cv);
							cv.put("name", "Near Me");
							cv.put("hashtags", "[\"\"]");
							cv.put("color", "#9E9E9E");
							cv.put("last_id_instagram", "");
							cv.put("last_id_twitter", "");
							cv.put("last_time", "");
							db.insert("categories", null, cv);
							errorCode = -1;
						} else if (jsonResponse.getString("result").equals(
								"empty")) {
							errorCode = 1;
						} else if (jsonResponse.getString("result").equals(
								"exists")) {
							errorCode = 2;
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
				SplashActivity.activity.finish();
				finish();
				startActivity(new Intent(RegisterActivity.this,
						MenuActivity.class));
			} else if (errorCode == 0) {
				Toast.makeText(RegisterActivity.this, "Unknown error",
						Toast.LENGTH_LONG).show();
			} else if (errorCode == 1) {
				Toast.makeText(RegisterActivity.this, "Some fields are empty",
						Toast.LENGTH_LONG).show();
			} else if (errorCode == 2) {
				Toast.makeText(RegisterActivity.this, "User already exists",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public static String random() {
		Random generator = new Random();
		StringBuilder randomStringBuilder = new StringBuilder();
		char tempChar;
		for (int i = 0; i < 10; i++) {
			tempChar = (char) (generator.nextInt(96) + 32);
			randomStringBuilder.append(tempChar);
		}
		return randomStringBuilder.toString();
	}

	public static final String md5(final String s) {
		final String MD5 = "MD5";
		try {
			// Create MD5 Hash
			MessageDigest digest = MessageDigest.getInstance(MD5);
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
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
				new registerTask().execute();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void register(View v) {
		if (!registerPasswordEditText.getText().toString()
				.equals(registerRepeatPasswordEditText.getText().toString())) {
			Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG)
					.show();
		} else {
			if (isNetworkConnected()) {
				new registerTask().execute();
			} else {
				showInternetDialog();
			}
		}
	}

	public static void Log(Object text) {
		Log.d("Log", text + "");
	}

}

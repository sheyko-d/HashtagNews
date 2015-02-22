package com.moysof.hashtagnews;

import java.io.IOException;

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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends ActionBarActivity {

	private String loginUrl = "http://hashtagnews.net/app/login.php";
	private EditText loginLoginEditText;
	private EditText loginPasswordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);
		toolbar.setContentInsetsAbsolute(
				CommonUtilities.convertDpToPixel(72, this), 0);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		loginLoginEditText = (EditText) findViewById(R.id.loginLoginEditText);
		loginPasswordEditText = (EditText) findViewById(R.id.loginPasswordEditText);

		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}

	class loginTask extends AsyncTask<String, Void, Void> {

		private String login;
		private String password;
		private ProgressDialog loadingDialog;
		private int errorCode = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loadingDialog = new ProgressDialog(LoginActivity.this);
			loadingDialog.setMessage("Loading");
			loadingDialog.show();
			login = loginLoginEditText.getText().toString();
			password = loginPasswordEditText.getText().toString();
		}

		protected Void doInBackground(String... files) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("login", login);
				jsonObject.put("password", password);
				DefaultHttpClient client = new DefaultHttpClient();

				HttpPost httpPost = new HttpPost(loginUrl);
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
											LoginActivity.this)
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
							JSONArray categoriesJSON = new JSONArray(
									jsonResponse.getString("categories"));
							SharedPreferences preferences = PreferenceManager
									.getDefaultSharedPreferences(LoginActivity.this);
							preferences
									.edit()
									.putBoolean(
											"news_category",
											new JSONObject(jsonResponse
													.getString("preferences"))
													.getBoolean("news_category"))
									.commit();
							preferences
									.edit()
									.putBoolean(
											"loc_category",
											new JSONObject(jsonResponse
													.getString("preferences"))
													.getBoolean("loc_category"))
									.commit();
							SQLiteDatabase db = new DBHelper(LoginActivity.this)
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
							for (int i = 0; i < categoriesJSON.length(); i++) {
								if (!(categoriesJSON.getJSONObject(i)
										.getString("id").equals("1") || categoriesJSON
										.getJSONObject(i).getString("id")
										.equals("2"))) {
									cv = new ContentValues();
									cv.put("name", categoriesJSON
											.getJSONObject(i).getString("name"));
									cv.put("hashtags",
											categoriesJSON.getJSONObject(i)
													.getString("hashtags"));
									cv.put("color", categoriesJSON
											.getJSONObject(i)
											.getString("color"));
									String last_id_twitter = categoriesJSON
											.getJSONObject(i).getString(
													"last_id_twitter");
									if (last_id_twitter.equals("")) {
										last_id_twitter = null;
									}
									cv.put("last_id_twitter",
											categoriesJSON.getJSONObject(i)
													.getString(
															"last_id_twitter"));
									cv.put("last_id_instagram",
											categoriesJSON
													.getJSONObject(i)
													.getString(
															"last_id_instagram"));
									cv.put("last_time",
											categoriesJSON.getJSONObject(i)
													.getString("last_time"));
									db.insert("categories", null, cv);
								}
							}
							db.close();
							errorCode = -1;
						} else if (jsonResponse.getString("result").equals(
								"empty")) {
							errorCode = 1;
						} else if (jsonResponse.getString("result").equals(
								"exists")) {
							errorCode = 2;
						} else if (jsonResponse.getString("result").equals(
								"password")) {
							errorCode = 3;
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
				startActivity(new Intent(LoginActivity.this, MenuActivity.class));
			} else if (errorCode == 0) {
				Toast.makeText(LoginActivity.this, "Unknown error",
						Toast.LENGTH_LONG).show();
			} else if (errorCode == 1) {
				Toast.makeText(LoginActivity.this, "Some fileds are empty",
						Toast.LENGTH_LONG).show();
			} else if (errorCode == 2) {
				Toast.makeText(LoginActivity.this, "User allready exists",
						Toast.LENGTH_LONG).show();
			} else if (errorCode == 3) {
				Toast.makeText(LoginActivity.this, "Incorrect password",
						Toast.LENGTH_LONG).show();
			}
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
				new loginTask().execute();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void login(View v) {
		if (isNetworkConnected()) {
			new loginTask().execute();
		} else {
			showInternetDialog();
		}
	}

	public static void Log(Object text) {
		Log.d("Log", text + "");
	}

}

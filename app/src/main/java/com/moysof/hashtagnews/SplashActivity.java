package com.moysof.hashtagnews;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.moysof.hashtagnews.util.DBHelper;
import com.moysof.hashtagnews.util.Util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SplashActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static SplashActivity activity;
    private CallbackManager mCallbackManager;
    private Button mGoogleButton;
    private GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN = 0;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_splash);
        activity = this;
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API,
                        Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();

        LoginManager.getInstance().logOut();

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInWithFacebook();
            }
        });
        mCallbackManager = CallbackManager.Factory.create();
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("Log", "Facebook keyhash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("name not found", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        }

        // Callback registration
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Util.Log(object);
                                        String id;
                                        String email;
                                        String name;
                                        try {
                                            id = object.getString("id");
                                        } catch (JSONException e) {
                                            id = "";
                                        }
                                        try {
                                            email = object.getString("email");
                                        } catch (JSONException e) {
                                            email = "";
                                        }
                                        try {
                                            name = object.getString("name");
                                        } catch (JSONException e) {
                                            name = "";
                                        }

                                        logIn(id, email, name, "http://graph.facebook.com/"
                                                + id + "/picture?type=normal");


                                    }

                                });

                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // Do nothing
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(SplashActivity.this,
                                "Can't sign up with facebook. Try again later", Toast.LENGTH_LONG).show();
                    }
                }
        );

        Button googleButton = (Button) findViewById(R.id.sign_in_button);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signInWithGplus();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    mSignInClicked = false;
                }

                mIntentInProgress = false;

                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
            default:
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        if (mSignInClicked) {
            // Get user's information
            getProfileInformation();
        } else {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi
                    .revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Log.d("Log", "Disconnected");
                        }
                    });
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
        mSignInClicked = false;
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    /**
     * Sign-in into google
     */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Log-in into facebook
     */
    private void logInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));
    }

    /**
     * Method to resolve any signin errors
     */
    private void resolveSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Fetching user's information name, email, profile pic
     */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);

                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + 100;

                logIn(currentPerson.getId(), email, personName, personPhotoUrl);

                Log.d("Log", "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);


                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X


            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logIn(String id, String email, String name, String image) {
        new loginTask().execute(new String[]{id, email, name, image});
    }

    class loginTask extends AsyncTask<String, Void, Void> {

        private String login;
        private String password;
        private ProgressDialog loadingDialog;
        private int errorCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new ProgressDialog(SplashActivity.this);
            loadingDialog.setMessage("Loading");
            loadingDialog.show();
        }

        protected Void doInBackground(String... ids) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", ids[0]);
                DefaultHttpClient client = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(Util.LOGIN_URL);
                StringEntity s = new StringEntity(jsonObject.toString(),
                        "UTF-8");

                s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                        "application/json"));
                httpPost.setEntity(s);
                HttpResponse response = client.execute(httpPost);
                String responseString = EntityUtils.toString(response
                        .getEntity());
                Util.Log(responseString);
                JSONObject jsonResponse = new JSONObject(responseString);
                if (jsonResponse.has("result")) {
                    if (jsonResponse.getString("result").equals("success")) {
                        PreferenceManager
                                .getDefaultSharedPreferences(
                                        SplashActivity.this)
                                .edit()
                                .putString("id", ids[0])
                                .putString("email", ids[1])
                                .putString("name", ids[2])
                                .putString("avatar", ids[3])
                                .apply();
                        JSONArray categoriesJSON = new JSONArray(
                                jsonResponse.getString("categories"));
                        SharedPreferences preferences = PreferenceManager
                                .getDefaultSharedPreferences(SplashActivity.this);
                        preferences
                                .edit()
                                .putBoolean(
                                        "news_category",
                                        new JSONObject(jsonResponse
                                                .getString("preferences"))
                                                .getBoolean("news_category"))
                                .apply();
                        preferences
                                .edit()
                                .putBoolean(
                                        "loc_category",
                                        new JSONObject(jsonResponse
                                                .getString("preferences"))
                                                .getBoolean("loc_category"))
                                .apply();
                        SQLiteDatabase db = new DBHelper(SplashActivity.this)
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

            } catch (Exception e) {
                Util.Log(e);
                errorCode = 0;
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
                startActivity(new Intent(SplashActivity.this, MenuActivity.class));
            } else if (errorCode == 0) {
                Toast.makeText(SplashActivity.this, "Unknown error",
                        Toast.LENGTH_LONG).show();
            } else if (errorCode == 1) {
                Toast.makeText(SplashActivity.this, "Some fields are empty",
                        Toast.LENGTH_LONG).show();
            } else if (errorCode == 2) {
                Toast.makeText(SplashActivity.this, "User already exists",
                        Toast.LENGTH_LONG).show();
            } else if (errorCode == 3) {
                Toast.makeText(SplashActivity.this, "Incorrect password",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}

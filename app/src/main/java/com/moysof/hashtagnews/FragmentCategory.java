package com.moysof.hashtagnews;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentCategory extends DialogFragment {

    String id = "";
    String title = "";
    String color = "";
    String lastIdTwitter = "0";
    String lastIdInstagram = "0";
    String lastTime = "0";

    int position = 0;
    private String searchUrl = "http://hashtagnews.net/app/search.php";
    private View root;
    private RecyclerView mCategoryRecycler;
    private ArrayList<String> ids = new ArrayList<String>();
    private ArrayList<String> images = new ArrayList<String>();
    private ArrayList<String> avatars = new ArrayList<String>();
    private ArrayList<String> authors = new ArrayList<String>();
    private ArrayList<String> texts = new ArrayList<String>();
    private ArrayList<String> timestamps = new ArrayList<String>();
    private ArrayList<String> sources = new ArrayList<String>();
    private ArrayList<String> seens = new ArrayList<String>();
    private ArrayList<String> links = new ArrayList<String>();
    private ArrayList<Integer> favorites = new ArrayList<Integer>();
    private ArrayList<Integer> likes = new ArrayList<Integer>();
    private AdapterCategory adapter;
    private AlertDialog dialog;
    private SwipeRefreshLayout categoryRefreshLayout;
    public static JSONArray oldPostsArray;
    public static boolean firstItemSeen;
    private LinearLayoutManager mLayoutManager;

    public static FragmentCategory newInstance() {
        FragmentCategory f = new FragmentCategory();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_category, container, false);

        mCategoryRecycler = (RecyclerView) root.findViewById(R.id.categoryRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mCategoryRecycler.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mCategoryRecycler.setLayoutManager(mLayoutManager);

        categoryRefreshLayout = (SwipeRefreshLayout) root
                .findViewById(R.id.refreshLayout);

        adapter = new AdapterCategory(CategoryActivity.activity, color, ids,
                sources, images, avatars, authors, texts, timestamps, seens,
                links, favorites, likes);
        mCategoryRecycler.setAdapter(adapter);
        if (ids.size() > 0) {
            root.findViewById(R.id.categoryProgressBar)
                    .setVisibility(View.GONE);
        }
        categoryRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                search();
            }
        });
        categoryRefreshLayout.setColorSchemeColors(new int[]{Color
                .parseColor(color)});
        ((ProgressBar) root.findViewById(R.id.categoryProgressBar))
                .getIndeterminateDrawable().setColorFilter(
                Color.parseColor(color),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        return root;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        if (visible) {
            CategoryActivity.actionBar.setTitle("#" + title.replace("#", ""));
            changeColor(Color.parseColor(color));
            Log("search = " + CategoryActivity.currentPosition);
            if (ids.size() == 0) {
                search();
            }
            CategoryActivity.currentPosition = position;
            CategoryActivity.activity.invalidateOptionsMenu();
        }
        super.setMenuVisibility(visible);
    }

    @SuppressLint("NewApi")
    private void changeColor(int newColor) {
        if (CommonUtilities.isLollipop()) {
            float[] hsv = new float[3];
            Color.colorToHSV(newColor, hsv);
            hsv[2] *= 0.8f; // value component
            int darkerColor = Color.HSVToColor(hsv);
            CategoryActivity.activity.getWindow()
                    .setStatusBarColor(darkerColor);
        }

        // change ActionBar color just if an ActionBar is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            Drawable colorDrawable = new ColorDrawable(newColor);

            if (CategoryActivity.oldBackground == null) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    colorDrawable
                            .setCallback(CategoryActivity.drawableCallback);
                } else {
                    // CategoryActivity.actionBar.setBackgroundDrawable(colorDrawable);
                    CategoryActivity.activity.findViewById(R.id.tabsWrapper)
                            .setBackgroundDrawable(colorDrawable);
                }

            } else {

                TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                        CategoryActivity.oldBackground, colorDrawable});

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    td.setCallback(CategoryActivity.drawableCallback);
                } else {
                    // CategoryActivity.actionBar.setBackgroundDrawable(td);
                    CategoryActivity.activity.findViewById(R.id.tabsWrapper)
                            .setBackgroundDrawable(td);
                }

                td.startTransition(100);

            }

            CategoryActivity.oldBackground = colorDrawable;

        }

        CategoryActivity.currentColor = newColor;

    }

    private void search() {
        new searchPostsTask().execute();
    }

    private boolean isNetworkConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) CategoryActivity.activity
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo() != null);
        } catch (Exception e) {
        }
        return false;
    }

    public void showInternetDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    CategoryActivity.activity);
            View dialogView = CategoryActivity.activity.getLayoutInflater()
                    .inflate(R.layout.dialog_internet, null);
            builder.setView(dialogView);
            builder.setCancelable(false);
            builder.setNegativeButton("Exit", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CategoryActivity.activity.moveTaskToBack(true);
                }
            });
            builder.setPositiveButton("Retry", new OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    search();
                }
            });
            dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
        }
    }

    class searchPostsTask extends AsyncTask<String, Void, Void> {

        private Cursor c;
        private SQLiteDatabase db;
        private JSONObject jsonRequest;
        private String responseString;
        private ContentValues cv;
        private JSONObject responseJSON;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(String... files) {
            ids.clear();
            avatars.clear();
            authors.clear();
            texts.clear();
            images.clear();
            timestamps.clear();
            sources.clear();
            seens.clear();
            favorites.clear();
            likes.clear();
            try {

                db = new DBHelper(BaseApplication.getAppContext())
                        .getReadableDatabase();
                c = db.query("categories", null, "name=?",
                        new String[]{title}, null, null, null);
                jsonRequest = new JSONObject();
                jsonRequest.put(
                        "radius",
                        PreferenceManager.getDefaultSharedPreferences(
                                BaseApplication.getAppContext()).getInt(
                                "radius", 10));
                jsonRequest.put(
                        "lat",
                        PreferenceManager.getDefaultSharedPreferences(
                                BaseApplication.getAppContext()).getString(
                                "lat", ""));
                jsonRequest.put(
                        "lng",
                        PreferenceManager.getDefaultSharedPreferences(
                                BaseApplication.getAppContext()).getString(
                                "lng", ""));
                jsonRequest.put(
                        "twitter",
                        PreferenceManager.getDefaultSharedPreferences(
                                BaseApplication.getAppContext()).getBoolean(
                                "twitter", true));
                jsonRequest.put(
                        "instagram",
                        PreferenceManager.getDefaultSharedPreferences(
                                BaseApplication.getAppContext()).getBoolean(
                                "instagram", true));
                JSONArray jsonArray = new JSONArray();
                while (c.moveToNext()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", c.getString(c.getColumnIndex("id")));
                    jsonObject.put("name",
                            c.getString(c.getColumnIndex("name")));
                    jsonObject.put("last_id_instagram",
                            c.getString(c.getColumnIndex("last_id_instagram")));
                    Log("put last id for " + c.getString(c.getColumnIndex("name")) + " = " + c.getString(c.getColumnIndex("last_id_twitter")));
                    jsonObject.put("last_id_twitter",
                            c.getString(c.getColumnIndex("last_id_twitter")));
                    jsonObject.put("last_time",
                            c.getString(c.getColumnIndex("last_time")));
                    jsonObject.put(
                            "hashtags",
                            new JSONArray(c.getString(c
                                    .getColumnIndex("hashtags"))));
                    jsonArray.put(jsonObject);
                }
                jsonRequest.put("categories", jsonArray);
                DefaultHttpClient client = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(searchUrl);
                StringEntity s = new StringEntity(jsonRequest.toString(),
                        "UTF-8");

                s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                        "application/json"));
                httpPost.setEntity(s);
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log(responseString);
                responseJSON = new JSONObject(responseString);
                JSONArray postsArray = responseJSON.getJSONArray("posts");

                AdapterCategory.serverTimestamp = Long.parseLong(responseJSON
                        .getString("server_timestamp"));
                cv = new ContentValues();
                if (!responseJSON.getString("last_id_twitter").equals("null")) {
                    cv.put("last_id_twitter",
                            responseJSON.getString("last_id_twitter"));
                    lastIdTwitter = responseJSON.getString("last_id_twitter");
                }
                if (!responseJSON.getString("last_id_instagram").equals("null")) {
                    cv.put("last_id_instagram",
                            responseJSON.getString("last_id_instagram"));
                    Log("update instagram id = " + responseJSON.getString("last_id_instagram"));
                    lastIdInstagram = responseJSON
                            .getString("last_id_instagram");
                } else {
                    Log("don't update instagram id");
                }
                if (!responseJSON.getString("last_time").equals("null")) {
                    cv.put("last_time", responseJSON.getString("last_time"));
                }
                for (int i = 0; i < postsArray.length(); i++) {
                    ids.add(postsArray.getJSONObject(i).getString("id"));
                    images.add(postsArray.getJSONObject(i).getString("image"));
                    avatars.add(postsArray.getJSONObject(i).getString("avatar"));
                    authors.add(postsArray.getJSONObject(i).getString("author"));
                    texts.add(postsArray.getJSONObject(i).getString("text"));
                    timestamps.add(postsArray.getJSONObject(i).getString(
                            "timestamp"));
                    sources.add(postsArray.getJSONObject(i).getString("source"));
                    seens.add(postsArray.getJSONObject(i).getString("seen"));
                    links.add(postsArray.getJSONObject(i).getString("link"));
                    favorites.add(postsArray.getJSONObject(i).getInt(
                            "favorites"));
                    likes.add(postsArray.getJSONObject(i).getInt("likes"));
                }

                if (!TextUtils.isEmpty(title)) {
                    Log("update table where name = " + title);
                    db.update("categories", cv, "name=?",
                            new String[]{title});
                } else {
                    Log("don't update table");
                }
            } catch (Exception e) {
                Log("here" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            root.findViewById(R.id.categoryProgressBar)
                    .setVisibility(View.GONE);
            adapter.notifyDataSetChanged();

            if (ids.size() == 0) {
                root.findViewById(R.id.emptyText).setVisibility(View.VISIBLE);
            } else {
                root.findViewById(R.id.emptyText).setVisibility(View.GONE);
            }

            if (!isNetworkConnected()) {
                if (dialog == null) {
                    showInternetDialog();
                } else if (!dialog.isShowing()) {
                    showInternetDialog();
                } else {
                    dialog.cancel();
                    showInternetDialog();
                }
            }

            categoryRefreshLayout.setRefreshing(false);

        }
    }

    public static void Log(Object text) {
        Log.d("Log", text + "");
    }

}
// So you changed some file

package com.moysof.hashtagnews;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.ViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<String> ids;
    private ArrayList<String> sources;
    private ArrayList<String> images;
    private ArrayList<String> avatars;
    private ArrayList<String> authors;
    private ArrayList<String> texts;
    private ArrayList<String> timestamps;
    private ArrayList<String> seens;
    private ArrayList<String> links;
    private ArrayList<Integer> favorites;
    private ArrayList<Integer> likes;
    private String color;
    private DisplayImageOptions optionsAvatar;
    private ImageLoader imgLoader;
    private Context context;
    private View itemView;
    private DisplayImageOptions optionsPhoto;
    private SharedPreferences preferences;
    protected int postPosition;
    public static Long serverTimestamp;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View mCategoryBg;
        TextView mCategoryDate;
        ImageView mCategoryShareImg;
        TextView mCategoryAuthor;
        TextView mCategoryFavorites;
        TextView mCategoryLikes;
        TextView mCategoryText;
        View mCategoryImageBlock;
        ImageView mCategoryImage;
        View mCategoryImageClick;
        ImageView mCategoryAvatar;

        public ViewHolder(View v) {
            super(v);
            mCategoryBg = v.findViewById(R.id.categoryBg);
            mCategoryDate = (TextView) v.findViewById(R.id.categoryDate);
            mCategoryAuthor = ((TextView) v.findViewById(R.id.categoryAuthor));
            mCategoryFavorites = (TextView) v
                    .findViewById(R.id.categoryFavorites);
            mCategoryLikes = (TextView) v
                    .findViewById(R.id.categoryLikes);
            mCategoryShareImg = (ImageView) v.findViewById(R.id.categoryShareImg);
            mCategoryText = (TextView) v.findViewById(R.id.categoryText);
            mCategoryImageBlock = v.findViewById(R.id.categoryImageBlock);
            mCategoryImage = (ImageView) v.findViewById(R.id.categoryImage);
            mCategoryImageClick = v.findViewById(R.id.categoryImageClick);
            mCategoryAvatar = ((ImageView) v.findViewById(R.id.categoryAvatar));
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterCategory(Context context, String color,
                           ArrayList<String> ids, ArrayList<String> sources,
                           ArrayList<String> images, ArrayList<String> avatars,
                           ArrayList<String> authors, ArrayList<String> texts,
                           ArrayList<String> timestamps, ArrayList<String> seens,
                           ArrayList<String> links, ArrayList<Integer> favorites,
                           ArrayList<Integer> likes) {
        this.context = context;
        this.ids = ids;
        this.sources = sources;
        this.images = images;
        this.avatars = avatars;
        this.authors = authors;
        this.texts = texts;
        this.color = color;
        this.timestamps = timestamps;
        this.seens = seens;
        this.links = links;
        this.favorites = favorites;
        this.likes = likes;

        // initialise ImageLoader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).build();
        imgLoader = ImageLoader.getInstance();
        imgLoader.init(config);
        optionsPhoto = new DisplayImageOptions.Builder().displayer(
                new FadeInBitmapDisplayer(300, true, false, false)).build();

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdapterCategory.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCategoryImage.setImageBitmap(null);
        holder.mCategoryAvatar.setImageBitmap(null);

        holder.mCategoryDate
                .setText(calculateTime(timestamps.get(position)));
        holder.mCategoryBg.setTag(position);
        holder.mCategoryBg.setOnClickListener(
                shareClickListener);

        holder.mCategoryAuthor.setText(authors
                .get(position));
        holder.mCategoryAuthor
                .setTextColor(Color.parseColor(color));

        TextView favoritesTxt = holder.mCategoryFavorites;

        if (favorites.get(position) == 0) {
            favoritesTxt.setVisibility(View.GONE);
        } else {
            favoritesTxt.setVisibility(View.VISIBLE);
            favoritesTxt.setText(favorites.get(position) + "");
        }

        TextView likesTxt = holder.mCategoryLikes;
        if (sources.get(position).equals("Twitter")) {
            likesTxt.setCompoundDrawablesWithIntrinsicBounds(context
                            .getResources().getDrawable(R.drawable.ic_retweet), null,
                    null, null);
        } else {
            likesTxt.setCompoundDrawablesWithIntrinsicBounds(context
                            .getResources().getDrawable(R.drawable.ic_likes), null,
                    null, null);
        }
        if (likes.get(position) == 0) {
            likesTxt.setVisibility(View.GONE);
        } else {
            likesTxt.setVisibility(View.VISIBLE);
            likesTxt.setText(likes.get(position) + "");
        }

        holder.mCategoryShareImg
                .setColorFilter(Color.parseColor(color));

        if (sources.get(position).equals("Twitter")) {
            holder.mCategoryDate
                    .setCompoundDrawablesWithIntrinsicBounds(context
                                    .getResources().getDrawable(R.drawable.ic_twitter),
                            null, null, null);
        } else {
            holder.mCategoryDate
                    .setCompoundDrawablesWithIntrinsicBounds(context
                                    .getResources()
                                    .getDrawable(R.drawable.ic_instagram), null, null,
                            null);
        }

        // Item is seen
        if (preferences.getBoolean("shade", true)
                & seens.get(position).equals("1")) {
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        holder.mCategoryText.setText(texts
                .get(position));
        int colorLink = mixTwoColors(Color.parseColor("#555555"),
                Color.parseColor(color), 0.9f);
        holder.mCategoryText
                .setLinkTextColor(colorLink);

        if (images.get(position).equals("")) {
            holder.mCategoryImageBlock.setVisibility(
                    View.GONE);
        } else {
            imgLoader.displayImage(images.get(position),
                    holder.mCategoryImage,
                    optionsPhoto);
            holder.mCategoryImageClick.setTag(position);
            holder.mCategoryImageClick.setOnClickListener(
                    new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            showImage(v);
                        }
                    });
            holder.mCategoryImageBlock.setVisibility(
                    View.VISIBLE);
        }
        imgLoader.displayImage(avatars.get(position),
                holder.mCategoryAvatar,
                optionsPhoto);
    }

    public static int mixTwoColors(int color1, int color2, float amount) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        final byte BLUE_CHANNEL = 0;

        final float inverseAmount = 1.0f - amount;

        int a = ((int) (((float) (color1 >> ALPHA_CHANNEL & 0xff) * amount) + ((float) (color2 >> ALPHA_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int r = ((int) (((float) (color1 >> RED_CHANNEL & 0xff) * amount) + ((float) (color2 >> RED_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int g = ((int) (((float) (color1 >> GREEN_CHANNEL & 0xff) * amount) + ((float) (color2 >> GREEN_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int b = ((int) (((float) (color1 & 0xff) * amount) + ((float) (color2 & 0xff) * inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL
                | b << BLUE_CHANNEL;
    }

    // Calculate time since timestamp
    private String calculateTime(String timestamp) {
        int seconds = Integer.parseInt(timestamp);
        try {
            long difference = serverTimestamp - seconds;
            if (difference < 60) {
                return difference + " secs ago";
            } else if (difference < 60 * 60) {
                return difference / 60 + " mins ago";
            } else if (difference < 60 * 60 * 24) {
                return difference / 60 / 60 + " hours ago";
            } else {
                return difference / 60 / 60 / 24 + " days ago";
            }
        } catch (Exception e) {
            return "";
        }
    }

    OnClickListener shareClickListener = new OnClickListener() {

        private AlertDialog shareDialog;

        @Override
        public void onClick(View v) {
            postPosition = Integer.parseInt(v.getTag() + "");

            AlertDialog.Builder shareDialogBuilder = new AlertDialog.Builder(
                    context);
            View shareDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_share_main,
                    null);

            shareDialogBuilder.setTitle("Share via...");

            GridView shareGridView = (GridView) shareDialogView
                    .findViewById(R.id.shareGridView);
            shareGridView.setAdapter(new AdapterShareMain(context));
            shareGridView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    shareDialog.cancel();
                    if (position == 0) {
                        Session session = Session.getActiveSession();
                        if (session == null || !session.isOpened()) {
                            // start Facebook Login
                            BaseApplication.openActiveSession(
                                    CategoryActivity.activity, true,
                                    new Session.StatusCallback() {

                                        // callback
                                        // when
                                        // session
                                        // changes
                                        // state
                                        @Override
                                        public void call(Session session,
                                                         SessionState state,
                                                         Exception exception) {
                                            if (session.isOpened()) {
                                                publish(texts.get(postPosition),
                                                        authors.get(postPosition),
                                                        links.get(postPosition),
                                                        images.get(postPosition));
                                            }
                                            if (exception != null) {
                                                Toast.makeText(
                                                        BaseApplication
                                                                .getAppContext(),
                                                        "Facebook error ("
                                                                + exception
                                                                + ")",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        }

                                    });
                        } else {
                            publish(texts.get(postPosition),
                                    authors.get(postPosition),
                                    links.get(postPosition),
                                    images.get(postPosition));
                        }
                    } else if (position == 1) {
                        new ShareTwitterTask().execute();
                    } else if (position == 2) {
						/*Builder builder = new PlusShare.Builder(
								CategoryActivity.activity);
						builder.setType("text/plain");
						builder.setText("Found this in HashtagNews app, check it out!");
						builder.setContentUrl(Uri.parse(links.get(postPosition)));
						context.startActivity(Intent.createChooser(
								builder.getIntent(), "Share"));*/
                    } else if (position == 3) {
                        AlertDialog.Builder shareDialogBuilder = new AlertDialog.Builder(
                                context);
                        View shareDialogView = LayoutInflater.from(context).inflate(
                                R.layout.dialog_share, null);

                        shareDialogBuilder.setTitle("Share via...");

                        Intent share = new Intent(
                                Intent.ACTION_SEND);
                        share.setType("text/plain");
                        final List<ResolveInfo> resInfo = context
                                .getPackageManager().queryIntentActivities(
                                        share, 0);
                        for (int i = 0; i < resInfo.size(); i++) {
                            String packageName = resInfo.get(i).activityInfo.packageName
                                    .toLowerCase(Locale.US);
                            if (packageName.contains("bluetooth")
                                    || packageName.contains("browser")
                                    || packageName.contains("audio")
                                    || packageName.contains("note")
                                    || packageName.contains("translate")
                                    || packageName.contains("twitter")
                                    || packageName.contains("facebook")
                                    || packageName
                                    .contains("com.google.android.apps.plus")) {
                                resInfo.remove(i);
                            } else {
                                Log(resInfo.get(i).activityInfo.packageName);
                            }
                        }
                        ListView shareListView = (ListView) shareDialogView
                                .findViewById(R.id.shareListView);
                        shareListView.setAdapter(new AdapterShare(context,
                                resInfo));
                        shareListView
                                .setOnItemClickListener(new OnItemClickListener() {

                                    @Override
                                    public void onItemClick(
                                            AdapterView<?> parent, View view,
                                            int position, long id) {
                                        shareDialog.cancel();
                                        String packageName = resInfo
                                                .get(position).activityInfo.packageName
                                                .toLowerCase(Locale.US);
                                        Intent shareIntent = new Intent(
                                                Intent.ACTION_SEND);
                                        shareIntent.setType("text/plain");
                                        shareIntent
                                                .putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "\""
                                                                + texts.get(postPosition)
                                                                + "\" - "
                                                                + authors
                                                                .get(postPosition)
                                                                + "\n\n"
                                                                + links.get(postPosition)
                                                                + "\n\nShared via HashtagNews app for Android");
                                        shareIntent.setPackage(packageName);
                                        context.startActivity(shareIntent);
                                    }

                                });

                        shareDialogBuilder.setView(shareDialogView);

                        shareDialog = shareDialogBuilder.create();
                        shareDialog.show();

                    }
                }

            });

            shareDialogBuilder.setView(shareDialogView);

            shareDialog = shareDialogBuilder.create();
            shareDialog.show();

			/*
			 * if (!resInfo.isEmpty()) { for (ResolveInfo info : resInfo) { //
			 * if // (info.activityInfo.packageName.toLowerCase().contains(type)
			 * // || // info.activityInfo.name.toLowerCase().contains(type) ) {
			 * share.putExtra(Intent.EXTRA_SUBJECT, "subject");
			 * share.putExtra(Intent.EXTRA_TEXT, "your text");
			 * share.setPackage(info.activityInfo.packageName); // }
			 * Log(info.activityInfo.packageName); }
			 *
			 * // startActivity(Intent.createChooser(share, "Select")); }
			 */
        }
    };

    class ShareTwitterTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog loadingDialog;
        private Intent chooserIntent;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new ProgressDialog(context);
            loadingDialog.setMessage("Loading...");
            loadingDialog.show();
        }

        protected Void doInBackground(String... files) {
            List<Intent> targetedShareIntents = new ArrayList<Intent>();
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            List<ResolveInfo> resInfo = context.getPackageManager()
                    .queryIntentActivities(share, 0);

            if (!resInfo.isEmpty()) {
                Log("isn't empty now");
                for (ResolveInfo info : resInfo) {
                    Intent targetedShare = new Intent(
                            Intent.ACTION_SEND);
                    targetedShare.setType("image/jpeg");

                    if (info.activityInfo.packageName.toLowerCase().contains(
                            "twi")
                            || info.activityInfo.name.toLowerCase().contains(
                            "twi")) {
                        String signature = "\" � " + "@"
                                + authors.get(postPosition) + " #HashtagNews";
                        targetedShare.putExtra(
                                Intent.EXTRA_TEXT,
                                setTweetLength("\"" + texts.get(postPosition),
                                        signature) + signature);
                        if (images.get(postPosition) != null) {
                            try {
                                URL url = new URL(images.get(postPosition));

                                HttpURLConnection connection = (HttpURLConnection) url
                                        .openConnection();

                                connection.setDoInput(true);

                                connection.connect();

                                InputStream input = connection.getInputStream();

                                Bitmap immutableBpm = BitmapFactory
                                        .decodeStream(input);

                                Bitmap mutableBitmap = immutableBpm.copy(
                                        Bitmap.Config.ARGB_8888, true);

                                View view2 = new View(context);

                                view2.draw(new Canvas(mutableBitmap));

                                String path = Images.Media.insertImage(
                                        context.getContentResolver(),
                                        mutableBitmap,
                                        "cache-" + System.currentTimeMillis(),
                                        null);

                                Uri uri = Uri.parse(path);
                                targetedShare
                                        .putExtra(Intent.EXTRA_STREAM, uri);
                            } catch (IOException e) {
                                Log(e + "");
                            }
                        }
                        targetedShare.setPackage(info.activityInfo.packageName);
                        targetedShareIntents.add(targetedShare);
                    }
                }

                chooserIntent = Intent
                        .createChooser(targetedShareIntents.remove(0),
                                "Select Twitter client");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                        targetedShareIntents.toArray(new Parcelable[]{}));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            loadingDialog.dismiss();
            context.startActivity(chooserIntent);
        }

        private String setTweetLength(String tweetTxt, String signature) {
            String fullTweet = tweetTxt + signature;
            if (fullTweet.length() > 140 - 23) {
                return fullTweet.substring(0, 139 - 23 - signature.length())
                        + "...";
            } else {
                return fullTweet;
            }
        }
    }

    private void publish(String text, String author, String link, String picture) {
        Bundle params = new Bundle();
        params.putString("name", "Found this in HashtagNews app");
        params.putString("caption", "News at the speed of #");
        params.putString("description", "\"" + text + "\" - " + author);
        params.putString("link", link);
        if (picture != null) {
            params.putString("picture", picture);
        }

        WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
                CategoryActivity.activity, Session.getActiveSession(), params))
                .build();
        feedDialog.show();

    }

    public static void Log(Object text) {
        Log.d("Log", "" + text);
    }

    private void showImage(View v) {
        CategoryActivity.fullScreenImage.setImageDrawable(null);
        CategoryActivity.fullScreenImage.setVisibility(View.VISIBLE);
        imgLoader.displayImage(images.get(Integer.parseInt(v.getTag() + "")),
                CategoryActivity.fullScreenImage, new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String arg0, View arg1) {
                        CategoryActivity.imageProgressBar
                                .setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String arg0, View arg1,
                                                FailReason arg2) {
                        CategoryActivity.imageProgressBar
                                .setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String arg0, View arg1,
                                                  Bitmap bitmap) {
                        CategoryActivity.imageProgressBar
                                .setVisibility(View.GONE);
                        CategoryActivity.mAttacher.update();
                    }

                    @Override
                    public void onLoadingCancelled(String arg0, View arg1) {
                        CategoryActivity.imageProgressBar
                                .setVisibility(View.GONE);
                    }
                });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return ids.size();
    }
}
package com.moysof.hashtagnews.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.moysof.hashtagnews.CategoryActivity;
import com.moysof.hashtagnews.MenuActivity;
import com.moysof.hashtagnews.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class AdapterMenu extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private ArrayList<String> titles;
	private ArrayList<String> hashtags;
	private ArrayList<String> counters;
	private ArrayList<String> colors;

	public AdapterMenu(Context context, ArrayList<String> titles,
			ArrayList<String> counters, ArrayList<String> colors,
			ArrayList<String> hashtags) {
		this.context = context;
		this.titles = titles;
		this.counters = counters;
		this.colors = colors;
		this.hashtags = hashtags;
	}

	public int getCount() {
		return titles.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View itemView = (View) inflater.inflate(R.layout.item_menu, parent,
				false);
		((TextView) itemView.findViewById(R.id.titleText)).setText(titles
				.get(position));

		itemView.findViewById(R.id.menuBg).setTag(position);
		itemView.findViewById(R.id.menuBg)
				.setOnClickListener(menuClickListener);
		itemView.findViewById(R.id.menuBg).setOnLongClickListener(
				menuLongClickListener);

		if (counters.get(position).equals("")) {
			itemView.findViewById(R.id.counterProgressBar).setVisibility(
					View.VISIBLE);
		} else if (counters.get(position).equals("0")) {
			itemView.findViewById(R.id.counterBg).setVisibility(View.GONE);
		} else {
			try {
				if (Integer.parseInt(counters.get(position)) < 100) {
					((TextView) itemView.findViewById(R.id.counterText))
							.setText(counters.get(position));
				} else {
					((TextView) itemView.findViewById(R.id.counterText))
							.setText("100+");
				}
				itemView.findViewById(R.id.counterWrapper).setVisibility(
						View.VISIBLE);
			} catch (Exception e) {
				itemView.findViewById(R.id.counterWrapper).setVisibility(
						View.GONE);
			}
		}

		((ProgressBar) itemView.findViewById(R.id.counterProgressBar))
				.getIndeterminateDrawable().setColorFilter(
						Color.parseColor(colors.get(position)),
						PorterDuff.Mode.SRC_ATOP);

		((TextView) itemView.findViewById(R.id.counterText)).setTextColor(Color
				.parseColor(colors.get(position)));

		JSONArray hashtagsArray;
		ViewGroup hashtagLayout = (ViewGroup) itemView
				.findViewById(R.id.hashtagsLayout);
		try {
			hashtagLayout.setVisibility(View.VISIBLE);

			hashtagsArray = new JSONArray(hashtags.get(position));

			for (int i = 0; i < hashtagsArray.length(); i++) {
				if (!TextUtils.isEmpty(hashtagsArray.getString(i))) {
					ViewGroup hashtagWrapper = (ViewGroup) inflater.inflate(
							R.layout.text_hashtag, null);
					TextView hashtagTxt = (TextView) hashtagWrapper
							.findViewById(R.id.hashtagTxt);
					((GradientDrawable) hashtagTxt.getBackground())
							.setColor(Color.parseColor(colors.get(position)));

					hashtagTxt.setText(hashtagsArray.getString(i));

					hashtagLayout.addView(hashtagWrapper);

					if (position == 0
							& titles.get(position).equals("HashtagNews")) {
						hashtagTxt.setCompoundDrawablesWithIntrinsicBounds(
								R.drawable.ic_hashtagnews, 0, 0, 0);
					}

					itemView.findViewById(R.id.locationHashtag).setVisibility(
							View.GONE);
				} else {
					itemView.findViewById(R.id.locationHashtag).setVisibility(
							View.VISIBLE);
				}
			}
		} catch (JSONException e) {
			hashtagLayout.setVisibility(View.GONE);
		}

		return itemView;
	}

	OnClickListener menuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int pos = Integer.parseInt(v.getTag() + "");

			Intent categoryIntent = new Intent(MenuActivity.activity,
					CategoryActivity.class);
			categoryIntent.putExtra("title", titles.get(pos));
			categoryIntent.putExtra("color", colors.get(pos));
			categoryIntent.putExtra("position", (pos));
			MenuActivity.activity.startActivity(categoryIntent);
		}
	};

	OnLongClickListener menuLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			MenuActivity.activity.registerForContextMenu(v);
			MenuActivity.activity.openContextMenu(v);
			MenuActivity.activity.unregisterForContextMenu(v);
			return true;
		}
	};

	public static void Log(Object text) {
		Log.d("Log", "" + text);
	}

}

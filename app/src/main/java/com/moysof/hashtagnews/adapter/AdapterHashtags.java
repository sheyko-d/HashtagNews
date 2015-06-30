package com.moysof.hashtagnews.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moysof.hashtagnews.CategoryAddActivity;
import com.moysof.hashtagnews.CategoryEditActivity;
import com.moysof.hashtagnews.R;

import java.util.ArrayList;

public class AdapterHashtags extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private ArrayList<String> hashtags;
	private View itemView;

	public AdapterHashtags(Context context, ArrayList<String> hashtags) {
		this.context = context;
		this.hashtags = hashtags;
	}

	public int getCount() {
		return hashtags.size();
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
		itemView = (View) inflater
				.inflate(R.layout.item_hashtag, parent, false);
		((TextView) itemView.findViewById(R.id.hashtagTitle)).setText(hashtags
				.get(position).replaceAll("#", "").replaceAll(" ", ""));
		itemView.findViewById(R.id.delete).setTag(position);
		itemView.findViewById(R.id.delete).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						hashtags.remove(Integer.parseInt(v.getTag() + ""));
						notifyDataSetChanged();
						if (hashtags.size() == 0) {
							if (CategoryAddActivity.hashtagsList != null) {
								CategoryAddActivity.hashtagsList
										.setVisibility(View.GONE);
							} else if (CategoryEditActivity.hashtagsList != null) {
								CategoryEditActivity.hashtagsList
										.setVisibility(View.GONE);
							}
						} else {
							if (CategoryAddActivity.hashtagsList != null) {
								CategoryAddActivity.hashtagsList
										.setVisibility(View.VISIBLE);
								CategoryAddActivity
										.setListViewHeightBasedOnChildren(CategoryAddActivity.hashtagsList);
							} else if (CategoryEditActivity.hashtagsList != null) {
								CategoryEditActivity.hashtagsList
										.setVisibility(View.VISIBLE);
								CategoryEditActivity
										.setListViewHeightBasedOnChildren(CategoryEditActivity.hashtagsList);
							}
						}
					}
				});
		return itemView;
	}

	public static void Log(Object text) {
		Log.d("Log", "" + text);
	}

}

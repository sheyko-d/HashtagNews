package com.moysof.hashtagnews.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moysof.hashtagnews.R;

import java.util.ArrayList;

public class AdapterShareMain extends BaseAdapter {

	private LayoutInflater inflater;
	private Context context;
	private View itemView;
	private ArrayList<String> titlesArrayList = new ArrayList<String>();
	private ArrayList<Integer> iconsArrayList = new ArrayList<Integer>();
	private boolean twitterInstalled;

	public AdapterShareMain(Context context) {
		this.context = context;
		twitterInstalled = false;
		try {
			context.getPackageManager().getApplicationInfo(
					"com.twitter.android", 0);
			twitterInstalled = true;
		} catch (Exception e) {
		}
		titlesArrayList.add("Facebook");
		iconsArrayList.add(R.drawable.ic_facebook);
		if (twitterInstalled) {
			titlesArrayList.add("Twitter");
			iconsArrayList.add(R.drawable.ic_twitter_2);
		}
		titlesArrayList.add("Google+");
		iconsArrayList.add(R.drawable.ic_google);
		titlesArrayList.add(context.getString(R.string.other));
		iconsArrayList.add(R.drawable.ic_other);
	}

	public int getCount() {

		if (twitterInstalled) {
			return 4;
		} else {
			return 3;
		}
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

		itemView = (View) inflater.inflate(R.layout.item_share_main, parent,
				false);
		((TextView) itemView.findViewById(R.id.shareTxt))
				.setText(titlesArrayList.get(position));

		((ImageView) itemView.findViewById(R.id.shareImg))
				.setImageResource(iconsArrayList.get(position));

		return itemView;
	}
}
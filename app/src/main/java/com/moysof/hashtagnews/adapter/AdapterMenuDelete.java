package com.moysof.hashtagnews.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moysof.hashtagnews.MenuActivity;
import com.moysof.hashtagnews.MenuDeleteActivity;
import com.moysof.hashtagnews.R;

public class AdapterMenuDelete extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private ArrayList<String> ids;
	private ArrayList<String> titles;
	private ArrayList<String> colors;

	public AdapterMenuDelete(Context context, ArrayList<String> ids,
			ArrayList<String> titles, ArrayList<String> colors) {
		this.context = context;
		this.ids = ids;
		this.titles = titles;
		this.colors = colors;
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

		View itemView = (View) inflater.inflate(R.layout.item_menu_delete,
				parent, false);

		itemView.findViewById(R.id.background).setTag(position);
		itemView.findViewById(R.id.background).setOnClickListener(
				menuClickListener);

		if (ids.get(position).equals("2")) {
			((ImageView) itemView.findViewById(R.id.menuDeleteImg))
					.setImageResource(R.drawable.ic_hide);
		}
		((TextView) itemView.findViewById(R.id.titleText)).setText(titles
				.get(position));

		((CardView) itemView.findViewById(R.id.background))
				.setCardBackgroundColor(Color.parseColor(colors.get(position)));
		return itemView;
	}

	OnClickListener menuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int pos = Integer.parseInt(v.getTag() + "");

			Intent menuIntent = new Intent(MenuDeleteActivity.activity,
					MenuActivity.class);
			menuIntent.putExtra("position", pos);
			MenuDeleteActivity.activity.startActivity(menuIntent);
		}
	};

	public static void Log(Object text) {
		Log.d("Log", "" + text);
	}

}

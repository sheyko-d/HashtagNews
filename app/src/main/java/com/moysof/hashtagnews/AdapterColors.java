package com.moysof.hashtagnews;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class AdapterColors extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	public static String[] colors = { "#F44336", "#E91E63", "#9C27B0",
			"#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688",
			"#4CAF50", "#8BC34A", "#CDDC39", "#FFC107", "#FF9800",
			"#FF5722", "#795548", "#9E9E9E", "#607D8B", "#000000" };
	private View itemView;

	public AdapterColors(Context context) {
		this.context = context;
	}

	public int getCount() {
		return colors.length;
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
		itemView = (View) inflater.inflate(R.layout.item_color, parent, false);
		itemView.setBackgroundColor(Color.parseColor(colors[position]));

		return itemView;
	}

	public static void Log(Object text) {
		Log.d("Log", "" + text);
	}

}

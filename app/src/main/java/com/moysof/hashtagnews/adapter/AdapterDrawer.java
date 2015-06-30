package com.moysof.hashtagnews.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moysof.hashtagnews.R;

public class AdapterDrawer extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private String[] menuArray = { "Categories", "Home", "Add category",
			"Delete item", "Support", "Settings", "Help", "Feedback" };
	private View itemView;
	public static int selectedPos = 0;
	private Integer[] mIconsArray;
	private Integer[] mIconsSelectedArray;

	public AdapterDrawer(Context context, int selectedPos) {
		this.context = context;
		AdapterDrawer.selectedPos = selectedPos;

		mIconsArray = new Integer[] { 0, R.drawable.ic_drawer_home,
				R.drawable.ic_drawer_add, R.drawable.ic_drawer_delete, 0,
				R.drawable.ic_drawer_settings, R.drawable.ic_drawer_help,
				R.drawable.ic_drawer_feedback };
		mIconsSelectedArray = new Integer[] { 0,
				R.drawable.ic_drawer_home_selected,
				R.drawable.ic_drawer_add_selected,
				R.drawable.ic_drawer_delete_selected, 0,
				R.drawable.ic_drawer_settings_selected,
				R.drawable.ic_drawer_help_selected,
				R.drawable.ic_drawer_feedback_selected };
	}

	public int getCount() {
		return menuArray.length;
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

		if (position == 0 || position == 4) {
			if (position == 0) {
				itemView = (View) inflater.inflate(R.layout.item_drawer_title,
						parent, false);
			} else {
				itemView = (View) inflater.inflate(
						R.layout.item_drawer_title_2, parent, false);
				((TextView) itemView.findViewById(R.id.drawerTitle))
						.setText(menuArray[position]);
			}
		} else {
			if (position == selectedPos) {
				itemView = (View) inflater.inflate(
						R.layout.item_drawer_selected, parent, false);

				((ImageView) itemView.findViewById(R.id.drawerItemIcon))
						.setImageResource(mIconsSelectedArray[position]);
			} else {
				itemView = (View) inflater.inflate(R.layout.item_drawer,
						parent, false);

				((ImageView) itemView.findViewById(R.id.drawerItemIcon))
						.setImageResource(mIconsArray[position]);
			}
			((TextView) itemView.findViewById(R.id.drawerTitle))
					.setText(menuArray[position]);
		}

		return itemView;
	}

	public static void Log(Object text) {
		Log.d("Log", "" + text);
	}

}

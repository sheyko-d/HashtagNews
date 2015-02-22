package com.moysof.hashtagnews;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterShare extends BaseAdapter {

	private LayoutInflater inflater;
	private List<ResolveInfo> resInfo;
	private Context context;
	private View itemView;
	public static Long serverTimestamp;

	public AdapterShare(Context context, List<ResolveInfo> resInfo) {
		this.context = context;
		this.resInfo = resInfo;
	}

	public int getCount() {
		return resInfo.size();
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
		PackageManager pm = context.getPackageManager();
		itemView = (View) inflater.inflate(R.layout.item_share, parent, false);
		((TextView) itemView.findViewById(R.id.shareTxt)).setText(resInfo.get(
				position).loadLabel(pm));
		((ImageView) itemView.findViewById(R.id.shareImg)).setImageDrawable(resInfo.get(
				position).loadIcon(pm));
		return itemView;
	}
}
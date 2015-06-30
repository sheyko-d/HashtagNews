package com.moysof.hashtagnews;

import android.app.Application;
import android.content.Context;


public class BaseApplication extends Application {

	private static Context mContext;

	public static Context getAppContext() {
		return mContext;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
	}

	public static boolean isAmazonPhone() {
		return android.os.Build.MANUFACTURER.equalsIgnoreCase("Amazon") ? true
				: false;
	}

}

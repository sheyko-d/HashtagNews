package com.moysof.hashtagnews;

import java.util.Arrays;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.facebook.Session;
import com.facebook.Session.Builder;
import com.facebook.Session.OpenRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;


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

	public static Session openActiveSession(Activity activity,
			boolean allowLoginUI, StatusCallback callback) {
		OpenRequest openRequest = new OpenRequest(activity)
				.setCallback(callback);
		Session session = new Builder(activity).build();
		if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState())
				|| allowLoginUI) {
			Session.setActiveSession(session);
			openRequest.setPermissions(Arrays.asList("publish_actions"));
			session.openForPublish(openRequest);
			return session;
		}
		return null;
	}

	public static boolean isAmazonPhone() {
		return android.os.Build.MANUFACTURER.equalsIgnoreCase("Amazon") ? true
				: false;
	}

}

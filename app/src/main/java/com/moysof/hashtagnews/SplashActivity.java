package com.moysof.hashtagnews;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SplashActivity extends ActionBarActivity {

	public static SplashActivity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		activity = this;
		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	public void register(View v) {
		startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
	}

	public void login(View v) {
		startActivity(new Intent(SplashActivity.this, LoginActivity.class));
	}

}

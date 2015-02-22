package com.moysof.hashtagnews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextThin extends TextView {

	public TextThin(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TextThin(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TextThin(Context context) {
		super(context);
		init();
	}

	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
				"Roboto-Thin.ttf");
		setTypeface(tf);
	}

}
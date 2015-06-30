package com.moysof.hashtagnews.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextMedium extends TextView {

	public TextMedium(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TextMedium(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TextMedium(Context context) {
		super(context);
		init();
	}

	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
				"Roboto-Medium.ttf");
		setTypeface(tf);
	}

}
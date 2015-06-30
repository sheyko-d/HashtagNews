package com.moysof.hashtagnews.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextBold extends TextView {

	public TextBold(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TextBold(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TextBold(Context context) {
		super(context);
		init();
	}

	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
				"Roboto-Bold.ttf");
		setTypeface(tf);
	}

}
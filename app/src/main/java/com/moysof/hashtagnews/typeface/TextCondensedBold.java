package com.moysof.hashtagnews.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextCondensedBold extends TextView {

	public TextCondensedBold(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TextCondensedBold(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TextCondensedBold(Context context) {
		super(context);
		init();
	}

	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
				"RobotoCondensed-Bold.ttf");
		setTypeface(tf);
	}

}
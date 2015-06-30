package com.moysof.hashtagnews.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextItalic extends TextView {

	public TextItalic(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TextItalic(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TextItalic(Context context) {
		super(context);
		init();
	}

	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
				"Roboto-Italic.ttf");
		setTypeface(tf);
	}

}
package com.moysof.hashtagnews.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextLightItalic extends TextView {

	public TextLightItalic(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TextLightItalic(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TextLightItalic(Context context) {
		super(context);
		init();
	}

	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
				"Roboto-LightItalic.ttf");
		setTypeface(tf);
	}

}
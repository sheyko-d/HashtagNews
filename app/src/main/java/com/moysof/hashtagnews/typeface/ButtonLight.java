package com.moysof.hashtagnews.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class ButtonLight extends Button {

    public ButtonLight(Context context) {
        super(context);
    }

    public ButtonLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonLight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    

	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
				"Roboto-Light.ttf");
		setTypeface(tf);
	}
}
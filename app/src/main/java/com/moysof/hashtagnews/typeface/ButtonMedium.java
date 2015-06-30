package com.moysof.hashtagnews.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class ButtonMedium extends Button {

    public ButtonMedium(Context context) {
        super(context);
    }

    public ButtonMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonMedium(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    

	private void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
				"Roboto-Medium.ttf");
		setTypeface(tf);
	}
}
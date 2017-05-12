package com.jonlatane.beatpad.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class NonDelayedHorizontalScrollView extends HorizontalScrollView {
	public NonDelayedHorizontalScrollView(Context context) {
		super(context);
	}
	public NonDelayedHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public NonDelayedHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}
}
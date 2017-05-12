package com.jonlatane.beatpad.view.keyboard;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import com.jonlatane.beatpad.R;

public class KeyboardView extends HorizontalScrollView {
	private static final String TAG = KeyboardView.class.getSimpleName();
	public int margin;
	
	public KeyboardView(Context context) {
		super(context);
		init(context);
	}
	public KeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public KeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	void init(Context c){
		margin = c.getResources().getDisplayMetrics().densityDpi / 5;
		LayoutInflater.from(c).inflate(R.layout.view_keyboard, this, true);
	}

	@Override
    public boolean onTouchEvent(MotionEvent event) {
		if(event.getX() < margin || event.getX() > getWidth() - margin)
			enableScrolling();
		
		if(enableScrolling && event.getPointerCount() < 2)
			super.onTouchEvent(event);
		
		if(event.getActionMasked() == MotionEvent.ACTION_UP && event.getPointerCount() < 2)
			disableScrolling();
		return true;
	}
	@Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
		if(event.getActionMasked() == MotionEvent.ACTION_DOWN
				&& event.getX() < margin || event.getX() > getWidth() - margin)
			return true;
		return false;
	}
	
	/*
	 * onDraw and these types and variables are used to render the drop shadows on the right and left.
	 */
	private int leftShadowAlpha = 255, rightShadowAlpha = 255;
	private final IntEvaluator leftShadowEvaluator = new IntEvaluator() {
		@Override
		public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
			int num = super.evaluate(fraction, startValue, endValue);
			leftShadowAlpha = num;
			return num;
		}
	};
	private final IntEvaluator rightShadowEvaluator = new IntEvaluator() {
		@Override
		public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
			int num = super.evaluate(fraction, startValue, endValue);
			rightShadowAlpha = num;
			return num;
		}
	};
	@Override
    protected void onDraw(Canvas c) {
		super.onDraw(c);
		
		if(canScrollHorizontally(-1))
			ValueAnimator.ofObject(leftShadowEvaluator, leftShadowAlpha, 255).start();
		else
			ValueAnimator.ofObject(leftShadowEvaluator, leftShadowAlpha, 255).start();
		if(canScrollHorizontally(1))
			ValueAnimator.ofObject(rightShadowEvaluator, rightShadowAlpha, 255).start();
		else
			ValueAnimator.ofObject(rightShadowEvaluator, rightShadowAlpha, 255).start();
	}
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}

	public void toggleVisibility() {
		if(isHidden()) {
			show();
		} else {
			hide();
		}
	}

	public void show() {
		animate().translationY(0).alpha(1f).start();
	}

	public void hide() {
		animate().translationY(getHeight()).alpha(0f).start();
	}

	public boolean isHidden() {
		return getTranslationY() != 0;
	}
	
	private boolean enableScrolling = false;
	public void enableScrolling() {
		enableScrolling = true;
	}
	public void disableScrolling() {
		enableScrolling = false;
	}
}
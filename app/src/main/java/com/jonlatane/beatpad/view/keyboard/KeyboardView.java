package com.jonlatane.beatpad.view.keyboard;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import com.jonlatane.beatpad.R;
import com.jonlatane.beatpad.view.NonDelayedHorizontalScrollView;

public class KeyboardView extends NonDelayedHorizontalScrollView {
	private static final String TAG = KeyboardView.class.getSimpleName();
	public int margin;
	
	public KeyboardView(Context context) {
		super(context);
		onCreate(context);
	}
	public KeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(context);
	}
	
	public KeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		onCreate(context);
	}
	
	void onCreate(Context c){
		margin = c.getResources().getDisplayMetrics().densityDpi / 5;
		LayoutInflater.from(c).inflate(R.layout.view_keyboard, this, true);
	}

	@Override
    public boolean onTouchEvent(MotionEvent event) {
		if(event.getX() < margin || event.getX() > getWidth() - margin)
			enableScrolling();
		
		if(_enableScrolling && event.getPointerCount() < 2)
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
	private int _leftShadowAlpha = 255, _rightShadowAlpha = 255;
	private class LeftShadowEvaluator extends IntEvaluator {
	    private KeyboardView v;
	    public LeftShadowEvaluator(KeyboardView v) {
	        this.v = v;
	    }
		@Override
	    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
	        int num = super.evaluate(fraction, startValue, endValue);
	        v._leftShadowAlpha = num;
	        return num;
	    }
	}
	private class RightShadowEvaluator extends IntEvaluator {
	    private KeyboardView v;
	    public RightShadowEvaluator(KeyboardView v) {
	        this.v = v;
	    }
		@Override
	    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
	        int num = super.evaluate(fraction, startValue, endValue);
	        v._rightShadowAlpha = num;
	        return num;
	    }
	}
	@Override
    protected void onDraw(Canvas c) {
		super.onDraw(c);
		
		if(canScrollHorizontally(-1))
			ValueAnimator.ofObject(new LeftShadowEvaluator(this), _leftShadowAlpha, 255).start();
		else
			ValueAnimator.ofObject(new LeftShadowEvaluator(this), _leftShadowAlpha, 255).start();
		if(canScrollHorizontally(1))
			ValueAnimator.ofObject(new RightShadowEvaluator(this), _rightShadowAlpha, 255).start();
		else
			ValueAnimator.ofObject(new RightShadowEvaluator(this), _rightShadowAlpha, 255).start();
	}

	public void toggleVisibility() {
		if(getTranslationY() != 0) {
			animate().translationY(0).start();
		} else {
			animate().translationY(getHeight()).start();
		}
	}
	
	private boolean _enableScrolling = false;
	public void enableScrolling() {
		_enableScrolling = true;
	}
	public void disableScrolling() {
		_enableScrolling = false;
	}
}
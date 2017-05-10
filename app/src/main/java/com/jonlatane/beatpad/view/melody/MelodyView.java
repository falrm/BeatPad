package com.jonlatane.beatpad.view.melody;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

import com.jonlatane.beatpad.R;
import com.jonlatane.beatpad.instrument.MIDIInstrument;
import com.jonlatane.beatpad.sensors.Orientation;

import java.util.Collections;
import java.util.List;

import static com.jonlatane.beatpad.sensors.Orientation.normalizedDevicePitch;

/**
 * Created by jonlatane on 5/6/17.
 */
public class MelodyView extends View {
    private static final String TAG = MelodyView.class.getSimpleName();
    float density;
    private MIDIInstrument instrument = null;

    private List<Integer> tones = Collections.emptyList();
    int lowest = -60;
    int highest = 28;

    public MelodyView(Context context) {
        super(context);
        init();
    }

    public MelodyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MelodyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * After calling this, you should not reuse the instrument elsewhere.
     *
     * @param instrument
     * @return
     */
    public MelodyView setInstrument(MIDIInstrument instrument) {
        if(this.instrument != null) {
            this.instrument.stop();
        }
        this.instrument = instrument;
        return this;
    }

    public void setTones(List<Integer> tones) {
        this.tones = tones;
    }

    void init() {
        density = getContext().getResources().getDisplayMetrics().density;
    }

    SparseArray<PointF> activePointers = new SparseArray<>();
    SparseIntArray pointerTones = new SparseIntArray();
    SparseIntArray pointerVelocities = new SparseIntArray();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();
        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);
        int maskedAction = event.getActionMasked();
        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                // We have a new pointer. Lets add it to the list of pointers

                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);
                activePointers.put(pointerId, f);
                int tone = pointerId % 88 - 24;
                if(!tones.isEmpty()) {
                    float screenWidth = tones.size() / 4f;
                    Log.i(TAG, "x="+f.x+",width="+getWidth());
                    float basePitch =   (normalizedDevicePitch() * (tones.size() - screenWidth))
                                      + (screenWidth * (f.x) / getWidth());
                    tone = tones.get(Math.max(0,Math.min(tones.size()-1,Math.round(basePitch))));
                }
                float velocity01 = (getHeight() - f.y) / getHeight();

                //velocity01 = 0.5f + (float)Math.cbrt(velocity01 - 0.5f);
                velocity01 = (float)Math.sqrt(Math.sqrt(Math.max(0.1f,velocity01)));
                int velocity = Math.min(127,Math.max(10, Math.round(
                        velocity01 * 127
                )));
                pointerTones.put(pointerId, tone);
                pointerVelocities.put(pointerId, velocity);
                Log.i(TAG, "playing " + tone + " with velocity " + velocity);
                instrument.play(tone, velocity);
                break;
            }
            case MotionEvent.ACTION_MOVE: { // a pointer was moved
                for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                    PointF point = activePointers.get(event.getPointerId(i));
                    if (point != null) {
                        point.x = event.getX(i);
                        point.y = event.getY(i);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                instrument.stop(pointerTones.get(pointerId));
                activePointers.remove(pointerId);
                break;
            }
        }
        invalidate();

        //Log.i(TAG, actionToString(maskedAction) + " " + pointerIndex +
        //        ": " + event.getX(pointerIndex) + ", " + event.getY(pointerIndex));
        return true;
    }

    public static String actionToString(int action) {
        switch (action) {

            case MotionEvent.ACTION_DOWN: return "Down";
            case MotionEvent.ACTION_MOVE: return "Move";
            case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
            case MotionEvent.ACTION_UP: return "Up";
            case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return "Outside";
            case MotionEvent.ACTION_CANCEL: return "Cancel";
        }
        return "";
    }

    final int backgroundColor = getResources().getColor(R.color.colorAccent);
    final float[] hsl = new float[3];
    Paint paint = new Paint();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(0xCCFFFFFF);
        ColorUtils.colorToHSL(backgroundColor, hsl);
        hsl[1] = Orientation.normalizedDevicePitch();
        canvas.drawColor(ColorUtils.HSLToColor(hsl));
        for(int i = 0; i < activePointers.size(); i++) {
            int key = activePointers.keyAt(i);
            // get the object by the key.
            PointF pointer = activePointers.get(key);
            paint.setAlpha(pointerVelocities.get(key)*2);
            canvas.drawCircle(pointer.x, pointer.y, 100f, paint);
        }
        invalidate();
    }
}

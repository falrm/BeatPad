package com.jonlatane.beatpad.view.keyboard;

import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;

import com.jonlatane.beatpad.R;
import com.jonlatane.beatpad.harmony.chord.Chord;
import com.jonlatane.beatpad.instrument.MIDIInstrument;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class KeyboardIOHandler implements OnLongClickListener, OnTouchListener {
	private static String TAG = KeyboardIOHandler.class.getSimpleName();

	private static int[] KEY_IDS = new int[]
			{ R.id.keyA0, R.id.keyAS0, R.id.keyB0,
		R.id.keyC1, R.id.keyCS1, R.id.keyD1, R.id.keyDS1,R.id.keyE1, R.id.keyF1, R.id.keyFS1, R.id.keyG1,R.id.keyGS1, R.id.keyA1, R.id.keyAS1, R.id.keyB1,
		R.id.keyC2, R.id.keyCS2, R.id.keyD2, R.id.keyDS2,R.id.keyE2, R.id.keyF2, R.id.keyFS2, R.id.keyG2,R.id.keyGS2, R.id.keyA2, R.id.keyAS2, R.id.keyB2,
		R.id.keyC3, R.id.keyCS3, R.id.keyD3, R.id.keyDS3,R.id.keyE3, R.id.keyF3, R.id.keyFS3, R.id.keyG3,R.id.keyGS3, R.id.keyA3, R.id.keyAS3, R.id.keyB3,
		R.id.keyC4, R.id.keyCS4, R.id.keyD4, R.id.keyDS4,R.id.keyE4, R.id.keyF4, R.id.keyFS4, R.id.keyG4,R.id.keyGS4, R.id.keyA4, R.id.keyAS4, R.id.keyB4,
		R.id.keyC5, R.id.keyCS5, R.id.keyD5, R.id.keyDS5,R.id.keyE5, R.id.keyF5, R.id.keyFS5, R.id.keyG5,R.id.keyGS5, R.id.keyA5, R.id.keyAS5, R.id.keyB5,
		R.id.keyC6, R.id.keyCS6, R.id.keyD6, R.id.keyDS6,R.id.keyE6, R.id.keyF6, R.id.keyFS6, R.id.keyG6,R.id.keyGS6, R.id.keyA6, R.id.keyAS6, R.id.keyB6,
		R.id.keyC7, R.id.keyCS7, R.id.keyD7, R.id.keyDS7,R.id.keyE7, R.id.keyF7, R.id.keyFS7, R.id.keyG7,R.id.keyGS7, R.id.keyA7, R.id.keyAS7, R.id.keyB7,
		R.id.keyC8
			};
	private static SparseIntArray KEY_IDS_INVERSE = new SparseIntArray();
	static {
		for( int i = 0; i < KEY_IDS.length; i = i +  1 ) {
			KEY_IDS_INVERSE.put(KEY_IDS[i], i - 39);
		}
	}
	
	private final Set<Integer> currentlyPressed = Collections.synchronizedSet(new HashSet<Integer>());
	private final MIDIInstrument instrument;
	private final KeyboardView keyboardView;
	
	public KeyboardIOHandler(KeyboardView keyboardView, MIDIInstrument instrument) {
		this.keyboardView = keyboardView;
		this.instrument = instrument;
		for(int k : KEY_IDS) {
			Button b = (Button)(keyboardView.findViewById(k));
			b.setOnTouchListener(this);
			//b.setOnLongClickListener(this);
			ViewTreeObserver o = b.getViewTreeObserver();
			
			// Make sure we don't get stuck keys
			o.addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					catchRogues();
					return true;
				}
			});
		}
	}
	
	void liftNote(int n) {
		synchronized(currentlyPressed) {
			currentlyPressed.remove(n);
		}
		instrument.stop(n);
	}
	void pressNote(int n) {
		synchronized(currentlyPressed) {
			currentlyPressed.add(n);
		}
		instrument.play(n);
	}
	
	@Override
	public boolean onLongClick(View view) {
		return false;
	}
	
	/**
	 * Highlights the given chord on the keyboard
	 * @param harmonicChord
	 */
	public void highlightChord(Chord harmonicChord) {
		if(harmonicChord != null) {
			Log.i(TAG,"Highlighting chord " + harmonicChord.getName());

			for(int id : KEY_IDS) {
				Button b = (Button) keyboardView.findViewById(id);
				int tone = KEY_IDS_INVERSE.get(id);
				int toneClass = (1200 + tone) % 12;
				boolean isRoot = (toneClass == harmonicChord.root);
				boolean isBlack = isBlack(tone);
				boolean isInChord = harmonicChord.containsTone(toneClass);
				if(isBlack) {
					if(isRoot) {
						b.setBackgroundResource(R.drawable.key_black_highlighted_root);
					} else if(isInChord) {
						b.setBackgroundResource(R.drawable.key_black_highlighted);
					} else {
						b.setBackgroundResource(R.drawable.key_black);
					}
				} else {
					if(isRoot) {
						b.setBackgroundResource(R.drawable.key_white_highlighted_root);
					} else if(isInChord) {
						b.setBackgroundResource(R.drawable.key_white_highlighted);
					} else {
						b.setBackgroundResource(R.drawable.key_white);
					}
				}
			}
		} else {
			Log.i(TAG, "Clearing highlights");
			for(int id : KEY_IDS) {
				int n = KEY_IDS_INVERSE.get(id);
				if(isBlack(n)) {
					(keyboardView.findViewById(id)).setBackgroundResource(R.drawable.key_black);
				} else {
					(keyboardView.findViewById(id)).setBackgroundResource(R.drawable.key_white);
				}
			}
		}
	}
	
	private boolean isBlack(int note) {
		int nClass = (1200 + note) % 12;
		return (nClass == 1 || nClass == 3 || nClass == 6  || nClass == 8  || nClass == 10);
	}
	
	public synchronized void clearHarmonicRoot() {
		//Log.i(TAG,"Clearing Root");
		highlightChord(null);
	}
	
	// Utility method in case the keyboard is scrolled when keys are pressed.
	void catchRogues() {
		Iterator<Integer> iter = currentlyPressed.iterator();
		while(iter.hasNext()) {
			int n = iter.next();
			Button b = (Button) keyboardView.findViewById(KEY_IDS[n+39]);
			if(!b.isPressed()) {
				iter.remove();
				liftNote(n);
			}
		}
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		catchRogues();
		boolean result = false;
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			pressNote(KEY_IDS_INVERSE.get(arg0.getId()));
		} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			liftNote(KEY_IDS_INVERSE.get(arg0.getId()));
		} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
				&& event.getPointerCount() != 1 
				&& currentlyPressed.size() > 1) {
			Log.i(TAG,"onTouch Disallow");
			result = true;
		}
		
		return result;
	}
}
package com.jonlatane.beatpad.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jonlatane.beatpad.R;
import com.jonlatane.beatpad.harmony.Chord;
import com.jonlatane.beatpad.harmony.Sequence;

import java.util.ArrayList;
import java.util.List;

import static com.jonlatane.beatpad.harmony.Chord.MAJOR_7;

/**
 * Created by jonlatane on 5/5/17.
 */
public class TopologyView extends RelativeLayout {
    private Chord chord = new Chord(0, MAJOR_7, 2, 24);
    private TextView currentChord;

    private class SequenceViews {
        final Sequence sequence;
        final TextView forward = getChordView();
        final TextView back = getChordView();
        SequenceViews(final Sequence s) {
            this.sequence = s;
            forward.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setChord(sequence.forward(chord));
                }
            });
            forward.setText(sequence.forward(chord).getName());
            back.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setChord(sequence.back(chord));
                }
            });
            back.setText(sequence.back(chord).getName());
        }
    }
    private List<SequenceViews> sequences = new ArrayList<>();
    private OnChordChangedListener onChordChangedListener;
    public interface OnChordChangedListener {
        void onChordChanged(Chord c);
    }


    public TopologyView(Context context) {
        super(context);
        init();
    }

    public TopologyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TopologyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        currentChord = getChordView();
        currentChord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setChord(chord);
            }
        });
        post(new Runnable() {
            @Override
            public void run() {
                updateTranslations();
            }
        });
    }

    private void updateTranslations() {
        double theta = Math.PI / sequences.size();
        float maxTX = getWidth() * 0.4f;
        float maxTY = getHeight() * 0.4f;
        currentChord.animate().scaleX(2).scaleY(2).start();
        for(int i = 0; i < sequences.size(); i++) {
            SequenceViews sv = sequences.get(i);
            double sin = Math.sin((i * theta) - ((Math.PI - theta) / 2));
            double cos = Math.cos((i * theta) - ((Math.PI - theta) / 2));
            float x = (float)(maxTX * cos);
            float y = (float)(maxTY * sin);
            sv.forward.animate()
                    .translationX(x).translationY(-y).start();
            sv.back.animate()
                    .translationX(-x).translationY(y).start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void resetTranslations() {
        for(SequenceViews sv : sequences) {
            sv.forward.animate().translationX(0).translationY(0).setDuration(250).start();
            sv.back.animate().translationX(0).translationY(0).setDuration(250).withEndAction(new Runnable() {
                @Override
                public void run() {
                    updateTranslations();
                }
            }).start();
        }
    }

    public Chord getChord() {
        return chord;
    }

    public void setChord(Chord c) {
        chord = c;
        onChordChangedListener.onChordChanged(c);
        currentChord.setText(c.getName());
        for(SequenceViews sv : sequences) {
            sv.forward.setText(sv.sequence.forward(chord).getName());
            sv.back.setText(sv.sequence.back(chord).getName());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            resetTranslations();
        }
    }

    private TextView getChordView() {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_chord, this, true);
        TextView result = (TextView) findViewWithTag("newChord");
        result.setTag(null);
        return result;
    }

    public void addSequence(Sequence sequence) {
        sequences.add(new SequenceViews(sequence));
    }

    public void setOnChordChangedListener(OnChordChangedListener onChordChangedListener) {
        this.onChordChangedListener = onChordChangedListener;
    }
}

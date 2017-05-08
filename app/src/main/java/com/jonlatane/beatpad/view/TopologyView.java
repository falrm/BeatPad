package com.jonlatane.beatpad.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jonlatane.beatpad.R;
import com.jonlatane.beatpad.harmony.Sequence;
import com.jonlatane.beatpad.harmony.chord.Chord;

import java.util.ArrayList;
import java.util.List;

import static com.jonlatane.beatpad.harmony.Sequence.CHROMATIC;
import static com.jonlatane.beatpad.harmony.chord.Chord.MAJ_6;

/**
 * Created by jonlatane on 5/5/17.
 */
public class TopologyView extends RelativeLayout {
    private static final String TAG = TopologyView.class.getSimpleName();
    private Chord chord;
    private OnChordChangedListener onChordChangedListener;

    TextView selectedChord;
    TextView centralChord;
    TextView halfStepUp;
    TextView halfStepDown;
    List<SequenceViews> sequences = new ArrayList<>();

    class SequenceViews {
        final ImageView axis = inflateAxisView();
        final ImageView connectForward = inflateConnectorView();
        final ImageView connectBack = inflateConnectorView();
        final TextView forward = inflateChordView();
        final TextView back = inflateChordView();
        final Sequence sequence;
        SequenceViews(final Sequence s) {
            this.sequence = s;
            forward.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedChord = (TextView) v;
                    setChord(sequence.forward(chord));
                }
            });
            forward.setText(sequence.forward(chord).getName());
            back.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedChord = (TextView) v;
                    setChord(sequence.back(chord));
                }
            });
            back.setText(sequence.back(chord).getName());
        }
    }
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

    public void setOnChordChangedListener(OnChordChangedListener onChordChangedListener) {
        this.onChordChangedListener = onChordChangedListener;
    }

    public void setCurrentChordClickListener(final OnClickListener listener) {
        centralChord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                centralChord.animate().scaleX(2.5f).scaleY(2.5f).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        centralChord.animate().scaleX(2f).scaleY(2f).start();
                    }
                }).start();
                listener.onClick(v);
            }
        });
    }

    private void init() {
        chord = new Chord(0, MAJ_6);
        centralChord = inflateChordView();
        halfStepUp = inflateChordView();
        halfStepDown = inflateChordView();
        inflateBG();
        centralChord.setZ(5);
        halfStepUp.setZ(1);
        halfStepDown.setZ(1);
        halfStepUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedChord = (TextView) v;
                setChord(CHROMATIC.forward(chord));
            }
        });
        halfStepDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedChord = (TextView) v;
                setChord(CHROMATIC.back(chord));
            }
        });
        updateChordText();
        post(new Runnable() {
            @Override
            public void run() {
                TopologyAnimations.skipToInitialState(TopologyView.this);
                TopologyAnimations.animateToSelectionPhase(TopologyView.this);
            }
        });
    }

    public Chord getChord() {
        return chord;
    }

    public void setChord(Chord c) {
        chord = c;
        if(selectedChord != null) {
            TopologyAnimations.animateToTargetChord(this);
        } else {
            updateChordText();
        }
        if(onChordChangedListener != null) onChordChangedListener.onChordChanged(chord);
    }

    void updateChordText() {
        centralChord.setText(chord.getName());
        halfStepUp.setText(CHROMATIC.forward(chord).getName());
        halfStepDown.setText(CHROMATIC.back(chord).getName());
        for(SequenceViews sv : sequences) {
            sv.forward.setText(sv.sequence.forward(chord).getName());
            sv.back.setText(sv.sequence.back(chord).getName());
        }
    }


   private TextView inflateChordView() {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_chord, this, true);
        TextView result = (TextView) findViewWithTag("newChord");
        result.setTag(null);
        result.setZ(2);
        return result;
    }

    private ImageView inflateAxisView() {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_axis, this, true);
        ImageView result = (ImageView) findViewWithTag("newConnector");
        result.setZ(0);
        result.setTag(null);
        return result;
    }

    private ImageView inflateConnectorView() {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_connector, this, true);
        ImageView result = (ImageView) findViewWithTag("newConnector");
        result.setZ(1);
        result.setTag(null);
        return result;
    }

    private ImageView inflateBG() {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_bg, this, true);
        ImageView result = (ImageView) findViewWithTag("newBG");
        result.setZ(3);
        result.setTag(null);
        return result;
    }

    public void addSequence(Sequence sequence) {
        sequences.add(new SequenceViews(sequence));
    }
}

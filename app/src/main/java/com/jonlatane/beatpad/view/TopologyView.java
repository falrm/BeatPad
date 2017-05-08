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
import static com.jonlatane.beatpad.harmony.chord.Chord.MAJ_7;

/**
 * Created by jonlatane on 5/5/17.
 */
public class TopologyView extends RelativeLayout {
    private static final String TAG = TopologyView.class.getSimpleName();
    static final float CONNECTOR_Z = 1;
    private Chord chord;
    private OnChordChangedListener onChordChangedListener;

    TextView selectedChord;
    TextView centralChord;
    ImageView centralChordBackground;
    TextView halfStepUp;
    TextView halfStepDown;
    ImageView halfStepBackground;
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
                    if(!forward.getText().equals(centralChord.getText())) {
                        selectedChord = (TextView) v;
                        setChord(sequence.forward(chord));
                    }
                }
            });
            forward.setText(sequence.forward(chord).getName());
            back.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!back.getText().equals(centralChord.getText())) {
                        selectedChord = (TextView) v;
                        setChord(sequence.back(chord));
                    }
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
                TopologyAnimations.animateCentralChordClick(TopologyView.this);
                listener.onClick(v);
            }
        });
    }

    private void init() {
        chord = new Chord(0, MAJ_7);
        inflateBG();
        centralChord = inflateChordView(6);
        halfStepUp = inflateChordView(4);
        halfStepDown = inflateChordView(4);
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
        return inflateChordView(2);
    }

    private TextView inflateChordView(float defaultZ) {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_chord, this, true);
        TextView result = (TextView) findViewWithTag("newChord");
        result.setTag(null);
        result.setZ(defaultZ);
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
        result.setZ(CONNECTOR_Z);
        result.setTag(null);
        return result;
    }

    private void inflateBG() {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_bg, this, true);
        centralChordBackground = (ImageView) findViewWithTag("newBG");
        centralChordBackground.setZ(5);
        centralChordBackground.setTag(null);
        LayoutInflater.from(getContext()).inflate(R.layout.topology_bg_axis, this, true);
        halfStepBackground = (ImageView) findViewWithTag("newBG");
        halfStepBackground.setZ(3);
        halfStepBackground.setTag(null);
    }

    public void addSequence(Sequence sequence) {
        sequences.add(new SequenceViews(sequence));
    }
}

package hk.amae.frag;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import hk.amae.sampler.R;

public class BacklitFrag extends Fragment implements SeekBar.OnSeekBarChangeListener {
    TextView labelNormal;
    TextView labelSaving;

    public BacklitFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_backlit, container, false);
        labelNormal = (TextView) v.findViewById(R.id.label_normal);
        labelSaving = (TextView) v.findViewById(R.id.label_saving);

        SeekBar barNormal = (SeekBar) v.findViewById(R.id.seekBar_normal);
        SeekBar barSaving = (SeekBar) v.findViewById(R.id.seekBar_saving);

        setSeekBar(true, 4);
        barNormal.setProgress(4);
        setSeekBar(false, 5);
        barSaving.setProgress(5);

        barNormal.setOnSeekBarChangeListener(this);
        barSaving.setOnSeekBarChangeListener(this);
        return v;
    }

    private void setSeekBar(boolean normal, int i) {
        if (normal)
            labelNormal.setText(String.format("正常背光(%d%%)", i*10));
        else
            labelSaving.setText(String.format("节能背光(%d%%)", i*10));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (seekBar.getId() == R.id.seekBar_normal)
            setSeekBar(true, i);
        else
            setSeekBar(false, i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}

package hk.amae.frag;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.util.Command;

public class BacklitFrag extends Fragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    TextView labelNormal;
    TextView labelSaving;

    SeekBar barNormal, barSaving;

    final String SP_NORMAL = "normal_backlit";
    final String SP_SAVING = "saving_backlit";

    public BacklitFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_backlit, container, false);
        labelNormal = (TextView) v.findViewById(R.id.label_normal);
        labelSaving = (TextView) v.findViewById(R.id.label_saving);

        barNormal = (SeekBar) v.findViewById(R.id.seekBar_normal);
        barSaving = (SeekBar) v.findViewById(R.id.seekBar_saving);

        barNormal.setOnSeekBarChangeListener(this);
        barSaving.setOnSeekBarChangeListener(this);

        v.findViewById(R.id.btn_confirm).setOnClickListener(this);
        v.findViewById(R.id.btn_back).setOnClickListener(this);

        setBacklit(false, -1, -1); // query

        return v;
    }

    private void setSeekBar(boolean normalBacklit, int i) {
        if (normalBacklit) {
            barNormal.setProgress(i);
            labelNormal.setText(String.format("正常背光(%d%%)", i * 10));
        } else {
            labelSaving.setText(String.format("节能背光(%d%%)", i * 10));
            barSaving.setProgress(i);
        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                setBacklit(true, barNormal.getProgress(), barSaving.getProgress());
                break;
            case R.id.btn_back:
                getActivity().onBackPressed();
                break;
        }
    }

    private void setBacklit(final boolean doSet, final int normal, final int saving) {
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (doSet) {
                    Toast.makeText(getActivity(), "背光设置保存成功", Toast.LENGTH_SHORT).show();
                } else {
//                    cmd.NormalBacklit = (byte) Comm.getIntSP(SP_NORMAL);
//                    cmd.SavingBacklit = (byte) Comm.getIntSP(SP_SAVING);
                    setSeekBar(true, cmd.NormalBacklit);
                    setSeekBar(false, cmd.SavingBacklit);
                }

//                Comm.setIntSP(SP_NORMAL, cmd.NormalBacklit);
//                Comm.setIntSP(SP_SAVING, cmd.SavingBacklit);

                setSeekBar(true, cmd.NormalBacklit);
                setSeekBar(false, cmd.SavingBacklit);
            }
        }).setBacklit(doSet, normal, saving);
    }
}

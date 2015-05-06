package hk.amae.frag;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.widget.TextProgressBar;

public class ClearSampleFrag extends Fragment implements DialogInterface.OnClickListener {

    TextProgressBar progressBar;
    Timer timer = new Timer();
    final String SP_CLEAR = "clear_progress";

    public ClearSampleFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_clear_sample, container, false);

        progressBar = (TextProgressBar) v.findViewById(R.id.prog_clearing);

        doClearSample(false); // 先查询，看是否仍在清空数据中。。。

        return v;
    }

    void doClearSample(boolean doSet) {
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                cmd.Progress = (byte) Comm.getIntSP(SP_CLEAR);
                if (cmd.Progress == 0) {
                    new AlertDialog.Builder(getActivity()).setTitle("确认清空采样数据？")
                            .setCancelable(false).setPositiveButton("清空", ClearSampleFrag.this)
                            .setNegativeButton("取消", ClearSampleFrag.this).show();
                } else {
                    cycleQuery();
                }
            }
        }).setClearSample(doSet);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                doClearSample(true);
                Comm.setIntSP(SP_CLEAR, 1);
                cycleQuery();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                getActivity().onBackPressed();
                break;
        }
    }

    private void cycleQuery() {

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Command(new Command.Once() {
                    @Override
                    public void done(boolean verify, Command cmd) {
                        cmd.Progress = (byte) Comm.getIntSP(SP_CLEAR);
                        cmd.Progress += 10;

                        progressBar.setProgress(cmd.Progress);

                        if (cmd.Progress >= 100) {
                            timer.cancel();
                            cmd.Progress = 0;
                        }
                        Comm.setIntSP(SP_CLEAR, cmd.Progress);

                    }
                }).setClearSample(false);
            }
        }, 0, 1000);
    }
}

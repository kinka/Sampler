package hk.amae.frag;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        v.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        doClearSample(false); // 先查询，看是否仍在清空数据中。。。

        return v;
    }

    void doClearSample(final boolean doSet) {
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                int progress = Comm.getIntSP(SP_CLEAR);
                if (progress == 0) {
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
                Comm.setIntSP(SP_CLEAR, 1);
                doClearSample(true);
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
                        cmd.Progress = (byte) Comm.getIntSP(SP_CLEAR); // todo 使用机器返回的进度
                        cmd.Progress += 10;

                        progressBar.setProgress(cmd.Progress);

                        if (cmd.Progress >= 100) {
                            timer.cancel();
                            timer.purge();
                            cmd.Progress = 0;
                        }
                        Comm.setIntSP(SP_CLEAR, cmd.Progress);
                    }
                }).setClearSample(false);
            }
        }, 0, 1000);
    }

    @Override
    public void onPause() {
        try {
            timer.cancel();
        } catch (Exception e) {

        }
        super.onPause();
    }
}

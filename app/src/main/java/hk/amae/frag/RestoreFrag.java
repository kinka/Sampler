package hk.amae.frag;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.widget.AmaeDateTimePicker;
import hk.amae.widget.TextProgressBar;

public class RestoreFrag extends Fragment implements DialogInterface.OnClickListener {

    TextProgressBar progressBar;
    Timer timer = new Timer();
    final String SP_RESTORE = "restore_progress";

    public RestoreFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_restore, container, false);

        progressBar = (TextProgressBar) v.findViewById(R.id.prog_restoring);

        doRestore(false); // 先查询，看是否仍在恢复中。。。

        return v;
    }

    void doRestore(boolean doSet) {
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                cmd.Progress = (byte) Comm.getIntSP(SP_RESTORE);
                if (cmd.Progress == 0) {
                    new AlertDialog.Builder(getActivity()).setTitle("确认恢复出厂设置？")
                            .setCancelable(false).setPositiveButton("清空", RestoreFrag.this)
                            .setNegativeButton("取消", RestoreFrag.this).show();
                } else {
                    cycleQuery();
                }
            }
        }).setRestore(doSet);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                doRestore(true);
                Comm.setIntSP(SP_RESTORE, 1);
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
                        cmd.Progress = (byte) Comm.getIntSP(SP_RESTORE);
                        cmd.Progress += 10;

                        progressBar.setProgress(cmd.Progress);

                        if (cmd.Progress >= 100) {
                            timer.cancel();
                            timer.purge();
                            cmd.Progress = 0;
                        }
                        Comm.setIntSP(SP_RESTORE, cmd.Progress);

                    }
                }).setRestore(false);
            }
        }, 0, 100);
    }

    @Override
    public void onPause() {
        try {
            timer.cancel();
            timer.purge();
        } catch (Exception e) {

        }
        super.onPause();
    }
}

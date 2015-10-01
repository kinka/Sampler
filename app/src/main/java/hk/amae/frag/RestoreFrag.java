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

    TextView lblStatus;

    TextProgressBar progressBar;
    Timer timer = new Timer();
    final String SP_RESTORE = "restore_progress";
    boolean restoring = false;

    public RestoreFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_restore, container, false);

        progressBar = (TextProgressBar) v.findViewById(R.id.prog_restoring);
        lblStatus = (TextView) v.findViewById(R.id.lbl_status);

        v.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        v.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (restoring) return;

                new AlertDialog.Builder(getActivity()).setTitle("确认恢复出厂设置？")
                        .setCancelable(false).setPositiveButton("确定恢复", RestoreFrag.this)
                        .setNegativeButton("取消", RestoreFrag.this).show();
            }
        });


        return v;
    }

    void doRestore() {
        Comm.setIntSP(SP_RESTORE, 0);
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                lblStatus.setText("正在恢复");
                timer = new Timer();
                cycleQuery();
            }
        }).setRestore(true);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                doRestore();
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

                        Comm.setIntSP(SP_RESTORE, cmd.Progress);
                        if (cmd.Progress >= 100) {
                            timer.cancel();
                            timer.purge();
                            lblStatus.setText("恢复完成");
                        } else {
                            cycleQuery();
                        }
                    }
                }).setRestore(false);
            }
        }, 100);
    }
}

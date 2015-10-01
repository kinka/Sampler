package hk.amae.frag;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.widget.TextProgressBar;

public class ClearSampleFrag extends Fragment implements DialogInterface.OnClickListener {

    TextView lblStatus;

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
                new AlertDialog.Builder(getActivity()).setTitle("确认清空采样数据？")
                        .setCancelable(false).setPositiveButton("清空", ClearSampleFrag.this)
                        .setNegativeButton("取消", ClearSampleFrag.this).show();
            }
        });

        return v;
    }

    void doClearSample() {
        Comm.setIntSP(SP_CLEAR, 0);
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                lblStatus.setText("正在清空");
                timer = new Timer();
                cycleQuery();
            }
        }).setClearSample(true);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                doClearSample();
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
                        Comm.setIntSP(SP_CLEAR, cmd.Progress);

                        if (cmd.Progress >= 100) {
                            timer.cancel();
                            timer.purge();
                            lblStatus.setText("清空完成");
                        } else {
                            cycleQuery();
                        }
                    }
                }).setClearSample(false);
            }
        }, 100);
    }
}

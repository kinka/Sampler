package hk.amae.sampler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.util.Command;
import hk.amae.util.Command.Once;
import hk.amae.widget.TextProgressBar;

public class CleanMachineAct extends Activity implements DialogInterface.OnClickListener {

    static TextProgressBar progClean;
    static TextView lblStatus;
    static TextView lblRemaining;

    static String fmtRemaining;

    TimerTask cleanTask;
    static Timer timer;
    static int progress = 0;

    static int total = 60 * 5 * 1000;
    static int cnt = total;
    static boolean cleaning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_clean_machine);

        progClean = (TextProgressBar) findViewById(R.id.prog_cleaning);

        lblStatus = (TextView) findViewById(R.id.lbl_status);
        lblStatus.setText("等待开始");
        lblRemaining = (TextView) findViewById(R.id.lbl_remain_seconds);
        fmtRemaining = lblRemaining.getText().toString();

        progress = 0;

        lblRemaining.setText("\n");

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CleanMachineAct.this.onBackPressed();
            }
        });
        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doClean();
            }
        });
    }

    private Handler handler = new TimerHandler();

    static class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            progress = 100 - cnt * 100 / total;

            progClean.setProgress(progress);
            lblRemaining.setText(String.format(fmtRemaining, cnt / 1000));
            if (progress >= 100) {
                progress = 100;
                cleaning = false;
                try {
                    timer.cancel();
                    timer.purge();
                    lblStatus.setText("清洗完毕");
                    lblRemaining.setText("\n");
                    Toast.makeText(progClean.getContext(), "清洗完毕", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {

                }
            } else {
                cnt -= 1000;
                if (cnt < 0) cnt = 0;
            }
        }
    }

    private void doClean() {
        if (cleaning) return;
        lblStatus.setText("正在清洗");
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (cmd.CleanTime != 0)
                    cnt = total = cmd.CleanTime * 1000;
                cleaning();
            }
        }).setClean(true);
    }

    private void cancelClean() {
        try {
            timer.cancel();
            timer.purge();
            progress = 0;
        } catch (Exception e) {

        }
        if (!cleaning) return;
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                Toast.makeText(getApplicationContext(), "已经停止清洗", Toast.LENGTH_SHORT).show();
            }
        }).setClean(false);
    }

    private void cleaning() {
        cleaning = true;
        timer = new Timer();
        cleanTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                handler.sendMessage(msg);
            }
        };
        timer.schedule(cleanTask, 0, 1000);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        if (!cleaning) {
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this).setTitle("清洁中").setMessage("确定离开，将立即取消清洗")
                    .setPositiveButton("离开", this).setNegativeButton("留下", this).setCancelable(false).show();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case AlertDialog.BUTTON_POSITIVE:
                cancelClean();
                super.onBackPressed();
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                break;
        }
    }

}

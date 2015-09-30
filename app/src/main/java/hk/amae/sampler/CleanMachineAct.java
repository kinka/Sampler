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

    TextProgressBar progClean;
    TextView lblStatus;
    TextView lblRemaining;

    static String fmtRemaining;

    TimerTask cleanTask;
    Timer timer;
    static int progress = 0;

    int total = 60 * 5 * 1000;
    int cnt = total;
    boolean cleaning = false;

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

        lblRemaining.setText(String.format(fmtRemaining, cnt / 1000));

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

//        if (progress == 0)
//            doClean();
//        else
//            cleaning();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            cnt -= 1000;
            progress = 100 - cnt * 100 / total;
            progClean.setProgress(progress);
            if (progress >= 100) {
                progress = 100;
                cleaning = false;
                try {
                    timer.cancel();
                    timer.purge();
                    lblStatus.setText("清洗完毕");
                    Toast.makeText(CleanMachineAct.this, "清洗完毕", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {

                }
            }
            lblRemaining.setText(String.format(fmtRemaining, cnt / 1000));
        }
    };
    private void doClean() {
        if (cleaning) return;
        lblStatus.setText("正在清洗");
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
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
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                cleaning();
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
        timer.schedule(cleanTask, 1000, 1000);
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
    protected void onPause() {
        cancelClean();
        super.onPause();
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

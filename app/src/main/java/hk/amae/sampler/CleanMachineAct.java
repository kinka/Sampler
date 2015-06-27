package hk.amae.sampler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.util.Command;
import hk.amae.util.Command.Once;
import hk.amae.widget.TextProgressBar;

public class CleanMachineAct extends Activity implements DialogInterface.OnClickListener {

    TextProgressBar progClean;

    TimerTask cleanTask;
    Timer timer;
    static int progress = 0;

    int total = 60 * 3 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_clean_machine);

        progClean = (TextProgressBar) findViewById(R.id.prog_cleaning);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CleanMachineAct.this.onBackPressed();
            }
        });

        if (progress == 0)
            doClean();
        else
            cleaning();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progress += 1;
            progClean.setProgress(progress);
            if (progress == 100) {
                try {
                    timer.cancel();
                    timer.purge();
                    Toast.makeText(CleanMachineAct.this, "清洗完毕", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {

                }
            }
        }
    };
    private void doClean() {
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                cleaning();
            }
        }).setClean();
    }

    private void cleaning() {
        timer = new Timer();
        cleanTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                handler.sendMessage(msg);
            }
        };
        timer.schedule(cleanTask, progress == 0 ? total / 100 : 0, total / 100);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        if (progress >= 100) {
            progress = 0;
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this).setTitle("清洁中").setMessage("正在清洁中...")
                    .setPositiveButton("离开", this).setNegativeButton("留下", this).setCancelable(false).show();
        }
    }

    @Override
    protected void onPause() {
        try {
            timer.cancel();
            timer.purge();
        } catch (Exception e) {

        }
        super.onPause();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case AlertDialog.BUTTON_POSITIVE:
//                timer.cancel();
                super.onBackPressed();
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                break;
        }
    }

}

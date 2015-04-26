package hk.amae.sampler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.util.Command;
import hk.amae.util.Command.Once;
import hk.amae.widget.TextProgressBar;

public class CleanMachineAct extends Activity implements DialogInterface.OnClickListener {

    TextProgressBar progClean;
    Button btnBack;

    TimerTask cleanTask;
    Timer timer;
    int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_clean_machine);

        progClean = (TextProgressBar) findViewById(R.id.prog_cleaning);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        doClean();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progress += 1;
            progClean.setProgress(progress);
            if (progress == 100)
                timer.cancel();
        }
    };
    private void doClean() {
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                timer = new Timer();
                cleanTask = new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        handler.sendMessage(msg);
                    }
                };
                timer.schedule(cleanTask, 100, 100);
            }
        }).setClean();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        if (progress == 100)
            super.onBackPressed();
        else
            new AlertDialog.Builder(this).setTitle("确认").setMessage("正在清洁中，确定离开吗？")
                .setPositiveButton("离开", this).setNegativeButton("留下", this).setCancelable(false).show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case AlertDialog.BUTTON_POSITIVE:
                timer.cancel();
                super.onBackPressed();
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                break;
        }
    }

}

package hk.amae.sampler;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.util.ActivityGestureDetector;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.util.SwipeInterface;


public class AdjustAct extends Activity implements View.OnClickListener, SwipeInterface, DialogInterface.OnClickListener {
    static String FMT_CHANNEL = "第%d通道校准";
    LinearLayout adjustContainer;
    TextView labelChannel;
    int channel = 1;

    TextView outputPower, dutyCycle, pickPower, pickVoltage, adjustStatus;
    EditText adjustPressure, adjustSpeed;
    Button btnPressure, btnSpeed, btnAction;

    final int ACT_QUERY = 1; // 查询状态
    final int ACT_START = 2; // 开始校准
    final int ACT_STATE = 3; // 校准状态
    final int ACT_PRESSURE= 4; // 设置预估压力
    final int ACT_SPEED = 5; // 设置被校流量
    final int ACT_STOP = 6; // 结束校准

    int state;
    final int STATE_ING = 1;
    final int STATE_STOPPED = 2;
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_adjustment);

        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);

        labelChannel = (TextView) findViewById(R.id.label_channel);
        labelChannel.setText(String.format(FMT_CHANNEL, channel));

        adjustContainer = (LinearLayout) findViewById(R.id.adjust_container);

        ActivityGestureDetector gestureDetector = new ActivityGestureDetector(this, this);
        adjustContainer.setOnTouchListener(gestureDetector);

        outputPower = (TextView) findViewById(R.id.txt_output);
        dutyCycle = (TextView) findViewById(R.id.txt_duty_cycle);
        pickPower = (TextView) findViewById(R.id.txt_pressure);
        pickVoltage = (TextView) findViewById(R.id.txt_voltage);
        adjustStatus = (TextView) findViewById(R.id.txt_status);

        adjustPressure = (EditText) findViewById(R.id.txt_adjust_pressure);
        adjustSpeed = (EditText) findViewById(R.id.txt_adjust_speed);

        btnAction = (Button) findViewById(R.id.btn_action);
        btnAction.setOnClickListener(this);

        findViewById(R.id.btn_back).setOnClickListener(this);

        btnPressure = (Button) findViewById(R.id.btn_act_pressure);
        btnSpeed = (Button) findViewById(R.id.btn_act_speed);
        btnPressure.setOnClickListener(this);
        btnSpeed.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        if (state == STATE_ING) {
            new AlertDialog.Builder(this).setTitle("正在校准").setMessage("确定离开，将结束校准")
                    .setPositiveButton("离开", this).setNegativeButton("留下", this).setCancelable(false).show();
            return;
        }
        doAdjust(ACT_STOP);
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        view.startAnimation(animation);
        switch (view.getId()) {
            case R.id.btn_prev:
                flip(false);
                break;
            case R.id.btn_next:
                flip(true);
                break;
            case R.id.btn_action:
                Button btn = (Button) view;
                if (state == STATE_ING) { // 正在校准
                    doAdjust(ACT_STOP);
                } else {
                    doAdjust(ACT_START);
                }
                break;
            case R.id.btn_act_pressure:
                doAdjust(ACT_PRESSURE);
                break;
            case R.id.btn_act_speed:
                doAdjust(ACT_SPEED);
                break;
            case R.id.btn_back:
                this.onBackPressed();
                break;
        }

    }

    private void doAdjust(final int act) {
        if (act == ACT_STOP) killTimer();
        String strPressure = adjustPressure.getText().toString();
        String strSpeed = adjustSpeed.getText().toString();
        if (strPressure.length() == 0) strPressure = "0";
        if (strSpeed.length() == 0) strSpeed = "0";

        int expect = Integer.valueOf(strPressure);
        int speed = Integer.valueOf(strSpeed);
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;

                outputPower.setText(cmd.OutputPower + "");
                dutyCycle.setText("(" + cmd.DutyCycle + "%)");
                pickPower.setText(cmd.PickPower + "");
                pickVoltage.setText(String.format("(%.02fV)", cmd.PickVoltage / 1000.0));
                if (act == ACT_START || act == ACT_SPEED)
                    adjustPressure.setText(cmd.AdjustPressure + "");

                if (act == ACT_STOP) {
                    btnAction.setText("开始校准");
                    adjustStatus.setText("等待校准");
                    state = STATE_STOPPED;
                    btnPressure.setEnabled(false);
                    btnSpeed.setEnabled(false);
                } else if (act == ACT_START) {
                    btnAction.setText("结束校准");
                    adjustStatus.setText("正在校准");
                    state = STATE_ING;
                    btnPressure.setEnabled(true);
                    btnSpeed.setEnabled(true);
                    query();
                }

                if (cmd.AdjustStatus == ACT_STOP)
                    adjustStatus.setText("校准完成");
                Comm.logI("status " + cmd.AdjustStatus);
            }
        }).setAdjust(act, Comm.Channel.init(channel), expect, speed);
    }

    private void killTimer() {
        try {
            timer.cancel();
            timer.purge();
        } catch (Exception e) {

        }
    }
    private void query() {
        killTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                doAdjust(ACT_QUERY);
            }
        }, 1000, 1000);
    }

    boolean switchChannel(boolean add) {
        if ((add && channel >= 8) || (!add && channel <=1 ))
            return false;

        if (state == STATE_ING) {
            Toast.makeText(this, "请先结束校准", Toast.LENGTH_SHORT).show();
            return false;
        }

        channel = add ? channel+1 : channel-1;
        outputPower.setText("0");
        dutyCycle.setText("(0%)");
        pickPower.setText("0");
        pickVoltage.setText("(0.00)");
//        adjustPressure.setText("0");
//        adjustSpeed.setText("0");
        labelChannel.setText(String.format(FMT_CHANNEL, channel));
        return true;
    }

    void flip(boolean add) {
        if (!switchChannel(add))
            return;

        int duration = 200;
        ObjectAnimator transA, transB;
        AnimatorSet set = new AnimatorSet();
        if (add) {
            transA = ObjectAnimator.ofFloat(adjustContainer, "translationX", 0, -adjustContainer.getMeasuredWidth());

            transB = ObjectAnimator.ofFloat(adjustContainer, "translationX", adjustContainer.getMeasuredWidth(), 0);
        } else {
            transA = ObjectAnimator.ofFloat(adjustContainer, "translationX", 0, adjustContainer.getMeasuredWidth());

            transB = ObjectAnimator.ofFloat(adjustContainer, "translationX", -adjustContainer.getMeasuredWidth(), 0);
        }

        transA.setDuration(duration);
        transB.setDuration(duration);
        set.play(transA).before(transB);
        set.start();
    }

    @Override
    public void onLeftWipe(View v) {
//        Comm.logI("LeftWipe");
        flip(false);
    }

    @Override
    public void onRightWipe(View v) {
//        Comm.logI("RightWipe");
        flip(true);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case AlertDialog.BUTTON_POSITIVE:
                state = 2;
                onBackPressed();
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                break;
        }
    }
}

package hk.amae.sampler;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import hk.amae.util.ActivityGestureDetector;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.util.SwipeInterface;


public class AdjustAct extends Activity implements View.OnClickListener, SwipeInterface {
    static String FMT_CHANNEL = "第%d通道校准";
    LinearLayout adjustContainer;
    TextView labelChannel;
    int channel = 1;

    TextView outputPower, dutyCycle, pickPower, pickVoltage;
    TextView expectPressure, targetSpeed;

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

        expectPressure = (TextView) findViewById(R.id.txt_expect);
        targetSpeed = (TextView) findViewById(R.id.txt_target_speed);

        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
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
            case R.id.btn_save:
                doAdjust();
                break;
            case R.id.btn_cancel:
                onBackPressed();
                break;
        }

    }

    private void doAdjust() {
        int expect = Integer.valueOf(expectPressure.getText().toString());
        int speed = Integer.valueOf(targetSpeed.getText().toString());
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                outputPower.setText(cmd.OutputPower + "");
                dutyCycle.setText("(" + cmd.DutyCycle + "%)");
                pickPower.setText(cmd.PickPower + "");
                pickVoltage.setText(String.format("(%.02fV)", cmd.PickVoltage / 1000.0));
                expectPressure.setText(cmd.ExpectPressure + "");
                targetSpeed.setText(cmd.AdjustSpeed + "");
                Toast.makeText(AdjustAct.this, String.format("第%d通道已经保存。", channel), Toast.LENGTH_SHORT).show();
            }
        }).setAdjust(Comm.Channel.init(channel), expect, speed);
    }

    boolean switchChannel(boolean add) {
        if ((add && channel >= 8) || (!add && channel <=1 ))
            return false;
        channel = add ? channel+1 : channel-1;
        outputPower.setText("0");
        dutyCycle.setText("(0%)");
        pickPower.setText("0");
        pickVoltage.setText("(0.00)");
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
}

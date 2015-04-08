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

import hk.amae.util.ActivityGestureDetector;
import hk.amae.util.Comm;
import hk.amae.util.SwipeInterface;


public class AdjustAct extends Activity implements View.OnClickListener, SwipeInterface {
    static String FMT_CHANNEL = "第%d通道校准";
    LinearLayout adjustContainer;
    TextView labelChannel;
    int channel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_adjustment);

        findViewById(R.id.btn_pre).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);

        labelChannel = (TextView) findViewById(R.id.label_channel);
        labelChannel.setText(String.format(FMT_CHANNEL, channel));

        adjustContainer = (LinearLayout) findViewById(R.id.adjust_container);

        ActivityGestureDetector gestureDetector = new ActivityGestureDetector(this, this);
        adjustContainer.setOnTouchListener(gestureDetector);
    }

    @Override
    public void onClick(View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        view.startAnimation(animation);

        switch (view.getId()) {
            case R.id.btn_pre:
                flip(false);
                break;
            case R.id.btn_next:
                flip(true);
                break;
        }

    }

    boolean switchChannel(boolean add) {
        if ((add && channel >= 8) || (!add && channel <=1 ))
            return false;
        channel = add ? channel+1 : channel-1;
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
        Comm.logI("LeftWipe");
        flip(false);
    }

    @Override
    public void onRightWipe(View v) {
        Comm.logI("RightWipe");
        flip(true);
    }
}

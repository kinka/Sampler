package hk.amae.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.sampler.R;

/**
 * Created by kinka on 6/4/15.
 */
public class AmaeClickDetector implements View.OnTouchListener {
    public static final int MSG_CLICK = 1;
    public static final int MSG_DBLCLICK = 2;
    public static final int MSG_PRESSED_3 = 3; // 长按3秒

    TimerTask poweroffTask = null;
    TimerTask clickTask = null;
    int clickCnt = 0;
    long lastupTime = 0;
    long touchStartTime = 0;

    private Timer btnRunTimer = new Timer();
    private Handler handler;

    public AmaeClickDetector(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        final int id = view.getId();
        switch (view.getId()) {
            case R.id.btn_stop:
            case R.id.toggle_run:
                if (poweroffTask == null)
                    poweroffTask = new TimerTask() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.what = MSG_PRESSED_3;
                            handler.sendMessage(msg);
                            poweroffTask = null;
                        }
                    };
                if (clickTask == null)
                    clickTask = new TimerTask() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.what = MSG_CLICK;
                            Bundle data = new Bundle();
                            data.putInt("which", id);
                            msg.setData(data);
                            handler.sendMessage(msg);
                            clickTask = null;
                            clickCnt = 0;
                        }
                    };

                final int CLICKGAP = 150, DBLCLICKGAP = 150;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btnRunTimer.schedule(poweroffTask, 3000);

                    touchStartTime = System.currentTimeMillis();

//                    Comm.logI("gap " + (touchStartTime - lastupTime));
                    if (clickCnt==1 && touchStartTime - lastupTime < DBLCLICKGAP) { // 触发双击
                        clickCnt++;
                        clickTask.cancel();
                        clickTask = null;
                        Message msg = new Message();
                        msg.what = MSG_DBLCLICK;
                        handler.sendMessage(msg);
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    long touchEndTime = System.currentTimeMillis();
//                    Comm.logI("passed time " + (touchEndTime - touchStartTime));
                    if (touchEndTime - touchStartTime < 3000) {
                        poweroffTask.cancel();
                        poweroffTask = null;
                    }

                    if (touchEndTime - touchStartTime <= CLICKGAP) { // click
                        clickCnt++;
                        if (clickCnt == 1)
                            btnRunTimer.schedule(clickTask, DBLCLICKGAP);
                        else
                            clickCnt = 0;
                    }

                    lastupTime = System.currentTimeMillis();
                }
                break;
        }
        return false;
    }
}

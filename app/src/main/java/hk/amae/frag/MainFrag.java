package hk.amae.frag;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.sampler.MonitorAct;
import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.widget.TextProgressBar;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFrag extends Fragment implements View.OnClickListener, View.OnTouchListener {
    OnMainFragListerer mCallback;

    Spinner spinChannel;
    Spinner spinModel;
    TextProgressBar progSampling;
    Activity parent;
    ImageButton btnLock;
    ImageButton btnRun;

    TextView txtTimingGroups;

    private int runningState = -1; // -1 停止 0 暂停 1 运行
    private boolean isLocked = false;
    private boolean isCharging = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_main, container, false);
        spinChannel = (Spinner) v.findViewById(R.id.spin_channel);
        spinModel = (Spinner) v.findViewById(R.id.spin_model);
        ArrayAdapter<CharSequence> spinAdapter =
                ArrayAdapter.createFromResource(parent, R.array.channels_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> modelAdapter =
                ArrayAdapter.createFromResource(parent, R.array.models_array, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinChannel.setAdapter(spinAdapter);
        spinModel.setAdapter(modelAdapter);

        progSampling = (TextProgressBar) v.findViewById(R.id.prog_sampling);
        progSampling.setProgress(98);

        btnLock = (ImageButton) v.findViewById(R.id.toggle_lock);
        btnLock.setOnClickListener(this);

        btnRun = (ImageButton) v.findViewById(R.id.toggle_run);
        btnRun.setOnTouchListener(this);

        v.findViewById(R.id.btn_setting).setOnClickListener(this);
        v.findViewById(R.id.btn_connect).setOnClickListener(this);
        v.findViewById(R.id.btn_query).setOnClickListener(this);
        v.findViewById(R.id.btn_clean).setOnClickListener(this);

        v.findViewById(R.id.btn_monitor).setOnClickListener(this);

        txtTimingGroups = (TextView) v.findViewById(R.id.txt_timing_groups);
        txtTimingGroups.setMovementMethod(new ScrollingMovementMethod());

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parent = activity;
        mCallback = (OnMainFragListerer) parent;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggle_lock:
                isLocked = !isLocked;
                if (isLocked)
                    ((ImageButton) view).setImageResource(R.drawable.lock);
                else
                    ((ImageButton) view).setImageResource(R.drawable.unlock);
                Rect rect = new Rect();
                btnLock.getDrawingRect(rect);
                mCallback.onLockToggled(isLocked);
                break;

            case R.id.btn_setting:
            case R.id.btn_connect:
            case R.id.btn_query:
            case R.id.btn_clean:
                mCallback.onButtonClick(view.getId());
                break;
            case R.id.btn_monitor:
                startActivity(new Intent(getActivity(), MonitorAct.class));
                break;
        }
    }

    TimerTask poweroffTask = null;
    TimerTask clickTask = null;
    int clickCnt = 0;
    long lastupTime = 0;
    long touchStartTime = 0;

    private Timer btnRunTimer = new Timer();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(parent, "关机提醒", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    if (runningState == 1) {
                        runningState = 0;
                        btnRun.setImageResource(R.drawable.pause);
                    } else {
                        runningState = 1;
                        btnRun.setImageResource(R.drawable.play);
                    }
                    break;
            }
        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (view.getId()) {
            case R.id.toggle_run:
                if (poweroffTask == null)
                    poweroffTask = new TimerTask() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.what = 1;
                            handler.sendMessage(msg);
                            poweroffTask = null;
                        }
                    };
                if (clickTask == null)
                    clickTask = new TimerTask() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.what = 2;
                            handler.sendMessage(msg);
                            clickTask = null;
                            clickCnt = 0;
                        }
                    };

                final int CLICKGAP = 150, DBLCLICKGAP = 150;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btnRunTimer.schedule(poweroffTask, 3000);

                    touchStartTime = System.currentTimeMillis();

                    Comm.logI("gap " + (touchStartTime - lastupTime));
                    if (clickCnt==1 && touchStartTime - lastupTime < DBLCLICKGAP) { // 触发双击
                        clickCnt++;
                        clickTask.cancel();
                        clickTask = null;
                        runningState = -1;
                        btnRun.setImageResource(R.drawable.stop);
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    long touchEndTime = System.currentTimeMillis();
                    Comm.logI("passed time " + (touchEndTime - touchStartTime));
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

    public interface OnMainFragListerer {
        public void onLockToggled(boolean locked);
        public void onButtonClick(int id);
    }
}

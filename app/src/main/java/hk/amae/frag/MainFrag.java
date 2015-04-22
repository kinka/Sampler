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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.sampler.ChannelAct;
import hk.amae.sampler.MonitorAct;
import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.widget.Battery;
import hk.amae.widget.TextProgressBar;

import hk.amae.util.Command.Once;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFrag extends Fragment implements View.OnClickListener, View.OnTouchListener, AdapterView.OnItemSelectedListener {
    OnMainFragListerer mCallback;

    Spinner spinChannel;
    Spinner spinModel;
    TextProgressBar progSampling;
    Activity parent;
    ImageButton btnLock;
    ImageButton btnRun;
    LinearLayout wrapManual, wrapTiming;
    TextView txtSpeed, txtVolume;

    TextView txtTimingGroups;

    private int runningState = Comm.STOPPED; // -1 停止 0 暂停 1 运行
    private boolean isLocked = false;
    private String sampleMode;

    private String fmtSpeed = "%d\nmL/min";
    private String fmtVolume = "%.2fL";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_main, container, false);
        spinChannel = (Spinner) v.findViewById(R.id.spin_channel);
        spinModel = (Spinner) v.findViewById(R.id.spin_model);

        int channels_res = getChannels();

        ArrayAdapter<CharSequence> spinAdapter =
                ArrayAdapter.createFromResource(parent, channels_res, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> modelAdapter =
                ArrayAdapter.createFromResource(parent, R.array.models_array, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinChannel.setAdapter(spinAdapter);
        spinModel.setAdapter(modelAdapter);

        spinChannel.setOnItemSelectedListener(this);
        spinModel.setOnItemSelectedListener(this);

        txtSpeed = (TextView) v.findViewById(R.id.txt_speed);
        txtVolume = (TextView) v.findViewById(R.id.txt_volume);

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

        wrapManual = (LinearLayout) v.findViewById(R.id.wrap_manual);
        wrapTiming = (LinearLayout) v.findViewById(R.id.wrap_timing);

        askBatteryState();

        sampleMode = Comm.getSP("sample_mode");

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parent = activity;
        mCallback = (OnMainFragListerer) parent;
    }

    private int getChannels() {
        String s_mode = Comm.getSP("channel_mode");
        int mode = 0;
        if (s_mode.length() == 0)
            mode = ChannelAct.MODE_SINGLE;
        else
            mode = Integer.valueOf(s_mode);

        switch (mode) {
            case ChannelAct.MODE_COUPLE:
                return R.array.channels_C;
            case ChannelAct.MODE_4IN1:
                return R.array.channels_B;
            case ChannelAct.MODE_8IN1:
                return R.array.channels_A;
            default:
                return R.array.channels_CH;
        }
    }

    private void askBatteryState() {
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                Battery battery = (Battery) parent.findViewById(R.id.battery);
                battery.setCharging(true);
                battery.setCapacity(50);
            }
        }).reqBattery();
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
                    if (runningState == Comm.PLAYING) {
                        runningState = Comm.PAUSED;
                        btnRun.setImageResource(R.drawable.pause);
                    } else {
                        runningState = Comm.PLAYING;
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
                        runningState = Comm.STOPPED;
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(spinChannel)) {
            String selected = spinChannel.getSelectedItem().toString();
            if (selected.equals("全选"))
                selected = "ALL";
            new Command(new Once() {
                @Override
                public void done(boolean verify, Command cmd) {
                    cmd.ChannelState = Comm.PLAYING;
                    cmd.Volume = 880;
                    cmd.Speed = 300;
                    txtSpeed.setText(String.format(fmtSpeed, cmd.Speed));
                    txtVolume.setText(String.format(fmtVolume, cmd.Volume / 1000.0));
                    runningState = cmd.ChannelState;
                }
            }).reqChannelState(Comm.Channel.valueOf(selected));
        } else if (parent.equals(spinModel)) {
            String selected = spinModel.getSelectedItem().toString();
            if (selected.equals("手动")) {
                wrapManual.setVisibility(View.VISIBLE);
                wrapTiming.setVisibility(View.GONE);
            } else {
                wrapManual.setVisibility(View.GONE);
                wrapTiming.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public interface OnMainFragListerer {
        void onLockToggled(boolean locked);
        void onButtonClick(int id);
    }
}

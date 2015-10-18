package hk.amae.frag;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import hk.amae.sampler.ChannelAct;
import hk.amae.sampler.MainAct;
import hk.amae.sampler.ModeSettingAct;
import hk.amae.sampler.MonitorAct;
import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.util.Comm.Channel;
import hk.amae.util.Command;
import hk.amae.widget.Battery;
import hk.amae.widget.NumberOperator;
import hk.amae.widget.TextProgressBar;

import hk.amae.util.Command.Once;

import hk.amae.widget.SettingItem;
import hk.amae.util.AmaeClickDetector;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFrag extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    OnMainFragListener mCallback;

    Spinner spinChannel;
    Spinner spinMode;
    TextProgressBar progSampling;
    Activity parent;
    ImageButton btnLock;
    ImageButton btnRun;
    LinearLayout wrapManual, wrapTiming;
    LinearLayout layoutCap, layoutTiming;
    TextView txtSpeed, txtVolume;

    NumberOperator npSpeed, npTiming, npVolume;

    TextView txtTimedSetting;
    TextView txtCountDown;

    TextView txtTips;

    private Channel currChannel = Channel.ALL;
    private int runningState = Comm.STOPPED; // -1 停止 0 暂停 1 运行
    private boolean isLocked = false;
    private int sampleMode;
    private boolean isSpinnerClick = true;

    private String fmtSpeed = "%d\nmL/min";
    private String fmtVolume = "%.2fL";

    private final String SP_MANUALMODE = "manual_mode"; // 手动情况下设定时长还是设定容量
    public static final String SP_SAMPLEMODE = "sample_mode";
    public static final String SP_CURRENTCHANNEL = "current_channel";

    private int lastManualMode;

    private Timer __battery, __progress, __launch, __state;
    private final int durationBattery = 60*1000, durationProgress = 1*1000, durationState = 3*1000;

    private static final int UNITSPEED = 1000; // 单通道最高流量
    public static int MaxSpeed = UNITSPEED;

    private String[] timedLaunchAt = new String[ModeSettingAct.GROUPCOUNT];
    private int[] timedDuration = new int[ModeSettingAct.GROUPCOUNT];
    private int[] timedVolume = new int[ModeSettingAct.GROUPCOUNT];
    private int[] timedSpeed = new int[ModeSettingAct.GROUPCOUNT];
    private boolean[] timedSet = new boolean[ModeSettingAct.GROUPCOUNT];
    private int waitingGroup = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_main, container, false);

        spinChannel = (Spinner) v.findViewById(R.id.spin_channel);
        spinMode = (Spinner) v.findViewById(R.id.spin_model);
        // channel and mode
        int channels_res = getChannels();

        ArrayAdapter<CharSequence> spinAdapter =
                ArrayAdapter.createFromResource(parent, channels_res, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> modelAdapter =
                ArrayAdapter.createFromResource(parent, R.array.models_array, android.R.layout.simple_spinner_item);

        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinChannel.setAdapter(spinAdapter);
        spinMode.setAdapter(modelAdapter);

        // state
        txtSpeed = (TextView) v.findViewById(R.id.txt_speed);
        txtVolume = (TextView) v.findViewById(R.id.txt_volume);

        progSampling = (TextProgressBar) v.findViewById(R.id.prog_sampling);

        btnLock = (ImageButton) v.findViewById(R.id.toggle_lock);
        btnLock.setOnClickListener(this);

        btnRun = (ImageButton) v.findViewById(R.id.toggle_run);
        btnRun.setOnTouchListener(new AmaeClickDetector(new ClickHandler()));
        v.findViewById(R.id.btn_stop).setOnClickListener(this);

        v.findViewById(R.id.btn_setting).setOnClickListener(this);
        v.findViewById(R.id.btn_connect).setOnClickListener(this);
        v.findViewById(R.id.btn_query).setOnClickListener(this);
        v.findViewById(R.id.btn_clean).setOnClickListener(this);

        v.findViewById(R.id.btn_monitor).setOnClickListener(this);

        txtTimedSetting = (TextView) v.findViewById(R.id.txt_timed_setting);
        txtTimedSetting.setMovementMethod(new ScrollingMovementMethod());

        txtCountDown = (TextView) v.findViewById(R.id.txt_countdown);
        txtCountDown.setText("");

        txtTips = (TextView) v.findViewById(R.id.txt_tips);

        npSpeed = (NumberOperator) v.findViewById(R.id.np_speed);
        npTiming = (NumberOperator) v.findViewById(R.id.np_timing);
        npVolume = (NumberOperator) v.findViewById(R.id.np_cap);
        setMaxSpeed(0);

        wrapManual = (LinearLayout) v.findViewById(R.id.wrap_manual);
        wrapTiming = (LinearLayout) v.findViewById(R.id.wrap_timing);

        layoutCap = (LinearLayout) v.findViewById(R.id.layout_capacity);
        layoutTiming = (LinearLayout) v.findViewById(R.id.layout_timing);
        v.findViewById(R.id.label_cap).setOnClickListener(this);
        v.findViewById(R.id.label_timing).setOnClickListener(this);

        if (Comm.getIntSP("locked") == 1) {
            isLocked = true;
            setLock();
        }

        return v;
    }

    // 查询并初始化当前通道的运行状态
    void initChannelState() {
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
//                    cmd.ChannelState = Comm.PLAYING;
//                    cmd.Volume = 880;
//                    cmd.Speed = 300;
                progSampling.setProgress(cmd.Progress);
                txtSpeed.setText(String.format(fmtSpeed, cmd.Speed));
                txtVolume.setText(String.format(fmtVolume, cmd.Volume / 1000.0));
                runningState = cmd.ChannelState;
//                runningState = Comm.PLAYING;
                updateRunningState();
            }
        }).reqChannelState(currChannel);
    }

    void init() {
        initChannelState();

        // 主要是想知道是手动模式还是定时模式(定时长/定容量), 以及当前工作的通道
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;

                currChannel = cmd.Channel;

                spinChannel.setOnItemSelectedListener(MainFrag.this);
                spinMode.setOnItemSelectedListener(MainFrag.this);

                sampleMode = cmd.Manual ? Comm.MANUAL_SET : cmd.SampleMode;
                Comm.setIntSP(SP_MANUALMODE, cmd.ManualMode);
                npSpeed.setValue(cmd.TargetSpeed);
                npVolume.setValue(cmd.TargetVolume);
                npTiming.setValue(cmd.TargetDuration);
                Comm.logI("svr sampleMode " + sampleMode);
                spinMode.setSelection(sampleMode);
                switchSampleMode();
            }
        }).reqSampleState(Channel.ALL);

        askBatteryState();

        reqMachineState();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parent = activity;
        mCallback = (OnMainFragListener) parent;
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
        killTimer();
        stopCountDown();
    }
    private void killTimer() {
        try {
            if (__battery != null)
                __battery.cancel();
        } catch (Exception e) {

        }
        try {
            if (__progress != null)
                __progress.cancel();
        } catch (Exception e) {

        }
        try {
            if (__state != null)
                __state.cancel();
        } catch (Exception e) {

        }
    }
    private void stopProgress() {
        try {
            if (__progress != null)
                __progress.cancel();
        } catch (Exception e) {

        }
    }
    private void stopCountDown() {
        try {
            if (__launch != null)
                __launch.cancel();
        } catch (Exception e) {

        }
    }
    private void startCountDown() {
        waitingGroup = -1;
        for (int i=0; i<ModeSettingAct.GROUPCOUNT; i++) {
            String diff = Comm.getDateDiff(timedLaunchAt[i]);
            if (diff.indexOf("-") == 0) // launch time passed
                continue;
            waitingGroup = i;
            break;
        }

        if (__launch != null)
            __launch.cancel();

        updateTimedStatus();

        if (waitingGroup < 0) {
            txtCountDown.setText("定时任务已经全部完成");
            return;
        }

        __launch = new Timer();
        __launch.schedule(new TimerTask() {
            @Override
            public void run() {
                Comm.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (waitingGroup == -1) return;

                        String diff = Comm.getDateDiff(timedLaunchAt[waitingGroup]);
                        txtCountDown.setText(String.format("第%d组 启动倒计时 %s", waitingGroup+1, diff));
                        if (diff.indexOf("-") == 0) { // 下一组
                            startCountDown();
                        }
                    }
                });
            }
        }, 0, 1*1000);
    }

    private int getChannels() {
        int mode = Comm.getIntSP(ChannelAct.SP_CHANNELMODE);

        switch (mode) {
            case ChannelAct.MODE_COUPLE:
                MaxSpeed = UNITSPEED * 2;
                currChannel = Channel.C1;
                return R.array.channels_C;
            case ChannelAct.MODE_4IN1:
                MaxSpeed = UNITSPEED * 4;
                currChannel = Channel.B1;
                return R.array.channels_B;
            case ChannelAct.MODE_8IN1:
                MaxSpeed = UNITSPEED * 8;
                currChannel = Channel.A1;
                return R.array.channels_A;
            default:
                MaxSpeed = UNITSPEED;
                currChannel = Channel.CH1;
                return R.array.channels_CH;
        }
    }

    private void askBatteryState() {
        __battery = new Timer();
        __battery.schedule(new TimerTask() {
            @Override
            public void run() {
                new Command(new Command.Once() {
                    @Override
                    public void done(boolean verify, Command cmd) {
                        if (!verify) return;

                        Battery battery = (Battery) parent.findViewById(R.id.battery);
                        if (battery == null) return;

                        battery.setCharging(cmd.Charging);
                        battery.setCapacity(cmd.Power);
                    }
                }).reqBattery();
            }
        }, 0, durationBattery);
    }

    private void setLock() {
        if (isLocked)
            btnLock.setImageResource(R.drawable.lock);
        else
            btnLock.setImageResource(R.drawable.unlock);
        mCallback.onLockToggled(isLocked);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggle_lock:
                isLocked = !isLocked;
                setLock();
                break;

            case R.id.btn_stop:
                runningState = Comm.STOPPED;
                switchRunningState();
                Toast.makeText(parent, "已发送停止命令", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_setting:
            case R.id.btn_connect:
            case R.id.btn_query:
            case R.id.btn_clean:
                mCallback.onButtonClick(view.getId());
                if (currChannel != null)
                    Comm.setIntSP(SP_CURRENTCHANNEL, currChannel.getValue());
                break;
            case R.id.btn_monitor:
                startActivity(new Intent(getActivity(), MonitorAct.class));
                break;
            case R.id.label_timing:
            case R.id.label_cap:
                lastManualMode = Comm.getIntSP(SP_MANUALMODE);
                if (view.getId() == R.id.label_timing) // toggle
                    Comm.setIntSP(SP_MANUALMODE, Comm.TIMED_SET_CAP);
                else
                    Comm.setIntSP(SP_MANUALMODE, Comm.TIMED_SET_TIME);
                initManualMode();
                break;
        }
    }

    /**
     * runningState 反映当前运行状态，但是图标展示的是下一步能进行的操作
     */
    private void __updateRunningState(boolean sendCmd) {
        int toDo = Comm.DO_PLAY;
        if (runningState == Comm.PAUSED || runningState == Comm.STOPPED) {
            npEnable(true);
            btnRun.setImageResource(R.drawable.play);
            toDo = runningState == Comm.PAUSED ? Comm.DO_PAUSE : Comm.DO_STOP;
            stopProgress();
        } else if (runningState == Comm.PLAYING) {
            int manualMode = Comm.getIntSP(SP_MANUALMODE);
            int cap = manualMode == Comm.TIMED_SET_TIME ? npTiming.getValue() : npVolume.getValue();
            boolean invalid = npSpeed.getValue() == 0 || cap == 0;
            if (sendCmd && sampleMode == Comm.MANUAL_SET && invalid) {
                runningState = Comm.STOPPED;
                Toast.makeText(getActivity(), "参数非法，请检查流量等参数是否为0", Toast.LENGTH_SHORT).show();
                return;
            }
            btnRun.setImageResource(R.drawable.pause);
            npEnable(false);
        }
        if (!sendCmd) return;

        if (sampleMode == Comm.MANUAL_SET)
            setManual(toDo);
        else
            setTimed(toDo);
    }
    private void updateRunningState() {
        __updateRunningState(false);
    }
    // 改变机器的运行状态, 即同步当前的变化到机器侧
    private void switchRunningState() {
        __updateRunningState(true);
    }

    private void setManual(int op) {
        int speed = npSpeed.getValue();
        final int manualMode = Comm.getIntSP(SP_MANUALMODE);
        int cap = manualMode == Comm.TIMED_SET_TIME ? npTiming.getValue() : npVolume.getValue();
        // 发送状态切换命令
        // 根据通道设置分配手动设置参数
//        String selected = spinChannel.getSelectedItem().toString();
//        Channel channel = selected.equals("全选") ? Channel.ALL : Channel.init(spinChannel.getSelectedItemPosition() + 1);
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (Channel.ALL.equals(currChannel))
                    spinChannel.setSelection(0);

                if (!verify) return;

                npSpeed.setValue(cmd.TargetSpeed);
                npVolume.setValue(cmd.TargetVolume);
                npTiming.setValue(cmd.TargetDuration);

                if (manualMode != cmd.ManualMode) {
                    Comm.setIntSP(SP_MANUALMODE, cmd.ManualMode);
                    initManualMode();
                }
            }
        }).setManualChannel(op, manualMode, currChannel, speed, cap);
        // 开始定时查询
        pollManualState();
    }
    private void setTimed(int op) {
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
//                progSampling.setProgress(cmd.Progress);
//                txtSpeed.setText(String.format(fmtSpeed, cmd.Speed));
//                txtVolume.setText(String.format(fmtVolume, cmd.Volume / 1000.0));
            }
        }).setManualChannel(op, 0, currChannel, 0, 0); // 定时模式下切换运行状态
        pollTimedState();
    }

    private void reqChannelState() {
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;

                progSampling.setProgress(cmd.Progress); // todo 也许定时模式也应该显示进度条的
                txtSpeed.setText(String.format(fmtSpeed, cmd.Speed));
                txtVolume.setText(String.format(fmtVolume, cmd.Volume / 1000.0));
                runningState = cmd.ChannelState;
                if (cmd.Progress >= 100) {
                    runningState = Comm.STOPPED;
                }
                updateRunningState();
            }
        }).reqChannelState(Channel.ALL.equals(currChannel) ? Channel.CH1 : currChannel);
    }
    // 定时模式下的采样状态轮询
    private void pollTimedState() {
        if (__progress != null)
            __progress.cancel();

        if (runningState != Comm.PLAYING)
            return;

        __progress = new Timer();
        __progress.schedule(new TimerTask() {
            @Override
            public void run() {
                reqChannelState();
            }
        }, 0, durationProgress);
    }
    // 查询手动设置下当前采样状态
    private void pollManualState() {
        if (__progress != null)
            __progress.cancel();

        if (runningState != Comm.PLAYING)
            return;

        __progress = new Timer();
        __progress.schedule(new TimerTask() {
            @Override
            public void run() {
                reqChannelState();
            }
        }, 0, durationProgress);
    }

    private void reqMachineState() {
        if (__state != null)
            __state.cancel();

        __state = new Timer();
        __state.schedule(new TimerTask() {
            @Override
            public void run() {
                final String[] State = new String[]{"停止", "等待", "正在采样", "暂停", "完成", "延时等待"};
                new Command(new Once() {
                    @Override
                    public void done(boolean verify, Command cmd) {
                        if (!verify) return;
                        List<Integer> running = new ArrayList<Integer>();
                        List<Integer> stopped = new ArrayList<Integer>();
                        List<Integer> paused = new ArrayList<Integer>();
                        String states = "";
                        for (int i = 0; i < Command.CHANNELCOUNT; i++) {
//                            cmd.MachineState[i] = (byte) (Math.round(Math.random() * 100) % 5);
                            if (cmd.MachineState[i] >= State.length || cmd.MachineState[i] < 0)
                                cmd.MachineState[i] = 0;

                            if (cmd.MachineState[i] == 2) running.add(i+1);
                            else if (cmd.MachineState[i] == 3) paused.add(i+1);
                            else stopped.add(i+1);
//                            states += String.format("通道%d%s ", i + 1, State[cmd.MachineState[i]]);
                        }
                        states = "采样：" + running.toString() + "\n";
                        states += "暂停：" + paused.toString() + "\n";
                        states += "停止：" + stopped.toString();
                        txtTips.setText(states);

                        resolveState(cmd);

                        if (sampleMode == Comm.MANUAL_SET)
                            pollManualState();
                        else
                            pollTimedState();
                    }
                }).reqMachineState();
            }
        }, 0, durationState);
    }
    private int resolveState(int index) {
        if (index == 1 || index == 2)
            return Comm.PLAYING;
        if (index == 3 || index == 5)
            return Comm.PAUSED;
        if (index == 0 || index == 4)
            return Comm.STOPPED;
        return Comm.PLAYING;
    }
    private void resolveState(Command cmd) {
        int channel = currChannel.getValue();
        if (channel >= Channel.CH1.getValue() && channel <= Channel.CH8.getValue()) {
            int state = cmd.MachineState[channel - 1];
            runningState = resolveState(state);
        } else if (channel >= Channel.C1.getValue() && channel <= Channel.C4.getValue()) {
            channel = channel - Channel.C1.getValue();
            int stateA = resolveState(cmd.MachineState[channel*2]);
            int stateB = resolveState(cmd.MachineState[channel*2 + 1]);
            if (channel % 2 == 1) {
                stateA = resolveState(cmd.MachineState[channel*2 - 1]);
                stateB = resolveState(cmd.MachineState[channel*2]);
            }
            if (stateA == Comm.PLAYING || stateB == Comm.PLAYING)
                runningState = Comm.PLAYING;
            else if (stateA == Comm.PAUSED || stateB == Comm.PAUSED)
                runningState = Comm.PAUSED;
            else
                runningState = Comm.STOPPED;

        } else if (channel >= Channel.B1.getValue() && channel <= Channel.B2.getValue()) {
            channel = channel - Channel.B1.getValue();
            runningState = Comm.STOPPED;
            int base = 4 * (channel % 2);
            int i= base;
            for (; i<base+4 ;i++) {
                if (resolveState(cmd.MachineState[i]) == Comm.PLAYING) {
                    runningState = Comm.PLAYING;
                    break;
                }
            }
            if (i == 4)
                runningState = Comm.PAUSED;
        } else if (channel == Channel.A1.getValue()) {
            runningState = Comm.STOPPED;
            int i = 0;
            for (; i<Command.CHANNELCOUNT; i++) {
                if (resolveState(cmd.MachineState[i]) == Comm.PLAYING) {
                    runningState = Comm.PLAYING;
                    break;
                }
            }
            if (i == Command.CHANNELCOUNT)
                runningState = Comm.PAUSED;
        }

//        updateRunningState();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!isSpinnerClick) {
            isSpinnerClick = true;
            return;
        }

        if (parent.equals(spinChannel)) {
            String selected = spinChannel.getSelectedItem().toString();
            if (selected.equals("全选")) {
                selected = "ALL";
                setMaxSpeed(Command.CHANNELCOUNT*UNITSPEED);
            } else {
                setMaxSpeed(0);
            }
            currChannel = Channel.valueOf(selected);
            initChannelState();

        } else if (parent.equals(spinMode)) {
            String selected = spinMode.getSelectedItem().toString();
            Intent intent = new Intent(getActivity(), ModeSettingAct.class);
            int lastMode = Comm.getIntSP(SP_SAMPLEMODE);
            // selected 选中文字对应spinMode的选项文字，在strings.xml中
            switch (selected) {
                case "手动":
                    sampleMode = Comm.MANUAL_SET;
                    runningState = Comm.STOPPED; // 强制切换状态为停止
                    updateRunningState();
                    intent = null;
                    break;
                case "定时模式":
                    sampleMode = Comm.TIMED_SET_TIME;
                    Comm.setIntSP(SP_MANUALMODE, sampleMode);
                    intent.putExtra(ModeSettingAct.KEY_MODE, ModeSettingAct.TimingSet);
                    if (Channel.ALL.equals(currChannel))
                        spinChannel.setSelection(0);
                    break;
                case "定容模式":
                    sampleMode = Comm.TIMED_SET_CAP;
                    Comm.setIntSP(SP_MANUALMODE, sampleMode);
                    intent.putExtra(ModeSettingAct.KEY_MODE, ModeSettingAct.CapacitySet);
                    if (Channel.ALL.equals(currChannel))
                        spinChannel.setSelection(0);
                    break;
            }
            Comm.setIntSP(SP_SAMPLEMODE, sampleMode);

            if (lastMode == sampleMode) // 防止切换frag的时候意外触发,比如 设置
                isSpinnerClick = false;

//            if (intent != null && isSpinnerClick) {
//                startActivityForResult(intent, 0);
//            }

            switchSampleMode();
            isSpinnerClick = true;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void switchSampleMode() {
        try {
            if (sampleMode == Comm.MANUAL_SET) {
                wrapManual.setVisibility(View.VISIBLE);
                wrapTiming.setVisibility(View.GONE);
                stopCountDown();
                pollManualState();
                initManualMode();
            } else {
                wrapManual.setVisibility(View.GONE);
                wrapTiming.setVisibility(View.VISIBLE);
                pollTimedState();
                initTimedMode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            updateRunningState();
        }
    }

    private void setMaxSpeed(int maxSpeed) {
        if (maxSpeed == 0)
            npSpeed.setMax(MaxSpeed);
        else
            npSpeed.setMax(maxSpeed);
        if (npSpeed.getValue() > npSpeed.getMax())
            npSpeed.setValue(npSpeed.getValue());
        npTiming.setMax(1000);
        npVolume.setMax(npSpeed.getMax() * npTiming.getMax());
    }

    /**
     * 手动模式下的相关设置，切换模式为"设定时长"或"设定容量"， 以及最大流量/时长/容量等限制
     */
    private void initManualMode() {
        int targetSpeed = npSpeed.getValue();
        int targetDuration = npTiming.getValue();
        int targetVolume = npVolume.getValue();

        if (targetSpeed == 0)
            targetSpeed = npSpeed.getDefault();
        if (targetDuration == 0)
            targetDuration = npTiming.getDefault();
        if (targetVolume == 0)
            targetVolume = npVolume.getDefault();

        npSpeed.setValue(targetSpeed);

        int manualMode = Comm.getIntSP(SP_MANUALMODE);
        if (manualMode == 0) {
            manualMode = Comm.TIMED_SET_CAP;
            Comm.setIntSP(SP_MANUALMODE, manualMode);
        }

        if (manualMode == Comm.TIMED_SET_CAP) {
            if (lastManualMode == Comm.TIMED_SET_TIME)
                targetVolume = targetSpeed * targetDuration;
            layoutCap.setVisibility(View.VISIBLE);
            layoutTiming.setVisibility(View.GONE);
            npVolume.setValue(targetVolume);
            npVolume.setDelta(100);
        } else if (manualMode == Comm.TIMED_SET_TIME) {
            if (lastManualMode == Comm.TIMED_SET_CAP)
                targetDuration = targetVolume / targetSpeed;
            layoutCap.setVisibility(View.GONE);
            layoutTiming.setVisibility(View.VISIBLE);
            npTiming.setValue(targetDuration);
            npTiming.setDelta(1);
        }
    }
    private void npEnable(boolean enable) {
        npSpeed.setEnabled(enable);
        npVolume.setEnabled(enable);
        npTiming.setEnabled(enable);
    }

    private void initTimedMode() {
        // 查询定时设置，并开始倒计时
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;

                for (SettingItem item:cmd.SettingItems) {
                    int i = item.id - 1;
                    timedLaunchAt[i] = item.date + " " + item.time;
                    timedDuration[i] = item.targetDuration;
                    timedVolume[i] = item.targetVol;
                    timedSpeed[i] = item.targetSpeed;
                    timedSet[i] = item.isSet;
                }
//                timedLaunchAt[0] = "2015-06-26 22:38";
//                timedLaunchAt[1] = "2015-06-26 22:39";
                startCountDown();
            }
        }).reqTimedSetting(sampleMode, currChannel);
    }
    private void updateTimedStatus() {
        String str = "";
        boolean isSetCap = sampleMode == Comm.TIMED_SET_CAP;
        String strMode = isSetCap ? "容量" : "时长";
        String strUnit = isSetCap ? "mL" : "min";
        String strFmt = "第%d组 %s启动 " + strMode + "：%d" + strUnit + " 流量：%dmL/min\n";

        if (waitingGroup < 0) {
            txtTimedSetting.setText("\n");
            return;
        }

        for (int i=waitingGroup, len = timedDuration.length; i<len; i++) {
            if (timedSet[i])
                str += String.format(strFmt, (i+1), timedLaunchAt[i], isSetCap ? timedVolume[i]: timedDuration[i], timedSpeed[i]);
        }
        txtTimedSetting.setText(str);
    }

    public interface OnMainFragListener {
        void onLockToggled(boolean locked);
        void onButtonClick(int id);
    }

    class ClickHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AmaeClickDetector.MSG_PRESSED_3:
                    Toast.makeText(parent, "关机提醒", Toast.LENGTH_SHORT).show();
                    // 发送当前时间
                    break;
                case AmaeClickDetector.MSG_CLICK:
                    if (runningState == Comm.PLAYING) {
                        runningState = Comm.PAUSED;
                    } else {
                        runningState = Comm.PLAYING;
                    }
                    switchRunningState();
                    break;
                case AmaeClickDetector.MSG_DBLCLICK:
                    // 单独一个停止按钮 btn_stop
                    break;
            }
        }
    }
}

package hk.amae.sampler;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import hk.amae.frag.MainFrag;
import hk.amae.util.ActivityGestureDetector;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.util.SwipeInterface;
import hk.amae.util.Comm.Channel;
import hk.amae.widget.SettingArrayAdapter;
import hk.amae.widget.SettingItem;

public class ModeSettingAct extends Activity implements View.OnClickListener, SwipeInterface {
    public static String CapacitySet = "定容设置";
    public static String TimingSet = "定时设置";
    public static final String KEY_MODE = "mode";
    public static final int GROUPCOUNT = 8;

    static String FMT_CHANNEL = "%s通道";

    private String strSampleMode = CapacitySet; // 定时设置
    int sampleMode = Comm.TIMED_SET_CAP;
    ListView listView;
    TextView labelChannel;
    int channel;

    String[] Channels;
    ArrayList<SettingItem> dataList;
    SettingArrayAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_mode_setting);

        Intent intent = getIntent();
        strSampleMode = intent.getStringExtra(KEY_MODE);
        if (strSampleMode == null)
            strSampleMode = TimingSet;
        if (TimingSet.equals(strSampleMode))
            sampleMode = Comm.TIMED_SET_TIME;

        Channels = getChannels();

        TextView labelModel = (TextView) findViewById(R.id.label_model);
        labelModel.setText(strSampleMode);

        labelChannel = (TextView) findViewById(R.id.label_channel);
        labelChannel.setText(String.format(FMT_CHANNEL, Channels[channel]));

        ImageButton btnPre = (ImageButton) findViewById(R.id.btn_prev);
        ImageButton btnNext = (ImageButton) findViewById(R.id.btn_next);
        if (Channels.length == 1) {
            btnPre.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.INVISIBLE);
        } else {
            btnPre.setOnClickListener(this);
            btnNext.setOnClickListener(this);
        }

        listView = (ListView) findViewById(R.id.list_settings);

        dataList = new ArrayList<>(GROUPCOUNT);
        for (int i=0, len = GROUPCOUNT; i<len; i++)
            dataList.add(new SettingItem(i+1));
        listAdapter = new SettingArrayAdapter(this, R.layout.mode_setting_item, dataList);
        listView.setAdapter(listAdapter);

        ActivityGestureDetector gestureDetector = new ActivityGestureDetector(this, this);
        listView.setOnTouchListener(gestureDetector);

        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);

        fetchSetting();
    }

    private String[] getChannels() {
        int mode = Comm.getIntSP(ChannelAct.SP_CHANNELMODE);

        int res;
        switch (mode) {
            case ChannelAct.MODE_COUPLE:
                res = R.array.channels_C;
                break;
            case ChannelAct.MODE_4IN1:
                res = R.array.channels_B;
                break;
            case ChannelAct.MODE_8IN1:
                res = R.array.channels_A;
                break;
            default:
                res = R.array.channels_CH;
        }
        String[] src = getResources().getStringArray(res);
        String[] dest = new String[mode != ChannelAct.MODE_8IN1 ? src.length - 1 : 1];
        System.arraycopy(src, 0, dest, 0, dest.length);
        return dest;
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
            case R.id.btn_save: // channel one by one to save
                if (checkValid())
                    saveSetting();
                break;
            case R.id.btn_cancel:
                super.onBackPressed();
                break;
        }

    }

    boolean checkValid() {
        int cntEmpt = 0;
        for (int i=0; i<dataList.size(); i++) {
            SettingItem item = dataList.get(i);
            if (!item.isSet) {
                cntEmpt++;
                continue;
            }

            if (item.targetSpeed == 0) {
                Toast.makeText(this, String.format("第%d组流量不能为空", i+1), Toast.LENGTH_LONG).show();
                return false;
            }
            if (item.targetSpeed > MainFrag.MaxSpeed) {
                Toast.makeText(this, String.format("第%d组流量超出限制", i+1), Toast.LENGTH_LONG).show();
                return false;
            }

            if (item.isSetCap && item.targetVol == 0) {
                Toast.makeText(this, String.format("第%d组容量不能为空", i+1), Toast.LENGTH_LONG).show();
                return false;
            }
            if (!item.isSetCap && item.targetDuration == 0) {
                Toast.makeText(this, String.format("第%d组容量不能为空", i+1), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        if (cntEmpt == dataList.size()) {
            Toast.makeText(this, "还没有勾选任何设置", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            for (int i=0; i<dataList.size(); i++) {
                SettingItem itemA = dataList.get(i);
                if (!itemA.isSet) continue;

                Date startA = (new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)).parse(itemA.date + " " + itemA.time);
                long durationA = (itemA.isSetCap ? itemA.targetVol / itemA.targetSpeed : itemA.targetDuration) * 60 * 1000;
                Date endA = new Date(startA.getTime());
                endA.setTime(startA.getTime() + durationA);
                for (int j=i+1; j<dataList.size(); j++) {
                    SettingItem itemB = dataList.get(j);
                    if (!itemB.isSet) continue;

                    Date startB = (new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)).parse(itemB.date + " " + itemB.time);
                    long durationB = (itemB.isSetCap ? itemB.targetVol / itemB.targetSpeed : itemB.targetDuration) * 60 * 1000;
                    Date endB = new Date(startB.getTime());
                    endB.setTime(startB.getTime() + durationB);
                    if (endA.getTime() < startB.getTime()
                            || startA.getTime() > endB.getTime()) {
                        continue;
                    } else {
                        Toast.makeText(this, String.format("时间设置冲突 第%d组 和 第%d组", i+1, j+1), Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    void saveSetting() {
        SettingItem[] items = new SettingItem[8];
        dataList.toArray(items);
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify)
                    return;
                Toast.makeText(ModeSettingAct.this, "定时设置已经保存", Toast.LENGTH_SHORT).show();
                updateSetting(cmd);
            }
        }).setTimedChannel(true, sampleMode, Channel.valueOf(Channels[channel]), items);
    }

    boolean switchChannel(boolean add) {
        if ((add && channel >= Channels.length - 1) || (!add && channel <=0 ))
            return false;
        channel = add ? channel+1 : channel-1;
        labelChannel.setText(String.format(FMT_CHANNEL, Channels[channel]));

        fetchSetting();

        return true;
    }
    private void fetchSetting() {
        int timedMode = strSampleMode.equals(CapacitySet) ? Comm.TIMED_SET_CAP : Comm.TIMED_SET_TIME;

        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;
                updateSetting(cmd);
            }
        }).reqTimedSetting(timedMode, Channel.valueOf(Channels[channel]));
    }

    private void updateSetting(Command cmd) {
        for (int i=0, len = dataList.size(); i<len; i++) {
//                    SettingItem item = new SettingItem(i+1, (int) (Math.random()*10000), (int) (Math.random()*1000), Math.random() < 0.5, strSampleMode.equals(CapacitySet));
            if (sampleMode == Comm.TIMED_SET_CAP)
                cmd.SettingItems[i].isSetCap = true;
            else
                cmd.SettingItems[i].isSetCap = false;
            dataList.set(i, cmd.SettingItems[i]);
        }
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        Comm.setIntSP(MainFrag.SP_SAMPLEMODE, sampleMode);
    }

    void flip(boolean add) {
        if (!switchChannel(add))
            return;

        int duration = 200;
        ObjectAnimator transA, transB;
        AnimatorSet set = new AnimatorSet();
        if (add) {
            transA = ObjectAnimator.ofFloat(listView, "translationX", 0, -listView.getMeasuredWidth());

            transB = ObjectAnimator.ofFloat(listView, "translationX", listView.getMeasuredWidth(), 0);
        } else {
            transA = ObjectAnimator.ofFloat(listView, "translationX", 0, listView.getMeasuredWidth());

            transB = ObjectAnimator.ofFloat(listView, "translationX", -listView.getMeasuredWidth(), 0);
        }

        transA.setDuration(duration);
        transB.setDuration(duration);
        set.play(transA).before(transB);
        set.start();
    }

    @Override
    public void onLeftWipe(View v) {
        Comm.logI("LeftWipe");
//        flip(false);
    }

    @Override
    public void onRightWipe(View v) {
        Comm.logI("RightWipe");
//        flip(true);
    }
}

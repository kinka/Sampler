package hk.amae.sampler;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import hk.amae.util.ActivityGestureDetector;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.util.SwipeInterface;
import hk.amae.widget.AmaeDateTimePicker;
import hk.amae.util.Comm.Channel;

public class ModeSettingAct extends Activity implements View.OnClickListener, SwipeInterface {
    public static String CapacitySet = "定容设置";
    public static String TimingSet = "定时设置";
    public static final int GROUPCOUNT = 8;

    static String FMT_CHANNEL = "%s通道";

    private String model = CapacitySet; // 定时设置
    ListView listView;
    TextView labelChannel;
    int channel;

    String[] Channels;
    ArrayList<SettingItem> dataList;
    SettingArrayAdapter listAdapter;
 // todo 时间冲突检查
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_mode_setting);

        Intent intent = getIntent();
        model = intent.getStringExtra("model");
        if (model == null)
            model = TimingSet;

        Channels = getChannels();

        TextView labelModel = (TextView) findViewById(R.id.label_model);
        labelModel.setText(model);

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
        String s_mode = Comm.getSP(ChannelAct.SP_CHANNELMODE);
        int mode = 0;
        if (s_mode.length() == 0)
            mode = ChannelAct.MODE_SINGLE;
        else
            mode = Integer.valueOf(s_mode);

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
                for (int i=0; i<dataList.size(); i++)
                    Comm.logI(" " + dataList.get(i));
                break;
            case R.id.btn_cancel:
                super.onBackPressed();
                break;
        }

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
        int timedMode = model.equals(CapacitySet) ? Comm.TIMED_SET_CAP : Comm.TIMED_SET_TIME;

        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                for (int i=0, len = dataList.size(); i<len; i++) {
//                    SettingItem item = new SettingItem(i+1, (int) (Math.random()*10000), (int) (Math.random()*1000), Math.random() < 0.5, model.equals(CapacitySet));
                    dataList.set(i, cmd.SettingItems[i]);
                }
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        }).reqTimedSetting(timedMode, Channel.valueOf(Channels[channel]));
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

    public class SettingArrayAdapter extends ArrayAdapter<SettingItem> implements View.OnClickListener {
        private final Context context;
        private final ArrayList<SettingItem> values;
        private final int rowLayout;
        private ViewGroup myList;

        public SettingArrayAdapter(Context context, int resource, ArrayList<SettingItem> values) {
            super(context, resource, values);
            this.context = context;
            this.values = values;
            this.rowLayout = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            myList = parent;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SettingItem item = values.get(position);

            View rowView;
            if (convertView == null) {
                rowView = inflater.inflate(rowLayout, parent, false);
            } else {
                rowView = convertView;
            }
            TextView rowId = (TextView) rowView.findViewById(R.id.txt_rowid);
            CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
            final EditText total = (EditText) rowView.findViewById(R.id.txt_total);
            final EditText speed = (EditText) rowView.findViewById(R.id.txt_speed);
            TextView labelTotal = (TextView) rowView.findViewById(R.id.label_total);
            TextView labelUnit = (TextView) rowView.findViewById(R.id.label_total_unit);
            if (item.isSetCap) {
                labelTotal.setText("容量");
                labelUnit.setText("mL");
            } else {
                labelTotal.setText("时长");
                labelUnit.setText("min");
            }

            final TextView datepicker = (TextView) rowView.findViewById(R.id.txt_datepicker);
            final TextView timepicker = (TextView) rowView.findViewById(R.id.txt_timepicker);
            datepicker.setOnClickListener(this);
            timepicker.setOnClickListener(this);

            datepicker.setText(item.date);
            timepicker.setText(item.time);

            rowId.setText(String.valueOf(item.id));
            checkBox.setChecked(item.isSet);
            total.setText(String.valueOf(item.targetVol));
            speed.setText(String.valueOf(item.targetSpeed));

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    item.isSet = checked;
                }
            });

            if (convertView == null) {
                total.addTextChangedListener(new TextWatcher(rowView, total.getId(), position));
                speed.addTextChangedListener(new TextWatcher(rowView, speed.getId(), position));
            }

            return rowView;
        }

        int getRowId(View view) {
            TextView rowId = (TextView) view.findViewById(R.id.txt_rowid);

            return Integer.valueOf(rowId.getText().toString());
        }

        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.txt_datepicker:
                    AmaeDateTimePicker.showDateDialog(context, (TextView) view, "%d-%02d-%02d", "--", new AmaeDateTimePicker.Picker() {
                        @Override
                        public void onPick(String value) {

                            Comm.logI("pos " + getRowId((GridLayout) view.getParent()));
                        }
                    });
                    break;

                case R.id.txt_timepicker:
                    AmaeDateTimePicker.showTimeDialog(context, (TextView) view, "%02d:%02d");
                    break;
            }
        }

        class TextWatcher implements android.text.TextWatcher {
            int resId;
            int pos;
            View row;

            public TextWatcher(View row, int resId, int rowId) {
                this.row = row;
                this.pos = rowId;
                this.resId = resId;
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    SettingItem item = values.get(pos);
                    switch (resId) {
                        case R.id.txt_total:
                            if (editable.length() > 0 && getRowId(row) == pos + 1) {
                                item.targetVol = item.targetDuration = Integer.valueOf(editable.toString());
                                Comm.logI("total " + item.targetVol);
                            }
                            break;
                        case R.id.txt_speed:
                            if (editable.length() > 0 && getRowId(row) == pos + 1) {
                                item.targetSpeed = Integer.valueOf(editable.toString());
                            }
                            break;
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    public static class SettingItem {
        public int id = 0;
        public int targetVol;
        public int targetDuration;
        public int targetSpeed;
        public boolean isSet;
        public boolean isSetCap; // 容量设置 或者 定时设置
        public String date = "2015-06-02";
        public String time = "00:00";

        @Override
        public String toString() {
            return String.format("[%d]%b (V: %d D: %d S: %d) %s %s", id, isSet, targetVol, targetDuration, targetSpeed, date, time);
        }

        public SettingItem(int id) {
            this.id = id;
        }
    }
}

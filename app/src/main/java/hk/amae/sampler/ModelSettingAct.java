package hk.amae.sampler;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

import hk.amae.util.ActivityGestureDetector;
import hk.amae.util.Comm;
import hk.amae.util.SwipeInterface;


public class ModelSettingAct extends Activity implements View.OnClickListener, SwipeInterface {
    public static String CapacitySet = "定容设置";
    public static String TimingSet = "定时设置";
    static String FMT_CHANNEL = "第%d通道";

    private String model = CapacitySet; // 定时设置
    ListView listView;
    TextView labelChannel;
    int channel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_model_setting);

        Intent intent = getIntent();
        model = intent.getStringExtra("model");
        if (model == null)
            model = TimingSet;

        TextView labelModel = (TextView) findViewById(R.id.label_model);
        labelModel.setText(model);

        labelChannel = (TextView) findViewById(R.id.label_channel);
        labelChannel.setText(String.format(FMT_CHANNEL, channel));

        findViewById(R.id.btn_pre).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list_settings);
        final ArrayList<SettingItem> list = new ArrayList<>();
        for (int i=0; i<8; i++)
            list.add(new SettingItem(i+1, (int) (Math.random()*10000), (int) (Math.random()*1000), Math.random() < 0.5, model.equals(CapacitySet)));

        final ListAdapter adapter = new SettingArrayAdapter(this, R.layout.model_setting_item, list);
        listView.setAdapter(adapter);

        ActivityGestureDetector gestureDetector = new ActivityGestureDetector(this, this);
        listView.setOnTouchListener(gestureDetector);
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

        public SettingArrayAdapter(Context context, int resource, ArrayList<SettingItem> values) {
            super(context, resource, values);
            this.context = context;
            this.values = values;
            this.rowLayout = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            SettingItem item = values.get(position);

            View rowView;
            if (convertView == null) {
                rowView = inflater.inflate(rowLayout, parent, false);
            } else {
                rowView = convertView;
            }
            TextView rowId = (TextView) rowView.findViewById(R.id.txt_rowid);
            CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
            EditText total = (EditText) rowView.findViewById(R.id.txt_total);
            EditText speed = (EditText) rowView.findViewById(R.id.txt_speed);
            TextView labelTotal = (TextView) rowView.findViewById(R.id.label_total);
            TextView labelUnit = (TextView) rowView.findViewById(R.id.label_total_unit);
            if (item.isCapacitySet) {
                labelTotal.setText("容量");
                labelUnit.setText("L");
            } else {
                labelTotal.setText("时长");
                labelUnit.setText("min");
            }

            rowView.findViewById(R.id.txt_datepicker).setOnClickListener(this);
            rowView.findViewById(R.id.txt_timepicker).setOnClickListener(this);

            rowId.setText(String.valueOf(item.rowid));
            checkBox.setChecked(item.checked);
            total.setText(String.valueOf(item.total/1000.0));
            speed.setText(String.valueOf(item.speed));

            return rowView;
        }

        @Override
        public void onClick(final View view) {
            Calendar calendar = Calendar.getInstance();
            switch (view.getId()) {
                case R.id.txt_datepicker:
                    new DatePickerDialog(context,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                                    ((TextView) view).setText(String.format("%d-%02d-%02d", y, m+1, d));
                                }
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                    break;

                case R.id.txt_timepicker:
                    new TimePickerDialog(context,
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int h, int m) {
                                    ((TextView) view).setText(String.format("%02d:%02d", h, m));
                                }
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                    break;
            }
        }
    }

    public class SettingItem {
        int rowid = 0;
        int total;
        int speed;
        boolean checked;
        boolean isCapacitySet; // 容量设置 或者 定时设置
        int year;
        int month;
        int dayOfMonth;

        public SettingItem(int id, int total, int speed, boolean checked, boolean isCapacitySet) {
            this.rowid = id;
            this.total = total;
            this.speed = speed;
            this.checked = checked;
            this.isCapacitySet = isCapacitySet;
        }
    }
}

package hk.amae.sampler;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
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

    private String model = CapacitySet; // 定时设置
    ListView listView;

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
        findViewById(R.id.btn_pre).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list_settings);
        final ArrayList<SettingItem> list = new ArrayList<>();
        for (int i=0; i<8; i++)
            list.add(new SettingItem(i+1, (int) (Math.random()*10000), (int) (Math.random()*1000), Math.random() < 0.5));

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

                break;
            case R.id.btn_next:

                break;
        }
    }

    @Override
    public void onLeftWipe(View v) {
        Comm.logI("LeftWipe");
    }

    @Override
    public void onRightWipe(View v) {
        Comm.logI("RightWipe");
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
            View rowView = inflater.inflate(rowLayout, parent, false);
            TextView rowId = (TextView) rowView.findViewById(R.id.txt_rowid);
            CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
            EditText total = (EditText) rowView.findViewById(R.id.txt_total);
            EditText speed = (EditText) rowView.findViewById(R.id.txt_speed);
            rowView.findViewById(R.id.txt_datepicker).setOnClickListener(this);
            rowView.findViewById(R.id.txt_timepicker).setOnClickListener(this);

            SettingItem item = values.get(position);
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
        int year;
        int month;
        int dayOfMonth;

        public SettingItem(int id, int total, int speed, boolean checked) {
            this.rowid = id;
            this.total = total;
            this.speed = speed;
            this.checked = checked;
        }
    }
}

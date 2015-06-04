package hk.amae.widget;

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;

import hk.amae.sampler.R;

/**
 * Created by kinka on 6/4/15.
 */
public class SettingArrayAdapter extends ArrayAdapter<SettingItem> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
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

        checkBox.setOnCheckedChangeListener(this);

        if (convertView == null) {
            total.addTextChangedListener(new TextWatcher(rowView, total.getId(), position));
            speed.addTextChangedListener(new TextWatcher(rowView, speed.getId(), position));
        }

        return rowView;
    }

    int getPos(View view) {
        TextView rowId = (TextView) view.findViewById(R.id.txt_rowid);

        return Integer.valueOf(rowId.getText().toString()) - 1;
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.txt_datepicker:
                AmaeDateTimePicker.showDateDialog(context, (TextView) view, "%d-%02d-%02d", "--", new AmaeDateTimePicker.Picker() {
                    @Override
                    public void onPick(String value) {
                        int pos = getPos((GridLayout) view.getParent());
                        SettingItem item = values.get(pos);
                        item.date = value;
                    }
                });
                break;

            case R.id.txt_timepicker:
                AmaeDateTimePicker.showTimeDialog(context, (TextView) view, "%02d:%02d", new AmaeDateTimePicker.Picker() {
                    @Override
                    public void onPick(String value) {
                        int pos = getPos((GridLayout) view.getParent());
                        SettingItem item = values.get(pos);
                        item.time = value;
                    }
                });
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        int pos = getPos((GridLayout) compoundButton.getParent());
        SettingItem item = values.get(pos);
        item.isSet = checked;
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
                        if (editable.length() > 0 && getPos(row) == pos) {
                            item.targetVol = item.targetDuration = Integer.valueOf(editable.toString());
                        }
                        break;
                    case R.id.txt_speed:
                        if (editable.length() > 0 && getPos(row) == pos) {
                            item.targetSpeed = Integer.valueOf(editable.toString());
                        }
                        break;
                }
            } catch (Exception e) {

            }
        }
    }
}
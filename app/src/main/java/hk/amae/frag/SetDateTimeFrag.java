package hk.amae.frag;


import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.widget.AmaeDateTimePicker;

public class SetDateTimeFrag extends Fragment implements View.OnClickListener {
    TextView txtDate;
    TextView txtTime;

    public SetDateTimeFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_time, container, false);
        txtDate = (TextView) v.findViewById(R.id.txt_date);
        txtTime = (TextView) v.findViewById(R.id.txt_time);

        txtDate.setOnClickListener(this);
        txtTime.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.txt_date:
                AmaeDateTimePicker.showDateDialog(getActivity(), (TextView) view, "%d 年 %02d 月 %02d 日", null);
                break;
            case R.id.txt_time:
                AmaeDateTimePicker.showTimeDialog(getActivity(), (TextView) view, "%02d : %02d : %02d");
                break;
        }
    }
}

package hk.amae.frag;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hk.amae.sampler.R;
import hk.amae.widget.AmaeDateTimePicker;

public class SetPowerOffFrag extends Fragment implements View.OnClickListener {
    TextView txtDate;
    TextView txtTime;

    public SetPowerOffFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_poweroff, container, false);
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

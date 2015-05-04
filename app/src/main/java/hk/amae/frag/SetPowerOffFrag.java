package hk.amae.frag;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.util.Command;
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

        v.findViewById(R.id.btn_confirm).setOnClickListener(this);
        v.findViewById(R.id.btn_back).setOnClickListener(this);

        String shutdownTime = Comm.getSP("shutdown_time");
        if (shutdownTime.length() > 0) {
            String[] res = Comm.getLocalDateTime(shutdownTime);
            txtDate.setText(res[0]);
            txtTime.setText(res[1]);
        } else {
            txtDate.setText(Comm.getCurrentDate());
            txtTime.setText(Comm.getCurrentTime());
        }

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
            case R.id.btn_confirm:
                final String shutdownTime = Comm.getServerDateTime(txtDate.getText().toString(), txtTime.getText().toString());
                new Command(new Command.Once() {
                    @Override
                    public void done(boolean verify, Command cmd) {
                        Comm.setSP("shutdown_time", shutdownTime);
                    }
                }).setShutdown(shutdownTime);
                break;
            case R.id.btn_back:
                getActivity().onBackPressed();
                break;
        }
    }
}

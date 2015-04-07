package hk.amae.frag;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.widget.ActionSheet;

public class HardwareFrag extends Fragment {
    View.OnClickListener listener = null;

    public HardwareFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_hardware, container, false);
        Comm.logI("listener2...");
        v.findViewById(R.id.btn_backlit).setOnClickListener(listener);
        v.findViewById(R.id.btn_time).setOnClickListener(listener);
        v.findViewById(R.id.btn_restore).setOnClickListener(listener);
        v.findViewById(R.id.btn_clear).setOnClickListener(listener);
        v.findViewById(R.id.btn_poweroff).setOnClickListener(listener);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (View.OnClickListener) activity;
        Comm.logI("listener1...");
    }
}

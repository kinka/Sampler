package hk.amae.frag;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hk.amae.sampler.R;
import hk.amae.widget.AmaeDateTimePicker;

public class RestoreFrag extends Fragment {

    public RestoreFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_restore, container, false);

        return v;
    }
}

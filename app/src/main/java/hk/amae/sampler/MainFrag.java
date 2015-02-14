package hk.amae.sampler;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import hk.amae.widget.TextProgressBar;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFrag extends Fragment {
    Spinner spinChannel;
    TextProgressBar progSampling;
    Activity parent;

    public MainFrag() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_main, container, false);
        spinChannel = (Spinner) v.findViewById(R.id.spin_channel);
        ArrayAdapter<CharSequence> spinAdapter =
                ArrayAdapter.createFromResource(parent, R.array.channels_array, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinChannel.setAdapter(spinAdapter);

        progSampling = (TextProgressBar) v.findViewById(R.id.prog_sampling);
//        progSampling.setText("Loading 50%...");
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parent = activity;

    }


}

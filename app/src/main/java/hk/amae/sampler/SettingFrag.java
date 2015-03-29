package hk.amae.sampler;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hk.amae.util.Comm;
import hk.amae.widget.ActionSheet;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFrag extends Fragment implements View.OnClickListener, ActionSheet.OnASItemClickListener {

    ActionSheet as;
    int as_owner;

    public SettingFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fraq_settings, container, false);
        v.findViewById(R.id.btn_model).setOnClickListener(this);
        v.findViewById(R.id.btn_database).setOnClickListener(this);
        v.findViewById(R.id.btn_hardware).setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_model:
                as_owner = R.id.btn_model;
                as = new ActionSheet(getActivity());
                as.setOnASItemClickListener(this);
                as.addItems(ModelSettingAct.CapacitySet, ModelSettingAct.TimingSet);
                as.showMenu();

                break;
            case R.id.btn_database:
                startActivity(new Intent(getActivity(), HistoryAct.class));
                break;

            case R.id.btn_hardware:
                startActivity(new Intent(getActivity(), HardwareAct.class));
                break;
        }
    }

    @Override
    public void onASItemClick(int position) {
        Comm.logI("position " + position);
        if (as_owner == R.id.btn_model) {
            Intent intent = new Intent(getActivity(), ModelSettingAct.class);
            intent.putExtra("model", position == 0 ? ModelSettingAct.CapacitySet : ModelSettingAct.TimingSet);
            startActivity(intent);
        }
    }
}

package hk.amae.sampler;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFrag extends Fragment implements View.OnClickListener {


    public SettingFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fraq_settings, container, false);
        v.findViewById(R.id.btn_model).setOnClickListener(this);
        return v;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_model:
                Intent intent = new Intent(getActivity(), ModelSettingAct.class);
                startActivity(intent);
                break;
        }
    }
}

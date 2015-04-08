package hk.amae.sampler;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import hk.amae.frag.BacklitFrag;
import hk.amae.frag.ClearSampleFrag;
import hk.amae.frag.HardwareFrag;
import hk.amae.frag.RestoreFrag;
import hk.amae.frag.SetDateTimeFrag;
import hk.amae.frag.SetPowerOffFrag;


public class ChannelAct extends Activity {
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_channel);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        final RadioButton p2 = (RadioButton) findViewById(R.id.radio_p2);
        final RadioButton p4 = (RadioButton) findViewById(R.id.radio_p4);
        final RadioButton p8 = (RadioButton) findViewById(R.id.radio_p8);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                switch (checkedId) {
                    case R.id.radio_p2:
                        break;
                    case R.id.radio_p4:
                        break;
                    case R.id.radio_p8:

                        break;
                }
            }
        });
    }

}

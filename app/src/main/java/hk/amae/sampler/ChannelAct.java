package hk.amae.sampler;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
    public static int MODE_8IN1 = 3;
    public static int MODE_4IN1 = 2;
    public static int MODE_COUPLE = 1;
    public static int MODE_SINGLE = 0;
    public static int ChannelMode = MODE_SINGLE;

    private RadioGroup radioGroup;
    private CheckBox enableCombine;
    private ImageView imgChannelMode;

    private int lastMode = MODE_COUPLE;
    private int[] mode_res = {
            R.drawable.channel_ch,
            R.drawable.channel_c,
            R.drawable.channel_b,
            R.drawable.channel
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_channel);

        imgChannelMode = (ImageView) findViewById(R.id.image_channel_mode);

        enableCombine = (CheckBox) findViewById(R.id.chk_enable);
        enableCombine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    ChannelMode = lastMode;
                else
                    ChannelMode = MODE_SINGLE;

                onModeChange();
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                switch (checkedId) {
                    case R.id.radio_p2:
                        ChannelMode = MODE_COUPLE;
                        break;
                    case R.id.radio_p4:
                        ChannelMode = MODE_4IN1;
                        break;
                    case R.id.radio_p8:
                        ChannelMode = MODE_8IN1;
                        break;
                }
                lastMode = ChannelMode;
                enableCombine.setChecked(true);

                onModeChange();
            }
        });
    }

    void onModeChange() {
        imgChannelMode.setImageResource(mode_res[ChannelMode]);
    }
}

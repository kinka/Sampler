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
import android.widget.Toast;

import hk.amae.frag.BacklitFrag;
import hk.amae.frag.ClearSampleFrag;
import hk.amae.frag.HardwareFrag;
import hk.amae.frag.RestoreFrag;
import hk.amae.frag.SetDateTimeFrag;
import hk.amae.frag.SetPowerOffFrag;
import hk.amae.util.Comm;
import hk.amae.util.Command;


public class ChannelAct extends Activity implements View.OnClickListener {
    public final static int MODE_8IN1 = 4;
    public final static int MODE_4IN1 = 3;
    public final static int MODE_COUPLE = 2;
    public final static int MODE_SINGLE = 1;
    public static int ChannelMode = MODE_SINGLE;
    public static final String SP_CHANNELMODE = "channel_mode";

    private RadioGroup radioGroup;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                new Command(new Command.Once() {
                    @Override
                    public void done(boolean verify, Command cmd) {
                        Comm.setIntSP(SP_CHANNELMODE, ChannelMode);
                        Toast.makeText(ChannelAct.this, "通道设置已经保存", Toast.LENGTH_SHORT).show();
                        ChannelAct.super.onBackPressed();
                    }
                }).setChannelMode(ChannelMode);
                break;
            case R.id.btn_back:
                super.onBackPressed();
                break;
        }
    }

    private CheckBox enableCombine;
    private ImageView imgChannelMode;
    private RadioButton radio_p2;
    private RadioButton radio_p4;
    private RadioButton radio_p8;

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

        ChannelMode = Comm.getIntSP(SP_CHANNELMODE);
        if (ChannelMode == 0 || ChannelMode > MODE_8IN1)
            ChannelMode = MODE_SINGLE;

        enableCombine.setChecked(ChannelMode != MODE_SINGLE);

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

        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);

        radio_p2 = (RadioButton) findViewById(R.id.radio_p2);
        radio_p4 = (RadioButton) findViewById(R.id.radio_p4);
        radio_p8 = (RadioButton) findViewById(R.id.radio_p8);
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

        onModeChange();
    }

    void onModeChange() {
        imgChannelMode.setImageResource(mode_res[ChannelMode-1]);
        radio_p2.setEnabled(enableCombine.isChecked());
        radio_p4.setEnabled(enableCombine.isChecked());
        radio_p8.setEnabled(enableCombine.isChecked());
        switch (ChannelMode) {
            case MODE_COUPLE:
                radio_p2.setChecked(true);
                break;
            case MODE_4IN1:
                radio_p4.setChecked(true);
                break;
            case MODE_8IN1:
                radio_p8.setChecked(true);
                break;
        }
    }
}

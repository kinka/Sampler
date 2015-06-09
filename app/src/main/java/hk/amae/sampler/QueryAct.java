package hk.amae.sampler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import hk.amae.frag.BasicInfoFrag;
import hk.amae.util.Comm;
import hk.amae.util.Command;


public class QueryAct extends Activity {
    private TextView labelSpeed, labelTargetSpeed, labelVolume, labelStandardVol,
        labelTime, labelATM, labelTEMP, labelProgress, labelElapse, labelDuration,
        labelSampleMode, labelLaunchMode, labelChannel, labelGroup;

    public static final String KEY_ITEM = "itemID";
    String itemID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_query);

        Intent intent = getIntent();
        itemID = intent.getStringExtra(KEY_ITEM);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QueryAct.this.onBackPressed();
            }
        });

        labelSpeed = (TextView) findViewById(R.id.label_speed);
        labelTargetSpeed = (TextView) findViewById(R.id.label_target_speed);
        labelVolume = (TextView) findViewById(R.id.label_total);
        labelStandardVol = (TextView) findViewById(R.id.label_total_standard);
        labelTime = (TextView) findViewById(R.id.label_local_time);
        labelATM = (TextView) findViewById(R.id.label_atm);
        labelTEMP = (TextView) findViewById(R.id.label_temp);
        labelProgress = (TextView) findViewById(R.id.label_sampled_prog);
        labelElapse = (TextView) findViewById(R.id.label_sampled_time);
        labelDuration = (TextView) findViewById(R.id.label_duration);
        labelSampleMode = (TextView) findViewById(R.id.label_sample_mode);
        labelLaunchMode = (TextView) findViewById(R.id.label_launch_mode);
        labelChannel = (TextView) findViewById(R.id.label_channel);
        labelGroup = (TextView) findViewById(R.id.label_set_group);

        doQuery();
    }

    private void doQuery() {
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
//                cmd.TargetSpeed = 200;
                cmd.DateTime = "2015-06-01 23:33";
                labelSpeed.setText(String.format("%dmL/min", cmd.Speed));
                labelTargetSpeed.setText(String.format("%dmL/min", cmd.TargetSpeed));
                labelVolume.setText(String.format("%dmL", cmd.Volume));
                labelStandardVol.setText(String.format("%dmL", cmd.StandardVol));

                labelTime.setText(cmd.DateTime);
                labelATM.setText(String.format(BasicInfoFrag.atmFormat, cmd.ATM));
                labelTEMP.setText(String.format(BasicInfoFrag.tempFormat, cmd.TEMP));

                labelProgress.setText(cmd.Progress + "%"); // H M S
                labelElapse.setText(cmd.Elapse + "s");
                labelDuration.setText(cmd.TargetDuration + "min");

                labelSampleMode.setText(cmd.SampleMode == Comm.TIMED_SET_CAP ? "定容量":"定时长");
                labelLaunchMode.setText(cmd.SampleMode == Comm.MANUAL_SET ? "手动":"定时");

//                cmd.Channel = Comm.Channel.CH1; // todo 使用服务器返回值
                labelChannel.setText(cmd.Channel == null ? "" : cmd.Channel.name());
                labelGroup.setText(cmd.SampleMode == Comm.MANUAL_SET ? "0" : String.format("第%d组", cmd.Group));
            }
        }).reqSampleData(itemID);
    }
}

package hk.amae.sampler;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import hk.amae.util.Comm;
import hk.amae.util.Command;


public class QueryAct extends Activity {
    private TextView labelSpeed, labelTargetSpeed, labelVolume, labelStandardVol,
        labelTime, labelATM, labelTEMP, labelProgress, labelElapse, labelDuration,
        labelSampleMode, labelLaunchMode, labelChannel, labelGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_query);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QueryAct.this.onBackPressed();
            }
        });

        labelSpeed = (TextView) findViewById(R.id.label_speed);
        labelTargetSpeed = (TextView) findViewById(R.id.label_set_speed);
        labelVolume = (TextView) findViewById(R.id.label_total);
        labelStandardVol = (TextView) findViewById(R.id.label_total_standard);
        labelTime = (TextView) findViewById(R.id.label_local_time);
        labelATM = (TextView) findViewById(R.id.label_atm);
        labelTEMP = (TextView) findViewById(R.id.label_temp);
        labelProgress = (TextView) findViewById(R.id.label_sample_prog);
        labelElapse = (TextView) findViewById(R.id.label_sample_time);
        labelDuration = (TextView) findViewById(R.id.label_set_time);
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
                labelSpeed.setText(String.format("%dmL/min", cmd.Speed));
                labelTargetSpeed.setText(String.format("%dmL/min", cmd.TargetSpeed));
                labelLaunchMode.setText(cmd.SampleMode == Comm.MANUAL_SET ? "手动":"定时");
            }
        }).reqSampleState();
    }
}

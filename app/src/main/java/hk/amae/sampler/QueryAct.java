package hk.amae.sampler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.frag.BasicInfoFrag;
import hk.amae.frag.MainFrag;
import hk.amae.util.Comm;
import hk.amae.util.Command;


public class QueryAct extends Activity {
    private TextView labelSpeed, labelTargetSpeed, labelVolume, labelStandardVol,
        labelTime, labelATM, labelTEMP, labelProgress, labelElapse, labelDuration, labelTargetVolume,
        labelSampleMode, labelLaunchMode, labelChannel, labelGroup, labelGPS;

    private LinearLayout wrapSampleMode, wrapLaunchMode, wrapTargetDuration, wrapTargetVolume;

    public static final String KEY_ITEM = "itemID";
    String itemID = null;
    boolean isHistory = false;
    Command.Once onceDone;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_query);

        Intent intent = getIntent();
        itemID = intent.getStringExtra(KEY_ITEM);
        if (itemID != null)
            isHistory = true;

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
        labelTargetVolume = (TextView) findViewById(R.id.label_target_volume);
        wrapTargetDuration = (LinearLayout) findViewById(R.id.label_wrap_duration);
        wrapTargetVolume = (LinearLayout) findViewById(R.id.label_wrap_volume);

        labelSampleMode = (TextView) findViewById(R.id.label_sample_mode);
        labelLaunchMode = (TextView) findViewById(R.id.label_launch_mode);
        labelChannel = (TextView) findViewById(R.id.label_channel);
        labelGroup = (TextView) findViewById(R.id.label_set_group);
        labelGPS = (TextView) findViewById(R.id.label_gps);

        wrapLaunchMode = (LinearLayout) findViewById(R.id.wrapLaunchMode);
        wrapSampleMode = (LinearLayout) findViewById(R.id.wrapSampleMode);

//        labelGPS.setText(Comm.getSP("gps"));

        onceDone = new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;
//                cmd.TargetSpeed = 200;
//                cmd.DateTime = "2015-06-01 23:33";
                labelSpeed.setText(String.format("%dmL/min", cmd.Speed));
                labelTargetSpeed.setText(String.format("%dmL/min", cmd.TargetSpeed));
                labelVolume.setText(String.format("%dmL", cmd.Volume));
                labelStandardVol.setText(String.format("%dmL", cmd.StandardVol));

                labelTime.setText(cmd.DateTime);
                labelATM.setText(String.format(BasicInfoFrag.atmFormat, cmd.ATM));
                labelTEMP.setText(String.format(BasicInfoFrag.tempFormat, cmd.TEMP));

                labelProgress.setText(cmd.Progress + "%");
                labelElapse.setText(cmd.Elapse + "s");

                if (cmd.SampleMode == Comm.TIMED_SET_CAP) {
                    wrapTargetDuration.setVisibility(View.GONE);
                    wrapTargetVolume.setVisibility(View.VISIBLE);
                    labelTargetVolume.setText(cmd.TargetVolume + "mL");
                } else {
                    wrapTargetDuration.setVisibility(View.VISIBLE);
                    wrapTargetVolume.setVisibility(View.GONE);
                    labelDuration.setText(cmd.TargetDuration + "min");
                }

                if (isHistory) {
                    wrapLaunchMode.setVisibility(View.GONE);
                    wrapSampleMode.setVisibility(View.GONE);
                } else {
                    wrapLaunchMode.setVisibility(View.VISIBLE);
                    wrapSampleMode.setVisibility(View.VISIBLE);
                    labelSampleMode.setText(cmd.ManualMode == Comm.TIMED_SET_CAP ? "定容量":"定时长");
                    labelLaunchMode.setText(cmd.Manual ? "手动":"定时");
                }

                labelChannel.setText(cmd.Channel == null ? "" : cmd.Channel.name());
                labelGroup.setText(cmd.SampleMode == Comm.MANUAL_SET ? "0" : String.format("第%d组", cmd.Group));
                if (cmd.GPS == null || cmd.GPS.length() == 0) {
                    Comm.logI("no gps from server");
                } else {
                    labelGPS.setText(cmd.GPS);
                }
            }
        };

        try {
            doQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doQuery() {
        if (isHistory)
            new Command(onceDone).reqSampleData(itemID);
        else
            realtimeQuery();
    }

    private void realtimeQuery() {
        timer = new Timer();
        final Comm.Channel channel = Comm.Channel.init(Comm.getIntSP(MainFrag.SP_CURRENTCHANNEL));
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Command(onceDone).reqSampleState(channel);
            }
        }, 0, 5000);
    }

    @Override
    protected void onPause() {
        try {
            if (timer != null)
                timer.cancel();
        } catch (Exception e) {

        }
        super.onPause();
    }
}

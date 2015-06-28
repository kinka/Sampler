package hk.amae.sampler;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.frag.BasicInfoFrag;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.util.Command.Once;

/**
 * Created by kinka on 3/28/15.
 */
public class MonitorAct extends Activity {
    private final String fmtRow = "row_%d_%d"; // 虽然这样子挺笨的，但是不想麻烦再去搞个列表项
    TextView[][] rows = new TextView[8][4];

    TextView atm, datetime, temp;

    Timer timer = new Timer();
    Timer ticker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_monitor);

        atm = (TextView) findViewById(R.id.txt_atm);
        temp = (TextView) findViewById(R.id.txt_temp);
        datetime = (TextView) findViewById(R.id.txt_datetime);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MonitorAct.this.onBackPressed();
            }
        });

        Resources res = getResources();
        String pkgName = getPackageName();
        for (int i=0; i<rows.length; i++) {
            rows[i] = new TextView[4];
            for (int j=0; j<4; j++) {
                int id = res.getIdentifier(String.format(fmtRow, i, j), "id", pkgName);
                rows[i][j] = (TextView) findViewById(id);
            }
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                query();

                updateInfo();
            }
        }, 0, 3000);

        ticker = Comm.syncSvrTime(ticker, datetime);
    }

    private void query() {
        for (int i=0; i<Command.CHANNELCOUNT; i++) {
            final int r = i;
            new Command(new Once() {
                @Override
                public void done(boolean verify, Command cmd) {
                    if (!verify) return;

                    TextView speed = rows[r][1];
                    TextView volume = rows[r][2];
                    TextView progress = rows[r][3];
                    speed.setText(String.format("%d", cmd.Speed));
                    volume.setText(String.format("%.2f", cmd.Volume / 1000f));
                    progress.setText(String.format("%d%%", cmd.Progress));
                }
            }).reqChannelState(Comm.Channel.init(i+1)); // CH1 - CH8
        }
    }

    public void updateInfo() {
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;
                atm.setText(String.format(BasicInfoFrag.atmFormat, cmd.ATM));
                temp.setText(String.format(BasicInfoFrag.tempFormat, cmd.TEMP));
            }
        }).reqATM_TEMP();

/*        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;
                if (cmd.DateTime != null)
                    datetime.setText(cmd.DateTime);
//                    datetime.setText(cmd.DateTime.replace(" ", "\n"));
            }
        }).reqDateTime();*/
    }

    @Override
    protected void onPause() {
        try {
            if (timer != null)
                timer.cancel();
        } catch (Exception e) {

        }
        try {
            if (ticker != null)
                ticker.cancel();
        } catch (Exception e) {

        }
        super.onPause();
    }
}

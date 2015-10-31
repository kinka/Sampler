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
    TextView[][] rows;

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


        initRows();

        Resources res = getResources();
        String pkgName = getPackageName();
        for (int i=0; i<Command.CHANNELCOUNT; i++) {
            if (i >= rows.length) {
                // 隐藏不必要的通道信息
                int id = res.getIdentifier(String.format("row_%d", i), "id", pkgName);
                findViewById(id).setVisibility(View.GONE);
                continue;
            }
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

    private void initRows() {
        int mode = Comm.getIntSP(ChannelAct.SP_CHANNELMODE);
        switch (mode) {
            case ChannelAct.MODE_SINGLE:
                rows = new TextView[8][4];
                break;
            case ChannelAct.MODE_COUPLE:
                rows = new TextView[4][4];
                break;
            case ChannelAct.MODE_4IN1:
                rows = new TextView[2][4];
                break;
            case ChannelAct.MODE_8IN1:
                rows = new TextView[1][4];
                break;
        }
    }

    private void query() {
        int base = 1; // CH1 - CH8
        if (rows.length == 4)
            base = 1 + 8; // C1 - C4
        else if (rows.length == 2)
            base = 1 + 8 + 4; // B1 - B2
        else if (rows.length == 1)
            base = 1 + 8 + 4 + 2; // A1

        for (int i=0; i<rows.length; i++) {
            final int r = i;
            int ch = i + base;
            new Command(new Once() {
                @Override
                public void done(boolean verify, Command cmd) {
                    if (!verify) return;

                    TextView txtCh = rows[r][0];
                    TextView speed = rows[r][1];
                    TextView volume = rows[r][2];
                    TextView progress = rows[r][3];
                    txtCh.setText(cmd.Channel.toString());
                    speed.setText(String.format("%d", cmd.Speed));
                    volume.setText(String.format("%.2f", cmd.Volume / 1000f));
                    progress.setText(String.format("%d%%", cmd.Progress));
                }
            }).reqChannelState(Comm.Channel.init(ch));
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

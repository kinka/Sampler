package hk.amae.sampler;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import hk.amae.util.Command;


public class SysInfoAct extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_sysinfo);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SysInfoAct.this.onBackPressed();
            }
        });

        doQuery();
    }

    private void doQuery() {
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                ((TextView) findViewById(R.id.txt_model)).setText(cmd.Model);
                ((TextView) findViewById(R.id.txt_software_ver)).setText(cmd.SoftwareVer);
                ((TextView) findViewById(R.id.txt_hardware_ver)).setText(cmd.HardwareVer);
                ((TextView) findViewById(R.id.txt_channel_num)).setText(cmd.ChannelCount);
                ((TextView) findViewById(R.id.txt_channel_cap)).setText(cmd.ChannelCap);
                ((TextView) findViewById(R.id.txt_storage)).setText(cmd.Storage);
            }
        }).reqSysInfo();
    }
}

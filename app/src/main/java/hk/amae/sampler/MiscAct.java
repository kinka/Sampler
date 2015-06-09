package hk.amae.sampler;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.util.Deliver;


public class MiscAct extends Activity {
    EditText txtHost, txtPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_misc);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscAct.this.onBackPressed();
            }
        });

        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMiscSetting();
            }
        });

        txtHost = (EditText) findViewById(R.id.txt_host);
        txtPort = (EditText) findViewById(R.id.txt_port);

        init();
    }

    void init() {
        String server = Comm.getSP(Deliver.SP_SVR);
        String host = "";
        String port = "";
        if (server == null || server.isEmpty())
            server = Deliver.DefaultSvr;

        if (server.contains(":")) {
            int pos = server.indexOf(":");
            host = server.substring(0, pos);
            port = server.substring(pos + 1);
        } else {
            host = server;
        }
        txtHost.setText(host);
        txtPort.setText(port);
    }

    void saveMiscSetting() {
        String host = txtHost.getText().toString();
        String port = txtPort.getText().toString();
        if (host.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, "存在空值，请检查", Toast.LENGTH_SHORT).show();
            return;
        }
        Comm.setSP(Deliver.SP_SVR, host + ":" + port);
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
    }
}

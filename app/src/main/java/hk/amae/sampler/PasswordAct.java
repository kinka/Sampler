package hk.amae.sampler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;


public class PasswordAct extends Activity {
    public static String ADMIN = "管理员";
    public static String ADJUST = "校准";

    String titleFormat = "%s密码设定";
    String pwdLabelFormat = "请设定%s密码";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_password);

        TextView labelTitle = (TextView) findViewById(R.id.label_title);
        TextView labelPassword = (TextView) findViewById(R.id.label_pwd);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if (type == null)
            type = ADMIN;

        labelTitle.setText(String.format(titleFormat, type));
        labelPassword.setText(String.format(pwdLabelFormat, type));
    }

}

package hk.amae.sampler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import hk.amae.util.Comm;


public class PasswordAct extends Activity implements View.OnClickListener {
    public static String ADMIN = "管理员";
    public static String ADJUST = "校准";

    final public static String SP_PWD_ADMIN = "pwd_admin";
    final public static String SP_PWD_ADJUST = "pwd_adjust";

    String titleFormat = "%s密码设定";
    String pwdLabelFormat = "请设定%s密码";

    TextView txtPassword, txtConfirm;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_password);

        TextView labelTitle = (TextView) findViewById(R.id.label_title);
        TextView labelPassword = (TextView) findViewById(R.id.label_pwd);
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        if (type == null)
            type = ADMIN;

        labelTitle.setText(String.format(titleFormat, type));
        labelPassword.setText(String.format(pwdLabelFormat, type));

        txtPassword = (TextView) findViewById(R.id.txt_pwd);
        txtConfirm = (TextView) findViewById(R.id.txt_confirm_pwd);

        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                String pwd = txtPassword.getText().toString();
                String confirmPwd = txtConfirm.getText().toString();
                if (pwd.length() == 0) {
                    Toast.makeText(this, "密码为空", Toast.LENGTH_SHORT).show();
                    break;
                } else if (!pwd.equals(confirmPwd)) {
                    Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (type.equals(ADMIN))
                    Comm.setSP(SP_PWD_ADMIN, pwd);
                else
                    Comm.setSP(SP_PWD_ADJUST,confirmPwd);
                Toast.makeText(this, "密码设置成功", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
            case R.id.btn_back:
                onBackPressed();
                break;
        }
    }
}

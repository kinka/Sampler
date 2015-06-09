package hk.amae.frag;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import hk.amae.sampler.AdjustAct;
import hk.amae.sampler.ChannelAct;
import hk.amae.sampler.HardwareAct;
import hk.amae.sampler.HistoryAct;
import hk.amae.sampler.ModeSettingAct;
import hk.amae.sampler.PasswordAct;
import hk.amae.sampler.R;
import hk.amae.sampler.SysInfoAct;
import hk.amae.util.Comm;
import hk.amae.util.Deliver;
import hk.amae.widget.ActionSheet;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFrag extends Fragment implements
        View.OnClickListener, ActionSheet.OnASItemClickListener, AlertDialog.OnClickListener {

    ActionSheet as;
    int as_owner;

    EditText inputPassword;
    int passwordType = 1;

    public SettingFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fraq_settings, container, false);
        v.findViewById(R.id.btn_model).setOnClickListener(this);
        v.findViewById(R.id.btn_database).setOnClickListener(this);
        v.findViewById(R.id.btn_hardware).setOnClickListener(this);
        v.findViewById(R.id.btn_channel).setOnClickListener(this);
        v.findViewById(R.id.btn_sysinfo).setOnClickListener(this);
        v.findViewById(R.id.btn_password).setOnClickListener(this);
        v.findViewById(R.id.btn_adjust).setOnClickListener(this);
        v.findViewById(R.id.btn_other).setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_model:
                as_owner = R.id.btn_model;
                as = new ActionSheet(getActivity());
                as.setOnASItemClickListener(this);
                as.addItems(ModeSettingAct.CapacitySet, ModeSettingAct.TimingSet);
                as.showMenu();

                break;
            case R.id.btn_database:
                startActivity(new Intent(getActivity(), HistoryAct.class));
                break;

            case R.id.btn_hardware:
                startActivity(new Intent(getActivity(), HardwareAct.class));
                break;

            case R.id.btn_channel:
                startActivity(new Intent(getActivity(), ChannelAct.class));
                break;

            case R.id.btn_sysinfo:
                startActivity(new Intent(getActivity(), SysInfoAct.class));
                break;

            case R.id.btn_password:
                verifyPassword(1);
                break;

            case R.id.btn_adjust:
                String password = Comm.getSP(PasswordAct.SP_PWD_ADJUST);
                if (password.length() == 0) {
                    Toast.makeText(getActivity(), "请先设置校准密码", Toast.LENGTH_SHORT).show();
                    break;
                }

                verifyPassword(2);
                break;

            case R.id.btn_other:
                // todo 设置连接服务器
                Toast.makeText(getActivity(), "server " + Deliver.server + ":" + Deliver.svrPort, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean verifyPassword(int type) {
        passwordType = type;

        Activity parent = getActivity();
        inputPassword = new EditText(parent);
        inputPassword.setSingleLine();
        inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        inputPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        String title = String.format("请输入%s密码", type == 1 ? "管理员" : "校准");
        new AlertDialog.Builder(parent).setTitle(title)
                .setView(inputPassword).setPositiveButton("确定", this)
                .setNegativeButton("取消", this).show();

        return false;
    }

    @Override
    public void onASItemClick(int position) {
        Comm.logI("position " + position);
        switch (as_owner) {
            case R.id.btn_model: {
                Intent intent = new Intent(getActivity(), ModeSettingAct.class);
                intent.putExtra("mode", position == 0 ? ModeSettingAct.CapacitySet : ModeSettingAct.TimingSet);
                startActivity(intent);
            }
            break;
            case R.id.btn_password: {
                Intent intent = new Intent(getActivity(), PasswordAct.class);
                intent.putExtra("type", position == 0 ? PasswordAct.ADJUST : PasswordAct.ADMIN);
                startActivity(intent);
            }
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case AlertDialog.BUTTON_POSITIVE: {
                if (passwordType == 1) {
                    String password = Comm.getSP(PasswordAct.SP_PWD_ADMIN);
                    if (password.length() == 0)
                        password = "888888";
                    if (!inputPassword.getText().toString().equals(password)) {
                        Toast.makeText(getActivity(), "密码错误", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    as_owner = R.id.btn_password;
                    as = new ActionSheet(getActivity());
                    as.setOnASItemClickListener(this);
                    as.addItems(PasswordAct.ADJUST + "密码设定", PasswordAct.ADMIN + "密码设定");
                    as.showMenu();
                } else {
                    String password = Comm.getSP(PasswordAct.SP_PWD_ADJUST);
                    if (!inputPassword.getText().toString().equals(password)) {
                        Toast.makeText(getActivity(), "密码错误", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    startActivity(new Intent(getActivity(), AdjustAct.class));
                }

            }
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                break;
        }
    }
}

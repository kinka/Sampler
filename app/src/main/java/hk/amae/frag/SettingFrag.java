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

import hk.amae.sampler.AdjustAct;
import hk.amae.sampler.ChannelAct;
import hk.amae.sampler.HardwareAct;
import hk.amae.sampler.HistoryAct;
import hk.amae.sampler.ModelSettingAct;
import hk.amae.sampler.PasswordAct;
import hk.amae.sampler.R;
import hk.amae.sampler.SysInfoAct;
import hk.amae.util.Comm;
import hk.amae.widget.ActionSheet;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFrag extends Fragment implements
        View.OnClickListener, ActionSheet.OnASItemClickListener, AlertDialog.OnClickListener {

    ActionSheet as;
    int as_owner;

    EditText adminPassword;

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
                as.addItems(ModelSettingAct.CapacitySet, ModelSettingAct.TimingSet);
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
                verifyAdmin();
                break;

            case R.id.btn_adjust:
                startActivity(new Intent(getActivity(), AdjustAct.class));
                break;

            case R.id.btn_other:
                break;
        }
    }

    private boolean verifyAdmin() {
        Activity parent = getActivity();
        adminPassword = new EditText(parent);
        adminPassword.setSingleLine();
        adminPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        adminPassword.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        adminPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
        new AlertDialog.Builder(parent).setTitle("请输入管理员密码")
                .setView(adminPassword).setPositiveButton("确定", this)
                .setNegativeButton("取消", this).show();

        return false;
    }

    @Override
    public void onASItemClick(int position) {
        Comm.logI("position " + position);
        switch (as_owner) {
            case R.id.btn_model: {
                Intent intent = new Intent(getActivity(), ModelSettingAct.class);
                intent.putExtra("model", position == 0 ? ModelSettingAct.CapacitySet : ModelSettingAct.TimingSet);
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
                if (!adminPassword.getText().toString().equals("888888"))
                    break;
                as_owner = R.id.btn_password;
                as = new ActionSheet(getActivity());
                as.setOnASItemClickListener(this);
                as.addItems(PasswordAct.ADJUST + "密码设定", PasswordAct.ADMIN + "密码设定");
                as.showMenu();
            }
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                break;
        }
    }
}

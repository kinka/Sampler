package hk.amae.frag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import hk.amae.sampler.R;
import hk.amae.util.Comm;


public class BasicInfoFrag extends Fragment implements View.OnClickListener, AlertDialog.OnClickListener {
    private Activity parent;
    private boolean passed = false; // 是否通过密码验证
    private EditText snPassword;
    private EditText snText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_basic_info, container, false);
        TextView txtSN = (TextView) v.findViewById(R.id.txt_sn);
        txtSN.setOnClickListener(this);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parent = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_sn:
                snPassword = new EditText(parent);
                snPassword.setSingleLine();
                snPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                snPassword.setInputType(InputType.TYPE_CLASS_NUMBER);
                snPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(parent);
                alertDialog.setTitle("请输入系列号密码")
                        .setView(snPassword).setPositiveButton("确定", this)
                        .setNegativeButton("取消", this).setCancelable(false);

                AlertDialog tmpDialog = alertDialog.create();
                tmpDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Comm.showSoftInput();
                    }
                });
                tmpDialog.show();
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (!passed) { // 弹的是密码框
            if (i == AlertDialog.BUTTON_POSITIVE) {
                if (snPassword.getText().toString().equals("888888")) {
                    passed = true;

                    snText = new EditText(parent);
                    snText.setSingleLine(); // 不能放在setTransformationMethod 后面！
                    snText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    snText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(parent);
                    alertDialog.setTitle("请输入新的序列号")
                            .setView(snText).setPositiveButton("确定", this)
                            .setNegativeButton("取消", this).setCancelable(false);

                    AlertDialog tmpDialog = alertDialog.create();
                    tmpDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            Comm.showSoftInput();
                        }
                    });
                    tmpDialog.show();
                } else {
                    new AlertDialog.Builder(parent).setTitle("密码错误")
                            .setPositiveButton("确定", null).setCancelable(false).show();
                }
            }
        } else {
            if (snText == null) return;
            String newSN = snText.getText().toString();
            if (newSN.length() == 0 || i == AlertDialog.BUTTON_NEGATIVE) {
                dialogInterface.dismiss();
            } else {
                ((TextView) parent.findViewById(R.id.txt_sn)).setText(String.format("系列号：%s", newSN));
            }
            passed = false;
        }

    }
}

package hk.amae.sampler;

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

import hk.amae.util.Comm;


public class BasicInfoFrag extends Fragment implements View.OnClickListener, AlertDialog.OnClickListener {
    private Activity parent;

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
                EditText editText = new EditText(parent);
                editText.setSingleLine(); // 不能放在setTransformationMethod 后面！
//                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
                editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
                new AlertDialog.Builder(parent).setTitle("请输入新的序列号")
                        .setView(editText).setPositiveButton("确定", this)
                        .setNegativeButton("取消", this).show();
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == AlertDialog.BUTTON_POSITIVE)
            Comm.logI("确定");
        else if (i == AlertDialog.BUTTON_NEGATIVE)
            Comm.logI("取消");
    }
}

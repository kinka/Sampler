package hk.amae.frag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

import hk.amae.sampler.R;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.util.Command.Once;

public class BasicInfoFrag extends Fragment implements View.OnClickListener, AlertDialog.OnClickListener {
    private Activity parent;
    private boolean passed = false; // 是否通过密码验证
    private EditText snPassword;
    private EditText snText;

    String snFormat;
    String hostFormat;
    String modelFormat;
    public static String atmFormat;
    public static String tempFormat;

    public final static String SP_TIMEFIX = "TIMEFIX";

    private Timer ticker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        snFormat = getResources().getString(R.string.app_sn);
        hostFormat = getResources().getString(R.string.app_host);
        modelFormat = getResources().getString(R.string.app_model);

        atmFormat = getResources().getString(R.string.atm);
        tempFormat = getResources().getString(R.string.temperature);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_basic_info, container, false);
        TextView txtSN = (TextView) v.findViewById(R.id.txt_sn);
        txtSN.setOnClickListener(this);

        appTitle = (TextView) v.findViewById(R.id.app_title);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parent = activity;
//        reqGPS();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (ticker != null)
                ticker.cancel();
        } catch (Exception e) {

        }
        removeGPS();
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
//                        Comm.showSoftInput(500);
                    }
                });
                tmpDialog.show();
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int btnId) {
        if (!passed) { // 弹的是密码框
            if (btnId == AlertDialog.BUTTON_POSITIVE) {
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
//                            Comm.showSoftInput(500);
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
            final String newSN = snText.getText().toString();
            if (newSN.length() == 0 || btnId == AlertDialog.BUTTON_NEGATIVE) {
                dialogInterface.dismiss();
            } else {
                new Command(new Once() {
                    @Override
                    public void done(boolean verify, Command cmd) {
                        ((TextView) parent.findViewById(R.id.txt_sn)).setText(String.format(snFormat, newSN));
                    }
                }).setSN(newSN);
            }
            passed = false;
        }
    }

    public void updateInfo() {
        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;
                ((TextView) parent.findViewById(R.id.txt_model)).setText(String.format(modelFormat, cmd.Model));
                ((TextView) parent.findViewById(R.id.txt_sn)).setText(String.format(snFormat, cmd.SN));
                ((TextView) parent.findViewById(R.id.txt_ssid)).setText(String.format(hostFormat, Comm.getSP("ssid")));
            }
        }).reqModel();

        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;
                ((TextView) parent.findViewById(R.id.txt_atm)).setText(String.format(atmFormat, cmd.ATM));
                ((TextView) parent.findViewById(R.id.txt_temp)).setText(String.format(tempFormat, cmd.TEMP));
            }
        }).reqATM_TEMP();

        new Command(new Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                if (!verify) return;

//                cmd.DateTime = "2015-06-28 10:31:12";
                final TextView textView = ((TextView) parent.findViewById(R.id.txt_datetime));
//                textView.setText(cmd.DateTime);

                calcTimeOffset(cmd.DateTime);
                ticker = Comm.syncSvrTime(ticker, textView);
            }
        }).reqDateTime();
    }

    private int calcTimeOffset(String datetime) {
        Date toDiff = null;
        try {
            toDiff = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)).parse(datetime);
            Date now = Calendar.getInstance().getTime();
            boolean before = now.before(toDiff);
            long __diff = 0;
            if (before) {
                __diff = (toDiff.getTime() - now.getTime());
            } else {
                __diff = (now.getTime() - toDiff.getTime());
            }

            Comm.setIntSP(SP_TIMEFIX, (int) __diff);
            return (int) __diff;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    LocationClient locationClient = null;
    BDLocationListener bdListener = null;
    TextView appTitle;

    // 获取GPS信息
    @Override
    public void onResume() {
        super.onResume();
        reqGPS();
    }

    void reqGPS() {
        removeGPS();

        Comm.setSP("gps", "");
        locationClient = new LocationClient(parent);
        bdListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                double lat = bdLocation.getLatitude();
                double lng = bdLocation.getLongitude();

                String ss = "gps " + bdLocation.getLocType() + " lat " + lat + " lng " + lng + " alt " + bdLocation.getAltitude();
                ss += " " + bdLocation.getAddrStr();
                Comm.logI(ss);

                Comm.setSP("gps", String.format("%s经 %f\n%s纬   %f", (lng > 0 ? "东":"西"), Math.abs(lng), (lat > 0 ? "北":"南"), Math.abs(lat)));
            }
        };
        locationClient.registerLocationListener(bdListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//设置定位模式
        option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
        option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
//        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        locationClient.setLocOption(option);

        locationClient.start();
    }
    void removeGPS() {
        if (locationClient != null)
            locationClient.stop();
    }

    // keytool -list -v -keystore sampler.jks
}

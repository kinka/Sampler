package hk.amae.sampler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import hk.amae.frag.BasicInfoFrag;
import hk.amae.frag.MainFrag;
import hk.amae.frag.SettingFrag;
import hk.amae.util.Comm;
import hk.amae.util.Command;
import hk.amae.util.Deliver;

/**
 * 这个是入口界面
 */
public class MainAct extends Activity implements MainFrag.OnMainFragListener {
    private boolean isLocked = false;
    BasicInfoFrag basicInfoFrag;
    public int lastid = -1;

    private Timer __basicInfo;
    private int durationBasicInfo = 60*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.act_main);

        Comm.init(getApplicationContext(), getPackageName());

        Comm.logI("entered main...");

        basicInfoFrag = (BasicInfoFrag) getFragmentManager().findFragmentById(R.id.basicinfo_frag);

//        connectServer();
    }

    private boolean isAlerting = false;
    private void connectServer() {
        // 判断连网状态，如果没有连网，弹出连网提示;否则，获取当前连接的SSID，并让用户确认
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final String ssid;
        if (wifiInfo == null || wifiInfo.getSSID() == null)
            ssid = "";
        else
            ssid = wifiInfo.getSSID().replaceAll("\"", "");

        if (info != null && info.isConnected() && ssid != null && ssid.length() > 0) {
            if (Comm.getSP("ssid").equals(ssid)) {// 已经确认过了
                if (lastid < 0) switchPanel(0); // init
                isAlerting = false;
                return;
            }

            if (isAlerting) return;
            new AlertDialog.Builder(this).setTitle("确认连接")
                    .setMessage("您当前已经连接到 " + ssid +", 请确认。")
                    .setPositiveButton("没问题", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Comm.setSP("ssid", ssid);
                            switchPanel(0);
                            isAlerting = false;
                        }
                    }).setNegativeButton("重新连接", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }).setCancelable(false).show();
        } else {
            if (isAlerting) return;
            new AlertDialog.Builder(this).setTitle("没有连接")
                    .setMessage("当前暂时没有WIFI连接，请点击连接。")
                    .setPositiveButton("连接", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            isAlerting = false;
                        }
                    }).setCancelable(false).show();
        }
        isAlerting = true;
    }

    private void switchPanel(int id) {
        FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        switch (id) {
            case R.id.btn_setting:
                ft.replace(R.id.container, new SettingFrag());
                break;
            case R.id.btn_connect:
                // 算是强制重新连接吧
                Comm.setSP("ssid", "");
                connectServer();
                return;
            case R.id.btn_clean:
                startActivity(new Intent(this, CleanMachineAct.class));
                return;
            case R.id.btn_query:
                startActivity(new Intent(this, QueryAct.class));
                return;

            default:
                if (lastid != id) // 避免重复
                    ft.replace(R.id.container, new MainFrag());
        }
        ft.addToBackStack("xxx" + id);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        lastid = id;
        if (id == 0) {
            new Command(new Command.Once() {
                @Override
                public void done(boolean verify, Command cmd) {
                    if (cmd.ChannelMode == 0 || cmd.ChannelMode > ChannelAct.MODE_8IN1)
                        cmd.ChannelMode = ChannelAct.MODE_SINGLE;
                    int lastMode = Comm.getIntSP(ChannelAct.SP_CHANNELMODE);
                    if (lastMode != cmd.ChannelMode) {
                        Comm.setIntSP(ChannelAct.SP_CHANNELMODE, cmd.ChannelMode);
                        ft.commit();
                    }
                }
            }).reqChannelMode();
        }
        // 不管结果如何都先commit, 避免由于网络超时出现长时间空白
        ft.commit();
    }

    void killTimer() {
        try {
            if (__basicInfo != null)
                __basicInfo.cancel();
        } catch (Exception e) {

        }
    }

    boolean hasGPSDevice() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = lm.getAllProviders();
        if (providers == null) return false;
        boolean yes = providers.contains(LocationManager.GPS_PROVIDER);
        Comm.logI("has gps? " + yes);
        return yes;
    }

    boolean isGPSOpen() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Comm.logI("onResume");
        if (lastid < 0) {
            connectServer();
        }
        if (hasGPSDevice() && !isGPSOpen()) {
            new AlertDialog.Builder(this).setTitle("GPS定位功能")
                    .setMessage("检测到GPS定位功能没有打开，请点击打开")
                    .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setCancelable(false).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Comm.logI("onStart...");
        __basicInfo = new Timer();
        __basicInfo.schedule(new TimerTask() {
            @Override
            public void run() {
                basicInfoFrag.updateInfo();
            }
        }, 1000, durationBasicInfo);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Comm.logI("onStop...");
        killTimer();
    }

    @Override
    public void onBackPressed() {
        if (lastid > 0) {
            lastid = -1;
            super.onBackPressed();
        } else {
            lastid = -1;
            finish();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isLocked) {
            int statusHeight = 75;
            try {
                Class c = Class.forName("com.android.internal.R$dimen");
                Object obj = c.newInstance();
                Field field = c.getField("status_bar_height");
                int res = (Integer) field.get(obj);
                statusHeight = getResources().getDimensionPixelSize(res);
            } catch (Exception e) {

            }

            ScrollView scrollContainer = (ScrollView) findViewById(R.id.scroll_container);

            FrameLayout container = (FrameLayout) findViewById(R.id.container);
            float offsetX = container.getX();
            float offsetY = container.getY() - scrollContainer.getScrollY();

            LinearLayout wrapper = (LinearLayout) findViewById(R.id.layout_lockwrapper);
            offsetX += wrapper.getX();
            offsetY += wrapper.getY();

            ImageButton lock = (ImageButton) findViewById(R.id.toggle_lock);
            offsetX += lock.getX();
            offsetY += lock.getY();

            Rect rectSrc = new Rect((int) offsetX, (int) offsetY, (int) offsetX + lock.getWidth(), (int) offsetY + lock.getHeight());
            int eX = (int) ev.getX();
            int eY = (int) ev.getY() - statusHeight;
//            Comm.logI("x=" + eX + " y=" + eY + " rect=" + rectSrc.toShortString());
            boolean isInLock = eX < rectSrc.right && eX > rectSrc.left && eY < rectSrc.bottom && eY > rectSrc.top;
            if (!isInLock)
                return true;
        } else {
            // 主要是因为NumberOperator在键盘隐藏之后仍然保持焦点的问题
            FrameLayout container = (FrameLayout) findViewById(R.id.container);
            container.requestFocus();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onLockToggled(final boolean locked) {
        isLocked = locked;

        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                FrameLayout container = (FrameLayout) findViewById(R.id.container);
                if (locked) {
                    container.setForeground(new ColorDrawable(0x60727272));
                } else {
                    container.setForeground(new ColorDrawable(0x00000000));
                }
                Comm.setIntSP("locked", locked ? 1 : 0);
            }
        }).setScreenLock(isLocked);
    }

    @Override
    public void onButtonClick(int id) {
        switchPanel(id);
    }
}

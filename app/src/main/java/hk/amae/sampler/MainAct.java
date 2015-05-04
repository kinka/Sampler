package hk.amae.sampler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.lang.reflect.Field;

import hk.amae.frag.BasicInfoFrag;
import hk.amae.frag.MainFrag;
import hk.amae.frag.SettingFrag;
import hk.amae.util.Comm;
import hk.amae.util.Command;

/**
 * 这个是入口界面
 */
public class MainAct extends Activity implements MainFrag.OnMainFragListener {
    private boolean isLocked = false;
    BasicInfoFrag basicInfoFrag;
    public static int lastid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.act_main);

        Comm.init(getApplicationContext(), getPackageName());

        Comm.logI("entered main...");

        basicInfoFrag = (BasicInfoFrag) getFragmentManager().findFragmentById(R.id.basicinfo_frag);

        connectServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lastid < 0)
            connectServer();
    }

    private void connectServer() {
        // 判断连网状态，如果没有连网，弹出连网提示;否则，获取当前连接的SSID，并让用户确认
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final String ssid = wifiInfo.getSSID().replaceAll("\"", "");

        if (info != null && info.isConnected() && ssid != null && ssid.length() > 0) {
            if (Comm.getSP("ssid").equals(ssid)) {// 已经确认过了
                if (lastid < 0) switchPanel(0); // init
                return;
            }

            new AlertDialog.Builder(this).setTitle("确认连接")
                    .setMessage("您当前已经连接到 " + ssid +", 请确认。")
                    .setPositiveButton("没问题", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Comm.setSP("ssid", ssid);
                            switchPanel(0);
                        }
                    }).setNegativeButton("重新连接", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }).setCancelable(false).show();
        } else {
            new AlertDialog.Builder(this).setTitle("没有连接")
                    .setMessage("当前暂时没有WIFI连接，请点击连接。")
                    .setPositiveButton("连接", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    }).setCancelable(false).show();
        }
    }

    private void switchPanel(int id) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
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
                ft.replace(R.id.container, new MainFrag());
                basicInfoFrag.updateInfo();
        }
        ft.addToBackStack("xxx" + id);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        lastid = id;
    }

    @Override
    public void onBackPressed() {
        if (lastid != 0) {
            super.onBackPressed();
        } else {
            lastid = -1;
            finish();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isLocked) { // think about scrolling
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
        }).setScreenLock();
    }

    @Override
    public void onButtonClick(int id) {
        switchPanel(id);
    }
}

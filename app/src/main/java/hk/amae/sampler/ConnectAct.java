package hk.amae.sampler;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;


public class ConnectAct extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_connect);

        TextView txtView = (TextView) findViewById(R.id.txt_ssids);
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wm.getConnectionInfo();
        int strenth = info.getRssi();
        int speed = info.getLinkSpeed();
        String uints = WifiInfo.LINK_SPEED_UNITS;
        String ssid = info.getSSID();

        List<ScanResult> results = wm.getScanResults();
        String otherWifi = "others:\n";

        for (ScanResult result: results)
            otherWifi += result.SSID + ":" + result.level + " 等级：" + wm.calculateSignalLevel(result.level, 5) + "\n";

        String text = "We are connecting to " + ssid + " at " + speed + " " + uints + "\n\n";
        txtView.setText(text + otherWifi);

        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"3idiots\"";
        wc.preSharedKey = "\"shuiwuju\"";
//        wc.hiddenSSID = true;
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        int networkId = wm.addNetwork(wc);
        if (networkId != -1) {
            wm.enableNetwork(networkId, false);
            wm.saveConfiguration();
        }
    }
}

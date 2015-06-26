package hk.amae.widget;

import java.util.Calendar;

import hk.amae.util.Comm;

/**
 * Created by kinka on 6/4/15.
 */
public class SettingItem {
    public int id = 0;
    public int targetVol;
    public int targetDuration;
    public int targetSpeed;
    public boolean isSet;
    public boolean isSetCap; // 容量设置 或者 定时设置
    public String date = "2015-07-02";
    public String time = "00:00";

    @Override
    public String toString() {
        return String.format("[%d]%b (V: %d D: %d S: %d) %s %s", id, isSet, targetVol, targetDuration, targetSpeed, date, time);
    }

    public SettingItem(int id) {
        this.id = id;
    }
}
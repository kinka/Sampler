package hk.amae.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by kinka on 2/8/15.
 */
public class Comm {
    private static final Logger logger = Logger.getLogger("sampler");

    public enum Channel {
        ALL(0),
        CH1(1), CH2(2), CH3(3), CH4(4), CH5(5), CH6(6), CH7(7), CH8(8),
        C1(9), C2(0xa), C3(0xb), C4(0xc),
        B1(0xd), B2(0xe),
        A1(0xf);

        private final int value;
        Channel(int value) {
            this.value = value;
        }
        public byte getValue() {
            return (byte) this.value;
        }

        public static Channel init(int ch) {
            for (Channel c:values())
                if (ch == c.value) return c;
            return ALL;
        }
    }

    public final static int MANUAL_SET = 0; // 手动模式
    public final static int AUTO_SET_TIME = 0x1; // 定时模式之定时长
    public final static int AUTO_SET_CAP = 0x2; // 定时模式之定容量
    public final static int DO_PLAY = 0x2;
    public final static int DO_PAUSE = 0x3;
    public final static int DO_STOP = 0x0;
    public final static int PLAYING = 0x2;
    public final static int PAUSED = 0x3;
    public final static int STOPPED = 0x0;

    private static Context ctx;
    private static android.os.Handler handler;
    public static void init(Context ctx, String pkgName) {
        Comm.ctx = ctx;
        initLogger(pkgName);
        handler = new android.os.Handler();
        Deliver.init();
    }

    public static Logger initLogger(String pkgName) {
        try {
            int FileSizeLimit = 1024 * 1024;
            int FileCount = 2;
            String LoggerDir = Environment.getExternalStorageDirectory() + "/" + pkgName;
            String LoggerFile = LoggerDir + "/logger.txt";
            if (!new File(LoggerDir).exists())
                new File(LoggerDir).mkdir();
            FileHandler fileHandler = new FileHandler(LoggerFile, FileSizeLimit, FileCount, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logger;
    }

    public static void logI(String msg) {
        logger.info(msg);
    }
    public static void logE(String msg) {
        logger.severe(msg);
    }

    public static void setSP(String key, String value) {
        SharedPreferences commSP = ctx.getSharedPreferences("comm", Context.MODE_PRIVATE);
        Editor editor = commSP.edit();
        editor.putString(key, value);
        editor.commit();
        Comm.logI("write " + key + "=" + value);
    }
    public static String getSP(String key) {
        SharedPreferences commSP = ctx.getSharedPreferences("comm", Context.MODE_PRIVATE);
        return commSP.getString(key, "");
    }

    public static void setIntSP(String key, int value) {
        SharedPreferences commSP = ctx.getSharedPreferences("comm", Context.MODE_PRIVATE);
        Editor editor = commSP.edit();
        editor.putInt(key, value);
        editor.commit();
        Comm.logI("write " + key + "=" + value);
    }
    public static int getIntSP(String key) {
        SharedPreferences commSP = ctx.getSharedPreferences("comm", Context.MODE_PRIVATE);
        return commSP.getInt(key, 0);
    }

    public static void showSoftInput() {
        showSoftInput(200);
    }
    public static void showSoftInput(int delay) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }, delay);
    }

    public static void hideSoftInput() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        }, 200);
    }

    public static void runOnUiThread(Runnable runnable) {
        if (handler != null)
            handler.post(runnable);
        else
            runnable.run();
    }

    public static String[] getLocalDateTime(String serverDateTime) {
        String[] res = new String[2];
        int posY = serverDateTime.indexOf("-");
        int posM = serverDateTime.lastIndexOf("-");
        int posD = serverDateTime.indexOf(" ");
        String y = serverDateTime.substring(0, posY);
        String m = serverDateTime.substring(posY+1, posM);
        String d = serverDateTime.substring(posM+1, posD);
        res[0] = String.format("%s 年 %s 月 %s 日", y, m, d);
        res[1] = serverDateTime.substring(posD+1);
        return res;
    }
    public static String getServerDateTime(String localDate, String localTime) {
        String theDate = localDate.replaceAll("\\s(年|月)\\s", "-").replaceFirst("\\s日", "");
        String theTime = localTime.replaceAll("\\s", "");

        return theDate + " " + theTime;
    }
    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%d 年 %02d 月 %02d 日",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%02d : %02d : %02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }
}

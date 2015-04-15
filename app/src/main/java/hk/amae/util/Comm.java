package hk.amae.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by kinka on 2/8/15.
 */
public class Comm {
    private static final Logger logger = Logger.getLogger("sampler");

    public static enum Channel {
        ALL(0),
        CH1(1), CH2(2), CH3(3), CH4(4), CH5(5), CH6(6), CH7(7), CH8(8),
        C1(9), C2(0xa), C3(0xb), C4(0xc),
        B1(0xd), B2(0xe),
        A1(0xf);

        private final int value;
        private Channel(int value) {
            this.value = value;
        }
        public byte getValue() {
            return (byte) this.value;
        }
    }

    public final static int AUTO_SET_TIME = 0x1;
    public final static int AUTO_SET_CAP = 0x2;
    public final static int DO_PLAY = 0x1;
    public final static int DO_PAUSE = 0x2;
    public final static int DO_STOP = 0x3;

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
}

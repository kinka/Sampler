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

package com.thehecklers.spiremote;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class LogMonkey {
    private static PrintStream remoteLog;

    static {
        try {
            remoteLog = new PrintStream(new FileOutputStream(new File("SerialReadings.log")), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void logIt(String reading) {
        if (remoteLog != null) {
            remoteLog.println(reading);
        } else {
            System.out.println(reading);
        }
    }

    @PreDestroy
    private void closeLog() {
        if (remoteLog != null) {
            remoteLog.close();
        }
    }
}

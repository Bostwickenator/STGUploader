package com.github.ma1co.pmcademo.app;

import org.bostwickenator.googlephotos.FileGetter;

import java.io.*;

public class Logger {
    private static void log(String msg) {
        System.out.println(msg);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FileGetter.getFile("LOG.TXT"), true));
            writer.append(msg);
            writer.newLine();
            writer.close();
        } catch (IOException e) {}
    }
    private static void log(String type, String msg) { log("[" + type + "] " + msg); }

    public static void info(String msg) { log("INFO", msg); }
    public static void error(String msg) { log("ERROR", msg); }
}
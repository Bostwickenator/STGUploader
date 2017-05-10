package org.bostwickenator.googlephotos;

import com.github.ma1co.pmcademo.app.Logger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 * This class is a key value store with defaults, data is automatically persisted as JSON for debugging efficiency.
 */
@SuppressWarnings("SameParameterValue")
public class SettingsStore {

    private static SettingsStore theSettingsStore;

    private JSONObject settings;
    private final File settingsFile = FileGetter.getFile("SET.JSN");

    public static SettingsStore getSettingsStore() {
        if (theSettingsStore == null) {
            theSettingsStore = new SettingsStore();
        }
        return theSettingsStore;
    }

    private SettingsStore() {
        loadFile();
    }

    public boolean getBoolean(String name, boolean def) {
        try {
            return settings.optBoolean(name, def);
        } catch (Exception e) {
            Logger.error(e.toString());
            return def;
        }
    }

    public void putBoolean(String name, boolean value) {
        try {
            settings.put(name, value);
        } catch (Exception e) {
            Logger.error(e.toString());
        }
        saveFile();
    }

    public void putString(String name, String value) {
        try {
            settings.put(name, value);
        } catch (Exception e) {
            Logger.error(e.toString());
        }
        saveFile();
    }

    private void saveFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFile, false));
            writer.append(settings.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.toString());
        }
    }

    private void loadFile() {
        try {
            FileInputStream fis = new FileInputStream(settingsFile);
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            settings = new JSONObject(responseStrBuilder.toString());
            fis.close();
        } catch (Exception e) {
            settings = new JSONObject();
            e.printStackTrace();
            Logger.error(e.toString());
        }
    }
}

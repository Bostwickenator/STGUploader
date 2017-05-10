package org.bostwickenator.googlephotos;

import android.os.Environment;

import com.github.ma1co.pmcademo.app.Logger;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class FileGetter {

    private static final Pattern pattern = Pattern.compile("\\S{1,8}\\.\\S{1,3}");

    /**
     * Get a file handle in our storage directory
     *
     * @param name the name of the file
     * @return the file handle
     */
    public static File getFile(String name) {
        if(!checkIfValidFilename(name)){
            Logger.error("Files must match the 8.3 filename format");
            return null;
        }
        File ret = new File(Environment.getExternalStorageDirectory(), "PRIVATE/STG/" + name);
        ret.getParentFile().mkdirs();
        return ret;
    }

    /**
     * It turns out the camera is very fussy about file name formats presumably because of the constrains of FAT memory cards.
     * @param name of the file you want to find
     * @return if the name is valid or not
     */
    private static boolean checkIfValidFilename(String name) {
        return pattern.matcher(name).matches();
    }

    public static void migrateFiles(){
        File oldDir = new File(Environment.getExternalStorageDirectory(), "STG");
        if(oldDir.exists()) {
            File[] files = oldDir.listFiles();
            for(File file : files) {
                File newFile = getFile(file.getName());
                try {
                    Files.move(file, newFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.error(e.toString());
                }
            }
            oldDir.delete();
        }
    }
}
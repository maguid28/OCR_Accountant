package com.finalyearproject.dan.ocraccountingapp.util;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SetupUtil {

    private static final String TAG = "SetupUtil";

    public void prepareTesseract(String DATA_PATH, String TESSDATA, Activity activity) {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(DATA_PATH, TESSDATA, activity);
    }

    //Prepare directory on external storage
    public void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }


    //Copy tessdata files (located on assets/tessdata) to destination directory
    private void copyTessDataFiles(String DATA_PATH, String TESSDATA, Activity activity) {
        try {
            String fileList[] = activity.getAssets().list(TESSDATA);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + TESSDATA + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = activity.getAssets().open(TESSDATA + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + " to tessdata");
                    Log.d(TAG, "FILEPATH: " + pathToDataFile);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }

}

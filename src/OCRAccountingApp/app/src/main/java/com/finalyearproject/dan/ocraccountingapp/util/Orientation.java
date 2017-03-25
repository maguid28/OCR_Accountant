package com.finalyearproject.dan.ocraccountingapp.util;

import android.media.ExifInterface;

// This class reads the exif orientation of an image an rotates it accordingly.

public class Orientation {
    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }
}

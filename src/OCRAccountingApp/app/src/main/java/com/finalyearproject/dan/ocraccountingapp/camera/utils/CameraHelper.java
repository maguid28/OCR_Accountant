package com.finalyearproject.dan.ocraccountingapp.camera.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public final class CameraHelper {

    private final static String TAG = "CameraHelper";


    private CameraHelper() {}

    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasCamera2(Context context) {
        if (context == null) return false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false;
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] idList = manager.getCameraIdList();
            boolean notNull = true;
            if (idList.length == 0) {
                notNull = false;
            } else {
                for (final String str : idList) {
                    if (str == null || str.trim().isEmpty()) {
                        notNull = false;
                        break;
                    }
                    final CameraCharacteristics characteristics = manager.getCameraCharacteristics(str);

                    final int supportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        notNull = false;
                        break;
                    }
                }
            }
            return notNull;
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static File getOutputMediaFile(Context context) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), context.getPackageName());

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Failed to create directory.");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    @SuppressWarnings("deprecation")
    public static Size getPictureSize(List<Size> choices) {
        if (choices == null || choices.isEmpty()) return null;
        if (choices.size() == 1) return choices.get(0);

        Size result;
        Size maxPictureSize = Collections.max(choices, new CompareSizesByArea2());

        Collections.sort(choices, new CompareSizesByArea2());

        result = maxPictureSize;

        return result;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size getPictureSize(Size[] sizes) {
        if (sizes == null || sizes.length == 0) return null;

        List<Size> choices = Arrays.asList(sizes);

        if (choices.size() == 1) return choices.get(0);

        Size result;
        Size maxPictureSize = Collections.max(choices, new CompareSizesByArea2());


        Collections.sort(choices, new CompareSizesByArea2());

        result = maxPictureSize;


        return result;
    }

    @SuppressWarnings("deprecation")
    public static Size getSizeWithClosestRatio(List<Size> sizes, int width, int height) {

        if (sizes == null) return null;

        double MIN_TOLERANCE = 100;
        double targetRatio = (double) height / width;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Size size : sizes) {
            if (size.getWidth() == width && size.getHeight() == height)
                return size;

            double ratio = (double) size.getHeight() / size.getWidth();

            if (Math.abs(ratio - targetRatio) < MIN_TOLERANCE) MIN_TOLERANCE = ratio;
            else continue;

            if (Math.abs(size.getHeight() - height) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - height);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - height);
                }
            }
        }
        return optimalSize;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size getOptimalPreviewSize(Size[] sizes, int width, int height) {

        if (sizes == null) return null;

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Size size : sizes) {
//            if (size.getWidth() == width && size.getHeight() == height)
//                return size;
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - height) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - height);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - height);
                }
            }
        }
        return optimalSize;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea2());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return null;
        }
    }







    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class CompareSizesByArea2 implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}

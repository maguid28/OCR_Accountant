package com.finalyearproject.dan.ocraccountingapp.util;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class Size {

    private int width;
    private int height;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Size(android.util.Size size) {
        this.width = size.getWidth();
        this.height = size.getHeight();
    }

    @SuppressWarnings("deprecation")
    public Size(Camera.Size size) {
        this.width = size.width;
        this.height = size.height;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size[] fromArray2(android.util.Size[] sizes) {
        if (sizes == null) return null;
        Size[] result = new Size[sizes.length];

        for (int i = 0; i < sizes.length; ++i) {
            result[i] = new Size(sizes[i]);
        }

        return result;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

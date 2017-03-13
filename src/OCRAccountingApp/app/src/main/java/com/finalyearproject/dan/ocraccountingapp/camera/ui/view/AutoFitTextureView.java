package com.finalyearproject.dan.ocraccountingapp.camera.ui.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.TextureView;

@SuppressLint("ViewConstructor")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AutoFitTextureView extends TextureView {


    public AutoFitTextureView(Context context, TextureView.SurfaceTextureListener surfaceTextureListener) {
        super(context, null);
        setSurfaceTextureListener(surfaceTextureListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        int ratioHeight = 0;
        int ratioWidth = 0;
        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * (ratioWidth / (float) ratioHeight)) {
                setMeasuredDimension(width, (int) (width * (ratioWidth / (float) ratioHeight)));
            } else {
                setMeasuredDimension((int) (height * (ratioWidth / (float) ratioHeight)), height);
            }
        }
    }
}

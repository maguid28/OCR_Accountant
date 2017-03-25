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
        setMeasuredDimension(width, height);
    }
}

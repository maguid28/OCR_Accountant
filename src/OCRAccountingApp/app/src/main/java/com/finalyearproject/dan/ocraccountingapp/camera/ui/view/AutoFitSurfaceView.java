package com.finalyearproject.dan.ocraccountingapp.camera.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressWarnings("deprecation")
@SuppressLint("ViewConstructor")
public class AutoFitSurfaceView extends SurfaceView {

    private final SurfaceHolder surfaceHolder;

    private int ratioWidth;
    private int ratioHeight;

    public AutoFitSurfaceView(@NonNull Context context, SurfaceHolder.Callback callback) {
        super(context);

        this.surfaceHolder = getHolder();

        this.surfaceHolder.addCallback(callback);
        this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

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

package com.finalyearproject.dan.ocraccountingapp.camera.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaActionSound;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.finalyearproject.dan.ocraccountingapp.R;

public class CaptureButton extends android.support.v7.widget.AppCompatImageButton {

    public static final int TAKE_PHOTO_STATE = 0;
    private int currentState = TAKE_PHOTO_STATE;
    private Drawable takePhotoDrawable;
    private RecordButtonListener listener;

    public CaptureButton(@NonNull Context context) {
        this(context, null, 0);
    }

    public CaptureButton(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureButton(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        takePhotoDrawable = ContextCompat.getDrawable(context, R.drawable.camera_take_photo_button);
    }

    public void setup(@NonNull RecordButtonListener listener) {
        setMediaAction();
        this.listener = listener;

        setIcon();
        setOnClickListener(new RecordClickListener());
        setSoundEffectsEnabled(false);
    }

    public void setMediaAction() {
        currentState = TAKE_PHOTO_STATE;
        setRecordState(currentState);
        setIcon();
    }


    public void setRecordState(int state) {
        currentState = state;
        setIcon();
    }


    private void setIcon() {
        setImageDrawable(takePhotoDrawable);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void takePhoto(MediaActionSound sound) {
        sound.play(MediaActionSound.SHUTTER_CLICK);
        takePhoto();
    }

    private void takePhoto() {
        if (listener != null)
            listener.onTakePhotoButtonPressed();
    }

    public interface RecordButtonListener {

        void onTakePhotoButtonPressed();
    }

    private class RecordClickListener implements OnClickListener {

        private final static int CLICK_DELAY = 1000;

        private long lastClickTime = 0;

        @Override
        public void onClick(View view) {
            if (System.currentTimeMillis() - lastClickTime < CLICK_DELAY) {
                return;
            } else lastClickTime = System.currentTimeMillis();

            if (Build.VERSION.SDK_INT > 15) {
                MediaActionSound sound = new MediaActionSound();
                if (TAKE_PHOTO_STATE == currentState) {
                    takePhoto(sound);
                }
            } else {
                if (TAKE_PHOTO_STATE == currentState) {
                    takePhoto();
                }
            }
            setIcon();
        }
    }

}

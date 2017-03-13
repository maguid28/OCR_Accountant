package com.finalyearproject.dan.ocraccountingapp.camera.manager;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.finalyearproject.dan.ocraccountingapp.camera.utils.CameraHelper;
import com.finalyearproject.dan.ocraccountingapp.camera.utils.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class Camera1Manager extends BaseCameraManager<Integer, SurfaceHolder.Callback>
        implements SurfaceHolder.Callback, Camera.PictureCallback {

    private static final String TAG = "Camera1Manager";
    private static Camera1Manager currentInstance;
    private Camera camera;
    //private Surface surface;
    private int orientation;


    private File outputPath;
    private CameraPhotoListener photoListener;

    private Camera1Manager() {

    }

    public static Camera1Manager getInstance() {
        if (currentInstance == null) currentInstance = new Camera1Manager();
        return currentInstance;
    }

    @Override
    public void openCamera(final Integer cameraId,
                           final CameraOpenListener<Integer, SurfaceHolder.Callback> cameraOpenListener) {
        this.currentCameraId = cameraId;
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    camera = Camera.open(cameraId);
                    prepareCameraOutputs();
                    if (cameraOpenListener != null) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cameraOpenListener.onCameraOpened(cameraId, previewSize, currentInstance);
                            }
                        });
                    }
                } catch (Exception error) {
                    Log.d(TAG, "Can't open camera: " + error.getMessage());
                    if (cameraOpenListener != null) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cameraOpenListener.onCameraOpenError();
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void closeCamera(final CameraCloseListener<Integer> cameraCloseListener) {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    camera.release();
                    camera = null;
                    if (cameraCloseListener != null) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cameraCloseListener.onCameraClosed(currentCameraId);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void takePhoto(File photoFile, CameraPhotoListener cameraPhotoListener) {
        this.outputPath = photoFile;
        this.photoListener = cameraPhotoListener;
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                setCameraPhotoQuality(camera);
                camera.takePicture(null, null, currentInstance);
            }
        });
    }

    @Override
    public void releaseCameraManager() {
        super.releaseCameraManager();
    }

    @Override
    public void initializeCameraManager(Rotation rotation, Context context) {
        super.initializeCameraManager(rotation, context);

        numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; ++i) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                faceBackCameraId = i;
                faceBackCameraOrientation = cameraInfo.orientation;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                faceFrontCameraId = i;
                faceFrontCameraOrientation = cameraInfo.orientation;
            }
        }
    }

    @Override
    public void setFlashMode(int flashMode) {
    }

    @Override
    protected void prepareCameraOutputs() {
        try {
            List<Size> previewSizes = Size.fromList(camera.getParameters().getSupportedPreviewSizes());
            List<Size> pictureSizes = Size.fromList(camera.getParameters().getSupportedPictureSizes());

            photoSize = CameraHelper.getPictureSize(
                    (pictureSizes == null || pictureSizes.isEmpty()) ? previewSizes : pictureSizes);


            previewSize = CameraHelper.getSizeWithClosestRatio(previewSizes, photoSize.getWidth(), photoSize.getHeight());

        } catch (Exception e) {
            Log.e(TAG, "Error while setup camera sizes.");
        }
    }

    private void startPreview(SurfaceHolder surfaceHolder) {
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(currentCameraId, cameraInfo);
            int cameraRotationOffset = cameraInfo.orientation;

            Camera.Parameters parameters = camera.getParameters();
            setAutoFocus(camera, parameters);

            //Always keep flash on
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);

            turnPhotoCameraFeaturesOn(camera, parameters);

            int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break; // Natural orientation
                case Surface.ROTATION_90:
                    degrees = 90;
                    break; // Landscape left
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;// Upside down
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;// Landscape right
            }

            int displayRotation;
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                displayRotation = (cameraRotationOffset + degrees) % 360;
                displayRotation = (360 - displayRotation) % 360; // compensate
            } else {
                displayRotation = (cameraRotationOffset - degrees + 360) % 360;
            }

            this.camera.setDisplayOrientation(displayRotation);

            parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
            parameters.setPictureSize(photoSize.getWidth(), photoSize.getHeight());

            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (IOException error) {
            Log.d(TAG, "Error setting camera preview: " + error.getMessage());
        } catch (Exception ignore) {
            Log.d(TAG, "Error starting camera preview: " + ignore.getMessage());
        }
    }

    private void turnPhotoCameraFeaturesOn(Camera camera, Camera.Parameters parameters) {
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(parameters);
    }

    private void setAutoFocus(Camera camera, Camera.Parameters parameters) {
        try {
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.setParameters(parameters);
            }
        } catch (Exception ignore) {
        }
    }


    private void setCameraPhotoQuality(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        parameters.setPictureFormat(PixelFormat.JPEG);

        // set picture quality to highest
        parameters.setJpegQuality(100);

        parameters.setPictureSize(photoSize.getWidth(), photoSize.getHeight());

        camera.setParameters(parameters);
    }

    @Override
    protected int getPhotoOrientation(int sensorPosition) {
        int rotate;
        if (currentCameraId.equals(faceFrontCameraId)) {
            rotate = (360 + faceFrontCameraOrientation + rotation.getDegrees()) % 360;
        } else {
            rotate = (360 + faceBackCameraOrientation - rotation.getDegrees()) % 360;
        }

        if (rotate == 0) {
            orientation = ExifInterface.ORIENTATION_NORMAL;
        } else if (rotate == 90) {
            orientation = ExifInterface.ORIENTATION_ROTATE_90;
        } else if (rotate == 180) {
            orientation = ExifInterface.ORIENTATION_ROTATE_180;
        } else if (rotate == 270) {
            orientation = ExifInterface.ORIENTATION_ROTATE_270;
        }

        return orientation;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        surfaceHolder.getSurface();

        try {
            camera.stopPreview();
        } catch (Exception ignore) {
        }

        startPreview(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        surfaceHolder.getSurface();

        try {
            camera.stopPreview();
        } catch (Exception ignore) {
        }

        startPreview(surfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        File pictureFile = outputPath;
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions.");
            return;
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (FileNotFoundException error) {
            Log.e(TAG, "File not found: " + error.getMessage());
        } catch (IOException error) {
            Log.e(TAG, "Error accessing file: " + error.getMessage());
        } catch (Throwable error) {
            Log.e(TAG, "Error saving file: " + error.getMessage());
        }

        try {
            ExifInterface exif = new ExifInterface(pictureFile.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + getPhotoOrientation(rotation.getSensorPosition()));
            exif.saveAttributes();

            if (photoListener != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        photoListener.onPhotoTaken(outputPath);
                    }
                });
            }
        } catch (Throwable error) {
            Log.e(TAG, "Can't save exif info: " + error.getMessage());
        }
    }
}

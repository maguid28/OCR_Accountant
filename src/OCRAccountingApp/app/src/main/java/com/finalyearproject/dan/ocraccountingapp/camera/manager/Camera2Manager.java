package com.finalyearproject.dan.ocraccountingapp.camera.manager;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.finalyearproject.dan.ocraccountingapp.camera.ui.BaseActivity;
import com.finalyearproject.dan.ocraccountingapp.util.CameraHelper;
import com.finalyearproject.dan.ocraccountingapp.util.ImageSaver;
import com.finalyearproject.dan.ocraccountingapp.util.Size;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Objects;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class Camera2Manager extends BaseCameraManager<String, TextureView.SurfaceTextureListener>
        implements ImageReader.OnImageAvailableListener, TextureView.SurfaceTextureListener {

    private final static String TAG = "Camera2Manager";

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRE_CAPTURE = 2;
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;
    private static Camera2Manager currentInstance;
    private CameraOpenListener<String, TextureView.SurfaceTextureListener> cameraOpenListener;
    private CameraPhotoListener cameraPhotoListener;
    private File outputPath;
    @CameraPreviewState
    private int previewState = STATE_PREVIEW;
    private CameraManager manager;
    private CameraDevice cameraDevice;
    private CaptureRequest previewRequest;
    private CaptureRequest.Builder previewRequestBuilder;
    private CameraCaptureSession captureSession;
    private CameraCharacteristics backCameraCharacteristics;
    private StreamConfigurationMap frontCameraStreamConfigurationMap;
    private StreamConfigurationMap backCameraStreamConfigurationMap;
    private SurfaceTexture texture;
    private ImageReader imageReader;
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            currentInstance.cameraDevice = cameraDevice;
            if (cameraOpenListener != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(currentCameraId) && previewSize != null && currentInstance != null)
                            cameraOpenListener.onCameraOpened(currentCameraId, previewSize, currentInstance);
                    }
                });
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            currentInstance.cameraDevice = null;

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    cameraOpenListener.onCameraOpenError();
                }
            });
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            currentInstance.cameraDevice = null;

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    cameraOpenListener.onCameraOpenError();
                }
            });
        }
    };
    private CameraCaptureSession.CaptureCallback captureCallback
            = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            processCaptureResult(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            processCaptureResult(result);
        }

    };

    private Camera2Manager() {
    }

    public static Camera2Manager getInstance() {
        if (currentInstance == null) currentInstance = new Camera2Manager();
        return currentInstance;
    }

    @Override
    public void initializeCameraManager(Rotation rotation, Context context) {
        super.initializeCameraManager(rotation, context);

        this.manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        windowSize = new Size(size.x, size.y);

        try {
            String[] ids = manager.getCameraIdList();
            numberOfCameras = ids.length;
            for (String id : ids) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);

                faceBackCameraId = id;
                faceBackCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                backCameraCharacteristics = characteristics;

            }
        } catch (Exception e) {
            Log.e(TAG, "Error during camera init");
        }
    }

    @Override
    public void openCamera(String cameraId, final CameraOpenListener<String, TextureView.SurfaceTextureListener> cameraOpenListener) {
        this.currentCameraId = cameraId;
        this.cameraOpenListener = cameraOpenListener;
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                if (context == null || rotation == null) {
                    if (cameraOpenListener != null) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cameraOpenListener.onCameraOpenError();
                            }
                        });
                    }
                    return;
                }
                prepareCameraOutputs();
                try {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    manager.openCamera(currentCameraId, stateCallback, backgroundHandler);
                } catch (Exception e) {
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
    public void closeCamera(final CameraCloseListener<String> cameraCloseListener) {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                closeCamera();
                if (cameraCloseListener != null) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraCloseListener.onCameraClosed(currentCameraId);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void setFlashMode(int flashMode) {
        setFlashModeAndBuildPreviewRequest();
    }

    @Override
    public void takePhoto(File photoFile, CameraPhotoListener cameraPhotoListener) {
        this.outputPath = photoFile;
        this.cameraPhotoListener = cameraPhotoListener;

        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                lockFocus();
            }
        });

    }


    private void startPreview(SurfaceTexture texture) {
        try {
            if (texture == null) return;

            this.texture = texture;

            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            Surface workingSurface = new Surface(texture);

            previewRequestBuilder
                    = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(workingSurface);

            cameraDevice.createCaptureSession(Arrays.asList(workingSurface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            updatePreview(cameraCaptureSession);
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.d(TAG, "Fail while starting preview: ");
                        }
                    }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error while preparing surface for preview: ", e);
        }
    }

    @Override
    protected int getPhotoOrientation(int sensorPosition) {
        int degrees = 0;
        switch (sensorPosition) {
            case BaseActivity.SENSOR_POSITION_UP:
                degrees = 0;
                break; // Natural orientation
            case BaseActivity.SENSOR_POSITION_LEFT:
                degrees = 90;
                break; // Landscape left
            case BaseActivity.SENSOR_POSITION_UP_SIDE_DOWN:
                degrees = 180;
                break;// Upside down
            case BaseActivity.SENSOR_POSITION_RIGHT:
                degrees = 270;
                break;// Landscape right
            case BaseActivity.SENSOR_POSITION_UNSPECIFIED:
                break;
        }

        int rotate;
        if (Objects.equals(currentCameraId, faceFrontCameraId)) {
            rotate = (360 + faceFrontCameraOrientation + degrees) % 360;
        } else {
            rotate = (360 + faceBackCameraOrientation - degrees) % 360;
        }
        return rotate;
    }

    private void closeCamera() {
        closePreviewSession();
        releaseTexture();
        closeCameraDevice();
        closeImageReader();
    }

    private void releaseTexture() {
        if (null != texture) {
            texture.release();
            texture = null;
        }
    }

    private void closeImageReader() {
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void closeCameraDevice() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    @Override
    protected void prepareCameraOutputs() {
        try {
            CameraCharacteristics characteristics = backCameraCharacteristics;

            if (currentCameraId.equals(faceFrontCameraId) && frontCameraStreamConfigurationMap == null)
                frontCameraStreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            else if (currentCameraId.equals(faceBackCameraId) && backCameraStreamConfigurationMap == null)
                backCameraStreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            StreamConfigurationMap map = currentCameraId.equals(faceBackCameraId) ? backCameraStreamConfigurationMap : frontCameraStreamConfigurationMap;
            photoSize = CameraHelper.getPictureSize(Size.fromArray2(map.getOutputSizes(ImageFormat.JPEG)));

            imageReader = ImageReader.newInstance(photoSize.getWidth(), photoSize.getHeight(),
                    ImageFormat.JPEG, 2);
            imageReader.setOnImageAvailableListener(this, backgroundHandler);



            if (windowSize.getHeight() * windowSize.getWidth() > photoSize.getWidth() * photoSize.getHeight()) {
                previewSize = CameraHelper.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), photoSize.getWidth(), photoSize.getHeight());
            } else {
                previewSize = CameraHelper.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), windowSize.getWidth(), windowSize.getHeight());
                }

                if (previewSize == null)
                    previewSize = CameraHelper.chooseOptimalSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), windowSize.getWidth(), windowSize.getHeight(), photoSize);


        } catch (Exception e) {
            Log.e(TAG, "Error while setup camera sizes.", e);
        }
    }


    private void updatePreview(CameraCaptureSession cameraCaptureSession) {
        if (null == cameraDevice) {
            return;
        }
        captureSession = cameraCaptureSession;
        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        previewRequest = previewRequestBuilder.build();

        try {
            captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
        } catch (Exception e) {
            Log.e(TAG, "Error updating preview: ", e);
        }
        setFlashModeAndBuildPreviewRequest();
    }

    private void closePreviewSession() {
        if (captureSession != null) {
            captureSession.close();
            try {
                captureSession.abortCaptures();
            } catch (Exception ignore) {
            } finally {
                captureSession = null;
            }
        }
    }

    private void lockFocus() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            previewState = STATE_WAITING_LOCK;
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (Exception ignore) {
        }
    }

    private void processCaptureResult(CaptureResult result) {
        switch (previewState) {
            case STATE_PREVIEW: {
                break;
            }
            case STATE_WAITING_LOCK: {
                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                if (afState == null) {
                    captureStillPicture();
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                        || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        previewState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    } else {
                        runPreCaptureSequence();
                    }
                }
                break;
            }
            case STATE_WAITING_PRE_CAPTURE: {
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    previewState = STATE_WAITING_NON_PRE_CAPTURE;
                }
                break;
            }
            case STATE_WAITING_NON_PRE_CAPTURE: {
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    previewState = STATE_PICTURE_TAKEN;
                    captureStillPicture();
                }
                break;
            }
            case STATE_PICTURE_TAKEN:
                break;
        }
    }

    private void runPreCaptureSequence() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            previewState = STATE_WAITING_PRE_CAPTURE;
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (CameraAccessException ignored) {}
    }

    private void setFlashModeAndBuildPreviewRequest() {
        try {
            previewRequest = previewRequestBuilder.build();

            try {
                captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
            } catch (Exception e) {
                Log.e(TAG, "Error updating preview: ", e);
            }
        } catch (Exception ignore) {
            Log.e(TAG, "Error setting flash: ", ignore);
        }
    }

    private void captureStillPicture() {
        try {
            if (null == cameraDevice) {
                return;
            }
            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getPhotoOrientation(rotation.getSensorPosition()));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.d(TAG, "onCaptureCompleted: ");
                }
            };

            captureSession.stopRepeating();
            captureSession.capture(captureBuilder.build(), CaptureCallback, null);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Error during capturing picture");
        }
    }

    private void unlockFocus() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
            previewState = STATE_PREVIEW;
            captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
        } catch (Exception e) {
            Log.e(TAG, "Error during focus unlocking");
        }
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        File outputFile = outputPath;
        backgroundHandler.post(new ImageSaver(imageReader.acquireNextImage(), outputFile, new ImageSaver.ImageSaverCallback() {
            @Override
            public void onSuccessFinish() {
                Log.d(TAG, "onPhotoSuccessFinish: ");
                if (cameraPhotoListener != null) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraPhotoListener.onPhotoTaken(outputPath);
                        }
                    });
                }
                unlockFocus();
            }

            @Override
            public void onError() {
                Log.d(TAG, "onPhotoError: ");
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }));
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (surfaceTexture != null) startPreview(surfaceTexture);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (surfaceTexture != null) startPreview(surfaceTexture);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @IntDef({STATE_PREVIEW, STATE_WAITING_LOCK, STATE_WAITING_PRE_CAPTURE, STATE_WAITING_NON_PRE_CAPTURE, STATE_PICTURE_TAKEN})
    @Retention(RetentionPolicy.SOURCE)
    @interface CameraPreviewState {
    }

}

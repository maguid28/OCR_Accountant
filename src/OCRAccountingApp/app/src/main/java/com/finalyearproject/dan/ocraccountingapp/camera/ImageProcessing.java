package com.finalyearproject.dan.ocraccountingapp.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.finalyearproject.dan.ocraccountingapp.util.SetupUtil;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.rectangle;

public class ImageProcessing {

    private final static String TAG = "ImageProcessing";

    List<Point> drawBoundingRectangle(Mat srcFrame) {

        Mat imageGrey = new Mat();
        Point p1,p2,p3;

        // convert to greyscale
        Imgproc.cvtColor(srcFrame, imageGrey, Imgproc.COLOR_BGR2GRAY);

        //Set gaussian blur
        int gBlurSize = 9;
        Imgproc.GaussianBlur(imageGrey, imageGrey, new Size(gBlurSize, gBlurSize), 0);

        // canny instead of thresholding as it helped reduce blurring due to lower frame rate
        // apply canny edge detection
        Imgproc.Canny(imageGrey, imageGrey, 50, 140, 5, true);
        // find the contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imageGrey, contours, new Mat(),
                Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
        {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea)
            {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }

        List<Point> points = new ArrayList<>();
        // prevents app from crashing when no contours are found
        if(contours.size()>0) {
            Rect rect = Imgproc.boundingRect(contours.get(maxValIdx));
            rectangle(srcFrame, rect.br(), new Point(rect.br().x - rect.width,
                    rect.br().y - rect.height), new Scalar(37, 185, 153), 3);

            p1 = new Point(rect.tl().x, rect.tl().y);
            p2 = new Point(rect.tl().x, rect.br().y);
            p3 = new Point(rect.br().x, rect.br().y);

            points.add(p1);
            points.add(p2);
            points.add(p3);

        }
        return points;
    }



    void cleanImage(Mat result, Activity activity) {

        // temp, delete when done testing
        String pathToFile1 = tempgetOutputFile(activity).toString();
        Imgcodecs.imwrite(pathToFile1, result);

        //remove noise from image
        Photo.fastNlMeansDenoising(result, result);

        //resize image to x5 the size of original image
        Size size2 = new Size((result.width() * 5), (result.height() * 5));
        Imgproc.resize(result, result, size2);

        // convert to greyscale
        Imgproc.cvtColor(result, result, Imgproc.COLOR_RGB2GRAY);

        // changing this value increases thresholding, allowing less lines/creases in
        int c = 4;

        // thickness of line
        int blockSize = 55;
        Imgproc.adaptiveThreshold(result,result, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, blockSize, c);

        Core.transpose(result, result);
        Core.flip(result, result,1);

        Imgproc.cvtColor(result, result, Imgproc.COLOR_BayerBG2RGB);

        result = removeArtifacts(result);

        // scale up image to x2 the size of original image
        Size size3 = new Size((result.width() * 2), (result.height() * 2));
        Imgproc.resize(result, result, size3);

        String pathToFile = getOutputFile(activity).toString();
        Imgcodecs.imwrite(pathToFile, result);
    }


    private Mat removeArtifacts(Mat srcImage){

        Mat rgbImg = new Mat();

        Size sz = new Size((srcImage.width() * 40) / 100, (srcImage.height() * 40) / 100);
        Imgproc.resize(srcImage, rgbImg, sz);

        Mat small = new Mat();

        Imgproc.cvtColor(rgbImg, small, Imgproc.COLOR_RGB2GRAY);

        Mat grad = new Mat();

        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));

        Imgproc.morphologyEx(small, grad, Imgproc.MORPH_GRADIENT , morphKernel);

        Mat bw = new Mat();

        Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_OTSU);

        Mat connected = new Mat();

        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13,1));

        Imgproc.morphologyEx(bw, connected, Imgproc.MORPH_CLOSE  , morphKernel);

        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,1));

        Imgproc.morphologyEx(connected, connected, Imgproc.MORPH_OPEN  , morphKernel);

        Mat mask2 = Mat.zeros(bw.size(), CvType.CV_8UC1);


        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(connected, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        Mat mask = Mat.zeros(bw.size(), CvType.CV_8UC1);
        for(int idx = 0; idx < contours.size(); idx++) {

            Rect rect = Imgproc.boundingRect(contours.get(idx));

            Mat maskROI = new Mat(mask, rect);

            Imgproc.drawContours(mask, contours, idx, new Scalar(255, 255, 255), Core.FILLED);

            double r = (double)Core.countNonZero(maskROI)/(rect.width*rect.height);

            if (r > .20 && (rect.height > 5 && rect.width > 5) && (rect.height < 200)) {
                rectangle(rgbImg, rect.br() , new Point( rect.br().x-rect.width ,rect.br().y-rect.height),  new Scalar(0, 255, 0));
                rectangle(mask2, rect.br() , new Point( rect.br().x-rect.width ,rect.br().y-rect.height),  new Scalar(255, 255, 255),Core.FILLED);
            }
        }

        Mat imageROI = new Mat(srcImage.size(), CV_8UC3);
        imageROI.setTo(new Scalar(255,255,255));
        //Imgcodecs.imwrite(pathname + "_ROI123.jpg", imageROI);

        sz = new Size(srcImage.width(), srcImage.height());
        Imgproc.resize(mask2, mask2, sz);

        srcImage.copyTo(imageROI, mask2);

        // convert to greyscale
        Imgproc.cvtColor(imageROI, imageROI, Imgproc.COLOR_RGB2GRAY);

        //resize image to x2 the size of original image
        Size size2 = new Size((imageROI.width() * 2.5), (imageROI.height() * 2.5));

        Mat imageROI2 = new Mat(srcImage.size(), CV_8UC3);
        Imgproc.resize(imageROI, imageROI2, size2);

        return imageROI;
    }

    static File getOutputFile(Context context) {

        File filePath;

        String IMGS_PATH = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/TesseractSample/imgs";
        // checks if the directory exists, if not create it
        SetupUtil setupUtil = new SetupUtil();
        setupUtil.prepareDirectory(IMGS_PATH);

        //path to image is /storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/ocr.jpg
        String img_path = IMGS_PATH + "/ocr.jpg";

        Log.i(TAG, "IMGS_PATH IS NOW " +img_path);

        filePath = new File(img_path);

        return filePath;
    }

    private static File tempgetOutputFile(Context context) {

        File filePath;

        String IMGS_PATH = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/TesseractSample/imgs";
        // checks if the directory exists, if not create it
        SetupUtil setupUtil = new SetupUtil();
        setupUtil.prepareDirectory(IMGS_PATH);

        //path to image is /storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/ocr.jpg
        String img_path = IMGS_PATH + "/ocrUNPROCESSED.jpg";

        Log.i(TAG, "IMGS_PATH IS NOW " +img_path);

        filePath = new File(img_path);

        return filePath;
    }
}

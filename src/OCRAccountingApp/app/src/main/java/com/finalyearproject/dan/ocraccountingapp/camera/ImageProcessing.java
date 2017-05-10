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

    public List<Point> drawBoundingRectangle(Mat src) {

        Mat imageGrey = new Mat();

        Point p1,p2,p3,p4;

        // convert to greyscale
        Imgproc.cvtColor(src, imageGrey, Imgproc.COLOR_BGR2GRAY);
        // used threshold instead of canny edge detection as frame rate is higher with threshold
        //Imgproc.threshold(imageGrey, imageGrey, 150, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        //Set gaussian blur
        int gBlurSize = 9;
        Imgproc.GaussianBlur(imageGrey, imageGrey, new Size(gBlurSize, gBlurSize), 0);

        //applied canny as it helped reduce blurring due to lower frame rate
        //apply canny edge detection
        Imgproc.Canny(imageGrey, imageGrey, 50, 140, 5, true);
        // find the contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imageGrey, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

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
        //Imgproc.drawContours(mRgba, contours, maxValIdx, new Scalar(0,255,0), 5);

        List<Point> points = new ArrayList<>();
        // statement prevents app from crashing when no contours are found
        if(contours.size()>0) {
            Rect rect = Imgproc.boundingRect(contours.get(maxValIdx));
            rectangle(src, rect.br(), new Point(rect.br().x - rect.width, rect.br().y - rect.height), new Scalar(37, 185, 153), 3);

            p1 = new Point(rect.tl().x, rect.tl().y);
            p2 = new Point(rect.tl().x, rect.br().y);
            p3 = new Point(rect.br().x, rect.br().y);
            p4 = new Point(rect.br().x, rect.tl().y);

            points.add(p1);
            points.add(p2);
            points.add(p3);
            points.add(p4);

        }
        return points;
    }



    public void writeToStorage(Mat result, Activity activity) {

        // temp, delete when done testing
        String pathToFile1 = tempgetOutputFile(activity).toString();
        Imgcodecs.imwrite(pathToFile1, result);

        //remove noise from image
        Photo.fastNlMeansDenoising(result, result);

        //resize image to x3 the size of original image
        Size size2 = new Size((result.width() * 5), (result.height() * 5));
        Imgproc.resize(result, result, size2);

        // convert to greyscale
        Imgproc.cvtColor(result, result, Imgproc.COLOR_RGB2GRAY);


        // changing this value increases thresholding, allowing less lines/creases in
        int c = 4;
        // thickness of the black data let through the threshold, keep blocksize at 55 for now, will possibly try 45 later
        int blockSize = 55;
        Imgproc.adaptiveThreshold(result,result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, blockSize, c);

        //Imgproc.adaptiveThreshold(result,result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 55, 2);

        //Bitmap imageBitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(result, imageBitmap);

        //imageBitmap = RotateBitmap(imageBitmap);

        Core.transpose(result, result);
        Core.flip(result, result,1);

        Imgproc.cvtColor(result, result, Imgproc.COLOR_BayerBG2RGB);

        result = removeArtifacts(result);

        //resize image to x6 the size of original image
        Size size3 = new Size((result.width() * 2), (result.height() * 2));
        Imgproc.resize(result, result, size3);

        //Mat returned = new Mat();
        //Utils.bitmapToMat(imageBitmap, returned);

        //returned = removeArtifacts(returned);

        String pathToFile = getOutputFile(activity).toString();
        Imgcodecs.imwrite(pathToFile, result);
    }


    public Mat removeArtifacts(Mat srcImage){

        Mat rgbImg = new Mat();


        Size sz = new Size((srcImage.width() * 40) / 100, (srcImage.height() * 40) / 100);
        Imgproc.resize(srcImage, rgbImg, sz);


        Mat small = new Mat();

        Imgproc.cvtColor(rgbImg, small, Imgproc.COLOR_RGB2GRAY);

        Mat grad = new Mat();

        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));

        Imgproc.morphologyEx(small, grad, Imgproc.MORPH_GRADIENT , morphKernel);

        //Imgcodecs.imwrite(pathname + "_check1.jpg", grad);



        Mat bw = new Mat();

        //Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_OTSU);

        Mat connected = new Mat();

        //Imgcodecs.imwrite(pathname + "_check1_1.jpg", bw);

        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13,1));

        Imgproc.morphologyEx(bw, connected, Imgproc.MORPH_CLOSE  , morphKernel);

        //Imgcodecs.imwrite(pathname + "_check2.jpg", connected);


        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,1));

        Imgproc.morphologyEx(connected, connected, Imgproc.MORPH_OPEN  , morphKernel);

        //Imgcodecs.imwrite(pathname + "_check3.jpg", connected);




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

        //Imgcodecs.imwrite(pathname + "_check6.jpg", rgbImg);
        //Imgcodecs.imwrite(pathname + "_ROI.jpg", imageROI);
        //Imgcodecs.imwrite(pathname + "_MASK2.jpg", mask2);
        //Imgcodecs.imwrite(pathname + "_MASK.jpg", mask);

        return imageROI;
    }

    public static File getOutputFile(Context context) {

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

    public static File tempgetOutputFile(Context context) {

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

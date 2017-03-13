package com.finalyearproject.dan.ocraccountingapp.imageprocessing;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.*;
import static org.opencv.imgproc.Imgproc.*;

public class ReceiptScanner {

    private static String jpg = ".jpg";
    private static String pathname = "/Users/daniel/IdeaProjects/opcvtest/src/main/resources/receipt12";
    private static String path = pathname + jpg;



    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ReceiptScanner rec = new ReceiptScanner();
        Mat test = rec.correctReceipt(path);
    }



    public Mat correctReceipt(String path) {

        Mat getReceipt = receiptPic(path);
        Mat clean = imageClean(getReceipt);
        Mat artifact = removeArtifacts(clean);
        return artifact;
    }

    public Mat receiptPic(String path) {

        Mat srcImage = Imgcodecs.imread(path);
        Mat canny = ApplyCanny(srcImage);
        Mat transform = imageTransform(canny, srcImage);
        return transform;
    }

    private Mat ApplyCanny(Mat rgbImage) {
        //mat gray image holder
        Mat imageGrey = new Mat();
        //mat canny image
        Mat imageCanny = new Mat();

        //convert to greyscale
        Imgproc.cvtColor(rgbImage, imageGrey, Imgproc.COLOR_BGR2GRAY);

        //resize image to 30% of original image
        Size sz = new Size((imageGrey.width() * 30) / 100, (imageGrey.height() * 30) / 100);
        Imgproc.resize(imageGrey, imageGrey, sz);

        //Set gaussian blur
        int gBlurSize = 9;
        Imgproc.GaussianBlur(imageGrey, imageGrey, new Size(gBlurSize, gBlurSize), 0);

        //Set erosion values
        int erosion_size = 5;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erosion_size, erosion_size));
        Imgproc.erode(imageGrey, imageGrey, element);

        //Set dilation values
        int dilation_size = 5;
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilation_size, dilation_size));
        Imgproc.dilate(imageGrey, imageGrey, element1);

        //apply canny edge detection
        Imgproc.Canny(imageGrey, imageCanny, 50, 140, 5, true);

        return imageCanny;
    }



    private Mat imageTransform(Mat cannyImage, Mat sourceImage) {

        //resize image back to original size
        Size sz = new Size((cannyImage.width() * 100) / 30, (cannyImage.height() * 100) / 30);
        Imgproc.resize(cannyImage, cannyImage, sz);

        //find the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(cannyImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        MatOfPoint temp_contour = contours.get(0);
        MatOfPoint largest_contour = contours.get(0);

        MatOfPoint2f approxCurve = new MatOfPoint2f();

        for (int i = 0; i < contours.size(); i++) {
            temp_contour = contours.get(i);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                // find out if this is a rectangle
                MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                int contourSize = (int) temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize * 0.2, true);
                if (approxCurve_temp.total() == 4) {
                    maxArea = contourarea;
                    largest_contour = temp_contour;
                    approxCurve = approxCurve_temp;
                }
            }
        }

        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        largest_contours.add(0, largest_contour);
        //convert the image to color
        Imgproc.cvtColor(cannyImage, cannyImage, Imgproc.COLOR_BayerBG2RGB);

        double[] temp_double;
        temp_double = approxCurve.get(0,0);
        Point p1 = new Point(temp_double[0], temp_double[1]);
        temp_double = approxCurve.get(1,0);
        Point p2 = new Point(temp_double[0], temp_double[1]);
        temp_double = approxCurve.get(2,0);
        Point p3 = new Point(temp_double[0], temp_double[1]);
        temp_double = approxCurve.get(3,0);
        Point p4 = new Point(temp_double[0], temp_double[1]);
        List<Point> source = new ArrayList<Point>();

        //circle(sourceImage,p1,100,new Scalar(255,255,255), 20, 8, 0);
        //circle(sourceImage,p2,100,new Scalar(255,0,0), 20, 8, 0);
        //circle(sourceImage,p3,100,new Scalar(0,0,255), 20, 8, 0);
        //circle(sourceImage,p4,100,new Scalar(0,255,0), 20, 8, 0);

        source.add(p1);
        source.add(p2);
        source.add(p3);
        source.add(p4);


        int distp1p2=(int) Math.sqrt((p2.x-p1.x)*(p2.x-p1.x) + (p2.y-p1.y)*(p2.y-p1.y));
        int distp2p3=(int) Math.sqrt((p3.x-p2.x)*(p3.x-p2.x) + (p3.y-p2.y)*(p3.y-p2.y));

        Mat startM = Converters.vector_Point2f_to_Mat(source);
        Mat result=extract(sourceImage,startM, distp1p2, distp2p3);

        //Imgcodecs.imwrite("/storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/ocr"+ "_circled_points.jpg", sourceImage);

        if(p2.x > p1.x){
            Core.flip(result, result,1);
        }

        Imgcodecs.imwrite("/storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/ocr" + "_receiptimage.jpg", result);
        return result;
    }

    private Mat extract(Mat inputMat, Mat startM, int height, int width) {

        Mat outputMat = new Mat(width, height, CvType.CV_8UC4);

        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(0, height);
        Point ocvPOut3 = new Point(width, height);
        Point ocvPOut4 = new Point(width, 0);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(width, height),
                Imgproc.INTER_CUBIC);

        if(outputMat.size().width > outputMat.size().height){
            Core.transpose(outputMat, outputMat);
            Core.flip(outputMat, outputMat, 1);
        }

        return outputMat;
    }







    private Mat imageClean(Mat srcImage){
        //convert to grey
        Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGR2GRAY);
        //remove noise from image
        Photo.fastNlMeansDenoising(srcImage, srcImage);
        //apply adaptive threshold
        Imgproc.adaptiveThreshold(srcImage,srcImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 55, 2);

        Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BayerBG2RGB);

        return srcImage;
    }



    private Mat removeArtifacts(Mat srcImage){

        Mat rgbImg = new Mat();


        // resize image to 40% of original size
        Size sz = new Size((srcImage.width() * 40) / 100, (srcImage.height() * 40) / 100);
        Imgproc.resize(srcImage, rgbImg, sz);


        Mat small = new Mat();

        Imgproc.cvtColor(rgbImg, small, Imgproc.COLOR_RGB2GRAY);

        Mat grad = new Mat();

        //Imgcodecs.imwrite(pathname + "_check0.jpg", grad);

        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));

        Imgproc.morphologyEx(small, grad, Imgproc.MORPH_GRADIENT , morphKernel);

        //Imgcodecs.imwrite(pathname + "_check1.jpg", grad);



        Mat bw = new Mat();

        Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        Mat connected = new Mat();

        //Imgcodecs.imwrite(pathname + "_check1_1.jpg", bw);
/*
        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13,1));

        Imgproc.morphologyEx(bw, connected, Imgproc.MORPH_CLOSE  , morphKernel);

        Imgcodecs.imwrite(pathname + "_check2.jpg", connected);

/*
        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4,1));

        Imgproc.morphologyEx(connected, connected, Imgproc.MORPH_OPEN  , morphKernel);

        Imgcodecs.imwrite(pathname + "_check3.jpg", connected);


        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10,10));

        Imgproc.morphologyEx(connected, connected, Imgproc.MORPH_CLOSE  , morphKernel);

        Imgcodecs.imwrite(pathname + "_check4.jpg", connected);

        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10,1));

        Imgproc.morphologyEx(connected, connected, Imgproc.MORPH_OPEN  , morphKernel);

        Imgcodecs.imwrite(pathname + "_check5.jpg", connected);
*/



        Mat mask2 = Mat.zeros(bw.size(), CvType.CV_8UC1);


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(bw, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        Mat mask = Mat.zeros(bw.size(), CvType.CV_8UC1);
        for(int idx = 0; idx < contours.size(); idx++) {

            Rect rect = Imgproc.boundingRect(contours.get(idx));

            Mat maskROI = new Mat(mask, rect);

            Imgproc.drawContours(mask, contours, idx, new Scalar(255, 255, 255), Core.FILLED);

            double r = (double)Core.countNonZero(maskROI)/(rect.width*rect.height);

            if (r > .10 && (rect.height > 10 && rect.width > 5) && rect.height < 65 && rect.width < 80) {
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


        //Imgcodecs.imwrite(pathname + "_check6.jpg", rgbImg);
        //Imgcodecs.imwrite(pathname + "_ROI.jpg", imageROI);
        //Imgcodecs.imwrite(pathname + "_MASK2.jpg", mask2);
        //Imgcodecs.imwrite(pathname + "_MASK.jpg", mask);

        return imageROI;
    }


}

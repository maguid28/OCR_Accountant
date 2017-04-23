package com.finalyearproject.dan.ocraccountingapp.imgtotext;

import android.util.Log;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.opencv.utils.Converters;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.circle;
import static org.opencv.imgproc.Imgproc.rectangle;

public final class ReceiptScanner {

    String dirPath = "/storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/";

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String a = new ReceiptScanner().getTextFromReceiptImage("/Users/daniel/IdeaProjects/opcvtest/src/main/resources/receipt12.jpg");
        System.out.println(a);
    }

    public Mat receiptPic(String path) {

        Mat srcImage = Imgcodecs.imread(path);
        Mat canny = CannyEdge(srcImage);
        Mat transform = imageTransform(canny, srcImage);
        return transform;
    }

    public Mat correctReceipt(String path) {

        Mat getReceipt = receiptPic(path);
        Mat clean = imageClean(getReceipt);
        Mat artifact = removeArtifacts(clean);
        Log.e("width of p-image", String.valueOf(artifact.width()));
        Log.e("height of p-image", String.valueOf(artifact.height()));
        return artifact;
    }

    private static String jpg = ".jpg";
    private static String pathname = "/Users/daniel/IdeaProjects/opcvtest/src/main/resources/receipt12";
    private static String path = pathname + jpg;

    private String getTextFromReceiptImage(String path) {

        //final File receiptImageFile = new File(receiptFileImagepath);
        //final String receiptImagePathFile = receiptImageFile.getAbsolutePath();
        //System.out.println(receiptImagePathFile);

        Mat receiptImage = Imgcodecs.imread(path);
        Mat applyCanny = CannyEdge(receiptImage);
        Mat transformImage = imageTransform(applyCanny, receiptImage);
        Mat cleanImage = imageClean(transformImage);
        Mat cleanedReceipt = removeArtifacts(cleanImage);

        String cleanedReceiptFilePath = pathname + "_cleaned.jpg";
        //Imgcodecs.imwrite(cleanedReceiptFilePath, cleanedReceipt);

        return cleanedReceiptFilePath;
    }

    private Mat CannyEdge(Mat srcImage) {

        Imgcodecs.imwrite(dirPath + "srcimg.jpg", srcImage);

        //mat gray image holder
        Mat imageGrey = new Mat();
        //mat canny image
        Mat imageCny = new Mat();

        //convert to greyscale
        Imgproc.cvtColor(srcImage, imageGrey, Imgproc.COLOR_BGR2GRAY);

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
        Imgproc.Canny(imageGrey, imageCny, 50, 140, 5, true);

        return imageCny;
    }


    private Mat imageTransform(Mat cannyImg, Mat srcImg) {

        //resize image back to original size
        Size sz = new Size((cannyImg.width() * 100) / 30, (cannyImg.height() * 100) / 30);
        Imgproc.resize(cannyImg, cannyImg, sz);
        //Imgcodecs.imwrite(pathname + "_canny.jpg", imgSource);

        //find the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(cannyImg, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("contours: " + contours);

        double maxArea = -1;
        System.out.println("size " +Integer.toString(contours.size()));
        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint largest_contour = contours.get(0);

        MatOfPoint2f approxCurve = new MatOfPoint2f();

        for (int i = 0; i < contours.size(); i++) {
            temp_contour = contours.get(i);
            double contourarea = Imgproc.contourArea(temp_contour);
            //System.out.println( contours.get(i) + "contour area: " + contourarea + "..... index: " + i);
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

        //print the largest contour
        System.out.println("Largest contour is " + largest_contour + ".... area: " + maxArea);
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        largest_contours.add(0, largest_contour);
        //convert the image to color
        Imgproc.cvtColor(cannyImg, cannyImg, Imgproc.COLOR_BayerBG2RGB);

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

        circle(cannyImg,p1,100,new Scalar(255,255,255), 20, 8, 0);
        circle(cannyImg,p2,100,new Scalar(255,0,0), 20, 8, 0);
        circle(cannyImg,p3,100,new Scalar(0,0,255), 20, 8, 0);
        circle(cannyImg,p4,100,new Scalar(0,255,0), 20, 8, 0);

        source.add(p1);
        source.add(p2);
        source.add(p3);
        source.add(p4);

        System.out.println("p1:" + p1);
        System.out.println("p2:" + p2);
        System.out.println("p3:" + p3);
        System.out.println("p4:" + p4);
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(dirPath + "point_locations.txt"));
            bw.write("p1:" + p1 + "   (white)");
            bw.newLine();
            bw.write("p2:" + p2 + "   (blue)");
            bw.newLine();
            bw.write("p3:" + p3 + "   (red)");
            bw.newLine();
            bw.write("p4:" + p4 + "   (green)");
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int distp1p2=(int) Math.sqrt((p2.x-p1.x)*(p2.x-p1.x) + (p2.y-p1.y)*(p2.y-p1.y));
        int distp2p3=(int) Math.sqrt((p3.x-p2.x)*(p3.x-p2.x) + (p3.y-p2.y)*(p3.y-p2.y));


        Mat startM = Converters.vector_Point2f_to_Mat(source);
        Mat result=extract(srcImg, startM, distp1p2, distp2p3);
        Imgcodecs.imwrite(dirPath + "debug_circled_points.jpg", cannyImg);

        if(p2.x > p1.x){
            Core.flip(result, result,1);
        }

        Imgcodecs.imwrite(dirPath + "debug_check.jpg", result);
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

        System.out.println("image width: " + outputMat.size().width);
        System.out.println("image height: " + outputMat.size().height);
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
        //Photo.fastNlMeansDenoising(srcImage, srcImage);

        //Imgcodecs.imwrite(pathname + "_output_greyscale.jpg", srcImage);

        Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BayerBG2RGB);

        return srcImage;
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

        //Imgcodecs.imwrite(pathname + "_check1.jpg", grad);



        Mat bw = new Mat();

        Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        Mat connected = new Mat();

        //Imgcodecs.imwrite(pathname + "_check1_1.jpg", bw);

        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13,1));

        Imgproc.morphologyEx(bw, connected, Imgproc.MORPH_CLOSE  , morphKernel);

        //Imgcodecs.imwrite(pathname + "_check2.jpg", connected);


        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,1));

        Imgproc.morphologyEx(connected, connected, Imgproc.MORPH_OPEN  , morphKernel);

        //Imgcodecs.imwrite(pathname + "_check3.jpg", connected);




        Mat mask2 = Mat.zeros(bw.size(), CvType.CV_8UC1);


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(connected, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        Mat mask = Mat.zeros(bw.size(), CvType.CV_8UC1);
        for(int idx = 0; idx < contours.size(); idx++) {

            Rect rect = Imgproc.boundingRect(contours.get(idx));

            Mat maskROI = new Mat(mask, rect);

            Imgproc.drawContours(mask, contours, idx, new Scalar(255, 255, 255), Core.FILLED);

            double r = (double)Core.countNonZero(maskROI)/(rect.width*rect.height);

            if (r > .45 && (rect.height > 10 && rect.width > 10)) {
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

        return imageROI2;
    }


}




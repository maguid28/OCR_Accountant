package com.finalyearproject.dan.ocraccountingapp;


import org.bytedeco.javacpp.*;

import java.io.File;
import java.net.URL;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;


/**
 * Created by daniel on 15/11/2016.
 */

public class ReceiptScannerImpl implements ReceiptScanner {

    @Override
    public String getTextFromReceiptImage(final String receiptFileImagePath) {
        final File receiptImageFile = new File(receiptFileImagePath);
        //path to receipt image
        final String receiptImagePathFile = receiptImageFile.getAbsolutePath();
        System.out.println(receiptImagePathFile);
        //IplImage of receipt to be processed by JAVACV methods
        IplImage receiptImage = cvLoadImage(receiptImagePathFile);

        IplImage cannyEdgeImage = applyCannySquareEdgeDetectionOnImage(receiptImage,30);

        CvSeq largestSquare = findLargestSquareOnCannyDetectedImage(cannyEdgeImage);

        receiptImage = applyPerspectiveTransformThresholdOnOriginalImage(receiptImage, largestSquare, 30);

        receiptImage = cleanImageSmoothingForOCR(receiptImage);

        final File cleanedReceiptFile = new File(receiptFileImagePath);
        final String cleanedReceiptPathFile = cleanedReceiptFile.getAbsolutePath();
        cvSaveImage(cleanedReceiptPathFile, receiptImage);
        System.out.println(cleanedReceiptPathFile);

        cvReleaseImage(cannyEdgeImage);
        cannyEdgeImage = null;
        cvReleaseImage(receiptImage);
        receiptImage = null;

        return  getStringFromImage(cleanedReceiptPathFile);
    }


    /*
     * Resizes the image given a percent
     */
    private IplImage downScaleImage(IplImage srcImage, int percent) {

        System.out.println("srcImage - height - " + srcImage.height() + ", width - " + srcImage.width());

        IplImage destImage = cvCreateImage(cvSize((srcImage.width()*percent)/100, (srcImage.height()*percent)/100), srcImage.depth(), srcImage.nChannels());
        cvResize(srcImage,destImage);

        System.out.println("destImage - height - " + destImage.height() + ", width - " + destImage.width());
        return destImage;
    }



    /*
     * Detect the edges of an image using the canny edge detect method given a percent in order to
     * reduce the size of the image and be able to process it better
     */
    private IplImage applyCannySquareEdgeDetectionOnImage(IplImage srcImage, int percent) {
        IplImage destImage = downScaleImage(srcImage, percent);
        IplImage greyImage = cvCreateImage(cvGetSize(destImage), IPL_DEPTH_8U, 1);

        //convert to grey
        cvCvtColor(destImage, greyImage, CV_BGR2GRAY);
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
        Frame greyImageFrame = converterToMat.convert(greyImage);
        Mat greyImageMat = converterToMat.convert(greyImageFrame);

        //Apply gaussuan blur
        GaussianBlur(greyImageMat, greyImageMat, new Size(5, 5), 0.0, 0.0, BORDER_DEFAULT);

        destImage = converterToMat.convertToIplImage(greyImageFrame);

        //clean it for better detection
        cvErode(destImage, destImage);
        cvDilate(destImage, destImage);

        //apply the canny edge detection method
        cvCanny(destImage,destImage, 75.0, 200.0);

        File f = new File(System.getProperty("user.home") + File.separator + "receipt-canny-detect.jpeg");
        cvSaveImage(f.getAbsolutePath(), destImage);
        return destImage;
    }



    /*
     * Once applied canny edge to the image, we can find the largest square
     * using the find contours (square) method and asking for the largest one
     * that will be the on the image hopefully
     */
    private CvSeq findLargestSquareOnCannyDetectedImage(IplImage cannyEdgeDetectedImage) {
        IplImage foundedContoursImage = cvCloneImage(cannyEdgeDetectedImage);
        CvMemStorage memory = CvMemStorage.create();
        CvSeq contours = new CvSeq();
        cvFindContours(foundedContoursImage, memory,contours, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE, cvPoint(0,0));
        int maxWidth = 0;
        int maxHeight = 0;
        CvRect contour = null;
        CvSeq seqFounded = null;
        CvSeq nextSeq = new CvSeq();

        for(nextSeq = contours; nextSeq != null; nextSeq = nextSeq.h_next()) {
            contour = cvBoundingRect(nextSeq, 0);
            if((contour.width()>=maxWidth) && (contour.height() >= maxHeight)) {
                maxWidth = contour.width();
                maxHeight = contour.height();
                seqFounded = nextSeq;
            }
        }

        CvSeq result = cvApproxPoly(seqFounded, Loader.sizeof(CvContour.class), memory, CV_POLY_APPROX_DP, cvContourPerimeter(seqFounded) * 0.02, 0);

        for(int i = 0; i < result.total(); i++) {
            CvPoint v = new CvPoint(cvGetSeqElem(result, i));
            cvDrawCircle(foundedContoursImage, v, 5, CvScalar.BLUE, 20, 8, 0);
            System.out.println("found point(" + v.x() + "," + v.y() + ")");
        }

        File f = new File(System.getProperty("user.home") + File.separator + "receipt-find-contours.jpeg");
        cvSaveImage(f.getAbsolutePath(), foundedContoursImage);

        return result;
    }


    /*
     * Finally we apply a tranformation on the original image to obtain a
     * top down image of the receipt from the same original image using the
     * points detected earlier.
     */
    private IplImage applyPerspectiveTransformThresholdOnOriginalImage(IplImage srcImage, CvSeq contour, int percent) {
        IplImage warpImage = cvCloneImage(srcImage);

        //first, given the percentage, adjust to the original image
        for( int i = 0; i < contour.total(); i++) {
            CvPoint point = new CvPoint(cvGetSeqElem(contour,i));
            point.x((int) (point.x() * 100) / percent);
            point.y((int) (point.y() * 100) / percent);
        }

        //get each corner point of the image
        CvPoint topRightPoint = new CvPoint(cvGetSeqElem(contour,0));
        CvPoint topLeftPoint = new CvPoint(cvGetSeqElem(contour, 1));
        CvPoint bottomLeftPoint = new CvPoint(cvGetSeqElem(contour, 2));
        CvPoint bottomRightPoint = new CvPoint(cvGetSeqElem(contour, 3));

        int resultWidth = (int) (topRightPoint.x() - topLeftPoint.x());
        int bottomWidth = (int) (bottomRightPoint.x() - bottomLeftPoint.x());
        if(bottomWidth > resultWidth){
            resultWidth = bottomWidth;
        }
        int resultHeight = (int) (bottomLeftPoint.y() - topLeftPoint.y());
        int bottomHeight = (int) (bottomRightPoint.y() - topRightPoint.y());
        if(bottomHeight > resultHeight) {
            resultHeight = bottomHeight;
        }

        float[] sourcePoints = { topLeftPoint.x(), topLeftPoint.y(), topRightPoint.x(), topRightPoint.y(),
                                bottomLeftPoint.x(), bottomLeftPoint.y(), bottomRightPoint.x(), bottomRightPoint.y() };
        float[] destinationPoints = { 0, 0, resultWidth, 0, 0, resultHeight, resultWidth, resultHeight};
        CvMat homography = cvCreateMat(3, 3, CV_32FC1);
        cvGetPerspectiveTransform(sourcePoints,destinationPoints,homography);
        System.out.println(homography.toString());
        IplImage destImage = cvCloneImage(warpImage);
        cvWarpPerspective(warpImage, destImage, homography, CV_INTER_LINEAR, CvScalar.ZERO);

        return cropImage(destImage, 0, 0, resultWidth, resultHeight);
    }


    /*
     * Crops a square from an image to the new width and height from the 0,0 position
     */
    private IplImage cropImage(IplImage srcImage, int fromX, int fromY, int toWidth, int toHeight) {
        cvSetImageROI(srcImage, cvRect(fromX, fromY, toWidth, toHeight));
        IplImage destImage = cvCloneImage(srcImage);
        cvCopy(srcImage, destImage);
        return destImage;
    }


    /*
     * Cleans the inage of noise converting to grey smoothing and applying Otsu
     * threshold to the image and leabing the image with white background and
     * black foreground (letters).
     */
    private IplImage cleanImageSmoothingForOCR(IplImage srcImage) {
        IplImage destImage = cvCreateImage(cvGetSize(srcImage), IPL_DEPTH_8U, 1);
        cvCvtColor(srcImage, destImage, CV_BGR2GRAY);
        cvSmooth(destImage, destImage, CV_MEDIAN, 3, 0, 0, 0);
        cvThreshold(destImage, destImage, 0, 255, CV_THRESH_OTSU);
        return destImage;
    }

    /*
     * Call tesseract with the receipt image and return the text found.
     */
    private String getStringFromImage(final String pathToReceiptImageFile) {
        try {
            final URL tessDataResource = getClass().getResource("/");
            final File tessFolder = new File(tessDataResource.toURI());
            final String tessFolderPath = tessFolder.getAbsolutePath();
            System.out.println(tessFolderPath);
            BytePointer outText;
            tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
            api.SetVariable("tessedit_char_whitelist", "0123456789,/ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            //initialize tesseract-ocr with eng
            if(api.Init(tessFolderPath, "eng") != 0) {
                System.err.println("Could not initialise tesseract.");
            }
            //Open input image with leptonica library
            lept.PIX image = pixRead(pathToReceiptImageFile);
            api.SetImage(image);
            //Get OCR result
            outText = api.GetUTF8Text();
            String string = outText.getString();
            //Destroy used object and release memory.
            api.End();

            outText.deallocate();
            pixDestroy(image);
            return string;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

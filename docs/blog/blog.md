# Blog: OCR Personal Accounting Application

**Daniel Maguire**

## Blog Entry 1
Today I have started to learn how to use tesseract and tess-two within my application. I am still unsure which API I will use as I am unsure if the additional functions of tess-two will be useful to me.

## Blog Entry 2
During the last week I have tried to implement the tesseract API in my application.
My first issue that I came to was when images captured from the device camera were pulled into the application they arrived rotated. I solved this by collecting EXIF data from the image and rotating it accordingly.
I have discovered due to the size of the image being captured it was causing the application to halt for a considerable amount of time while it processed the image. I have addressed this issue by reducing the size of the image before processing, but ultimately I will also need to run the OCR function on a thread other than the UI thread as it is still causing some latency.
The next step I am going to attempt is to improve the accuracy of the application as the current result accuracy is quite low.

## Blog Entry 3
I have discovered a set of tools that will hopefully help me with the image processing aspect of app, OpenCV. I have been experimenting with some of their tools and managed to build a program in java that finds the finds the 4 corners of a receipt and rotates it, and enhances the image accordingly. I have been having issues with images tilted to the right but I seem to be on the right track.
Once I have gotten used to these tools I plan to implement them in my application to prepare images prior to running tesseract.

## Blog Entry 4
I have been experimenting with OpenCVs Canny edge detection tool and have had success with it once calibrated correctly. Below is an example receipt to demonstrate how it functions as of now.

### Source image
![source receipt image](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4.jpg)

First we need to prepare the image so edge detection will be more accurate.

We convert the source image to greyscale:
```java
Imgproc.cvtColor(rgbImage, imageGrey, Imgproc.COLOR_BGR2GRAY);
```

resize it to 30% of its original size
```java
Size sz = new Size((imageGray.width() * 30) / 100, (imageGray.height() * 30) / 100);
Imgproc.resize(imageGrey, imageGrey, sz);
```

apply gaussian blur to image
```java
int gBlurSize = 5;
Imgproc.GaussianBlur(imageGrey, imageGrey, new Size(gBlurSize, gBlurSize), 0);
```

Apply erosion and dilation to image
```java
int erosion_size = 5;
Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erosion_size, erosion_size));
Imgproc.erode(imageGrey, imageGrey, element);

int dilation_size = 5;
Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilation_size, dilation_size));
Imgproc.dilate(imageGrey, imageGrey, element1);
```

We then apply canny edge detection
```java
Imgproc.Canny(imageGrey, imageCny, 75, 200, 3, true);
```

Below is the processed image:
![Image with canny edge detection applied](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_canny.jpg)


## Blog Entry 5
After some testing with various receipts, the values previously used for canny edge detections first and second threshold as well as the aperture size value needed was changed as the previous values did not provide accurate results with lower lighting conditions.

The current settings used for canny edge detection are as follows:
```java
Imgproc.Canny(imageGrey, imageCny, 50, 140, 5, true);
```
Below is the image with the updated canny settings applied:
![Image with updated canny edge detection applied](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_canny2.jpg)


The next step was to detect the largest contour in the image. Once edge detection has been applied correctly finding the largest contour is not a difficult task. We just find the area of each contour and compare it to the previous area, if area of the current contour is larger we then hold that as the largest contour and compare it the next contour area. For debugging purposes I have drawn the largest contour onto the source image. This can be seen below:

![Source image with largest contour visualised. ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_contour.jpg)

For debugging purposes I have added coloured points to the image to visualise where each point is being detected. I also colour coded each point so I can differentiate them from each other.

P1 = white
P2 = blue
P3 = red
P4 = green

Below is the image with the points applied.
![Image with circled points applied to image](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_circled_points.jpg)

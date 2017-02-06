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
### Below is the image with the updated canny settings applied:
![Image with updated canny edge detection applied](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_canny2.jpg)


The next step was to detect the largest contour in the image. Once edge detection has been applied correctly finding the largest contour is not a difficult task. We just find the area of each contour and compare it to the previous area, if area of the current contour is larger we then hold that as the largest contour and compare it the next contour area. For debugging purposes I have drawn the largest contour onto the source image. This can be seen below:

![Source image with largest contour visualised. ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_contour.jpg)


The next step was to find each corner of the receipt. For debugging purposes I have added coloured points to the image to visualise where each point is being detected. I also colour coded each point so I can differentiate them from each other.

P1 = white
P2 = blue
P3 = red
P4 = green

### Below is the image with the points applied.
![Image with circled points applied to image](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_circled_points.jpg)

The final step is to extract the receipt from the rest of the image and rotate it accordingly. The final results are as follows:

![Receipt image extracted and rotated accordingly ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_outputtest.jpg)



## Blog Entry 6 - Image cleaning

The next step to clean the image to make it easier for the OCR software to read.
I achieved this by removing first passing the processed the rotated and cropped receipt image to the function.
I then convert the image to greyscale:
```java
Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGR2GRAY);
```
I remove noise from the image:
```java
Photo.fastNlMeansDenoising(srcImage, srcImage);
```
I then apply an adaptive gaussian c threshold to the image. I chose an adaptive threshold as it balances out darker and brighter areas of the image.
```java
Imgproc.adaptiveThreshold(srcImage,srcImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 55, 2);
```

This image cleaning operation results in the following image:
![Receipt after being processed through image cleaning function ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_output_greyscale.jpg)


## Blog Entry 7 - Image cleaning 2: Artifact removal

The next step was to remove any artifacts that were left in the image after the cleaning in the previous step.
To do this I first shrink the image to 40% of its original size.
```java
Size sz = new Size((srcImage.width() * 40) / 100, (srcImage.height() * 40) / 100);
Imgproc.resize(srcImage, rgbImg, sz);
```

A series of morphological transformations are then applied along with OTSU threshold to improve accuracy when locating areas of text in the image.
```java
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
```
Below is the resulting image.
![Receipt after being processed through morphological transformations ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_check3.jpg)

We then set conditions to create bounding boxes around text and use these boxes as a mask. We only keep anything detected within this boxes. Using this technique I was able to remove a majority of artifacts from images while keeping text.

```java
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
    rectangle(mask2, rect.br() , new Point( rect.br().x-rect.width ,rect.br().y-rect.height),  new Scalar(255, 255, 255),Core.FILLED);
  }
}
```

The example results of this of the bounding boxes can been seen below:
![Receipt after being processed through morphological transformations ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_check6.jpg)

The masked areas are then added to a new white image the same size as the source image.
```java
Mat imageROI = new Mat(srcImage.size(), CV_8UC3);
imageROI.setTo(new Scalar(255,255,255));

sz = new Size(srcImage.width(), srcImage.height());
Imgproc.resize(mask2, mask2, sz);

srcImage.copyTo(imageROI, mask2);
```

Here is the receipt image after being processed through this function:
![Receipt image after being processed through artifact removal function ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/receipt4_ROI.jpg)

It is not perfect, but after comparing the receipt image before and after, a significant improvement can be seen.

![Receipt image before and after artifact removal function applied ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/beforeandafter.jpg)

Here is another example:
![Another example of a receipt image before and after artifact removal function applied ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/beforeandafter2.jpg)

## Blog Entry 8 - User Login
I have added user sign up and login to my application. I have used Amazon Web Services Mobile Hub to host the database that will store the users login credentials. I allowed users to sign up through email and password or using Facebook by taking advantage of the Facebook SDK to authenticate users. This uses Facebook lite which means the user does not need to have Facebook app installed to authenticate. The IdentityManager class within the application keeps track of the current sign-in provider and is responsible for caching credentials. This is in preparation to configure a database that will store the users receipt data. I plan on using Amazons NoSQL database to store this data.

## Blog Entry 9 - App Layout
I have configured the fundamental app layout. I have added a navigation drawer to easily access each of the applications main features quickly and efficiently.
Below is a screenshot of the navigation drawer within the application.
![Receipt image before and after artifact removal function applied ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/Nav_Drawer.png)

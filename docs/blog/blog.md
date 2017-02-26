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

## Blog entry 10 - Application crash
The application has been inconsistently crashing while the ReceiptCaptureActivity makes a call to the ReceiptScanner class which processes the image for more accurate OCR. The error is as follows:
```
FATAL EXCEPTION: main
         Process: com.finalyearproject.dan.ocraccountingapp, PID: 24575
         java.lang.RuntimeException: Unable to start activity ComponentInfo{com.finalyearproject.dan.ocraccountingapp/com.finalyearproject.dan.ocraccountingapp.CalendarActivity}: java.lang.NullPointerException: Attempt to invoke virtual method 'com.finalyearproject.dan.ocraccountingapp.mobile.user.IdentityManager com.finalyearproject.dan.ocraccountingapp.mobile.AWSMobileClient.getIdentityManager()' on a null object reference
         at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2434)
         at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2494)
         at android.app.ActivityThread.access$900(ActivityThread.java:157)
         at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1356)
         at android.os.Handler.dispatchMessage(Handler.java:102)
         at android.os.Looper.loop(Looper.java:148)
         at android.app.ActivityThread.main(ActivityThread.java:5527)
         at java.lang.reflect.Method.invoke(Native Method)
         at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:730)
         at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:620)
        Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'com.finalyearproject.dan.ocraccountingapp.mobile.user.IdentityManager com.finalyearproject.dan.ocraccountingapp.mobile.AWSMobileClient.getIdentityManager()' on a null object reference         at com.finalyearproject.dan.ocraccountingapp.nav.NavDrawerInstaller.getUserName(NavDrawerInstaller.java:125)
        at com.finalyearproject.dan.ocraccountingapp.nav.NavDrawerInstaller.installOnActivity(NavDrawerInstaller.java:43)
        at com.finalyearproject.dan.ocraccountingapp.CalendarActivity.onCreate(CalendarActivity.java:21)
        at android.app.Activity.performCreate(Activity.java:6272)
        at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1108)
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2387)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2494) 
        at android.app.ActivityThread.access$900(ActivityThread.java:157) 
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1356) 
        at android.os.Handler.dispatchMessage(Handler.java:102) 
        at android.os.Looper.loop(Looper.java:148) 
        at android.app.ActivityThread.main(ActivityThread.java:5527) 
        at java.lang.reflect.Method.invoke(Native Method) 
        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:730) 
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:620) 

```
I have discovered that the reason for the error is that Android calls the "onCreate" method before loading our OpenCV library. I used an Async Initialization of OpenCV using OpenCVManager and BaseLoaderCallback. Within BaseLoaderCallback I have declared my new Mat. The error has now been resolved and the app does not crash.


## Blog entry 10 - Cloud File Storage
Using Amazon Simple Storage Service I have added online storage for users to store receipt images. I had to make a decision as to whether I should store the image data in a database or on a filesystem to be referenced in the database. I have weighed out the pros and cons and I have decided that in this case I would store the image data on a filesystem to be referenced in the database. Database storage tends to be more expensive than filesystem storage, and images can be easily cached when stored on the file system. My next main action is to configure a database that will store receipt metadata for each user.

## Blog entry 11 - NoSQL database
I have set up and configured Amazons DynamoDB database with my application.
The application queries the database by user id and by date to display the receipt data for each week. I hope to later improve this and allow the user to select week, month and year views to display receipts. The next step is to add a view pager to my application to easily maneuver between weeks to display the receipts corresponding to the specific time.

## Blog entry 12 - ViewPager
I have added a view pager to the application and within each page it queries the database to retrieve the receipt data corresponding to the week the user is currently viewing. From within the view pager the user can click on any of the receipts to display a context menu with options allowing the user to update the receipt information, view the captured receipt image or delete the receipt. The application caches the receipt image so when the user clicks view image it will check if the receipt is present and if not it will download it from the users cloud storage.

Below is an screenshot of the view pager.
![view pager|small](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/viewpager.png | width=100)

## Blog entry 13 - ArrayList adapter issue
I am having an issue with the ArrayList and adapter that I am using to keep track of the queried entries and display them. The issue occurs when I capture an image and then try to go and view the entry in the list. The entry is there but when I click on it to display the context menu and try to view, delete, or update it the application crashes with this error:
```
FATAL EXCEPTION: main
                   Process: com.finalyearproject.dan.ocraccountingapp, PID: 29330
                   java.lang.IndexOutOfBoundsException: Invalid index 4, size is 4
                   at java.util.ArrayList.throwIndexOutOfBoundsException(ArrayList.java:255)
                   at java.util.ArrayList.get(ArrayList.java:308)
                   at android.widget.ArrayAdapter.getItem(ArrayAdapter.java:344)
                   at com.finalyearproject.dan.ocraccountingapp.FragmentContent.viewItem(FragmentContent.java:683)
                   at com.finalyearproject.dan.ocraccountingapp.FragmentContent.onContextItemSelected(FragmentContent.java:747)
                   at android.support.v4.app.Fragment.performContextItemSelected(Fragment.java:2227)
                   at android.support.v4.app.FragmentManagerImpl.dispatchContextItemSelected(FragmentManager.java:2309)
                   at android.support.v4.app.FragmentController.dispatchContextItemSelected(FragmentController.java:366)
                   at android.support.v4.app.FragmentActivity.onMenuItemSelected(FragmentActivity.java:417)
                   at android.support.v7.app.AppCompatActivity.onMenuItemSelected(AppCompatActivity.java:198)
                   at android.support.v7.view.WindowCallbackWrapper.onMenuItemSelected(WindowCallbackWrapper.java:107)
                   at android.support.v7.view.WindowCallbackWrapper.onMenuItemSelected(WindowCallbackWrapper.java:107)
                   at com.android.internal.policy.PhoneWindow$DialogMenuCallback.onMenuItemSelected(PhoneWindow.java:5052)
                   at com.android.internal.view.menu.MenuBuilder.dispatchMenuItemSelected(MenuBuilder.java:761)
                   at com.android.internal.view.menu.MenuItemImpl.invoke(MenuItemImpl.java:152)
                   at com.android.internal.view.menu.MenuBuilder.performItemAction(MenuBuilder.java:904)
                   at com.android.internal.view.menu.MenuBuilder.performItemAction(MenuBuilder.java:894)
                   at com.android.internal.view.menu.MenuDialogHelper.onClick(MenuDialogHelper.java:182)
                   at com.android.internal.app.AlertController$AlertParams$3.onItemClick(AlertController.java:1108)
                   at android.widget.AdapterView.performItemClick(AdapterView.java:310)
                   at android.widget.AbsListView.performItemClick(AbsListView.java:1188)
                   at android.widget.AbsListView$PerformClick.run(AbsListView.java:3100)
                   at android.widget.AbsListView$3.run(AbsListView.java:4091)
                   at android.os.Handler.handleCallback(Handler.java:739)
                   at android.os.Handler.dispatchMessage(Handler.java:95)
                   at android.os.Looper.loop(Looper.java:148)
                   at android.app.ActivityThread.main(ActivityThread.java:5527)
                   at java.lang.reflect.Method.invoke(Native Method)
                   at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:730)
                   at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:620)
```
I understand that it seems the arraylist adapter doesn't seem to have been notified correctly that there was an update in the list and when I try to view the item it creates an out of bounds error. I have worked on resolving this error for two days and I have discovered that it does update but when the context menu calls the adapter it seems to find an older instance of the adapter before it was updated. I have spent too much time on this single issue and I have opted for a less elegant option for the time being and will try to return to this issue at a later date.

The workaround:
I have noticed that after every application crash when I reopened the application and attempted to recreate the crash it would display the receipt image correctly with no error. In my logs I also noticed that the array list had updated correctly. Using this information I decided to force the application to recreate the activity after the user captures a receipt image and clicks save. This resolved the issue but I am not entirely happy about this solution.

## Blog entry 14 - Login functionality update
I have updated the UI layout of the login activity as well as adding validation checks to the sign up activity.
The checks make sure that the user has:
* At least 3 characters in their name.
* Their email is in the correct format.
* Their password is greater than 8 characters and that it contains an uppercase, lowercase and digit.
* Their re-entered password matches the entered password.

```java
public boolean validate() {
       boolean valid = true;

       String name = ViewHelper.getStringValue(this, R.id.signup_given_name);
       String email = ViewHelper.getStringValue(this, R.id.signup_email);
       String password = ViewHelper.getStringValue(this, R.id.signup_password);
       String reEnterPassword = ViewHelper.getStringValue(this, R.id.input_reEnterPassword);

       boolean hasUppercase = !password.equals(password.toLowerCase());
       boolean hasLowercase = !password.equals(password.toUpperCase());

       if (name.isEmpty() || name.length() < 3) {
           _nameText.setError("at least 3 characters");
           valid = false;
       } else {
           _nameText.setError(null);
       }

       if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
           _emailText.setError("enter a valid email address");
           valid = false;
       } else {
           _emailText.setError(null);
       }


       if (password.isEmpty() || password.length() < 8) {
           _passwordText.setError("at least 8 characters");
           valid = false;
       }
       if(!hasLowercase || !hasUppercase || !(password.matches(".*\\d.*"))) {
           _passwordText.setError("Password must contain uppcase, lowercase and numeric characters");
           valid = false;
       } else {
           _passwordText.setError(null);
       }

       if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
           _reEnterPasswordText.setError("Password Do not match");
           valid = false;
       } else {
           _reEnterPasswordText.setError(null);
       }

       return valid;
   }
```

Below is an example of the checks being triggered.
![validation checks triggered in signup activity ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/signup_ui_validation.png)

And Below is the updated login UI layout.
![UI layout](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/login_ui_updated.png)

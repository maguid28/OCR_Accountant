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
![view pager|small](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/viewpager.png)

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
        } else {
            _passwordText.setError(null);
        }
        if(!hasLowercase || !hasUppercase || !(password.matches(".*\\d.*"))) {
            _passwordText.setError("Password must contain uppcase, lowercase and numeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty()) {
            _reEnterPasswordText.setError("Field cannot be empty");
            valid = false;
        }
        else if(reEnterPassword.length() < 4) {
            _reEnterPasswordText.setError("Password must be greater than 4 characters");
            valid = false;
        }
        else if(!(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Passwords do not match");
            valid = false;
        }
        else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }
```

Below is an example of the checks being triggered.
![validation checks triggered in signup activity ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/signup_ui_validation.png)

And Below is the updated login UI layout.
![UI layout](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/login_ui_updated.png)



## Blog entry 15 - ViewPager changed
Due to my ongoing issues with the view pager and the dynamic list view adapter used to display receipts, I have decided to go for a different approach to displaying the receipts. I have still kept the view pager but decided to go for a more simplistic method by displaying the months of the year within the view pager. This method has resolved the issues I have had with the list view adapter not updating correctly.
The image below is a screenshot of the current updated view.
![Cal display image ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/cal_display.jpg)

## Blog entry 16 - Custom camera
I have added a custom camera to the application which forces the flash on to allow clearer captures and more accurate processing of the receipt as it makes it much easier to differentiate the receipt image from the background. The camera also has an preview activity that allows the user to process the image within the activity and allows the user to easily recapture the images if it does not process correctly.
The camera manager also decides which version of the android camera API to use depending on the OS of the device. Older devices will need to use the deprecated Camera API, but newer devices will be able to take advantage of the newer Camera2 API.
![Image capture process ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/image_capture_process.jpg)

## Blog entry 17 - OCR 1: Improving image quality
I have now moved on to OCR side of the application now. I am currently performing tests to improve the accuracy of read text.
The image I will be performing these tests on is below. Once I get high accuracy results with this image I will test others and show results.
![OCR Test image ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/ocr_test_receipt.jpg)

At the moment tesseract is generating the following from the image.
DEFAULT:

width of image: 1976

height of image: 3076

```
									 Tower Records
                   40-42 Lower 0A00nnel St
                   Dublin
                   RepubTic Of Ireland
                   IEI 0035318786680
                   EmpTuyee ID 222
                   Order ID 3431966
                   TiTT Number 207 H
                   TOWER RECORDS DAWSON STREET LT
                   PLEASE NOTE AS PER TOCS LOYALTY CARDS
                   ARE NOT VALID FIR STAMPS OR REDEMPTION
                   FOR THE MONTH O DECEMBER
                   FOLLOW US ON TWITTER OTOWERDUBLIN
                   10/03/2017 15318
                   DOCTOR STRANGET BR €19.99
                   Tota 1 a 1 9 99
                   CLugtomeLViBmem
                   Dash €20,00
```
Tesseracts accuracy drops dramatically on images with pixel densities of lower than 300dpi. So the first test was to scale it up x2 the size of the original image and see if this improves accuracy.

Resized to x2 size of original image
width of p-image: 3618
height of p-image: 5486

```java
Size size2 = new Size((imageROI.width() * 2), (imageROI.height() * 2));  Mat imageROI2 = new Mat(srcImage.size(), CV_8UC3); Imgproc.resize(imageROI, imageROI2, size2);
```

The result is:
```
									   Tower Records
                     40-42 Lower OTConnel St
                     Dublin
                     RepubTic Of Ireland
                     TeT 0035318786680
                     Employee ID 222
                     Order ID 3431966
                     Till Number 207
                     TONER RECORDS DAWSON STREET V
                     PLEASE NOTE AS PER T&CS LOYALTY CARDS
                     ARE NOT VALID FOR STAMPS 0R REDEMPTION
                     FOR THE MONTH OF DECEMBER
                     FOLLOW US ON TWITTER OTDNERDUBLIN
                     10/03/2011L15048
                     DOCTOR STRANGEIV BR €19.99
                     Tota1 &€19.99
                     EDEIQEQCEPEYDEDI
                     Cash €20.00
```
There has been some improvement.
I have further increased the image size to x2.5 original size, and x3 of the original with little or no improvement in the text recognition accuracy, with larger images also slowing down the OCR task significantly. I've decided to settle on an a scaled image size of x2 original size.
I have followed the guidelines on the official GitHub page on improving image quality which mentions the following steps to improve Tesseracts accuracy:

#### Rescaling the image:
I have performed this by scaling the image by x2, mentioned above.

#### Binarisation:
I have converted the image to black and white and performed adaptive gaussian c threshold to the image to balance out the darker and brighter areas of the image.

#### Noise Removal:
I have performed image denoising using a Non-local Means Denoising algorithm located in the opencv library.
```java
Photo.fastNlMeansDenoising(srcImage, dstImage);
```

#### Rotate and Deskew:
I have rotated and deskewed the image by locating the four corners of the receipt in the image and rotating accordingly.

#### Border Removal:
This step is present in Tesseracts document as it mentions that "Scanned pages often have dark borders around them. These can be erroneously picked up as extra characters, especially if they vary in shape and gradation". My issue went slightly further than just borders as receipts may have creases or marks on them that may show up as unwanted artifacts in the processed receipt image.
I have tackled this issue by identifying areas of text while removing all other areas that were not identified as text (See Blog entry #7).


## Blog entry 18 - OCR 2: Training Tesseract
My next step is to train Tesseract to better identify receipt text as the sample english trained data on Tesseracts official GitHub page is tailored for sentences with full blocks of text.

The first step that I performed to train tesseract to more accurately recognize receipt text was to capture numerous receipts and clean them which I used my image processing algorithm in my application to accomplish.

I then used Adobe PhotoShop to clean any artifacts or noise still present in the image. Once there was only text left in the image I then extracted the text and put it all on one line. This step was necessary due to line height issues that are thrown later in the process if this step is not completed.
Here is a snippet of the final image.

![png snippet ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/eng.rec.exp0.png)

I did this for a total of 7 different receipts and ended up with 17 processed files, I had to split the files up as file sizes got too large and caused issues when running the tesseract training tools. Tiff format was recommended for these files but caused errors when I used it so instead I used png format which resulted in clearer images and no errors.

The next step was to create box files for each of the png files I had created. I did this by running:

```
tesseract eng.rec.expX.png eng.rec.expX batch.nochop makebox
```
where I replaced the 'X' with its respective number between 0-16. These box files contain a line for each character in the image in the format

P 228 22 243 57 0

Where the leftmost character is the character that tesseract assumes the character is  with the rest of the digits being coordinates on the image along with box heights and widths. I used a java program call jTessBoxEditor to correct any of the mistakes that were made by Tesseract. I then saved and proceeded to do this for all 17 files.

The next step was to create training files for each of the image-box pairs.
I performed this by running:

```
tesseract eng.rec.expX.png eng.rec.expX box.train
```
Once this was completed for all image files I had to generate the unicharset file from the box files:

```
unicharset_extractor eng.rec.exp0.box eng.rec.exp1.box eng.rec.exp2.box eng.rec.exp3.box eng.rec.exp4.box eng.rec.exp5.box eng.rec.exp6.box eng.rec.exp7.box eng.rec.exp8.box eng.rec.exp9.box eng.rec.exp10.box eng.rec.exp11.box eng.rec.exp12.box eng.rec.exp13.box eng.rec.exp14.box eng.rec.exp15.box eng.rec.exp16.box
```
and create the font properties file.

The next step is to perform shape clustering but from research I have performed shape clustering is not always beneficial for languages other than Indic languages. But as I am not performing standard OCR on blocks of text I will test shape clustering to see the outcome:

```
shapeclustering -F eng.font_properties -U output_unicharset  eng.rec.exp0.tr eng.rec.exp1.tr eng.rec.exp2.tr eng.rec.exp3.tr eng.rec.exp4.tr eng.rec.exp5.tr eng.rec.exp6.tr eng.rec.exp7.tr eng.rec.exp8.tr eng.rec.exp9.tr eng.rec.exp10.tr eng.rec.exp11.tr eng.rec.exp12.tr eng.rec.exp13.tr eng.rec.exp14.tr eng.rec.exp15.tr eng.rec.exp16.tr

```

My final step is to run mftraining which generates the shape prototypes file, shapetable, and pffmtable which is the number of expected features for each character and cntraining which generates the character normalization sensitivity prototypes by running:

```
mftraining -F eng.font_properties -U output_unicharset  eng.rec.exp0.tr eng.rec.exp1.tr eng.rec.exp2.tr eng.rec.exp3.tr eng.rec.exp4.tr eng.rec.exp5.tr eng.rec.exp6.tr eng.rec.exp7.tr eng.rec.exp8.tr eng.rec.exp9.tr eng.rec.exp10.tr eng.rec.exp11.tr eng.rec.exp12.tr eng.rec.exp13.tr eng.rec.exp14.tr eng.rec.exp15.tr eng.rec.exp16.tr

cntraining -F eng.font_properties -U output_unicharset  eng.rec.exp0.tr eng.rec.exp1.tr eng.rec.exp2.tr eng.rec.exp3.tr eng.rec.exp4.tr eng.rec.exp5.tr eng.rec.exp6.tr eng.rec.exp7.tr eng.rec.exp8.tr eng.rec.exp9.tr eng.rec.exp10.tr eng.rec.exp11.tr eng.rec.exp12.tr eng.rec.exp13.tr eng.rec.exp14.tr eng.rec.exp15.tr eng.rec.exp16.tr

```

I then compile the data down into the trained data file ready for use in my application by running:

```
combine_tessdata eng.
```

## Blog entry 19 - OCR 3: Training results

Below are the ocr results of both my trained data files on the same image, Shape clustering is applied on the right result, while the same process but without the shape clustering step is applied in the left result.

#### Case 1
![comparing training results 1 ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/clusteringVSnoclustering1.jpg)

#### Case 2
![comparing training results 2 ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/clusteringVSnoclustering2.jpg)

#### Case 3
![comparing training results 3 ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/clusteringVSnoclustering3.jpg)

There does not seem to be much difference and both balance each other out with errors in different areas.
To stick by the documentation I will not apply shape clustering to my training data.


## Blog entry 20 - Word correction
My next step to improve read accuracy is to apply a word correction algorithm to correct misinterpreted words e.g. Tota1 or lotal will be autocorrected to total.

The method I used for this step is a modification of the Spell checker program GitHub user shyam057cs has built. It is licensed under General Public License version 3.
Here is the link to the spell checker: https://github.com/shyam057cs/Spell-Checker

I have played around with both versions of his Spell-Checker and found his hash table implementation works for my case much more accurately than his bloomfilter implementation.

Originally, the program takes a text file as input reads it and prints out suggestions for words that are not in its dictionary file.

I have modified it to:
* replace words in the text file with the suggested word and write to an output text file.
* Words that are not in the dictionary file and have no suggestion are removed.
* Prices and dates are ignored as suggestions would not work correctly and removal is not an option.

The dictionary file and file used to calculate word probability also have to be modified to account for names and other words common to receipts that would not be present.

The results are shown below:

![word correction algorithm applied ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/wordcorrection.jpg)

There has been a significant improvement after this algorithm has been applied. I am still fine tuning this at the moment to further increase accuracy.

## Blog entry 21 - Text Extraction
App performance suffered from the word correction algorithm in the previous blog entry. Instead I have now built an algorithm that clears anything that is not considered a word from the extracted text and only applies word correction to the receipt line that the title is present on. I have made a dictionary file of list of the most common irish retailers and restaurants to reference against and correct accordingly. It then places this corrected title in the "title" edittext field. The date and total are extracted from the receipt by similar algorithms, The code is below:

### Date:
```java
public String getDate(String dirPath) {
        String date = "";
        String potentials = "";

        try {
            FileReader fr = new FileReader(dirPath);
            BufferedReader br = new BufferedReader(fr);

            while (br.ready()) {
                String s = br.readLine().toLowerCase();;

                String[] words = s.split("\\s");

                // search for the line that contains information about date
                for (int x=0; x<words.length; x++) {
                    if(words[x].matches("[^A-Z]+/[0-9a-z]+/[^A-Z]+")) {

                        String dateOnly = words[x];

                        if(dateOnly.contains("/")) {
                            StringBuilder sb = new StringBuilder(dateOnly);
                            sb.indexOf("/");
                            sb.lastIndexOf("/");
                            System.out.println("first index: " + sb.indexOf("/"));
                            System.out.println("last index: " + sb.lastIndexOf("/"));

                            // remove any extra characters that may have been added on to the end
                            if (dateOnly.length() > sb.lastIndexOf("/") + 5) {
                                System.out.println(dateOnly.substring(0, sb.lastIndexOf("/") + 5));
                                dateOnly = dateOnly.substring(0, sb.lastIndexOf("/") + 5);
                            }

                            // remove extra characters added on the the start of the date
                            if(sb.indexOf("/") != 2) {
                                // find the difference between the correct index and the actual index
                                int temp = sb.indexOf("/") - 2;
                                // remove the difference from the start of the string
                                System.out.println(dateOnly.substring(temp));
                                dateOnly = dateOnly.substring(temp);
                            }
                        }


                        potentials +=dateOnly;
                    }
                }
                potentials += "\r\n";
            }
            System.out.println(potentials);

            br.close();

            String[] datePotentials = potentials.split("\\r?\\n");
            for(int i = 0; i<datePotentials.length;i++){
                if(datePotentials[i].matches("[0-9][0-9]/[0-9][0-9]/[0-9]{4}")) {
                    date = datePotentials[i];
                }
            }

            System.out.println("Date: " + date);

        } catch (IOException i) {
            i.printStackTrace();
        }

        // if date is not found, default to todays day
        if(date.equals("")) {
            date = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        }

        return date;
    }

```

### Total:
``` java
public String getTotal(String dirPath) {
        double total = 0;

        // last resort will contain all words that contain '€'
        String lastResort = "";

        try {
            FileReader fr = new FileReader(dirPath);
            BufferedReader br = new BufferedReader(fr);

            String cleanedText = "";


            while (br.ready()) {
                String s = br.readLine().toLowerCase();
                String[] words = s.split("\\s");

                // search for the line that contains information about total
                for (int x=0; x<words.length; x++) {
                    if(s.contains("total") || s.contains("tot") || s.contains("otal") || s.contains("sale") ) { // || s.contains("tal") || s.matches("\\d[t|l][a-z]{3}[l|1|i]")

                        String extractedPrice = findTotal(words[x]);
                        cleanedText += extractedPrice + " ";

                    }
                    // "tal" is more common than "tot" and "otal", needs to be more specific
                    else if(words[x].matches("[a-z][a-z]tal")) {
                        String extractedPrice = findTotal(words[x+1]);
                        cleanedText += extractedPrice + " ";
                    }
                    else if(words[x].contains("€")){
                        lastResort += words[x] + " ";
                    }
                }
                cleanedText += "\r\n";
            }

            br.close();
            System.out.println("TOTAL: " + cleanedText);

            String lines[] = cleanedText.split("\\r?\\n");
            //loop through lines until text is found

            double[] potentialTotals = new double[lines.length];
            Arrays.fill(potentialTotals,0);

            for(int i = 0; i<lines.length;i++){
                if(lines[i].matches("(\\s)*[0-9]+\\.[0-9][0-9](\\s)*")) {
                    potentialTotals[i] = Double.parseDouble(lines[i]);
                }
            }

            total = 0;
            for(int i=0; i<potentialTotals.length; i++){
                if (potentialTotals[i] > total) {
                    total = potentialTotals[i];
                }
            }

        } catch (IOException i) {
            i.printStackTrace();
        }

        //if no value was found for total yet, check lastResort
        if(total==0) {
            // store every word in lastResort in an array
            String[] potentials = lastResort.split(" ");
            double temp = 0;
            double max = 0;

            for(int i=0; i<potentials.length; i++) {
                String extractedPrice = findTotal(potentials[i]);
                System.out.println(extractedPrice);
                if(extractedPrice.matches("(\\s)*[0-9]+\\.[0-9][0-9](\\s)*")) {
                    temp = Double.parseDouble(extractedPrice);
                    if(temp>max && temp <500){
                        max = temp;
                    }
                }
            }
            return String.valueOf(max);
        }

        String totalString = String.valueOf(total);
        //add an extra 0 if the total ends in a 0 to achieve the format €xx.xx
        if(totalString.substring(totalString.length()-1).equals("0")) {
            totalString = totalString + "0";
        }

        return totalString;
    }
```

And finally the category is selected by searching the extracted receipt text for words exclusively present on receipts of that category. For example, "fresh" would most likely only ever occur on food receipts, or "shoe" would most likely only occur on clothing receipts.

## Blog entry 21 - Improving performance
The application performs the image processing task and the OCR task which can both take an uncomfortable amount of time to complete, depending on the processing power of the device. For this reason I have modified my application to execute the img processing code as soon as the image is captured instead of waiting for the users button click cue. This reduces the wait time significantly. I have done the same with the OCR task.

## Blog entry 22 - Charts and Statistics
I have added a fragment that displays charts based on the users spending patterns. The charts are part of the MPAndroidChart library accessed here: https://github.com/PhilJay/MPAndroidChart.

Below are images of the 4 charts:

![comparing training results 1 ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/charts.jpg)


## Blog entry 23 - User testing 1
At the end of my first round of user tests I have discovered the following issues:
Displaying "passwords do not match" notification falsely.
Not allowing users to activate account.
Case sensitive email addresses.
Receipts being displayed mirrored when captured.
Some fragment overlaying issues.

## Blog entry 24 - Real-time image processing
Due to the lower accuracy of results when capturing an image and then performing image processing on the still image I have modified the camera activity to perform some of the image processing in real time so that the user can be sure that the receipt will be captured successfully. In the previous implementation a still image would be captured, and once captured I would search for the 4 corners of the image and rotate the image accordingly. This worked approximately 70% of the time as sometimes the contrast between the receipt and the background was too indistinguishable by the application and would not work correctly or if the 4 corners were not well defined this would also cause the process to fail and would result in the user trying again while getting frustrated.
In my current implementation the camera captures each frame, searches for the largest contour in the image, which should be outline of the receipt and then draws a bounding box around the receipt on the frame and passes the frame to the camera preview window. when the user clicks the capture button only the image within the bounding box is captured and the receipt cleaning process is executed. Once this process has completed, the user will be shown a preview of the cleaned image which user then has the option to recapture if they are not satisfied with the result, close the activity, or proceed to the receipt edit activity.
The images below demonstrate this process.

![Real time image processing ](https://gitlab.computing.dcu.ie/maguid28/2017-ca400-maguid28/raw/master/docs/blog/images/realtime.jpg)

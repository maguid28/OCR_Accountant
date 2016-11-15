# Blog: OCR Personal Accounting Application

**Daniel Maguire**

## Blog Entry 1
Today I have started to learn how to use tesseract and tess-two within my application. I am still unsure which API I will use as I am unsure if the additional functions of tess-two will be useful to me.

## Blog Entry 2
During the last week I have tried to implement the tesseract API in my application.
My first issue that I came to was when images captured from the device camera were pulled into the application they arrived rotated. I solved this by collecting EXIF data from the image and rotating it accordingly.
I have discovered due to the size of the image being captured it was causing the application to halt for a considerable amount of time while it processed the image. I have addressed this issue by reducing the size of the image before processing, but ultimately I will also need to run the OCR function on a thread other than the UI thread as it is still causing some latency.
The next step I am going to attempt is to improve the accuracy of the application as the current result accuracy is quite low.

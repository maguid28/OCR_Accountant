# OCR Accounting application

## General area covered by the project
The purpose of this project is to develop an application that will allow the user to capture and store receipt data in an online database, while providing an easy-to-use platform to access this data. The application provides the user with insight into their purchase habits and will allow the user to view their purchase history through various representations. The aim is to provide the end user with a tool that will allow them to educate themselves in their own spending affairs as well as providing some insight in the form of statistical representations of their receipt data.
The application takes
advantage of tesseracts optical character recognition technology, while applying error
correcting algorithms for smoother input to capture obscured images.
This application
is designed for android mobile devices. It will run on Android devices running
Android 5.0 or higher, and works in conjunction with Googles Tesseract API as well
as the OpenCV API, to provide a simple and accurate experience for the user while
capturing receipt data. The user can then save their captured receipt securely to their
unique user account hosted by Amazon Web Services. The application’s secondary
feature is to allow the user to view statistical data on their spending habits through the
use of various charts provided by the MPAndroidChart library.

## Description


#### Background:
  Found budgeting applications online

  None were very accurate as they required inputting the data manually.

  Had the idea of adding an OCR element to make input quicker and more efficient.

#### Achievements: 
#### What functions it provides, who its users will be
  Processes information on receipts/invoices/tickets

  Uses device camera and Optical Character Recognition with error correcting algorithms to capture obscured text for smoother input – user can capture image data more fluidly without having to worry if capture is perfect.

  Stores information in external SQL database to be manipulated in various ways

  Pulls information from emails for online purchases

  Calendar view: Display purchase information/statistics by day, month or year
  e.g. Amount spent on food on the 6th of August or difference in spending patterns since previous week.

  Aimed at: Men and Women above 18 that would like to control and manage spending more effectively

#### Justification
  Allows users to monitor their spending habits on a day to day basis

  Gain control and manage income more effectively 

         
## Programming Languages to be used
  Java - as it is the main platform for android app development

  XML - for the UI layout

     
## Programming tools to be used
  Android studio

  Image Processor: with error correcting algorithms to capture obscured text for smoother input – user can capture receipt image quicker without having to worry if receipt is

  Database: Online database (SQL Server - rent Amazon server free for 12 months)
       
## Learning Challenges
  Main new technology I will have to learn is the image processing aspect of the project

  Learn how image correcting algorithms function to improve recognition of characters.

## Hardware/Software platform
  Developing the app for Android devices running 4.4 or higher

  Approximately 80% of users use 4.4 or higher

  This allows the app to function on a majority of populations devices while still keeping a majority of features.

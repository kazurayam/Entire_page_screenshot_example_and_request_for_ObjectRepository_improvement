# Entire page screenshot example and Request for ObjectRepository improvment

This is a small [Katalon Studio](https://www.katalon.com/) project for demonstration purpose. This project was developed using Katalon Studio version 5.10.1.

In this project I will report a small problem in the   [com.kms.katalon.core.testobject.ObjectRepository](https://github.com/katalon-studio/katalon-studio-testing-framework/blob/master/Include/scripts/groovy/com/kms/katalon/core/testobject/ObjectRepository.java) class. I will describe the problem, and will show you how I worked around it.

## Taking entire screen shot of a web page - an example

Before talking about problems, I will give you a successful demo of taking screenshot of a web page. You can try the demo.

### How to around the demonstration

1. Download the zip of this project from [Releases](https://github.com/kazurayam/Entire_page_screenshot_example_and_request_for_ObjectRepository_improvement/releases) page. Unzip it. Open it with your local Katalon Studio.
2. Open the test suite `Test Suites/Execute`
3. Run the test suite with any browser you like.
4. The test script will open browser, navigate to [London Stock Exchange: Homepage](https://www.londonstockexchange.com/home/homepage.htm). It will take screen shot and save into `<project dir>/Materials` directory.
5. Open a file `<project dir>/Materials/index.html` with your browser. You will find a PNG file like this:
![londonstockexhange_homepage_plain](docs/images/londonstockexchange_homepage.png)

### Features implemented

This demo has a few notable points.

1. This demo uses [aShot](https://github.com/pazone/ashot), WebDriver Screenshot utility to take screen shots of web pages.
2. This demo takes entire/full page screen shot.
3. This demo makes a directory `<project dir>/Materials`. The PNG image file is located at the path of `<project dir>/Materials/<Test Suite Name>/<Test Suite Timestamp>/<Test Case Name>/Materials\https%3A%2F%2Fwww.londonstockexchange.com%2Fhome%2Fhomepage.htm.png`. This path is resolved automatically by the [`Materials`](https://github.com/kazurayam/Materials) library. The path structure is convenient if you are to run the test (taking screenshots) many times. The artifact files will be stored in structured directory tree.
4. The screenshot has grey-colored rectangles over specified web elements in the target page. This rectangles are useful for intentionally ignoring particular portions when 2 images are compared.
5. This demo shows how to deal with floating banner, or called [Sticky Header on Scroll](https://www.w3schools.com/howto/howto_js_sticky_header.asp). Floating banner is usually a `<div>` with style `position:fixed`. If you do not treat floating banner specially, the banner is repeated in the entire page image which looks unprofessional. This demo shows how to make the banner not to float.

## Request to improve com.kms.katalon.core.testobject.ObjectRepository class

### Problem

### How to fix it

### My interim implementation

I saied ObjectRepository class should be changed -- but how?

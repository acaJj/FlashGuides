# README #

This README contains the instructions needed to download and run FlashGuides on a mobile Android device.

## Whats this repository for anyway? ##
This repository holds our 2018 Capstone project for Sheridan College's Software Development and Network Engineering Program.
The project is a mobile application that focuses on creating quick how-to guides on an Android phone. Users are able to create an account
and from there create a guide on anything they want. They can save guides and return to editing them later, or publish them for others to view.

We use Google Firebase for our back-end, storing all user information and guide data (text and images). On the mobile app we also have a 
small search functionality that allows users to search for published guides by either looking up words in the title, or by user.

To see all published guides in a web browser, [click here](https://flash-guides.firebaseapp.com/)

## How do I get set up? ##

#### Summary of set up ####
Setup is really simple, all you have to do is clone the repo onto your own computer and then open it up in your Android IDE.
We like to use Android Studio so we recommend it using that but if you have your own preference you can use that too. 
Once you've got the android project loaded up all you have to do is connect your phone and deploy the app to it. 

The connections to Firebase and Algolia are already taken care of so you won't have to do anymore setting up, just deploy to your
phone and enjoy your new super amazing guide building app!

#### Dependencies ####
The app connects to Firebase for storage of all data, and Algolia for guide indexing which allows the user to search for published guides.
Within the app, we have set the necessary connections to the both of the services.

The image editor uses Photo Editor SDK, which we were given a free license for the remainder of 2018 so that we could use it to complete
our capstone. The license is set to expire Sunday, December 30 and after that time the image editor will no longer work. We are currently thinking of
ways around this issue that don't cost us too much because we're very poor and don't like paying a lot for things.

#### Deployment instructions ####
* Clone the repo onto your desktop
* Open the project in Android Studio
* Enable developer options for your phone, this lets Android Studio see your phone as an option for deployment 
* Connect your phone to your computer 
* Select run at the top of the IDE and deploy to your phone
* Get started making sweet guides!

### Who do I talk to? ###
For more information please contact either of the main project leaders, Jeffrey Jaca or Chris Aziz, at the following emails:

* jeffreyjjaca@gmail.com
* chrisaziz2112@gmail.com
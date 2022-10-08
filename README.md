<br />
<br />

<h1 align="center">
  OneFitness - Fitness Tracking App
</h1>

<br />

<p align="center">
  <img alt="license" src="https://img.shields.io/github/license/AlphaDaze/word-search-solver">
</p>

<h5 align="center">
  <b>
      Developed using Java.
  </b>
</h5>

<br />

## About

I started this project in November 2019 and completed it in February 2020. The project allows users to keep track over their daily steps, calories, distance, and running/cycling sessions. It also allows the user to set their own goals for the day too!

The app requires a Google account sign-in and lets you sync data such as weight and height as well as allowing real time tracking with Google Maps.

I logged everything I did in a [document](). This app can be recreated using this document file.

### Features

- Track steps, calories, distance in the background
- Start new running and cycling sessions
- View session data such as distance, pace, and calories
- Backup and restore session data
- Set goals for daily steps and calories
- Set weight and height(which are synced with your Google account!)
- Login with Google

<p align="center">
  <img src="https://s4.gifyu.com/images/ezgif.com-gif-makerbd442116522eee39.gif" />
</p>

<!-- DOCUMENTATION -->

## Documentation

This app can be made again by following the development log. This document should also make it easier to add features and debug the app.

## Getting Started

**1. Add keys**

Open src/OneFitness in Android Studio

Add directory to debug.keystore

```
├── src/OneFitness
│         └── build.gradle <–––
└── ...
```

Add Google Maps API key

```
├── src/OneFitness/app/src/debug/res/values/
│                   └── google_maps_api.xml <–––
└── ...
```

**2. Sync gradle**

Open build.gradle in Android Studio and Sync

**3. Run dev build**

Build and run the app!

## Requirements

- [Android Studio](https://developer.android.com/studio)
- [Google Maps Key](https://developers.google.com/maps/documentation/android/signup)
- Andriod SDK 29+

<!-- CONTACT -->

### Creator

[AlphaDaze](https://github.com/AlphaDaze)

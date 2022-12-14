<br />
<p align="center">
  <a href="https://github.com/AlphaDaze/OneFitness/">
    <img src="https://github.com/AlphaDaze/OneFitness/blob/main/images/logo.png?raw=true" alt="OneFitness Red Logo" width="80" height="80">
  </a>
</p>

<h1 align="center">
  OneFitness - Fitness Tracking App
</h1>

<p align="center">
  <img alt="license" src="https://img.shields.io/badge/Java-B9291D">
  <img alt="license" src="https://img.shields.io/github/license/AlphaDaze/word-search-solver">
</p>

## About

I started this project in November 2019 and completed it in February 2020. The project allows users to keep track over their daily steps, calories, distance, and running/cycling sessions. It also allows the user to set their own goals for the day too!

The app requires a Google account sign-in and lets you sync data such as weight and height as well as allowing real time tracking with Google Maps.

I logged everything I did in a [document](https://github.com/AlphaDaze/OneFitness/blob/main/OneFitness%20Build%20Log.pdf). This app can be recreated using this document file.

### Features

- Track steps, calories, distance in the background
- Start new running and cycling sessions
- View session data such as distance, pace, and calories
- Backup and restore session data
- Set goals for daily steps and calories
- Set weight and height(which are synced with your Google account!)
- Login with Google

<p align="center">
  <img src="https://github.com/AlphaDaze/OneFitness/blob/main/images/showcase.gif?raw=true" />
</p>

<!-- DOCUMENTATION -->

## Documentation

This app can be made again by following the [development log](https://github.com/AlphaDaze/OneFitness/blob/main/OneFitness%20Build%20Log.pdf). This document should also make it easier to add features and debug the app.

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

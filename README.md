android-sdk-bootstrapper
========================

[![Build Status](https://travis-ci.org/sensorberg-dev/android-sdk-bootstrapper.svg?branch=master)](https://travis-ci.org/sensorberg-dev/android-sdk-bootstrapper)

This repo contains the bootstrapper for the [Sensorberg Android SDK](https://github.com/sensorberg-dev/android-sdk).

The artifacts are deployed at **https://raw.github.com/sensorberg-dev/android-sdk/mvn-repo**

```
maven {
        url "https://raw.github.com/sensorberg-dev/android-sdk/mvn-repo"
    }
```

#What can you do with this:

##Custom presentation of the Messages:

If you want to present the messages yourself, extend the Bootstrapper and implement your own

   protected void presentBeaconEvent(BeaconEvent beaconEvent)

be aware that **beaconEvent.getAction()** might be null, because there is no action associated with this beacon.

#Release Notes:

##1.0.1

* removed the fixed icon of the presentation configuration, if you want an icon, extend the bootstrapper and write your own void presentBeaconEvent(BeaconEvent beaconEvent) 

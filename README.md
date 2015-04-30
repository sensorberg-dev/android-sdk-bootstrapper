android-sdk-bootstrapper
========================

[![Build Status](https://travis-ci.org/sensorberg-dev/android-sdk-bootstrapper.svg?branch=master)](https://travis-ci.org/sensorberg-dev/android-sdk-bootstrapper)

This repo contains the bootstrapper for the [Sensorberg Android SDK](https://github.com/sensorberg-dev/android-sdk).

The artifacts are deployed at **https://raw.github.com/sensorberg-dev/android-sdk/mvn-repo**

```groovy
maven {
        url "https://raw.github.com/sensorberg-dev/android-sdk/mvn-repo"
}
```

#What can you do with this:

##Custom presentation of the Messages:

If you want to present the messages yourself, extend the Bootstrapper and implement your own
```
protected void presentBeaconEvent(BeaconEvent beaconEvent)
```

be aware that **beaconEvent.getAction()** might be null, because there is no action associated with this beacon.

#Release Notes:

##1.0.1

* removed the fixed icon of the presentation configuration, if you want an icon, extend the bootstrapper and write your own void presentBeaconEvent(BeaconEvent beaconEvent)

 #Release

 Set your release name in the root **build.gradle**. If you want a regular release, leave the **project.ext.RC_VERSION** as an empty string. Update the SDK reference in the **android-sdk-boostrapper/build.gradle** file.

 paste your credentials in the **bintray.properties** file.

 run

 ``` bash
 	./gradlew clean android-sdk-bootstrapper:bintrayUpload
 ```

 If you want to test the procedure, change the **dryRun** variable in the **bintrayUpload.gradle** file to *true* temporarely. The --info flag will give you details if you need them.

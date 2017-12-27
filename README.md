Kotlin Android SafetyNet Sample
===================================

This sample demonstrates the SafetyNet API in Google Play Services. This API tests
whether the device and software have passed Android compatibility testing.
Use the option in the toolbar to make an API request.
The next step is to read and verify the result of the compatibility check. This should be done on
the server side but also done in the client - just to show how it goes. 

For more details, see the guide at https://developer.android.com/training/safetynet/index.html.

Pre-requisites
--------------

- Android SDK 27
- Latest Android Build Tools
- Latest Google Play Services
- Android Support Repository

----------------
## Screenshots

<img src="https://github.com/MaTriXy/SafetyNetSample/blob/master/ScreenShots/ss_1.png?raw=true" width = "264" height = "464"/><img src="https://github.com/MaTriXy/SafetyNetSample/blob/master/ScreenShots/ss_2.png?raw=true" width = "264" height = "464"/><img src="https://github.com/MaTriXy/SafetyNetSample/blob/master/ScreenShots/ss_3.png?raw=true" width = "264" height = "464"/>



Getting Started
---------------


You need to set up an API key for the SafetyNet attestation API and reference it in this project.

Follow the steps in the [SafetyNet Attestation API][add-api-key] guide to set up an API key in the
Google Developers console.

Then, override the configuration in the `gradle.properties` file to set the key. 

This value is used for the call to
<a href="https://developers.google.com/android/reference/com/google/android/gms/safetynet/SafetyNetClient.html#attest(byte[], java.lang.String)">`SafetyNetClient# attest()`</a>.


This project is based on [Google Sample code ][sample-google] and was done for showcasing purpose only. 


[sample-google]: https://github.com/googlesamples/android-play-safetynet
[add-api-key]: https://developer.android.com/training/safetynet/attestation.html#add-api-key

STEPS TO COMPILE AN OFFICIAL RELEASE (for internal use):
- copy strings.xml from values-en to values
- increase version code and version name in the manifest
- increase database version (necessary to circumvent alarm
removal by Android on app update) // done in the ADB class
- export signed package
- upload and test .apk on emulator
- commit code changes to the open-source project
- publish to Android Market
- blog post about changes
- upload .apk to Tag-ToDo-List website
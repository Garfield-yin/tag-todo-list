******************************************************************
*************** Tag ToDo List - License Addition *****************
******************************************************************
*******http://teodorfilimon.com/android/Tag-ToDo-List.com*********
******************************************************************

1. DISTRIBUTION: This program is meant for Android: the official
Android Market or other official distributing systems they might 
come up with. However, if you want, you can distribute it on
another (personal or whatever), i only ask that you credit the
Tag-ToDo-List project with at least a visible link to either one
of these web pages:

http://code.google.com/p/tag-todo-list/
http://teodorfilimon.com/android/Tag-ToDo-List/index.html

2. CHANGE: You aren't allowed to decompile this program, change
it and then redistribute it on your own. Changes will be made in
an organized way within the open-source project:
http://code.google.com/p/tag-todo-list/
The products of the project (consisting only of elements
provided within the project) will respect the New BSD License,
to which this is an informative addition.
 
3. RESOURCES (mainly icons - can be found in the 'res/drawable'
folder as .png files):
	- the 'icon.png' image has been designed by www.ganato.com;
	you can't use it for any app or web page :S
	- the other icons (from the 'drawable' folder) have been
	purchased under license from www.techlogica.us
All the icons can only be used for the original app.

4. USAGE: This app is provided as is, with no warranty of any
kind. If you find any bugs or have suggestions, you can help
a lot by posting an issue in the open-source project:
http://code.google.com/p/tag-todo-list/
or by sending a message from the Tag ToDo List web site:
http://teodorfilimon.com/android/Tag-ToDo-List/index.html
Also, some features may be experimental or dubbed 'beta' and
they are visually marked as such in the app. The app will also
contain visual ads in certain screens.

5. RELEASE LOG: 
http://tagtodolist.blogspot.com/
This is a release log / 'official' blog of Tag-ToDo-List.
It will tell you what's new in each version,
starting with the latest version.

6. STEPS TO COMPILE AN OFFICIAL RELEASE (no point in applying 
this for the open source project every time you test):
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

I'll be happy to answer any questions at:
http://teodorfilimon.com/android/Tag-ToDo-List/contact.html
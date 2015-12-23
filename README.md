AndroidJSCore
=============

AndroidJSCore is an Android Java JNI wrapper around Webkit's JavaScriptCore C library.
It is inspired by the Objective-C JavaScriptCore Framework included natively in
iOS 7.  Being able to natively use JavaScript in an app without requiring the use of
JavaScript injection on a bloated, slow, security-constrained WebView is very useful
for many types of apps, such as games or platforms that support plugins.  However, 
its use is artificially limited because the framework is only supported on iOS.  Most
developers want to use technologies that will scale across both major mobile
operating systems.  AndroidJSCore was designed to support that requirement.

Design Goals
------------
  - Enable full JavaScript support on Android with a Java-only interface (no need to write C/C++ code)
  - Maintain feature-level compatibility with the Objective-C JavaScriptCore framework

Version
-------
1.0

Example
-------

See Owen Matthew's excellent [blog post] on the iOS 7 JavaScriptCore framework for an
introduction.  This example is taken directly from the post.  In the included example
application, Owen's entire Objective-C tutorial is implemented in Java.

Browse the example app source for more detailed examples that cover the basics, sharing
data and functions between Java and JavaScript, wrapping JS classes in Java which
are accessible from both environments, and  asynchronous, multi-threaded callbacks between
environments.

```java
JSContext context = new JSContext();
context.property("a", 5);
JSValue aValue = context.property("a");
double a = aValue.toNumber();
DecimalFormat df = new DecimalFormat(".#");
System.out.println(df.format(a)); // 5.0
context.evaluateScript("a = 10");
JSValue newAValue = context.property("a");
System.out.printlin(df.format(newAValue.toNumber())); // 10.0
String script = 
  "function factorial(x) { var f = 1; for(; x > 1; x--) f *= x; return f; }\n" +
  "var fact_a = factorial(a);\n";
context.evaluateScript(script);
JSValue fact_a = context.property("fact_a");
System.out.printlin(df.format(fact_a.toNumber())); // 3628800.0
```

Using the AndroidJSCoreJNI SDK
-------------------------

If you just want to get started using the SDK in your app, simply download the
[latest release] AndroidJSCoreJNI tarball, and untar it in the root directory of your Android
project.  The tarball contains the necessary .jar with all classes, source code
and javadocs, as well as the compiled .so libraries for the arm, arm7, x86, and mips
platforms.


Refactor In Progress
====================

I am leaving the instructions below intact, because they will still work with older versions of
the SDK.  As of Android 6.0, nothing south of here seems to work right.  The app won't run
because of the deprecated navigation bar paradigm (it will build, but it crashes at runtime), and
I can't update to the new UI concepts because Ecplise has been deprecated as a development
platform.  On top of that, the referenced JavaScriptCore project is no longer a public repo, so
I am stuck with old webkit code.  So, I am going to do 3 things for version 2:

1. Upgrade to Android Studio
2. Get the latest version of webkit to build a standalone JavaScriptCore
3. Combine AndroidJSCoreJNI with JavaScriptCore to create a single library that compiles together

This is likely going to take awhile, given that I have a full time job.  In the meantime, the
release binaries should continue to work, with albeit an old version of JavaScriptCore, and you
can feel free to play with what's below at your own risk.

Building the Example App
------------------------

1. Clone the repo
2. Untar the [latest release] AndroidJSCoreJNI tarball in the AndroidJSCore/AndroidJSCoreExample directory
3. In Eclipse, File->Import->Android->Existing Android Code into Workspace
4. Browse to the AndroidJSCore directory
5. Select AndroidJSCoreExample (it is not necessary to import AndroidJSCoreJNI unless you want to build it)
6. Click 'Finish'.  There will be errors.  That's ok.  We need to link the compatibility library.
7. File->Import->Android->Existing Android Code into Workspace
8. Browse to Your_Android_SDK_Directory/extras/android/support/v7 and click 'Open'
9. Select 'appcompat' (you don't need the others) and click 'Finish'
10. Right click on AndroidJSCoreExample project, Properties->Android
11. Under 'Libraries', select 'Add...'
12. Select the appcompat library and click 'Finish'

You should now be able to build the .apk and run it on your device!

Building AndroidJSCoreJNI
-------------------------

Up to this point, you can simply drop the SDK into your project and use it.  If, however, you'd like
to contribute to the project or make any changes to the SDK, you will have to do some extra work.

#### Step 1: Build JavaScriptCore library for Android

*NOTE*: This step is based on the instructions in [this GitHub project] from Appcelerator.

This totes doesn't work anymore.  I don't control this project
and the instructions are out of date.  There is another way!  You can use my build.
Download the JavaScriptCore-Build from the [latest release] and untar it in some location 
to which you will point Eclipse in Step 3, e.g. /Users/Eric/workspace/AndroidModuleReleases).

I am working on getting this step to work again, and will post the project when I do.

#### Step 2: Import the AndroidJSCore project into Eclipse

To build the AndroidJSCoreJNI library, you must have the [Android ADT plugin] (which 
includes the SDK) and [NDK] installed.  You will have needed both the SDK and NDK in 
Step 1 as well.  You will also want to make sure you have the [CDT plugin] if you want
to work with the C++ (JNI) code.

#### Step 3: Point Eclipse at your NDK
Set the path for the NDK you installed in Preferences->Android->NDK->NDK Location

#### Step 4: Set environment variables
You need to set 2 environment variables.  Right-click on AndroidJSCoreJNI project and
select Properties->C/C++ Build->Environment.  Set the following variables:
  * ANDROID_NDK_ROOT to point at your NDK location (e.g. /Users/Eric/workspace/android-ndk-r9d)
  * NDK_MODULE_PATH to point to where you installed the JavaScriptCore libraries/includes in step 1 (e.g. /Users/Eric/workspace/AndroidModuleReleases)

#### Step 5: Build AndroidJSCoreExample app
You should now be able to build the example app.  To use the library in your project,
simply right-click on your project and select Properties->Android.  In the 'Library'
section, add the AndroidJSCoreJNI library and you should be set.

One final note: You should add some memory to Eclipse if you haven't already done
that.  [Here] is a Mac tutorial.  Bump it up to 1024m (-Xmx1024m).  The C/C++ indexer can
occasionally lock up Eclipse if you don't.

Work in Progress
----------------

  - Test framework

License
-------

I am just sticking with Webkit's license, since this thing depends on it.

 Copyright (c) 2014-2015 Eric Lange. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
[blog post]:http://www.bignerdranch.com/blog/javascriptcore-and-ios-7/
[this github project]:https://github.com/appcelerator/hyperloop/wiki/Building-JavaScriptCore-for-Android
[Android ADT plugin]:http://developer.android.com/sdk/installing/installing-adt.html
[NDK]:https://developer.android.com/tools/sdk/ndk/index.html
[CDT plugin]:http://www.eclipse.org/cdt/downloads.php
[Here]:https://confluence.sakaiproject.org/pages/viewpage.action?pageId=61341742
[latest release]:https://github.com/ericwlange/AndroidJSCore/releases


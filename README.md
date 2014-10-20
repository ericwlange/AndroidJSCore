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
0.1

Example
-------

See Owen Matthew's excellent [blog post] on the iOS 7 JavaScriptCore framework for an
introduction.  This example is taken directly from the post.  In the included example
application, Owen's entire Objective-C tutorial is implemented in Java.

Browse the example app source for more detailed examples that cover the basics, sharing
data and functions between Java and JavaScript, wrapping JS classes in Java which
are accessible from both environments, and  asynchronous, multi-threaded callbacks between
environments.

    JSContext = new JSContext();
    context.property("a", 5);
    JSValue aValue = context.property("a");
    double a = aValue.toNumber();
    DecimalFormat df = new DecimalFormat(".#");
    System.out.println(df.format(a)); // 5.0
    context.evaluateScript("a = 10");
    JSValue newAValue = context.property("a");
    System.out.printlin(df.format(newAValue.toNumber())); // 10.0

Building AndroidJSCore
----------------------

#### Step 1: Build JavaScriptCore library for Android

Follow the instructions in [this GitHub project] from Appcelerator to build the
arm and armeabi-v7a libraries.  At the moment, the x86 and MIPS builds are not
used in AndroidJSCore, but this was only because I couldn't immediately get their
JavaScriptCore libraries to build correctly and lost interest.  If you want to use
them and are able to build, then simply change this line
    
    APP_ABI := armeabi armeabi-v7a

in AndroidJSCoreJNI/jni/Application.mk to add the appropriate platforms.

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
  - Some issues remain around garbage collection, debugging

License
-------

I am just sticking with Webkit's license, since this thing depends on it.

 Copyright (c) 2014 Eric Lange. All rights reserved.

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


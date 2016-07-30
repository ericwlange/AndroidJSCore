AndroidJSCore
=============

AndroidJSCore allows Android developers to use JavaScript natively in their apps.

AndroidJSCore is an Android Java JNI wrapper around Webkit's JavaScriptCore C library.
It is inspired by the Objective-C JavaScriptCore Framework included natively in
iOS 7 and above.  Being able to natively use JavaScript in an app without requiring the use of
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
[3.0.1](https://github.com/ericwlange/AndroidJSCore/releases/tag/3.0.1) - Get it through [JitPack](https://jitpack.io/#ericwlange/AndroidJSCore/3.0.1)

Note there are some significant changes between 3.0 and the 2.x series.  In particular, handling of functions
and constructors is simpler (and more correct).

Working With AndroidJSCore
--------------------------

Please see the [Javadocs] for complete documentation of the API.  Also take a look at the
[example app source code].  It contains more detailed examples that cover the basics, sharing
data and functions between Java and JavaScript, wrapping JS classes in Java which
are accessible from both environments, and asynchronous, multi-threaded callbacks between
environments.

To get started, you need to create a JavaScript `JSContext`.  The execution of JS code
occurs within this context, and separate contexts are isolated virtual machines which
do not interact with each other.

```java
JSContext context = new JSContext();
```

This context is itself a JavaScript object.  And as such, you can get and set its properties.
Since this is the global JavaScript object, these properties will be in the top-level
context for all subsequent code in the environment.

```java
context.property("a", 5);
JSValue aValue = context.property("a");
double a = aValue.toNumber();
DecimalFormat df = new DecimalFormat(".#");
System.out.println(df.format(a)); // 5.0
```

You can also run JavaScript code in the context:

```java
context.evaluateScript("a = 10");
JSValue newAValue = context.property("a");
System.out.println(df.format(newAValue.toNumber())); // 10.0
String script =
  "function factorial(x) { var f = 1; for(; x > 1; x--) f *= x; return f; }\n" +
  "var fact_a = factorial(a);\n";
context.evaluateScript(script);
JSValue fact_a = context.property("fact_a");
System.out.println(df.format(fact_a.toNumber())); // 3628800.0
```

AndroidJSCore is much more powerful than that.  You can also write functions in
Java, but expose them to JavaScript:

```java
JSFunction factorial = new JSFunction(context,"factorial") {
    public Integer factorial(Integer x) {
        int factorial = 1;
        for (; x > 1; x--) {
        	   factorial *= x;
        }
        return factorial;
    }
};
```

This creates a JavaScript function that will call the Java method `factorial` when
called from JavaScript.  It can then be passed to the JavaScript VM:

```java
context.property("factorial", factorial);
context.evaluateScript("var f = factorial(10);")
JSValue f = context.property("f");
System.out.println(df.format(f.toNumber())); // 3628800.0
```

If you are used to working with JavaScriptCore in iOS, see the file
[OwenMatthewsExample.java] in the example app to see side-by-side how to use
AndroidJSCore in Java the same way you would use JavaScriptCore in
Objective-C.

The [Javadocs] and included example app have detailed descriptions of how to do
just about everything.

Use AndroidJSCore in your project
---------------------------------
#### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
	
#### Step 2. Add the dependency

	dependencies {
	        compile 'com.github.ericwlange:AndroidJSCore:3.0.1'
	}

You should be all set!

Building the AndroidJSCoreExample app
---------------------------------

If you want to see AndroidJSCore in action, you can run the example app:

    git clone https://github.com/ericwlange/AndroidJSCore.git ~/AndroidJSCore

Now you can open `~/AndroidJSCore/examples/AndroidJSCoreExample` in Android Studio and run it.

Building AndroidJSCore-3.0 library
----------------------------------

If you are interested in building the library directly and possibly contributing, you must
do the following:

    % git clone https://github.com/ericwlange/AndroidJSCore.git
    % cd AndroidJSCore/AndroidJSCore
    % echo ndk.dir=$ANDROID_NDK > local.properties
    % echo sdk.dir=$ANDROID_SDK >> local.properties
    % ./gradlew assembleRelease

Your library now sits in `AndroidJSCore/build/outputs/aar/AndroidJSCore-3.0.1-release.aar`.  To use it, simply
add the following to your app's `build.gradle`:

    repositories {
        flatDir {
            dirs '/path/to/lib'
        }
    }

    dependencies {
        compile(name:'AndroidJSCore-3.0.1-release', ext:'aar')
    }
    
##### Note: The JavaScriptCore library is built using [The Hemroid Project](https://github.com/ericwlange/hemroid)

The shared libraries are included in binary form.  If you need to build the libraries
directly for any reason, you can do it via `hemroid`.

[`hemroid`](https://github.com/ericwlange/hemroid) is a package manager for Android, similar in intent
to Homebrew on OSX or `apt` on Linux.  The JavaScriptCore library is part of WebKit.  `hemroid` manages the tweaks
required to get it to build on Android.  Building JavaScriptCore takes a long time, upwards of an hour or more, 
depending on your hardware.  If the process fails for any reason it will dump the build log in `/tmp/hemroid.burst`.
Most likely some tool or another needs to be installed that is not installed on your system.  Fix the dependency
and then re-run `hemroid install javascriptcore`.

    % git clone https://github.com/ericwlange/hemroid.git
    % export PATH=$PATH:$PWD/hemroid
    % export ANDROID_NDK=/path/to/ndk
    % export ANDROID_SDK=/path/to/sdk
    % hemroid install javascriptcore

Note that `hemroid` requires [GIT LFS](https://git-lfs.github.com/).  If you don't already have it installed,
you will need to install it.

License
-------

 Copyright (c) 2014-2016 Eric Lange. All rights reserved.

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

[NDK]:http://developer.android.com/ndk/index.html
[latest release]:https://github.com/ericwlange/AndroidJSCore/releases
[Android Studio]:http://developer.android.com/sdk/index.html
[webkit]:https://github.com/ericwlange/webkit
[Javadocs]:http://ericwlange.github.io/
[example app source code]:https://github.com/ericwlange/AndroidJSCore/tree/master/examples/AndroidJSCoreExample
[OwenMatthewsExample.java]:https://github.com/ericwlange/AndroidJSCore/blob/master/examples/AndroidJSCoreExample/app/src/main/java/org/liquidplayer/androidjscoreexample/OwenMatthewsExample.java

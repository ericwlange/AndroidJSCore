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
2.0

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
public interface IExposedToJS {
    public Integer factorial(Integer x);
}
public class FactorialObject extends JSObject
implements IExposedToJS {
    public FactorialObject(JSContext ctx) {
        super(ctx,IExposedToJS.class);
    }
    @Override
    public Integer factorial(Integer x) {
        int factorial = 1;
        for (; x > 1; x--) {
        	   factorial *= x;
        }
        return factorial;
    }
}
```

This class creates a Java object that is also a JavaScript object, which exposes
a single function property `factorial`.  It can then be passed to the JavaScript
VM:

```java
context.property("myJavaFunctions", new FactorialObject(context));
context.evaluateScript("var f = myJavaFunctions.factorial(10);")
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
The easy way is to simply download the file `AndroidJSCore-2.0-release.aar` from
the [latest release] and drop it somewhere in your project (`libs/` is meant just for this). Then
add the following to your app-level `build.gradle`:

    repositories {
        flatDir {
            dirs 'libs'
        }
    }

    dependencies {
        compile(name:'AndroidJSCore-2.0-release', ext:'aar')
    }

Building the AndroidJSCoreExample app
---------------------------------

If you want to see AndroidJSCore in action, you can run the example app:

    git clone https://github.com/ericwlange/AndroidJSCore.git ~/AndroidJSCore
    mkdir ~/AndroidJSCore/lib

Then download `AndroidJSCore-2.0-release.aar` from the [latest release] and
copy it into `~/AndroidJSCore/lib`.  Now you can open `~/AndroidJSCore/examples/AndroidJSCoreExample`
in Android Studio and run it.

Building AndroidJSCore-2.0 library
-------------------------------

If you are interested in building the library directly and possibly contributing, you must
do the following:

#### TL;DR - do this

Set `ANDROID_HOME` and `ANDROID_NDK_ROOT` environment variables

    % git clone --recursive https://github.com/ericwlange/AndroidJSCore.git
    % mkdir build
    % cd build
    % ../AndroidJSCore/scripts/build

Note the `--recursive` option in `git clone`.  This is required for building the
library, but not if you are just downloading the released library as with the example app above.
Your library now sits in `lib/AndroidJSCore-2.0-release.aar`.  To use it, simply
add the following to your app's `build.gradle`:

    repositories {
        flatDir {
            dirs '/path/to/lib'
        }
    }

    dependencies {
        compile(name:'AndroidJSCore-2.0-release', ext:'aar')
    }
    
If something goes wrong or you want to understand what's going on, read on.

#### Step 1 - Set up required tools

This has all been verified to work on Mac OSX (specifically 10.11.2 El Capitan)
and Linux (Ubuntu 14.04 LTS).  If anyone else is married to that OS from Seattle, 
please feel free to get it working and contribute!

1. Download and install the latest version of [Android Studio], including the [NDK]
2. Set two environment variables: `ANDROID_HOME` and `ANDROID_NDK_ROOT` to point to the SDK and NDK directories, respectively
3. Clone the repo: `git clone --recursive https://github.com/ericwlange/AndroidJSCore.git`

This last step will grab both the AndroidJSCore repo, as well as my fork of the
[webkit] repo.  The latter part is huge, like 6 GBs or something, so settle in.  Not that
the recursive clone is required for building the lib, but is not if you just want to
build the example app.

The build process requires a bunch of other standard UNIX tools, too.  The below script will
complain if it can't find something, but you should expect to have the command-line
tools (OSX), `gcc`, `make`, `cmake`, `python`, `perl`, `gperf`, `bison`, `ruby` and
a smattering of other standard developer tools installed.

#### Step 2 - Create a build directory

This directory can be anywhere, but an out-of-source build is always recommended, as
you can blow the whole thing away and start over if something goes awry.

    % mkdir build
    % cd build
    
#### Step 3 - Build AndroidJSCore-2.0-release.aar

From the `build` (or whatever you named it) directory, run the `build` script in `scripts/`:

    % ../AndroidJSCore/scripts/build

Note, the above assumes that your build directory is at the same level as the `AndroidJSCore`
project.  Salt to taste.

This can take an hour, as it does a lot.  Roughly, it will:
 1. Download the `iconv`, `ffi`, `gettext`, `glib-2.0`, and `icu` library sources
 2. Patch the sources to make them build on Android
 3. Build the `icu` library for your host OS

And then for each 32-bit architecture (armeabi, armeabi-v7a, x86, and mips), it will:
 1. Install the prebuilt toolchain for the ABI
 2. Build the five libraries downloaded above
 3. Build the appropriate sections of WebKit required for `JavaScriptCore`

Finally, it will pull it all together by building the `AndroidJSCore-2.0-release.aar`
library.  That file will be installed in the `lib/` directory of the `AndroidJSCore`
source tree.

The `build` script has some options:

`--link-icu-data` will force the ICU data library to be linked to the source.  By default,
this library is stubbed out.  The ICU data library adds a whopping 15MB or so to each
arch (uncompressed).  This library is used for unicode strings, and it isn't clear whether
it is truly required for JavaScriptCore to function or not.  It is definitely required for
WebKit as a whole, but it doesn't seem to impact JavaScript to leave it out.  If for some
reason your project isn't working because of this, you can link the lib back in with this
option.

`--disable-jit` will disable just-in-time compilation for all architectures.  Currently, it
is disabled by default for `armeabi` and `mips` because they will not even compile, and it
is turned off for `armeabi-v7a` because it causes the app to crash on load.  In subsequent
releases, I will try to get this to work.  This should theoretically significantly improve
the speed of JavaScript execution, and is currently enabled by default for `x86` and the
64-bit arches.

`--force-jit` will force enable just-in-time compilation even for arches that don't work.
Don't use this option unless you are trying to debug JIT.  This option overrides `--disable-jit`
if used together.

You may also specify target architectures explicitly.  Currently, `armeabi`, `armeabi-v7a`,
`x86` and `mips` build by default, but if you just want to build for a subset of these ABIs,
then you can specify them explicitly as options.  As of the 2.0 release, only the four
32-bit architectures work.  The `x86_64` and `arm64-v8a` ABIs get pretty far along before
they crap out due to what appears to be a compiler bug in GCC 4.9.  The `mips64` target
won't even get off the ground.  Future versions will include the 64-bit targets as the
tools mature.

Work in Progress
----------------

  - Test framework

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

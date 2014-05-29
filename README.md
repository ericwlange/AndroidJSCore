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

To do.  'Tis a bit complicated at the moment.

Work in Progress
----------------

  - Simplify build process, and split the AndroidJSCore library out of the
  app to build separately
  - Document the API
  - Test framework
  - Verify that garbage collection works properly

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

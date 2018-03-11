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

IMPORTANT NOTICE
------
**AndroidJSCore is DEPRECATED!  All of this functionality and more is now maintained at
its permanent home, [LiquidCore](https://github.com/LiquidPlayer/LiquidCore).  Please migrate to this
version going forward.**

Too see how to use LiquidCore as an AndroidJSCore replacement, read [this document](https://github.com/LiquidPlayer/LiquidCore/wiki/LiquidCore-as-a-Native-Javascript-Engine).

(For the old documentation, see [here](https://github.com/ericwlange/AndroidJSCore/wiki/AndroidJSCore-Deprecated-Documentation), but seriously, it's time to upgrade.)

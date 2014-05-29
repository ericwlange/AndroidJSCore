//
// SharingFunctionsExample.java
// AndroidJSCore project
//
// https://github.com/ericwlange/AndroidJSCore/
//
// Created by Eric Lange
//
/*
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
*/
package org.liquidplayer.jscore.example;

import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSObject;

public class SharingFunctionsExample implements IExample {
	public SharingFunctionsExample(ExampleContext ctx) {
		context = ctx;
	}
	private final ExampleContext context;

	// In this first example, we are creating a normal JavaScript object with a set
	// of functions assigned as properties on that object:
	// { func1 : function(a,b,c) { ... },
	//   func2 : function(x) { ... },
	//   ...
	// }
	public interface IObjectWithFunctionProperties {
		public Double add(Double a, Double b);
		public void setLocal(String value) throws JSException;
		public String getLocal() throws JSException;
		public void setLocalJava(String value);
		public String getLocalJava();
	}
	public class ObjectExample extends JSObject
	implements IObjectWithFunctionProperties {
		public ObjectExample(JSContext ctx) throws JSException {
			// Use this super constructor to create a JavaScript object which exposes the
			// Java functions in IObjectWithFunctionProperties as accessible properties.
			// Don't forget 'implements'!
			super(ctx,IObjectWithFunctionProperties.class);
		}
		// This is only accessible from Java
		private String myLocalString;
		
		@Override
		public Double add(Double a, Double b) { return a+b; }

		@Override
		public void setLocal(String value) throws JSException { property("local_val", value); }

		@Override
		public String getLocal() throws JSException { return property("local_val").toString(); }

		@Override
		public void setLocalJava(String value) { myLocalString = value; }

		@Override
		public String getLocalJava() { return myLocalString; }
		
		// The declared object in this script is functionally equivalent to this class excluding
		// the *LocalJava functions.  You can use variables and functions that are visible only to
		// the Java wrapper.  'myLocalString' cannot be accessed by JavaScript unless passed in
		// an exposed function (in this case, getLocalJava()).
		public final String script = 
				"var js_obj = {\n" +
				"    add: function(a,b) { return (a+b); },\n" +
				"    setLocal: function(value) { this.local_val = value; },\n" +
				"    getLocal: function() { return this.local_val; }\n" +
				"};";
	}
	
	// In the second example, we are creating a JavaScript constructor function with a set of
	// prototype functions:
	// function my_class(a,b,c) {
	//     this.prototype = {
	//         func1 : function(x,y) { ... },
	//         func2 : function() { ... },
	//         ...
	//     }
	//     < constructor code > ...
	// }
	public interface IConstructorWithPrototype {
		// Constructor function.  Always the name of the interface with an underscore (_) in front.
		// If you want to define an object as a constructor function with function prototypes, you must
		// include this in your interface.  Otherwise, it will be created as a static object with
		// the functions as properties as in ObjectExample above.
		public void _IConstructorWithPrototype(Integer number) throws JSException;
		
		// Prototype functions
		public Integer incr() throws JSException;
		public Integer decr() throws JSException;
		
		// Let's use these again, but this time as prototypes
		public void setLocal(String value) throws JSException;
		public String getLocal() throws JSException;
		public void setLocalJava(String msg);
		public String getLocalJava();
	}
	// Note: We are using a static class here because JSObject depends on being able to
	// instantiate new instances of this class with a constructor that takes just an object
	// reference and a JSContext.  Embeddeding a dynamic class requires passing the enclosing
	// objects.  Set it as static to allow it to be created outside this parent class.
	public static class ConstructorExample extends JSObject implements IConstructorWithPrototype {
		public ConstructorExample(JSContext ctx) throws JSException {
			// Notice that now we are including a third parameter to our super constructor: the
			// class of this object.  This will be used when JavaScript wants to instantiate a new
			// instance of our constructor function.
			super(ctx,IConstructorWithPrototype.class,ConstructorExample.class);
		}
		public ConstructorExample(long objRef, JSContext ctx) {
			// We must also include this constructor.  These objects will be instantiated
			// at runtime by callbacks, and this is the constructor it will use.  If you omit this,
			// you will get a NoSuchMethodException upon an attempt to call the constructor
			super(objRef, ctx);
		}
		
		@Override
		public void _IConstructorWithPrototype(Integer number) throws JSException {
			// Here's where you would implement any constructor code
			property("numbah",number);
		}
		@Override
		public Integer incr() throws JSException {
			Integer numbah = property("numbah").toNumber().intValue() + 1;
			property("numbah",numbah);
			return numbah;
		}
		@Override
		public Integer decr() throws JSException {
			Integer numbah = property("numbah").toNumber().intValue() - 1;
			property("numbah",numbah);
			return numbah;
		}
		
		private String myLocalString;
		@Override
		public void setLocalJava(String value) { myLocalString = value; }
		@Override
		public String getLocalJava() { return myLocalString; }
		@Override
		public void setLocal(String value) throws JSException { property("local_val", value); }
		@Override
		public String getLocal() throws JSException { return property("local_val").toString(); }
		
		// This is functionally equivalent to what the ConstructorExample object will create
		// in JavaScript.
		public final String script = 
				"function js_const(number) {\n" +
				"    this.numbah = number;" +
				"}\n" +
				"js_const.prototype.incr = function() { return ++this.numbah; };\n" +
				"js_const.prototype.decr = function() { return --this.numbah; };\n" +
				"js_const.prototype.setLocal = function(value) { this.local_val = value; };\n" +
				"js_const.prototype.getLocal = function() { return this.local_val; };\n";
	}
	
	public void run() throws JSException {
		
		context.log("JS Object with Java methods exposed as properties");
		context.log("---------------------");
		ObjectExample objX = new ObjectExample(context);
		context.property("java_obj",objX);
		context.evaluateScript(objX.script);
		context.log("js_obj.add(5,10) = " + context.evaluateScript("js_obj.add(5,10)"));
		context.log("java_obj.add(5,10) = " + context.evaluateScript("java_obj.add(5,10)"));
		context.evaluateScript("js_obj.setLocal('Hello world from JavaScript!')");
		context.evaluateScript("java_obj.setLocal('Hello world from Java!')");
		context.log("js_obj.getLocal() = " + context.evaluateScript("js_obj.getLocal()"));
		context.log("java_obj.getLocal() = " + context.evaluateScript("java_obj.getLocal()"));
		context.log("js_obj.local_val = " + context.evaluateScript("js_obj.local_val"));
		context.log("java_obj.local_val = " + context.evaluateScript("java_obj.local_val"));
		context.evaluateScript("java_obj.setLocalJava('Shh! I am a private variable')");
		context.log("java_obj.getLocalJava() = " + context.evaluateScript("java_obj.getLocalJava()"));

		context.log("");
		context.log("JS Constructor function with prototype");
		context.log("---------------------");
		ConstructorExample constX = new ConstructorExample(context);
		context.property("java_const", constX);
		context.evaluateScript(constX.script);
		// You can't call prototype functions on the constructor itself
		context.log("js_const.incr = " + context.evaluateScript("js_const.incr"));
		context.log("java_const.incr = " + context.evaluateScript("java_const.incr"));
		// But you can on any instance of the prototype
		context.evaluateScript("var js_inst = new js_const(5)");
		context.log("js_inst.incr = " + context.evaluateScript("js_inst.incr"));
		context.log("js_inst.incr() = " + context.evaluateScript("js_inst.incr()"));
		context.evaluateScript("var java_inst = new java_const(5)");
		context.log("java_inst.incr = " + context.evaluateScript("java_inst.incr"));
		context.log("java_inst.incr() = " + context.evaluateScript("java_inst.incr()"));
		// As in the ObjectExample, we can set properties on the object instance.
		// Also note that ExampleContext is itself a Java-wrapped object, so context.log()
		// is accessible from JavaScript as well.
		context.evaluateScript(
				"js_inst.setLocal('I am a property set in JS');\n" +
				"java_inst.setLocal('I am a property set in Java');\n" +
				"log(js_inst.getLocal());\n" +
				"log(java_inst.getLocal());"
		);		
		// We can even set local Java variables in our subclass which will persist
		// so long as the wrapped JavaScriptObject is still alive
		context.evaluateScript(
				"java_inst.setLocalJava('Hello from java_inst');\n" +
				"var java_inst2 = new java_const(10);\n" +
				"java_inst2.setLocalJava('Goodbye from java_inst2');\n" +
				"log(java_inst.getLocalJava());\n" +
				"log(java_inst2.getLocalJava());"
		);
	}
}

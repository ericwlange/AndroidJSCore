//
// OwenMatthewsExample.java
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
package org.liquidplayer.androidjscoreexample;

import java.text.DecimalFormat;

import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSFunction;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSValue;

public class OwenMatthewsExample implements IExample {

    public OwenMatthewsExample(ExampleContext ctx) {
        context = ctx;
    }
    private final ExampleContext context;

    // In Owen's section "Wrapping Up", he describes how to expose and control properties and functions
    // between Objective C and JavaScript.  We can do the same between Java and JavaScript, but the setup
    // is a bit more complicated.  The following two code snippets do the same thing.  They create an
    // object 'Thing'.  The property 'name' is exposed to both the Java/Objective-C code and JavaScript.
    // Changes to one are reflected in both environments.  The property 'number', however, is private to
    // Java/Objective-C and does not get reflected in JavaScript at all.  Both implementations also override
    // the function 'toString()' across both environments.
    //
    // Although the setup is a bit wonkier, as you can see in the example below, the usage of these two
    // 'Thing' objects is identical.
	
    // In Java                                          // In Objective-C
    //------------------------------------------------  // -------------------------------------------------------
    public interface ExportJSFunctions {
        String toString();
    }
    public interface Property {                         // @protocol ThingJSExports <JSExport>
        void set(Object value)                          // @property (nonatomic, copy) NSString *name;
                throws JSException;                     // @end
        Object get() throws JSException;     	        //
    }                                                   // @interface Thing : NSObject <ThingJSExports>
    public class Thing extends JSObject                 // @property (nonatomic, copy) NSString *name;
    implements ExportJSFunctions {
        private class ThingJSExports                    // @property (nonatomic) NSInteger number;
        implements Property {                           // @end
            ThingJSExports(String name) {               //
                this.name = name; }                     // @implementation Thing
            private final String name;                  // - (NSString *)description {
            public void set(Object value)               // return [NSString stringWithFormat:@"%@: %d", self.name,
                    throws JSException {                //     self.number];
                property(name,value); }                 // }
            public Object get()                         // @end
                    throws JSException {
                return property(name); }
        }
        private class ThingIsPrivate 
        implements Property {
            ThingIsPrivate() {}
            private Object value;
            public void set(Object value) throws JSException { this.value = value; }
            public Object get() throws JSException { return value; }
        }
		
        final Property name;
        final Property number;
		
        public Thing(JSContext ctx) throws JSException {
            super(ctx,ExportJSFunctions.class);
            name = new ThingJSExports("name");
            number = new ThingIsPrivate();
        }
        @Override
        public String toString() {
            try {
                return name.get() + ": " + number.get();
            } catch (JSException e) { return null; }
        }
    }
	
    /*
     * To get the most out of this example, it is best to read along with Owen Matthews' excellent
     * blog post introducing JavaScriptCore in iOS 7.  The following example follows his article
     * exactly.  http://www.bignerdranch.com/blog/javascriptcore-and-ios-7/
     */
    public void run() throws JSException {
        context.clear();
		
        context.log("Share And Share Alike");
        context.log("---------------------");
        // In Java                                          // In Objective-C
        //------------------------------------------------  // -------------------------------------------------------
        context.property("a", 5);                           // context[@"a"] = @5;
        JSValue aValue = context.property("a");             // JSValue *aValue = context[@"a"];
        double a = aValue.toNumber();                       // double a = [aValue toDouble];
        DecimalFormat df = new DecimalFormat(".#");         // NSLog(@"%.0f", a);
        context.log(df.format(a));
		
        context.evaluateScript("a = 10");                   // [context evaluateScript:@"a = 10"];
        JSValue newAValue = context.property("a");          // JSValue newAValue = context[@"a"];
        context.log(df.format(newAValue.toNumber()));       // NSLog(@"%.0f", [newAValue toDouble]);
		
        context.log("");
        context.log("Functional Execution");
        context.log("--------------------");
        // In Java                                          // In Objective-C
        //------------------------------------------------  // -------------------------------------------------------
        context.evaluateScript(                             // [context evaluateScript:
                "var square = function(x) {return x*x;}");  //     @"var square = function(x) {return x*x;}"];
        JSValue squareFunction = context.property("square");// JSValue *squareFunction = context[@"square"];
        context.log(squareFunction.toString());             // NSLog(@"%@", squareFunction);
        JSValue aSquared = squareFunction.toFunction().     // JSValue *aSquared = [squareFunction
                call(null, context.property("a"));          //     callWithArguments:@[context[@"a"]]];
        context.log("a^2: ".concat(aSquared.toString()));   // NSLog(@"a^2: %@", aSquared);
        JSValue nineSquared = squareFunction.toFunction().  // JSValue *nineSquared = [squareFunction
                call(null, 9);                              //     callWithArguments:@[@9]];
        context.log("9^2: ".concat(nineSquared.toString()));// NSLog(@"9^2: %@", nineSquared);
		
        context.property("factorial",                       // context[@"factorial"] = ^(int x) {
            new JSFunction(context,"factorial") {           //
                @SuppressWarnings("unused")                 //
                public Integer factorial(Integer x) {       //
                    int factorial = 1;                      //     int factorial = 1;
                    for (; x > 1; x--) {                    //     for (; x > 1; x--) {
                        factorial *= x;                     //         factorial *= x;
                    }                                       //     }
                    return factorial;                       //     return factorial;
                }                                           // };
        });
        context.evaluateScript(                             // [context evaluateScript:
                "var fiveFactorial = factorial(5);");       //     @"var fiveFactorial = factorial(5);"];
        JSValue fiveFactorial =                             // JSValue *fiveFactorial = context[@"fiveFactorial"];
                context.property("fiveFactorial");
        context.log("5! = ".concat(                         // NSLog(@"5! = %@", fiveFactorial);
                fiveFactorial.toString()));
		
        context.log("");
        context.log("Wrapping Up");
        context.log("--------------------");
        // In Java                                          // In Objective-C
        //------------------------------------------------  // -------------------------------------------------------
        Thing thing = new Thing(context);                   // Thing *thing = [[Thing alloc] init];
        thing.name.set("Alfred");                           // thing.name = @"Alfred";
        thing.number.set(3);                                // thing.number = 3;
        context.property("thing",thing);                    // context[@"thing"] = thing;
        JSValue thingValue = context.property("thing");     // JSValue *thingValue = context[@"thing"];
        context.log("Thing: " + thing);                     // NSLog(@"Thing: %@", thing);
        context.log("Thing JSValue: " + thingValue);        // NSLog(@"Thing JSValue: %@", thingValue);
		
        thing.name.set("Betty");                            // thing.name = @"Betty";
        thing.number.set(8);                                // thing.number = 8;
        context.log("Thing: " + thing);                     // NSLog(@"Thing: %@", thing);
        context.log("Thing JSValue: " + thingValue);        // NSLog(@"Thing JSValue: %@", thingValue);
        
        context.evaluateScript(                             // [context evaluateScript:
            "thing.name = \"Carlos\"; thing.number = 5");   //     @"thing.name = \"Carlos\"; thing.number = 5"];
        context.log("Thing: " + thing);                     // NSLog(@"Thing: %@", thing);
        context.log("Thing JSValue: " + thingValue);        // NSLog(@"Thing JSValue: %@", thingValue);
    }
}

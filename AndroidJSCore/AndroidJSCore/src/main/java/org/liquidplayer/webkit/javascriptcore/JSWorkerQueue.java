//
// JSWorkerQueue.java
// AndroidJSCore project
//
// https://github.com/ericwlange/AndroidJSCore/
//
// Created by Eric Lange
//
/*
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
*/
package org.liquidplayer.webkit.javascriptcore;

import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutionException;


/**
 * Ensures JavaScriptCore is not accessed from the main thread
 */
public class JSWorkerQueue {
    public JSWorkerQueue(final Runnable monitor) {
        mMonitor = monitor;
    }
    final Runnable mMonitor;

    private class JSTask extends AsyncTask<Runnable, Void, JSException> {
        @Override
        public JSException doInBackground(Runnable ... params) {
            try {
                ((Runnable) params[0]).run();
                mMonitor.run();
            } catch (JSException e) {
                return e;
            }
            return null;
        }
    }

    public void sync(final Runnable runnable) throws JSException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                JSException e = new JSTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, runnable).get();
                if (e != null) throw e;
            } catch (ExecutionException e) {
                Log.e("JSWorkerQueue", e.getMessage());
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        } else {
            runnable.run();
            mMonitor.run();
        }
    }

    public void async(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new JSTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, runnable);
        } else {
            runnable.run();
            mMonitor.run();
        }
    }

    public void quit() {

    }
}

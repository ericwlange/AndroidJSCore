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

import android.os.Handler;
import android.os.Looper;

/**
 * Ensures single-threaded access to JavaScriptCore
 */
public class JSWorkerQueue extends Thread {

    private Boolean mReady = false;
    private int mThreadId;
    private Boolean mutex = false;

    @Override
    public void run() {
        Looper.prepare();
        workerHandler = new Handler();
        mThreadId = android.os.Process.myTid();
        synchronized (mReady) {
            mReady = true;
        }
        Looper.loop();
    }

    public Handler workerHandler;

    public JSWorkerQueue() {
        super();
        start();
        Boolean ready = false;
        while (!ready) {
            synchronized (mReady) {
                ready = mReady;
            }
        }
    }

    private class SyncRunnable implements Runnable {
        SyncRunnable(Runnable runnable) {
            mRunnable = runnable;
        }
        private final Runnable mRunnable;
        private Boolean mComplete = false;
        private JSException exception = null;
        @Override
        public void run() {
            try {
                mRunnable.run();
            } catch (JSException e) {
                exception = e;
            }
            synchronized (mComplete) {
                mComplete = true;
            }
        }
        public void block() throws JSException {
            Boolean complete = false;
            while (!complete) {
                synchronized (mComplete) {
                    complete = mComplete;
                }
            }
            if (exception != null) throw exception;
        }
    }

    public void sync(final Runnable runnable) throws JSException {
        int currThreadId = android.os.Process.myTid();
        if (currThreadId == mThreadId) {
            runnable.run();
        } else {
            SyncRunnable syncr = new SyncRunnable(runnable);
            workerHandler.post(syncr);
            syncr.block();
        }
    }
    public void async(final Runnable runnable) {
        workerHandler.post(runnable);
    }

    public void quit() {
        synchronized (mutex) {
            if (!mutex) {
                workerHandler.getLooper().quit();
                try {
                    join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                mutex = true;
            }
        }
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        quit();
    }
};

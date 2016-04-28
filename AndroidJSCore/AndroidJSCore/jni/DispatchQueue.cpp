//
// DispatchQueue.cpp
// AndroidJSCore project
//
// https://github.com/ericwlange/AndroidJSCore/
//
// Created by Eric Lange
//
/*
 Copyright (c) 2016 Eric Lange. All rights reserved.

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
#include "DispatchQueue.h"

DispatchQueue::DispatchQueue(unsigned pool) {
    pool = (pool<1) ? 1 : (pool>16) ? 16:pool;
    _dispatchThreads = new DispatchThread[pool];
    _pool = pool;
}

DispatchQueue::~DispatchQueue() {
	delete [] _dispatchThreads;
}

int DispatchQueue::sync(std::function<void(void *)> func, void *payload) {
	return pickThread()->sync(func,payload);
}

int DispatchQueue::async(std::function<void(void *)> func, void *payload) {
	return pickThread()->async(func,payload);
}

DispatchThread* DispatchQueue::pickThread() {
	// If we are already being called from a worker thread, just use that one
	// Otherwise, we could deadlock
	unsigned min = 0;
	size_t best = 100;
	for( unsigned i=0; i<_pool; i++) {
		if (pthread_self() == _dispatchThreads[i].pThread()) return &_dispatchThreads[i];
		if (_dispatchThreads[i].depth() <= best) {
			min = i;
			best = _dispatchThreads[i].depth();
		}
	}
	return &_dispatchThreads[min];
}

/** class DispatchThread **/

DispatchThread::DispatchThread() {
	thread_queue_init(&_queue);
	pthread_create(&_thread, NULL, _run, this);
}

DispatchThread::~DispatchThread() {
	destroy();
	pthread_join(_thread,NULL);
	thread_queue_cleanup(&_queue,1);
}

int DispatchThread::add(std::function<void(void *)> func, void *payload,
	struct threadqueue *semaphore) {
	
	funct *f = new funct;

	f->func = func;
	f->payload = payload;
	f->semaphore = semaphore;
	return thread_queue_add(&_queue, f, DISPATCH_QUEUE_FUNCTION);
}

int DispatchThread::async(std::function<void(void *)> func, void *payload) {
	return add(func,payload);
}

int DispatchThread::sync(std::function<void(void *)> func, void *payload) {
	if (pthread_self() == _thread) {
		func(payload);
		return 0;
	}

	struct threadqueue semaphore;
	struct threadmsg msg;
	thread_queue_init(&semaphore);

	int ret = add(func,payload,&semaphore);
	if (!ret) {
		ret = thread_queue_get(&semaphore,NULL,&msg);
		thread_queue_cleanup(&semaphore,0);
	}
	return ret;
}

int DispatchThread::destroy() {
	return thread_queue_add(&_queue, NULL, DISPATCH_QUEUE_DESTRUCT);
}

void* DispatchThread::run() {
	struct threadmsg msg;
	volatile bool loop = true;
	while(loop) {
		thread_queue_get(&_queue,NULL,&msg);

		switch(msg.msgtype){
			case DISPATCH_QUEUE_FUNCTION: {
				funct *f = (funct *)msg.data;
				f->func(f->payload);
				if (f->semaphore) {
					thread_queue_add(f->semaphore, NULL, 0);
				}
				delete f;
			}
			break;
			case DISPATCH_QUEUE_DESTRUCT: {
				loop = false;
			}
			break;
		}
	}

	while (thread_queue_length(&_queue)) {
		thread_queue_get(&_queue,NULL,&msg);
		if(msg.msgtype == DISPATCH_QUEUE_FUNCTION) {
			funct *f = (funct *)msg.data;
			// Release any waiting semaphores
			if (f->semaphore) {
				thread_queue_add(f->semaphore, NULL, 0);
			}
			delete f;
		}
	}

	return NULL;
}


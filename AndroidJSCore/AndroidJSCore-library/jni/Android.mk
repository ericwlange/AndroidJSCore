#
# Android.mk
# AndroidJSCore project
#
# https://github.com/ericwlange/AndroidJSCore/
#
# Created by Eric Lange
#
#
# Copyright (c) 2014-2016 Eric Lange. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# - Redistributions of source code must retain the above copyright notice, this
# list of conditions and the following disclaimer.
#
# - Redistributions in binary form must reproduce the above copyright notice,
# this list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := javascriptcore
LOCAL_SHARED_LIBRARIES := glib icu
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libjavascriptcoregtk-4.0.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := icuuc
LOCAL_SHARED_LIBRARIES := icudata
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libicuhemuc.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := icui18n
LOCAL_SHARED_LIBRARIES := icudata
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libicuhemi18n.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := icudata
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libicuhemdata.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := ffi
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libffi.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := intl
LOCAL_SHARED_LIBRARIES := iconv
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libintl.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := glib
LOCAL_SHARED_LIBRARIES := iconv ffi intl
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libglib-2.0.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := gmodule
LOCAL_SHARED_LIBRARIES := glib
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libgmodule-2.0.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE           := gio
LOCAL_SHARED_LIBRARIES := glib gmodule
LOCAL_SRC_FILES        := lib/$(TARGET_ARCH_ABI)/libgio-2.0.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := gobject
LOCAL_SHARED_LIBRARIES := glib ffi
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libgobject-2.0.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := iconv
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libiconv.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := android-js-core
LOCAL_SRC_FILES := JSContext.cpp \
                   JSObject.cpp \
                   JSString.cpp \
                   JSValue.cpp \
                   Instance.cpp \
                   JSFunction.cpp

LOCAL_SHARED_LIBRARIES := javascriptcore

LOCAL_CPPFLAGS  := -std=c++11 -fexceptions -I$(LOCAL_PATH)/include
LOCAL_LDFLAGS := -llog

include $(BUILD_SHARED_LIBRARY)
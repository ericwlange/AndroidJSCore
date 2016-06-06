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

LOCAL_MODULE    := android-js-core
LOCAL_SRC_FILES := JSContext.cpp \
                   JSObject.cpp \
                   JSString.cpp \
                   JSValue.cpp \
                   Instance.cpp \
                   JSFunction.cpp

HEMROID_MODULES        := javascriptcore_$(TARGET_ARCH_ABI) icustub_$(TARGET_ARCH_ABI)
LOCAL_SHARED_LIBRARIES := $(HEMROID_MODULES)

LOCAL_CPPFLAGS  := -std=c++11 -fexceptions
LOCAL_LDFLAGS := -llog

include $(BUILD_SHARED_LIBRARY)

$(call import-module, hemroid)
$(call import-module, javascriptcore)
$(call import-module, icu/icustub)

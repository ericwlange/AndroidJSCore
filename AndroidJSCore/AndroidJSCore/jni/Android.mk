LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := android-js-core
LOCAL_SRC_FILES := DispatchQueue.cpp \
                   JSContext.cpp \
                   JSObject.cpp \
                   JSString.cpp \
                   JSValue.cpp \
                   threadqueue.c

HEMROID_MODULES        := javascriptcore_$(TARGET_ARCH_ABI) icustub_$(TARGET_ARCH_ABI)
LOCAL_SHARED_LIBRARIES := $(HEMROID_MODULES)

LOCAL_CPPFLAGS  := -std=c++11
LOCAL_LDFLAGS := -llog

include $(BUILD_SHARED_LIBRARY)

$(call import-module, hemroid)
$(call import-module, javascriptcore)
$(call import-module, icu/icustub)

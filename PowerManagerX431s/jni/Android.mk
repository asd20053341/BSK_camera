LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS    := -llog 
LOCAL_MODULE    := gpio_lib
LOCAL_SRC_FILES := gpio_lib.cpp

include $(BUILD_SHARED_LIBRARY)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS 	:= -llog
LOCAL_MODULE    := x804camerademo
LOCAL_SRC_FILES := x804camerademo.cpp

include $(BUILD_SHARED_LIBRARY)

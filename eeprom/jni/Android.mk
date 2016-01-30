LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := eeprom
### Add all source file names to be included in lib separated by a whitespace
LOCAL_SRC_FILES := eeprom.cpp

LOCAL_LDLIBS    := -llog 
include $(BUILD_STATIC_LIBRARY)

################################################
include $(CLEAR_VARS)
LOCAL_MODULE    := eeprom_bsk
### Add all source file names to be included in lib separated by a whitespace
LOCAL_SRC_FILES := eeprom_bsk.cpp

LOCAL_LDLIBS    := -llog 
LOCAL_STATIC_LIBRARIES := libeeprom
include $(BUILD_SHARED_LIBRARY)

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_LDLIBS    := -llog 
LOCAL_MODULE    := gpio_lib
LOCAL_SRC_FILES := gpio_lib.cpp

include $(BUILD_SHARED_LIBRARY)



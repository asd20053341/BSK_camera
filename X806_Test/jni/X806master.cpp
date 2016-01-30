#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <errno.h>
#include <unistd.h>
#include <stdlib.h>
#include <android/log.h>
#include <termios.h>
#include <string.h>
#include "X806JniApi.h"


#define TAG "x806AndroidSerialService"
#define ALOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define SERIAL_ERR -1
#define SERIAL_TRUE 0


jint slaveValue ;

jint setX806Serial(JNIEnv *env, jobject thiz, jint nSpeed, jint nBits, jchar nEvent, jint nStop, jint fd)
{
	ser_set(nSpeed, nBits, nEvent,nStop,fd);
	return SERIAL_TRUE;
}

jint openX806Serial(JNIEnv *env, jobject thiz, jint comport)
{
	int fd ;
	fd = ser_open(comport);
	return fd;
}

jint multiexcute(JNIEnv *env, jobject thiz, jchar oper, jchar data, jint fd)
{
	jchar ret;
	if(oper == 'R'){
		(SERIAL_TRUE == sread_one_byte(&slaveValue, fd))?(ret = SERIAL_TRUE):(ret = SERIAL_ERR);
	}
	if(oper == 'W'){
		(SERIAL_TRUE == swrite_one_byte(data, fd))?(ret = SERIAL_TRUE):(ret = SERIAL_ERR);
	}
	return ret;
}

jint getValue(JNIEnv *env, jobject thiz)
{
	return slaveValue;
}

/*
 * Class:     X804master
 * Method:    getVersionName
 */
jstring getVersionName(JNIEnv *env, jobject args) {
	return env->NewStringUTF("1.2");
}

JNINativeMethod  myMethods[] = {
		{ "getVersionName", "()Ljava/lang/String;", (void *) getVersionName },
		{"openSerial", "(I)I",  (void *)openX806Serial},
		{"setSerial", "(IICII)I", (void *)setX806Serial},
		{"readWrite", "(CCI)I", (void*)multiexcute},
		{"getSlaveValue", "()I", (void *)getValue}
};


jint JNI_OnLoad(JavaVM * vm,void * reserved)
{
	ALOGD("JNI_OnLoad\n");

	JNIEnv   *env;
	jint ret;


	ret = vm->GetEnv((void * * )&env, JNI_VERSION_1_6);//FIXME,the system java version is 1.6 ! Don't mistake it!
	if(ret != JNI_OK)
	{
		ALOGD("vm->GetEnv error\n");
		return -1;
	}

	jclass   mycls = env->FindClass("com/example/x806/X806master");
	if(mycls == NULL)
	{
		ALOGD("Could not find the Activity, Please check the FindClass !\n");
		return -1;
	}

	ret = env->RegisterNatives(mycls, myMethods, sizeof(myMethods)/sizeof(myMethods[0]));
	if(ret < 0)
	{
		ALOGD("env->RegisterNatives error\n");
		return -1;
	}

	return JNI_VERSION_1_6;//FIXME, the system java version is 1.6. Don't mistake it !

}





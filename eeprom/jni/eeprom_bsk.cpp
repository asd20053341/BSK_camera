
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <string.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include <fcntl.h>              /* low-level i/o */
#include <unistd.h>
#include <errno.h>
#include <malloc.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/mman.h>
#include "eeprom.h"
static const char *TAG="BSK";

#define ALOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define ALOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define ALOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

extern "C" {
	JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_OpenDev(JNIEnv * env, jobject obj);
	JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_CloseDev(JNIEnv * env, jobject obj);
	JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_ReadUuid(JNIEnv * env, jobject obj);
	JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_WriteUuid(JNIEnv * env, jobject obj,jstring buf);
	JNIEXPORT jchar  JNICALL Java_com_bsk_eeprom_MainActivity_ReadVersion(JNIEnv * env, jobject obj);
	JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_WriteVersion(JNIEnv * env, jobject obj,jchar value);
	JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_ReadBatch(JNIEnv * env, jobject obj);
	JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_WriteBatch(JNIEnv * env, jobject obj,jstring buf);
	JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_ReadSn(JNIEnv * env, jobject obj);
	JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_WriteSn(JNIEnv * env, jobject obj,jstring buf);

};
JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_OpenDev(JNIEnv * env, jobject obj)
{

	ALOGD("eeprom_OpenDev");
	bsk_OpenDev();
	 return env->NewStringUTF("eeprom_OpenDev");

}
JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_CloseDev(JNIEnv * env, jobject obj)
{

	ALOGD("eeprom_CloseDev");
	bsk_CloseDev();
	 return env->NewStringUTF("eeprom_CloseDev");

}
JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_ReadUuid(JNIEnv * env, jobject obj)
{

	char *buf =(char *) bsk_ReadUuid();
	 //strncpy(buf1,buf,16);
	 //strcpy(buf1+16,"\0");
	ALOGD("eeprom_ReadUuid");
	return env->NewStringUTF(buf);
	//return buf;

}
JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_WriteUuid(JNIEnv * env, jobject obj,jstring buf)
{
	ALOGD("eeprom_WriteUuid");
	const char *str =env->GetStringUTFChars(buf,NULL);

	if(str == NULL)
	  return NULL;
	printf("%s\n",str);
	bsk_WriteUuid(str);

	env->ReleaseStringUTFChars(buf,str);

	ALOGD("eeprom_WriteUuid1");

	return 	env->NewStringUTF("eeprom_writeuuid");


}
JNIEXPORT jchar JNICALL Java_com_bsk_eeprom_MainActivity_ReadVersion(JNIEnv * env, jobject obj)
{
	unsigned char buf;
	ALOGD("eeprom_Readsversion");
	buf  = bsk_ReadVersion();

	//return 	env->NewStringUTF(&buf);
	return buf;

}
JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_WriteVersion(JNIEnv * env, jobject obj,jchar value)
{

	ALOGD("eeprom_WritesVersion");
	bsk_WriteVersion(value);


	return 	env->NewStringUTF("eeprom_writesVersion");

}
JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_ReadBatch(JNIEnv * env, jobject obj)
{
	char *buf = (char *)bsk_ReadBatch();
		 //strncpy(buf1,buf,16);
		 //strcpy(buf1+16,"\0");
	ALOGD("eeprom_ReadBatch");
		return env->NewStringUTF(buf);

}
JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_WriteBatch(JNIEnv * env, jobject obj,jstring buf)
{

	const char *str =env->GetStringUTFChars(buf,NULL);

		if(str == NULL)
		  return NULL;
		printf("%s\n",str);
		bsk_WriteBatch(str);

		env->ReleaseStringUTFChars(buf,str);

		ALOGD("eeprom_WriteBatch");

	return 	env->NewStringUTF("eeprom_writebatch");

}
JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_ReadSn(JNIEnv * env, jobject obj)
{
	//	char buf1[17];

		char *buf = (char *)bsk_ReadSn();
			 //strncpy(buf1,buf,16);
			 //strcpy(buf1+16,"\0");
		ALOGD("eeprom_ReadSn");
		return env->NewStringUTF(buf);

}
JNIEXPORT jstring JNICALL Java_com_bsk_eeprom_MainActivity_WriteSn(JNIEnv * env, jobject obj,jstring buf)
{
	//ALOGD("eeprom_Writeother");
	const char *str =env->GetStringUTFChars(buf,NULL);

	if(str == NULL)
			 return NULL;
	printf("%s\n",str);
	bsk_WriteSn(str);
	env->ReleaseStringUTFChars(buf,str);
	ALOGD("eeprom_WriteSn");
	return 	env->NewStringUTF("eeprom_writeSn");

}


#include <stdio.h>
#include <stdint.h>
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
#include "android/log.h"

static const char *TAG="BSK";
static char dev_name[16];
static int fd = -1;
/*
#define VERSION_FLAG 1
#define BATCH_FLAG 2
#define UUID_FLAG 3
#define SN_FLAG 4
*/
#define VERSION_OFFSET  0
#define BATCH_OFFSET       1
#define UUID_OFFSET         17
#define SN_OFFSET               49
#define VERSION_LEN        1
#define BATCH_LEN            16
#define UUID_LEN              32
#define SN_LEN                    16
#define ALOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define ALOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define ALOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

struct user_data{
	unsigned char buff[256];
	unsigned char len;
	unsigned char offset;
};

struct user_data user_data;
int bsk_OpenDev(void)
{
	int err;
	struct stat st;
	sprintf(dev_name,"/dev/eeprom");
	if(-1 == stat(dev_name,&st)) {
	 		ALOGE("Cannot identify '%s': %d, %s", dev_name, errno, strerror (errno));
	 		return -1;
		
	}
	fd = open(dev_name,O_RDWR);
	if( -1 == fd){
		ALOGE("Cannot open '%s': %d, %s", dev_name, errno, strerror (errno));
		return -1;
	}
	return fd;

}
int bsk_CloseDev(void)
{
	ALOGD("bsk_CloseDev");
	if(-1 == close(fd)){
		fd = -1;
		return -1;
	}
	fd = -1;
	return 0;

}
unsigned char bsk_ReadVersion()
{
	int ret;
	user_data.offset = VERSION_OFFSET;
	user_data.len = VERSION_LEN;
	 ret = read(fd,&user_data,sizeof(struct user_data));
	if(-1 == ret){
	  	  ALOGE("err write");
	  	  return 0;

	  }
	ALOGD("BSK_READVERSION");
	 return user_data.buff[0];
}
int bsk_WriteVersion(unsigned char value)
{
	int ret;
	user_data.offset = VERSION_OFFSET;
	user_data.len = VERSION_LEN;
	user_data.buff[0] = value;
	ret = write(fd,&user_data,sizeof(struct user_data));
	if(-1 == ret){
		ALOGE("err write");
	}

	ALOGD("BSK_WRITEVERSION");
	return 0;

}
int bsk_WriteUuid( const char *uuid)
{
	int ret;
	ret = strlen(uuid);
	if(ret  != 32){
		ALOGE(" strlen != 32");
		return -1;
	}
	user_data.offset = UUID_OFFSET;
	user_data.len = UUID_LEN;
	strncpy((char*)user_data.buff,uuid,UUID_LEN);
	ret = write(fd,&user_data,sizeof(struct user_data));
	if(-1 == ret){
		ALOGE("err write");
	}
	ALOGD("BSK_WRITUUID");
	return 0;

}
 unsigned char *bsk_ReadUuid()
{
	int ret;
	user_data.offset = UUID_OFFSET;
	user_data.len = UUID_LEN;
	ret = read(fd,&user_data,sizeof(struct user_data));

	if(-1 == ret){
		printf("read err\n");
	}

	ALOGD("***********************************%s\n",user_data.buff);

	return user_data.buff;


}
int bsk_WriteBatch(const char *batch)
{
	int ret;
	ret = strlen(batch);
	if(ret  != 16){
			ALOGE(" strlen != 16");
			return -1;
	}
	user_data.offset = BATCH_OFFSET;
	user_data.len = BATCH_LEN;
	strncpy((char*)user_data.buff,batch,BATCH_LEN);

	ret = write(fd,&user_data,sizeof(struct user_data));
	if(-1 == ret){
		ALOGE("err write");
	}
	ALOGD("BSK_WRITEBATCH");
	return 0;

}
unsigned char * bsk_ReadBatch()
{
	int ret;
	user_data.offset = BATCH_OFFSET;
	user_data.len = BATCH_LEN;
	ret = read(fd,&user_data,sizeof(struct user_data));
	if(-1 == ret){
		ALOGE("err write");
	}	
	return user_data.buff;

}
 unsigned char *bsk_ReadSn()
{
	int ret;
	user_data.offset = SN_OFFSET;
	user_data.len = SN_LEN;
	ret = read(fd,&user_data,sizeof(struct user_data));
	if(-1 == ret){
		ALOGE("err write");
	}
	return user_data.buff;
}
int bsk_WriteSn(const char * sn)
{
	int ret;
	ret = strlen(sn);
	if(ret  != 16){
		ALOGE(" strlen != 16");
			return -1;
	}
	user_data.offset = SN_OFFSET;
	user_data.len = SN_LEN;
	strncpy((char*)user_data.buff,sn,SN_LEN);

	ret = write(fd,&user_data,sizeof(struct user_data));
	if(-1 == ret){
		ALOGE("err write");
	}
	return 0;
}

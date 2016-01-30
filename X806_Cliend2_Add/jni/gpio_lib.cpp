#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <linux/ioctl.h>
//#include "gpio.h"
#include <errno.h>
#include <android/log.h>

static char dev_name[16];


struct  pwm_data{
	unsigned char flag;
	unsigned char enable;
	unsigned long value;
	 int time;
};
struct pwm_data pwm;
static const char *TAG="GPIO";
#define GPIO_PATH "/sys/class/gpio"
#define GPIO_EXPORT_PATH "/sys/class/gpio/export"
#define GPIO_UNEXPORT_PATH "/sys/class/gpio/unexport"

#define ALOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

//#define A20
#define A23
//#define A23
extern "C" {
	JNIEXPORT jint  JNICALL  Java_com_example_gpio1_MainActivity_setValue(JNIEnv * env, jobject obj,jstring port,jint num ,jint value);
	JNIEXPORT jint  JNICALL  Java_com_example_gpio1_MainActivity_getValue(JNIEnv * env, jobject obj,jstring port,jint num);
	JNIEXPORT jstring  JNICALL  Java_com_example_gpio1_MainActivity_setDirection(JNIEnv * env, jobject obj, jstring port,jint num,jstring inout);
	JNIEXPORT jint  JNICALL Java_com_example_gpio1_MainActivity_enablePwm(JNIEnv * env, jobject obj);
	JNIEXPORT jint  JNICALL Java_com_example_gpio1_MainActivity_disablePwm(JNIEnv * env, jobject obj);
	JNIEXPORT jint  JNICALL Java_com_example_gpio1_MainActivity_enablePwmTime(JNIEnv * env, jobject obj,jint msecond);
	JNIEXPORT jint  JNICALL Java_com_example_gpio1_MainActivity_enablePwmUseKernelTimer(JNIEnv * env, jobject obj,jint msecond);
};


int index2num(char * gpioport_chars, int gpionum)
{
   int index=0;
#ifdef A23
   index = (gpioport_chars[1] - 'A')*32 + gpionum;
#endif

#ifdef A31S
   switch(gpioport_chars[1]){
   case'A':
	   index = gpionum;
	   break;
   case'B':
	   index = 30+gpionum;
	   break;
   case'C':
	   index = 40+gpionum;
	   break;
   case'D':
	   index = 70+gpionum;
	   break;
   case'E':
	   index = 100+gpionum;
	   break;
   case'F':
	   index = 119+gpionum;
	   break;
   case'G':
	   index = 127+gpionum;
	   break;
   case'H':
	   index = 148+gpionum;
	   break;
   case'L':
	   index = 181+gpionum;
	   break;
   case'M':
	   index = 192+gpionum;
	   break;
   }
#endif

#ifdef A20
   switch(gpioport_chars[1]){
   case'A':
	   index = gpionum;
	   break;
   case'B':
	   index = 24+gpionum;
	   break;
   case'C':
	   index = 54+gpionum;
	   break;
   case'D':
	   index = 85+gpionum;
	   break;
   case'E':
	   index = 119+gpionum;
	   break;
   case'F':
	   index = 137+gpionum;
	   break;
   case'G':
	   index = 149+gpionum;
	   break;
   case'H':
	   index = 167+gpionum;
	   break;
   case'I':
	   index = 201+gpionum;
	   break;
   }
#endif
   return index;
}

 int readFromFile(int fd, char* buf, int size)
{
    int count = read(fd, buf, size);
    if (count > 0) {
        while (count > 0 && buf[count-1] == '\n')
            count--;
        buf[count] = '\0';
    } else {
        buf[0] = '\0';
    }

    return count;
}


 int gpio_setValue(char* gpioport, int gpionum, int value)
 {
		 char *gpioport_chars = gpioport;
    char gpio[5] = {0},value_path[36] = {0},gpiovalue = ((value == 0)?'0':'1');
    int fd;

    if(gpioport_chars[1] < 'A' || gpioport_chars[1] > 'N'){
        ALOGE("Gpio port error while setValue");
        return -1;
    }

    //sprintf(gpio, "%d", (gpioport_chars[1] - 'A')*32 + gpionum);
    sprintf(gpio, "%d", index2num(gpioport_chars,gpionum));
    sprintf(value_path, "%s/gpio%s/value", GPIO_PATH,gpio);
    fd = open(value_path,O_WRONLY);
    if(fd<0){
        //export gpio
        fd = open(GPIO_EXPORT_PATH,O_WRONLY);
        if(fd<0){
            ALOGE("Could not open '%s' while setValue", GPIO_EXPORT_PATH);
            return -1;
        }
        write(fd,gpio,strlen(gpio));
        close(fd);
        fd = open(value_path,O_WRONLY);
           if(fd<0){
               ALOGE("Could not open '%s' while setValue", value_path);
               return -1;
           }
    }
    //set value

    write(fd,&gpiovalue,1);
    ALOGE("gpio set value fd=%d gpiovalue======%c ",fd,gpiovalue);
    close(fd);

    return value;

}


int gpio_getValue(char* gpioport, int gpionum)
{
	char *gpioport_chars = gpioport;
    char gpio[5] = {0},value_path[36] = {0},value[3] = {0};
    int fd;

    if(gpioport_chars[1] < 'A' || gpioport_chars[1] > 'N'){
        ALOGE("Gpio port error while getValue");
        return -1;
    }

    //sprintf(gpio, "%d", (gpioport_chars[1] - 'A')*32 + gpionum);
    sprintf(gpio, "%d", index2num(gpioport_chars,gpionum));
    sprintf(value_path, "%s/gpio%s/value", GPIO_PATH,gpio);
    fd = open(value_path,O_WRONLY);
    if(fd<0){
        //export gpio
        fd = open(GPIO_EXPORT_PATH,O_WRONLY);
        if(fd<0){
            ALOGE("Could not open '%s' while getValue", GPIO_EXPORT_PATH);
            return -1;
        }
        write(fd,gpio,strlen(gpio));

        close(fd);
        fd = open(value_path,O_RDONLY);
            if(fd<0){
                ALOGE("Could not open '%s' while getValue", value_path);
                return -1;
            }
    }
    //get value

    readFromFile(fd,value,sizeof(value));
    close(fd);

    ALOGE("gpio get value======%d \n",atoi(value));
    return atoi(value);
}



void  gpio_setDirection(char* gpioport, int gpionum, char* inout)
{
		 char *gpioport_chars = gpioport;
    char *inout_chars = inout;
    char gpio[5] = {0},direction_path[36] = {0};
    int fd;

    if(gpioport_chars[1] < 'A' || gpioport_chars[1] > 'N'){
        ALOGE("Gpio port error while setDirection");
        return;
    }

    //sprintf(gpio, "%d", (gpioport_chars[1] - 'A')*32 + gpionum);
    sprintf(gpio, "%d", index2num(gpioport_chars,gpionum));
    sprintf(direction_path, "%s/gpio%s/direction", GPIO_PATH,gpio);


    fd = open(GPIO_EXPORT_PATH,O_WRONLY);
    if(fd<0){
        ALOGE("Could not open '%s' while setDirection", GPIO_EXPORT_PATH);
        return;
    }
    write(fd,gpio,strlen(gpio));
    close(fd);
    //set direction

    fd = open(direction_path,O_WRONLY);
    if(fd<0){
        ALOGE("Could not open '%s'", direction_path);
        return;
    }
    write(fd,inout_chars,strlen(inout_chars));
    close(fd);



}

JNIEXPORT jint  JNICALL  Java_com_example_gpio1_MainActivity_setValue(JNIEnv * env, jobject obj,jstring port,jint num ,jint value)
{

	int ret;
	char buf[3] = {0};
	const  char *port1 = 	env->GetStringUTFChars(port,NULL);
	strncpy(buf,port1,2);
	strcpy(buf+2,"\0");
	ret = gpio_setValue(buf,num,value);

	env->ReleaseStringUTFChars(port,port1);
	if(ret < 0)
	  return -1;

 return ret;
}
JNIEXPORT jint  JNICALL  Java_com_example_gpio1_MainActivity_getValue(JNIEnv * env, jobject obj,jstring port,jint num)
{

	int ret = 0;

	char buf[3] = {0};
	const  char *port1 = 	env->GetStringUTFChars(port,NULL);
	strncpy(buf,port1,2);
	strcpy(buf+2,"\0");
	ret = gpio_getValue(buf,num);
	ALOGE("ret++++%d",ret);
	env->ReleaseStringUTFChars(port,port1);
	if(ret < 0)
	  return -1;

	return ret;

 }
JNIEXPORT jstring  JNICALL  Java_com_example_gpio1_MainActivity_setDirection(JNIEnv * env, jobject obj, jstring port,jint num,jstring inout)
{
	int ret;
	const char *port1 =env->GetStringUTFChars(port,NULL);
	const char *inout1 =env->GetStringUTFChars(inout,NULL);
	char buf[3] = {0};
	char buff2[4] = {0};
	strncpy(buf,port1,2);
	strcpy(buf+2,"\0");
	strncpy(buff2,inout1,3);
	ret = strcmp(&buff2[0],"i");
	if(ret == 0)
		strcpy(buff2+2,"\0");
	ret = strcmp(&buff2[0],"o");
	if(ret == 0)
		strcpy(buff2+3,"\0");
	gpio_setDirection(buf,num,buff2);
	env->ReleaseStringUTFChars(port,port1);
	env->ReleaseStringUTFChars(inout,inout1);

	return env->NewStringUTF("ok");

}

int OpenDev()
{

	int err,fd1;
		struct stat st;
		sprintf(dev_name,"/dev/pwm");
		if(-1 == stat(dev_name,&st)) {
		 		ALOGE("Cannot identify '%s': %d, %s", dev_name, errno, strerror (errno));
		 		return -1;

		}
		fd1 = open(dev_name,O_RDWR);
		if( -1 == fd1){
			ALOGE("Cannot open '%s': %d, %s", dev_name, errno, strerror (errno));
			return -1;
		}
		return fd1;


}

int CloseDev(int fd)
{

	if(-1 == close(fd)){
			ALOGE("close err");
			return -1;
		}
		return 0;

}

JNIEXPORT jint  JNICALL Java_com_example_gpio1_MainActivity_enablePwm(JNIEnv * env, jobject obj)
{

	ALOGE("pwm enable");
	int ret,pwmfd;
	pwmfd = OpenDev();
	if(pwmfd == -1)
	{
		ALOGE("pwm open err");
		return -1;
	}
	ALOGE("pwmfd == %d\n",pwmfd);
	pwm.flag = 0;
	pwm.enable = 1;
	pwm.value = 4000;
	ret = write(pwmfd,&pwm,sizeof(struct pwm_data));
	if(-1 == ret){
			ALOGE("pwm err write");
	}
	CloseDev(pwmfd);
	ALOGE("pwmfd colse== %d\n",pwmfd);
	return 0;
}
JNIEXPORT jint  JNICALL Java_com_example_gpio1_MainActivity_enablePwmTime(JNIEnv * env, jobject obj,jint msecond)
{
		ALOGE("pwm enable Time");
		int ret,pwmfd;

		pwmfd = OpenDev();
		if(pwmfd == -1)
		{
			ALOGE("pwm open err");
			return -1;
		}

		ALOGE("pwmfd == %d\n",pwmfd);

		pwm.flag = 0;
		pwm.enable = 1;
		pwm.value = 4000;
		pwm.time = msecond;
		ret = write(pwmfd,&pwm,sizeof(struct pwm_data));
		if(-1 == ret){
				ALOGE("pwm err write");
		}

		usleep(msecond*1000);
		pwm.flag = 0;
		pwm.enable = 0;
		pwm.value = 0;
		ret = write(pwmfd,&pwm,sizeof(struct pwm_data));
		if(-1 == ret){
			ALOGE("pwm err write");
		}

		ALOGE("pwmfd == %d\n",pwmfd);
		//close(fd1);
		CloseDev(pwmfd);

		return 0;
}
JNIEXPORT jint  JNICALL Java_com_example_gpio1_MainActivity_enablePwmUseKernelTimer(JNIEnv * env, jobject obj,jint msecond)
{
		ALOGE("pwm enable Time");
		int ret,pwmfd;

		pwmfd = OpenDev();
		if(pwmfd == -1)
		{
			ALOGE("pwm open err");
			return -1;
		}

		ALOGE("pwmfd == %d\n",pwmfd);
		pwm.flag = 1;
		pwm.enable = 1;
		pwm.value = 4000;
		pwm.time = msecond;
		ret = write(pwmfd,&pwm,sizeof(struct pwm_data));
		if(-1 == ret){
				ALOGE("pwm err write");
		}
		ALOGE("pwmfd == %d\n",pwmfd);
		//close(fd1);
		CloseDev(pwmfd);

		return 0;
}
JNIEXPORT jint  JNICALL Java_com_example_gpio1_MainActivity_disablePwm(JNIEnv * env, jobject obj)
{

	ALOGE("pwm disable");
	int ret,pwmfd;
	pwmfd = OpenDev();
	ALOGE("pwmfd == %d\n",pwmfd);
	if(pwmfd == -1)
	{
		ALOGE("pwm open err");

		return -1;
	}
	pwm.flag = 0;
    pwm.enable = 0;
	   pwm.value = 0;

	ret = write(pwmfd,&pwm,sizeof(struct pwm_data));
		if(-1 == ret){
			ALOGE("pwm err write");
		}
	CloseDev(pwmfd);
	ALOGE("pwmfd colse== %d\n",pwmfd);
	return 0;

}

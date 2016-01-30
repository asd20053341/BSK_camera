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

#define TAG "x806JniApi"
#define ALOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define SERIAL_ERR -1
#define SERIAL_TRUE 0

//jint fd;//serial file descript

int ser_set(int nSpeed, int nBits, char nEvent, int nStop,int fd)
{
    struct termios newtio,oldtio;
    if  ( tcgetattr( fd,&oldtio)  !=  0)
    {
        perror("SetupSerial 1");
        return SERIAL_ERR;
    }
    bzero( &newtio, sizeof( newtio ) );
    newtio.c_cflag  |=  CLOCAL | CREAD;
    newtio.c_cflag &= ~CSIZE;

    switch( nBits )
    {
    case 7:
        newtio.c_cflag |= CS7;
        break;
    case 8:
        newtio.c_cflag |= CS8;
        break;
    }

    switch( nEvent )
    {
    case 'O':                     //奇校验
        newtio.c_cflag |= PARENB;
        newtio.c_cflag |= PARODD;
        newtio.c_iflag |= (INPCK | ISTRIP);
        break;
    case 'E':                     //偶校验
        newtio.c_iflag |= (INPCK | ISTRIP);
        newtio.c_cflag |= PARENB;
        newtio.c_cflag &= ~PARODD;
        break;
    case 'N':                    //无校验
        newtio.c_cflag &= ~PARENB;
        break;
    }

switch( nSpeed )
    {
    case 2400:
        cfsetispeed(&newtio, B2400);
        cfsetospeed(&newtio, B2400);
        break;
    case 4800:
        cfsetispeed(&newtio, B4800);
        cfsetospeed(&newtio, B4800);
        break;
    case 9600:
        cfsetispeed(&newtio, B9600);
        cfsetospeed(&newtio, B9600);
        break;
    case 115200:
        cfsetispeed(&newtio, B115200);
        cfsetospeed(&newtio, B115200);
        break;
    default:
        cfsetispeed(&newtio, B9600);
        cfsetospeed(&newtio, B9600);
        break;
    }
    if( nStop == 1 )
    {
        newtio.c_cflag &=  ~CSTOPB;
    }
    else if ( nStop == 2 )
    {
        newtio.c_cflag |=  CSTOPB;
    }
    newtio.c_cc[VTIME]  = 0;
    newtio.c_cc[VMIN] = 0;
    tcflush(fd,TCIFLUSH);
    if((tcsetattr(fd,TCSANOW,&newtio))!=0)
    {
    	ALOGD("com set error");
        return SERIAL_ERR;
    }
    printf("set done!\n");
    return SERIAL_TRUE;
}

int ser_open(int comport)
{
    jlong  vdisable;
    int fd;
    if (comport==0)
    {
    	fd = open( "/dev/ttyS0", O_RDWR|O_NOCTTY|O_NDELAY);
        if (-1 == fd)
        {
        	ALOGD("Can't Open Serial Port");
            return SERIAL_ERR;
        }
        else
        {
        	ALOGD("open ttyS0 .....\n");
        }
    }
    else if(comport==1)
    {
    	fd = open( "/dev/ttyS1", O_RDWR|O_NOCTTY|O_NDELAY);
        if (-1 == fd)
        {
        	ALOGD("Can't Open Serial Port");
            return SERIAL_ERR;
        }
        else
        {
        	ALOGD("open ttyS1 .....\n");
        }
    }
    else if (comport==2)
    {
        fd = open( "/dev/ttyS2", O_RDWR|O_NOCTTY|O_NDELAY);
        if (-1 == fd)
        {
        	ALOGD("Can't Open Serial Port");
            return SERIAL_ERR;
        }
        else
        {
        	ALOGD("open ttyS2 .....\n");
        }
    }
    else if(comport == 3){
    	fd = open( "/dev/ttyS3", O_RDWR|O_NOCTTY|O_NDELAY);
    	if (-1 == fd)
    	{
    	       ALOGD("Can't Open Serial Port");
    	       return SERIAL_ERR;
    	}
    	else
    	{
    	       ALOGD("open ttyS3 .....\n");
    	}
    } else {
    	ALOGD("##########SERIAL PORT IS NOT EXIST !#############\n");
    }
    if(fcntl(fd, F_SETFL, 0)<0)
    {
    	ALOGD("fcntl failed!\n");
    }
    else
    {
    	ALOGD("fcntl=%d\n",fcntl(fd, F_SETFL,0));
    }
    if(isatty(STDIN_FILENO)==0)
    {
        printf("standard input is not a terminal device\n");
    }
    else
    {
    	ALOGD("isatty success!\n");
    }
    ALOGD("fd-open=%d\n",fd);
    return fd;
}



int sread_one_byte(int * value,int fd)
{
	int ret;
	char buf;

	ALOGD("sread_one_byte  \n");

	if(read(fd, &buf, 1) != 1)
		{
			ALOGD("No data  \n");
			return SERIAL_ERR;
		}

	ALOGD("receive : %x\n", buf);
	*value = (int)buf;
	return SERIAL_TRUE;
}

int swrite_one_byte(char data,int fd)
{
	int ret;
	int count = 10;

	do{
		if((ret = write(fd, &data, 1)) == 1){
//#ifdef JNI_DEBUG
			ALOGD("send a byte: %c !\n",data);
//#endif
			break;
		}

	}while(count--);
	return SERIAL_TRUE;
}

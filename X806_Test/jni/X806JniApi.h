#ifndef __X806JNIAPI_H__
#define __X806JNIAPI_H__

//#define JNI_DEBUG

int ser_set(int nSpeed, int nBits, char nEvent, int nStop, int fd);
int ser_open(int comport);
int sread_one_byte(int * value, int fd);
int swrite_one_byte(char data, int fd);

#endif

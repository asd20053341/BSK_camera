package com.example.x806;

interface IPerson {
	int recvValueTo(int fd);
	int sendDataInt(int command,int fd);
	int sendDataChar(char command,int fd);
	int openSerialTo(int port);
	int setSerialTo(int nSpeed, int nBits, char nEvent, int nStop,int fd);
	int getSlaveValueTo();
}
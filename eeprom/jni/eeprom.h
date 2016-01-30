#ifndef _EEPROM_BSK_H
#define _EEPROM_BSK_H

int bsk_OpenDev(void);
int bsk_CloseDev(void);
unsigned  char bsk_ReadVersion();
int bsk_WriteVersion(unsigned char value);
int bsk_WriteUuid(const char* uuid);
unsigned char *bsk_ReadUuid();
int bsk_WriteSn(const char * sn);
unsigned char  *bsk_ReadSn();
int  bsk_WriteBatch(const char *batch);
unsigned char  *bsk_ReadBatch();

//int bsk_ChangeNomalOffset(unsigned char flag,unsigned int offset);
#endif

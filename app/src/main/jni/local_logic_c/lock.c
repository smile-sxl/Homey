#include "lock.h"
#include "aes.h"
#include "../utils/android_log_print.h"

int take_hand(unsigned char *out, int *cmd_send_len)
{
	if(out==NULL) return -1;

    uint8 cmd_send[128] = {0};
	memset(out, 0, 128);
	cmd_send[0] = 0x7E;
	cmd_send[1] = 0x00;
	cmd_send[2] = 0x09;
	cmd_send[3] = 0x01;
	cmd_send[4] = 0x01;
	cmd_send[5] = 0x01;
	cmd_send[6] = 0x01;
	cmd_send[7] = 0x01;
	cmd_send[8] = 0x8C;
    
    aesInit();
    int len = (int)Encrypt(cmd_send, out, (uint8 *)"6E4A0002B5A3F393E0A9E50E24DCCA9E", 9);
    aesDestroy();

	*cmd_send_len = len;

	return 0;
}
//获取session
int take_hand_ack(unsigned char *cmd_send, unsigned char *cmd_recv, unsigned char *session, int *session_len)
{
	unsigned char  *p, *q;

	if(cmd_send==NULL || cmd_recv==NULL || session==NULL) return -1;
	if(cmd_recv[3] != 0x02) return -1;

	//cmd_send: 7E 00 09 01 01 01 01 01 8C
	//cmd_recv: 7E 00 09 02 04 04 04 04 99
	p = cmd_send+4;
	q = cmd_recv+4;
	
	memset(session, 0, 128);


	session[0] = *(p+0) ^ *(q+0);
	session[1] = *(p+1) ^ *(q+1);
	session[2] = *(p+2) ^ *(q+2);
	session[3] = *(p+3) ^ *(q+3);
/*	
	session[0] = *(q+0);
	session[1] = *(q+1);
	session[2] = *(q+2);
	session[3] = *(q+3);
*/	
	*session_len = 4;
	return 0;
}
//  out   int  传空  pass  6个字节0
int get_status(unsigned char *out, unsigned char *session, unsigned char *pass, int *cmd_send_len)
{
    int		i;
    if(out==NULL || session==NULL || pass==NULL) return -1;
    
    //0x7E 0x000F 0x07 0x11201070 0x 55 55 55 55 55 55 0x43
    uint8 cmd_send[128] = {0};
    memset(out, 0, 128);
    cmd_send[0] = 0x7E;
    cmd_send[1] = 0x00;
    cmd_send[2] = 0x09;
    cmd_send[3] = 0x15;
    cmd_send[4] = session[0];
    cmd_send[5] = session[1];
    cmd_send[6] = session[2];
    cmd_send[7] = session[3];
    cmd_send[8] = 0x00;
    for(i=0; i<=7; i++) cmd_send[8] += cmd_send[i];

    aesInit();
    int len = (int)Encrypt(cmd_send, out, (uint8 *)"6E4A0002B5A3F393E0A9E50E24DCCA9E", 9);
    aesDestroy();

    *cmd_send_len = len;
	//LOGE("AAAAAAAAAAA");
    return 0;
}

int get_status_ack(unsigned char *cmd_recv, int *lock_status, int *box_status, int *key_status, int *battery_status)
{
    if(cmd_recv==NULL) return -1;
    
    //0x7E 0x0009 0x08 0x11201070 0x40
    if(cmd_recv[3] != 0x16) return -1;
    
//    cmd_recv[8] = 0x02;
//    cmd_recv[9] = 0x03;
//    cmd_recv[10] = 0x04;
//    cmd_recv[11] = 0x0f;
    
    *lock_status = *(char *)(cmd_recv+8);
    *box_status = *(char *)(cmd_recv+9);
    *key_status = *(char *)(cmd_recv+10);
    *battery_status = *(char *)(cmd_recv+11);
    
    return 0;
}



int unlock(unsigned char *out, unsigned char *session, unsigned char *pass, int *cmd_send_len)
{
	int		i;

	if(out==NULL || session==NULL || pass==NULL) return -1;

    //0x7E 0x000F 0x07 0x11201070 0x 55 55 55 55 55 55 0x43
    memset(out, 0, 128);
    uint8 cmd_send[128] = {0};
	cmd_send[0] = 0x7E;
	cmd_send[1] = 0x00;
	cmd_send[2] = 0x0F;
	cmd_send[3] = 0x07;
	cmd_send[4] = session[0];
	cmd_send[5] = session[1];
	cmd_send[6] = session[2];
	cmd_send[7] = session[3];
	cmd_send[8] = pass[0];
	cmd_send[9] = pass[1];
	cmd_send[10] = pass[2];
	cmd_send[11] = pass[3];
	cmd_send[12] = pass[4];
	cmd_send[13] = pass[5];
	cmd_send[14] = 0x00;
	for(i=0; i<=13; i++) cmd_send[14] += cmd_send[i];
    
    aesInit();
    int len = (int)Encrypt(cmd_send, out, (uint8 *)"6E4A0002B5A3F393E0A9E50E24DCCA9E", 15);
    aesDestroy();

	*cmd_send_len = len;
	return 0;
}

int unlock_ack(unsigned char *cmd_recv)
{
	if(cmd_recv==NULL) return -1;
	
	//0x7E 0x0009 0x08 0x11201070 0x40
	if(cmd_recv[3] == 0x08) return 0;

	return 1;
}


int open_key_box(unsigned char *out, unsigned char *session, unsigned char *pass, int *cmd_send_len)
{
	int		i;
	if(out==NULL || session== NULL || pass==NULL) return -1;

	//0x7E 0x000F 0x0D 0x11201070 0x555555555555 0x49
    memset(out, 0, 128);
    uint8 cmd_send[128] = {0};
	cmd_send[0] = 0x7E;
	cmd_send[1] = 0x00;
	cmd_send[2] = 0x0F;
	cmd_send[3] = 0x0D;
	cmd_send[4] = session[0];
	cmd_send[5] = session[1];
	cmd_send[6] = session[2];
	cmd_send[7] = session[3];
	cmd_send[8] = pass[0];
	cmd_send[9] = pass[1];
	cmd_send[10] = pass[2];
	cmd_send[11] = pass[3];
	cmd_send[12] = pass[4];
	cmd_send[13] = pass[5];

	cmd_send[14] = 0x00;
	for(i=0; i<=13; i++) cmd_send[14] += cmd_send[i]; 

    aesInit();
    int len = (int)Encrypt(cmd_send, out, (uint8 *)"6E4A0002B5A3F393E0A9E50E24DCCA9E", 15);
    aesDestroy();
    
	*cmd_send_len = len;
	return 0;
}

int open_key_box_ack(unsigned char *cmd_recv)
{
	if(cmd_recv==NULL) return -1;

	//0x7E 0x0009 0x0E 0x11201070 0x46
	if(cmd_recv[3] == 0x0E) return 0;

	return 1;
}


int read_nfc(unsigned char *out, unsigned char *session, int *cmd_send_len)
{
	int		i;
	if(out==NULL || session==NULL) return -1;

	//0x7E 0x000F 0x11 0x11201070 0x555555555555 0x4D
    memset(out, 0, 128);
    uint8 cmd_send[128] = {0};
	cmd_send[0] = 0x7E;
	cmd_send[1] = 0x00;
	cmd_send[2] = 0x09;
	cmd_send[3] = 0x11;
	cmd_send[4] = session[0];
	cmd_send[5] = session[1];
	cmd_send[6] = session[2];
	cmd_send[7] = session[3];

	cmd_send[8] = 0x00;
	for(i=0; i<=7; i++) cmd_send[8] += cmd_send[i];
    
    aesInit();
    int len = (int)Encrypt(cmd_send, out, (uint8 *)"6E4A0002B5A3F393E0A9E50E24DCCA9E", 9);
    aesDestroy();
    
	*cmd_send_len = len;
	return 0;
}

int read_nfc_ack(unsigned char *cmd_recv, unsigned char *nfc, int *nfc_len)
{
	if(cmd_recv==NULL || nfc==NULL) return -1;

	//0x7E 0x0009 0x12 0x11201070  0x11223344 0xF4
	if(cmd_recv[3] != 0x12) return -1;
	
	memset(nfc, 0, 128);
	nfc[0] = cmd_recv[8];
	nfc[1] = cmd_recv[9];
	nfc[2] = cmd_recv[10];
	nfc[3] = cmd_recv[11];

	*nfc_len = 4;
	return 0;
}

int	set_nfc(unsigned char *out, unsigned char *session, unsigned char *nfc_id, int *cmd_send_len) {
    int		i;
    
    if(out==NULL || session==NULL || nfc_id==NULL) return -1;
    
    //0x7E 000d 0x13 0x11201070 0x11223344 0x00
    memset(out, 0, 128);
    uint8 cmd_send[128] = {0};
    cmd_send[0] = 0x7E;
    cmd_send[1] = 0x00;
    cmd_send[2] = 0x0d;
    cmd_send[3] = 0x13;
    cmd_send[4] = session[0];
    cmd_send[5] = session[1];
    cmd_send[6] = session[2];
    cmd_send[7] = session[3];
    cmd_send[8] = nfc_id[0];
    cmd_send[9] = nfc_id[1];
    cmd_send[10] = nfc_id[2];
    cmd_send[11] = nfc_id[3];
    cmd_send[12] = 0x00;
    for(i=0; i<=11; i++) cmd_send[12] += cmd_send[i];
    
    aesInit();
    int len = (int)Encrypt(cmd_send, out, (uint8 *)"6E4A0002B5A3F393E0A9E50E24DCCA9E", 13);
    aesDestroy();
    
    *cmd_send_len = len;
    
    return 0;
}

int set_nfc_ack(unsigned char *cmd_recv) {
    if(cmd_recv==NULL) return -1;
    //0x7E 0009 0x14 0x11201070 0x00
    if(cmd_recv[3] == 0x14) return 0;
    return 1;
}


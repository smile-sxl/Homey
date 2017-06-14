#include <stdio.h>
#include <string.h>
#include <stdlib.h>

int	take_hand(unsigned char *cmd_send, int *cmd_send_len);
int take_hand_ack(unsigned char *cmd_send, unsigned char *cmd_recv, unsigned char *session, int *session_len);

int get_status(unsigned char *cmd_send, unsigned char *session, unsigned char *pass, int *cmd_send_len);
int get_status_ack(unsigned char *cmd_recv, int *lock_status, int *box_status, int *key_status, int *battery_status);

int	read_nfc(unsigned char *cmd_send, unsigned char *session, int *cmd_send_len);
int read_nfc_ack(unsigned char *cmd_recv, unsigned char *nfc, int *nfc_len);

int	set_nfc(unsigned char *cmd_send, unsigned char *session, unsigned char *nfc_id, int *cmd_send_len);
int set_nfc_ack(unsigned char *cmd_recv);

int	unlock(unsigned char *cmd_send, unsigned char *session, unsigned char *pass, int *cmd_send_len);
int unlock_ack(unsigned char *cmd_recv);

int	open_key_box(unsigned char *cmd_send, unsigned char *session, unsigned char *pass, int *cmd_send_len);
int open_key_box_ack(unsigned char *cmd_recv);

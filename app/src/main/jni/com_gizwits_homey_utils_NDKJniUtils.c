#include <jni.h>
#include <string.h>
#include "./utils/android_log_print.h"
#include "./local_logic_c/lock.h"
#include "../../../../app/src/main/jni/com_gizwits_homey_utils_NDKJniUtils.h"
#include "local_logic_c/aes.h"

JNIEXPORT jbyteArray JNICALL Java_com_gizwits_homey_utils_NDKJniUtils_take
        (JNIEnv *env, jobject obj,jcharArray str,jint len){
    int dataLen = 0;
    unsigned char* data = (*env)->GetCharArrayElements(env,str, 0);
    take_hand(data, &dataLen);
//    LOGE("dataLen:%d",dataLen);
//    int i;
//    for(i=0;i<dataLen;i++){
//        LOGE("%02x", data[i]);
//    }
    jbyteArray bytes = (*env)->NewByteArray(env, dataLen);

    if (bytes != NULL) {
        (*env)->SetByteArrayRegion(env, bytes, 0, dataLen, (jbyte *)data);
        (*env)->DeleteLocalRef(env, str);
    }
    return bytes;
}
JNIEXPORT jbyteArray JNICALL Java_com_gizwits_homey_utils_NDKJniUtils_analysisSession
        (JNIEnv *env, jobject obj, jbyteArray byt){
//    unsigned char array[32];
//    (*env)->GetByteArrayRegion(env,byt,0,32,array);

    unsigned char * data =(char *) (*env)->GetByteArrayElements(env,byt, 0);
//      LOGE("传入大小211111为dataLen:%d", sizeof(data));
//    int y;
//    for(y=0;y< sizeof(array);y++){
//        LOGE("%02x", array[y]);
//    }
    int len;
    aesInit();
    unsigned char* out[128] = {0};
    len = (int)Decrypt(data, out, (uint8 *)"6E4A0002B5A3F393E0A9E50E24DCCA9E", 128);
    LOGE("传出大小为dataLen:%d", len);
    jbyteArray bytes = (*env)->NewByteArray(env, len);
    if (bytes != NULL) {
        (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *)out);
        (*env)->DeleteLocalRef(env, byt);
    }
    aesDestroy();
    return bytes;
}


JNIEXPORT jbyteArray JNICALL Java_com_gizwits_homey_utils_NDKJniUtils_takeAck
        (JNIEnv *env, jobject obj, jbyteArray send,jbyteArray recv){
    unsigned char * data_send =(char *) (*env)->GetByteArrayElements(env,send, 0);
    unsigned char * data_recv =(char *) (*env)->GetByteArrayElements(env,recv, 0);
    //    unsigned char array[32];
//    (*env)->GetByteArrayRegion(env,byt,0,32,array);
    unsigned char* session[128] = {0};
    int len=0;

    take_hand_ack(data_send,data_recv,session,&len);
    jbyteArray bytes = (*env)->NewByteArray(env, len);
    if (bytes != NULL) {
        (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *)session);
        (*env)->DeleteLocalRef(env, send);
        (*env)->DeleteLocalRef(env, recv);
    }
    return bytes;
}


JNIEXPORT jbyteArray JNICALL Java_com_gizwits_homey_utils_NDKJniUtils_getStatus
        (JNIEnv *env, jobject onj, jbyteArray session,jbyteArray pass){
    unsigned char * data_session =(char *) (*env)->GetByteArrayElements(env,session, 0);
    unsigned char * data_pass =(char *) (*env)->GetByteArrayElements(env,pass, 0);
    unsigned char* out[128] = {0};
    int len=0;
    get_status(out,data_session,data_pass,&len);

    jbyteArray bytes = (*env)->NewByteArray(env, len);

    if (bytes != NULL) {
        (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *)out);
        (*env)->DeleteLocalRef(env, session);
        (*env)->DeleteLocalRef(env, pass);
    }
    return bytes;
}

JNIEXPORT jbyteArray JNICALL Java_com_gizwits_homey_utils_NDKJniUtils_readNFC
        (JNIEnv *env, jobject obj,jbyteArray str){
    int dataLen = 0;
    unsigned char * data =(char *) (*env)->GetByteArrayElements(env,str, 0);
    unsigned char* out[128] = {0};
    read_nfc(out,data, &dataLen);

    jbyteArray bytes = (*env)->NewByteArray(env, dataLen);

    if (bytes != NULL) {
        (*env)->SetByteArrayRegion(env, bytes, 0, dataLen, (jbyte *)out);
        (*env)->DeleteLocalRef(env, str);
    }
    return bytes;
}

JNIEXPORT jbyteArray JNICALL Java_com_gizwits_homey_utils_NDKJniUtils_setNFC
        (JNIEnv *env, jobject onj, jbyteArray session,jbyteArray nfc){
    unsigned char * data_session =(char *) (*env)->GetByteArrayElements(env,session, 0);
    unsigned char * data_nfc =(char *) (*env)->GetByteArrayElements(env,nfc, 0);
    unsigned char* out[128] = {0};
    int len=0;

    set_nfc(out,data_session,data_nfc,&len);

    jbyteArray bytes = (*env)->NewByteArray(env, len);

    if (bytes != NULL) {
        (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *)out);
        (*env)->DeleteLocalRef(env, session);
        (*env)->DeleteLocalRef(env, nfc);
    }
    return bytes;
}

JNIEXPORT jbyteArray JNICALL Java_com_gizwits_homey_utils_NDKJniUtils_unLock
        (JNIEnv *env, jobject onj, jbyteArray session,jbyteArray pass){
    unsigned char * data_session =(char *) (*env)->GetByteArrayElements(env,session, 0);
    unsigned char * data_pass =(char *) (*env)->GetByteArrayElements(env,pass, 0);
    unsigned char* out[128] = {0};
    int len=0;

    unlock(out,data_session,data_pass,&len);

    jbyteArray bytes = (*env)->NewByteArray(env, len);

    if (bytes != NULL) {
        (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *)out);
        (*env)->DeleteLocalRef(env, session);
        (*env)->DeleteLocalRef(env, pass);
    }
    return bytes;
}

JNIEXPORT jbyteArray JNICALL Java_com_gizwits_homey_utils_NDKJniUtils_openKeyBox
        (JNIEnv *env, jobject onj, jbyteArray session,jbyteArray pass){
    unsigned char * data_session =(char *) (*env)->GetByteArrayElements(env,session, 0);
    unsigned char * data_pass =(char *) (*env)->GetByteArrayElements(env,pass, 0);
    unsigned char* out[128] = {0};
    int len=0;

    open_key_box(out,data_session,data_pass,&len);

    jbyteArray bytes = (*env)->NewByteArray(env, len);

    if (bytes != NULL) {
        (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *)out);
        (*env)->DeleteLocalRef(env, session);
        (*env)->DeleteLocalRef(env, pass);
    }
    return bytes;
}
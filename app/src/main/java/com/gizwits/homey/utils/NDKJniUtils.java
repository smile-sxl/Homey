package com.gizwits.homey.utils;

/**
 * Created by Smile on 2016/10/11.
 */
public class NDKJniUtils {
    public native byte[] take(char[] bys,int len);
    public native byte[] analysisSession(byte [] bts);
    public native byte[] readNFC(byte[] bys);
    public native byte[] takeAck(byte [] send,byte [] recv);
    public native byte[] getStatus(byte [] session,byte[] pass);
    public native byte[] setNFC(byte [] session,byte []nfc);
    public native byte[] unLock(byte [] session,byte[] pass);
    public native byte[] openKeyBox(byte [] session,byte[] pass);
    static {
        System.loadLibrary("JniLibName");
    }
}

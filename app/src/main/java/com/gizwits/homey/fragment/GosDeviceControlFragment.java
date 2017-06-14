package com.gizwits.homey.fragment;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.homey.R;
import com.gizwits.homey.utils.BluetoothLeService;
import com.gizwits.homey.utils.NDKJniUtils;
import com.gizwits.homey.view.SwitchButton;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * Created by Smile on 2016/9/20.
 */
public class GosDeviceControlFragment extends Fragment {

    /**
     * 蓝牙管理器
     */
    private BluetoothManager mBluetoothManager;
    /**
     * 蓝牙适配器
     */
    private BluetoothAdapter mBluetoothAdapter;

    private static final String TAG = "GosDeviceControlFragment";

    /**
     * 处理器
     */
    private Handler mHandler;
    private Handler handler;
    /**
     * 一小时后停止查找搜索
     */
    private static final long SCAN_PERIOD = 3600000;//一小时
    /**
     * fragment_control
     */
    private View view;
    /**
     * 蓝牙设备的mac 及address ，id ,name
     */
    private String mac;
    private String address;
    /**
     * 蓝牙服务
     */
    private BluetoothLeService mBluetoothLeService;
    /**
     * serviceUUID  及 characteristicUUID
     */
    public static String serviceUUID = "6e4a0001-b5a3-f393-e0a9-e50e24dcca9e";
    public static String characteristicUUID = "6e4a0002-b5a3-f393-e0a9-e50e24dcca9e";
    public static BluetoothGattCharacteristic mBGC;

    /**
     * 蓝牙返回的32个字节的数据
     */
    private byte[] getData = new byte[32];
    /**
     * 从蓝牙获取的session
     */
    private byte[] session = new byte[4];
    /**
     * 调用JNI
     */
    private NDKJniUtils jni;
    /**
     * 界面的控件
     */
    private RelativeLayout mrlTop;
    private RelativeLayout mrlBottom;
    private TextView mtvLock;
    private TextView mtvLockBox;
    private TextView mtvKey;
    private TextView mtvBat;
    private SwitchButton mSwitch;
    private TextView mtvConnect;

    /**
     * 等待框
     */
    private LinearLayout mLoading;
    /**
     * 弹出框
     */
    private AlertDialog alertDialog;
    private View mDialogView;

    /**
     * Notification 管理器  （本地推送）
     */
    private NotificationManager nManager;
    /**
     * 是否可以发送控制指令
     */
    private boolean isGONE = false;
    /**
     * 是否上锁了   （true 为上锁  false为没上锁）
     */
    private boolean isLockBox = true;
    private boolean isLock = true;
    /**
     * 是否是第一次进入界面（用来判断此时是否开锁盒）
     */
    private boolean isONE=true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "这里是控制的onCreateView: ");
        mac = getArguments().getString("mac");
        address = getAddress(mac);
        view = inflater.inflate(R.layout.fragment_control, null);
        mrlTop = (RelativeLayout) view.findViewById(R.id.linear_top);
        mrlBottom = (RelativeLayout) view.findViewById(R.id.linear_bottom);
        mtvLock = (TextView) view.findViewById(R.id.tv_lock);
        mtvLockBox = (TextView) view.findViewById(R.id.tv_lockbox);
        mtvKey = (TextView) view.findViewById(R.id.tv_key);
        mtvBat = (TextView) view.findViewById(R.id.tv_bat);
        mtvConnect = (TextView) view.findViewById(R.id.tv_connect);
        mSwitch = (SwitchButton) view.findViewById(R.id.switch_keybox);
        mLoading = (LinearLayout) view.findViewById(R.id.linear_loading);
        Log.e("test", "这里是GosDeviceControlFragment");
        Log.e(TAG, "onResume: ");
//步骤1：创建一个SharedPreferences接口对象
        SharedPreferences read = getActivity().getSharedPreferences("lock", getActivity().MODE_PRIVATE);
        //步骤2：获取文件中的值
        boolean value = read.getBoolean(mac, true);
        mSwitch.setChecked(value);
        mrlTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGONE) {
                    if (session != null) {
                        unLock(session);
                        isGONE = false;
                    }
                }
                //mBluetoothLeService.writeCharacteristic(mBGC,getSession);
            }
        });
        mrlBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGONE) {
                    if (session != null) {
                        openKeyBox(session);
                        isGONE = false;
                    }
                }
            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("lock", getActivity().MODE_PRIVATE).edit();
                //步骤2-2：将获取过来的值放入文件
                editor.putBoolean(mac, isChecked);
                //步骤3：提交
                editor.commit();
            }
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "这里是控制的onCreate ");
        mHandler = new Handler();
        handler = new Handler();
        //获取适配器
        mBluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), getResources().getString(R.string.nophone), Toast.LENGTH_SHORT).show();
        }

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.nodevice), Toast.LENGTH_SHORT).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            // 弹对话框的形式提示用户开启蓝牙
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
            //mBluetoothAdapter.enable(); // 强制开启，不推荐使用
        }
        //Log.e(TAG, "是否连接成功"+ b);
        //scanLeDevice(true);

        if (getActivity() == null) {
            return;
        }
        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Intent gattServiceIntent = new Intent(getActivity(), BluetoothLeService.class);
        getActivity().bindService(gattServiceIntent, mServiceConnection, getActivity().BIND_AUTO_CREATE);
        scanLeDevice(true);


    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
        getActivity().unbindService(mServiceConnection);
        mBluetoothLeService = null;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {// 初始化一个参考本地蓝牙适配器。
                getActivity().finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.在成功的启动初始化时自动连接到设备。
            mBluetoothLeService.connect(address);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;

            // Log.e(TAG, "onServiceDisconnected: " );
        }
    };


    // Handles various events fired by the Service.处理服务所发射的各种事件。
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read
    // or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {// 连接成功
//                Toast.makeText(context, BluetoothLeService.ACTION_GATT_CONNECTED,
//                        Toast.LENGTH_SHORT).show();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {// 连接断开
//                Toast.makeText(context, "ERROR" + BluetoothLeService.ACTION_GATT_DISCONNECTED,
//                        Toast.LENGTH_SHORT).show();
                mtvConnect.setText(getResources().getString(R.string.searching));
                mLoading.setVisibility(View.VISIBLE);
                isGONE = true;
                isONE=true;
                scanLeDevice(true);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {// 搜索服务成功
                checkOwn(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {// 接收数据
                // displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                if (intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA) == null) {
                    Log.e(TAG, " NULL");
                    scanLeDevice(true);
                    return;
                }
                //session="ABCD12AC";
                byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                zhuan(value);
                Log.e(TAG, "长度为: " + value.length);
                //将数据转成32字节
                if (value.length == 20) {
                    for (int i = 0; i < 20; i++) {
                        getData[i] = value[i];
                    }
                    for (int j = 20; j < 32; j++) {
                        getData[j] = 0;
                    }
                } else {
                    for (int i = 0; i < 12; i++) {
                        getData[i + 20] = value[i];
                    }
                }
                if (getData[30] != 0) {

                    Log.e(TAG, "获取的数据: " + Arrays.toString(getData));
                    byte[] b = jni.analysisSession(getData);
                    if (b != null) {
                        zhuan(b);

                        //获取session成功，开始获取状态
                        if (b[3] == 2) {
                            //session=new byte[4];
                            byte[] send = {0x7E, 0x00, 0x09, 0x01, 0x01, 0x01, 0x01, 0x01, (byte) 0x8C};
                            zhuan(send);
                            zhuan(b);
                            session = jni.takeAck(send, b);
                            if (session != null) {
                                zhuan(session);


                                getHomeyState(session);
                            }
                        }
                        //获取状态成功，开始获取NFC
                        if (b[3] == 0x16) {
                            if (session != null) {
                                String s2 = b[9] + "";
                                String s1 = b[8] + "";
                                if (s1.equals("1")) {
                                    isLock = false;
                                    mtvLock.setText(getResources().getString(R.string.lock)+getResources().getString(R.string.on));
                                } else {
                                    mtvLock.setText(getResources().getString(R.string.lock)+getResources().getString(R.string.off));
                                    isLock = true;
                                }
                                if (s2.equals("1")) {
                                    isLockBox = false;
                                    mtvLockBox.setText(getResources().getString(R.string.lockbox)+getResources().getString(R.string.on));
                                } else {
                                    isLockBox = true;
                                    mtvLockBox.setText(getResources().getString(R.string.lockbox)+getResources().getString(R.string.off));
                                }



                                mtvKey.setText(getResources().getString(R.string.key) + b[10]);
                                mtvBat.setText(getResources().getString(R.string.bat) + b[11] + "%");
                                if(isONE){
                                    if (mSwitch.isChecked()) {
                                        openKeyBox(session);
                                    }
                                    isONE = false;
                                }

//                                if (!isONE) {
//
//
//
//                                }

                            }
                        }
                        //获取NFC成功，开始设置NFC
//                        if (b[3] == 0x12) {
//                            if (session != null) {
//                                byte[] nfcData = new byte[4];
//                                System.arraycopy(b, 8, nfcData, 0, 4);
//                                setNFC(session, nfcData);
//                            }
//                        }
                        //设置NFC成功
//                        if (b[3] == 0x14) {
//                            handler.removeCallbacks(run1);
//                            handler.postDelayed(run1, 30000);
//                        }
                        //锁被打开
                        if (b[3] == 0x08) {
                            isGONE = true;
                            handler.removeCallbacks(run1);
                            handler.postDelayed(run1, 30000);
                        }

                        if (b[3] == 0x0E) {
                            handler.removeCallbacks(run1);
                            handler.postDelayed(run1, 30000);
                            isGONE = true;
                        }
                    }


                }
//
            }
        }
    };
    private static final int ID = 1;

    /**
     * 进行本地推送
     */
    private void setNotify(String content) {
        nManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(getActivity());
        builder.setContentText(content);
        builder.setContentTitle(getResources().getString(R.string.app_name));
        builder.setSmallIcon(R.mipmap.icon);
        builder.setTicker(getResources().getString(R.string.news));
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        Intent appIntent = new Intent(getActivity(), getActivity().getClass());
        //appIntent.setAction(Intent.ACTION_MAIN);
//        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        appIntent.setComponent(new ComponentName(getActivity().getPackageName(),
//                getActivity().getPackageName() + "." + getActivity().getLocalClassName()));
//        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, appIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        nManager.notify(ID, notification);
    }


    Runnable run1 = new Runnable() {
        @Override
        public void run() {
            if (!isLockBox&&isLock) {
                String notLockBox = getResources().getString(R.string.notLockBox);
                showAlertDialog(notLockBox);
                setNotify(notLockBox);
            }
            if (!isLock&&isLockBox) {
                String notLock = getResources().getString(R.string.notLock);
                showAlertDialog(notLock);
                setNotify(notLock);
            }
            if (!isLock&&!isLockBox) {
                String notLock = getResources().getString(R.string.notLock2);
                showAlertDialog(notLock);
                setNotify(notLock);
            }

        }
    };

    /**
     * 将字节数组以16进制的形式显示
     *
     * @param b
     */
    private void zhuan(byte[] b) {
        StringBuilder stringBuilder = null;
        if (b != null && b.length > 0) {
            stringBuilder = new StringBuilder(b.length);
            for (byte byteChar : b)
                stringBuilder.append(String.format("%02X ", byteChar));
        }
        Log.e(TAG, "zhuan: " + stringBuilder.toString());
    }

    /**
     * 弹出还没有上锁对话框
     */
    private void showAlertDialog(String content) {

        alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(true);
        mDialogView = View.inflate(getActivity(),
                R.layout.alert_gos_hint, null);

        alertDialog.getWindow().setContentView(mDialogView);
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = alertDialog.getWindow()
                .getAttributes();
        lp.width = (int) (display.getWidth() * 0.8); // 设置宽度
        alertDialog.getWindow().setAttributes(lp);

        TextView tvAllocate = (TextView) mDialogView.findViewById(R.id.tv_allocate);
        //getResources().getString(R.string.notLock)
        tvAllocate.setText(content);
        bindDialogEvent();
    }

    private void bindDialogEvent() {
        LinearLayout allocate = (LinearLayout) mDialogView
                .findViewById(R.id.linear_allocate);
        allocate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                alertDialog.dismiss();
                isGONE = true;
            }
        });

    }

    /**
     * 扫描设备
     *
     * @param enable
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "run: " + device.getAddress());
                    if (mBluetoothLeService != null) {
                        if (device != null && device.getName() != null) {
                            if (device.getName().length() == 20) {
                                if (device.getName().startsWith("KEY-BOX-")) {
                                    if (device.getAddress().equals(address)) {
                                        Log.e(TAG, "当前的蓝牙设备: " + device.getAddress());
                                        boolean b = mBluetoothLeService.connect(address);
                                        Log.e(TAG, "是否连接成功" + b);
                                        Log.e(TAG, "这里执行吗");
                                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                    }
                                }
                            }

                        }
                    }

                }
            });
        }
    };

    /**
     * 得到房间状态
     *
     * @param session
     */
    private void getHomeyState(byte[] session) {
        byte[] pass = {00, 00, 00, 00, 00, 00};
        byte[] status = jni.getStatus(session, pass);
        if (status != null) {
            zhuan(status);
            byte[] headStatus = new byte[20];
            byte[] footStatus = new byte[12];
            System.arraycopy(status, 0, headStatus, 0, 20);
            System.arraycopy(status, 20, footStatus, 0, 12);
            Log.e(TAG, "headbyts: " + Arrays.toString(headStatus));
            Log.e(TAG, "footbyts: " + Arrays.toString(footStatus));
            if (headStatus != null && mBluetoothLeService != null) {
                mBluetoothLeService.writeCharacteristic(mBGC, headStatus);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
            if (footStatus != null && mBluetoothLeService != null) {
                mBluetoothLeService.writeCharacteristic(mBGC, footStatus);
            }
        }

    }

    /**
     * 获取NFC
     *
     * @param session
     */
//    private void getNFC(byte[] session) {
//        byte[] nfc = jni.readNFC(session);
//        if (nfc != null) {
//            byte[] headNFC = new byte[20];
//            byte[] footNFC = new byte[12];
//            System.arraycopy(nfc, 0, headNFC, 0, 20);
//            System.arraycopy(nfc, 20, footNFC, 0, 12);
//            zhuan(headNFC);
//            zhuan(footNFC);
//            if (headNFC != null && mBluetoothLeService != null) {
//                mBluetoothLeService.writeCharacteristic(mBGC, headNFC);
//            }
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                return;
//            }
//            if (footNFC != null && mBluetoothLeService != null) {
//                mBluetoothLeService.writeCharacteristic(mBGC, footNFC);
//            }
//        }
//
//
//    }

    /**
     * 设置NFC
     *
     * @param session
     * @param nfcData
     */
//    private void setNFC(byte[] session, byte[] nfcData) {
//        byte[] nfc = jni.setNFC(session, nfcData);
//        if (nfc != null) {
//            byte[] headSetNFC = new byte[20];
//            byte[] footSetNFC = new byte[12];
//            System.arraycopy(nfc, 0, headSetNFC, 0, 20);
//            System.arraycopy(nfc, 20, footSetNFC, 0, 12);
//            zhuan(headSetNFC);
//            zhuan(footSetNFC);
//            if (headSetNFC != null && mBluetoothLeService != null) {
//                mBluetoothLeService.writeCharacteristic(mBGC, headSetNFC);
//            }
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                return;
//            }
//            if (footSetNFC != null && mBluetoothLeService != null) {
//                mBluetoothLeService.writeCharacteristic(mBGC, footSetNFC);
//
//            }
//        }
//
//
//    }

    /**
     * 打开锁头
     *
     * @param session
     */
    private void unLock(byte[] session) {
        byte[] pass = {00, 00, 00, 00, 00, 00};
        byte[] unlock = jni.unLock(session, pass);
        if (unlock != null) {
            zhuan(unlock);
            byte[] headLock = new byte[20];
            byte[] footLock = new byte[12];
            System.arraycopy(unlock, 0, headLock, 0, 20);
            System.arraycopy(unlock, 20, footLock, 0, 12);
            Log.e(TAG, "headlock: " + Arrays.toString(headLock));
            Log.e(TAG, "footlock: " + Arrays.toString(footLock));
            if (headLock != null && mBluetoothLeService != null) {
                mBluetoothLeService.writeCharacteristic(mBGC, headLock);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
            if (footLock != null && mBluetoothLeService != null) {
                mBluetoothLeService.writeCharacteristic(mBGC, footLock);
            }
        }

    }

    /**
     * 打开锁盒
     *
     * @param session
     */
    private void openKeyBox(byte[] session) {
        byte[] pass = {00, 00, 00, 00, 00, 00};
        byte[] openKey = jni.openKeyBox(session, pass);
        if (openKey != null) {
            zhuan(openKey);
            byte[] headOpenKey = new byte[20];
            byte[] footOpenKey = new byte[12];
            System.arraycopy(openKey, 0, headOpenKey, 0, 20);
            System.arraycopy(openKey, 20, footOpenKey, 0, 12);
            Log.e(TAG, "headOpenKey: " + Arrays.toString(headOpenKey));
            Log.e(TAG, "footOpenKey: " + Arrays.toString(footOpenKey));
            if (headOpenKey != null && mBluetoothLeService != null) {
                mBluetoothLeService.writeCharacteristic(mBGC, headOpenKey);

            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
            if (footOpenKey != null && mBluetoothLeService != null) {
                mBluetoothLeService.writeCharacteristic(mBGC, footOpenKey);
            }
        }

    }

    /**
     * 得到address
     *
     * @param mac
     * @return
     */
    private String getAddress(String mac) {
        char[] macs = mac.toCharArray();
        String s = "";
        for (int x = 0; x < macs.length; x++) {
            Log.e(TAG, "macs " + ((x + 1) / 2));
            s = s + macs[x];
            if (((x + 1) % 2) == 0) {
                s = s + ":";
            }
        }
        s = s.substring(0, 17);
        return s;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * 查询对应服务
     *
     * @param gattServices
     */
    private void checkOwn(List<BluetoothGattService> gattServices) {
        for (BluetoothGattService bluetoothGattService : gattServices) {
            Log.e(TAG, "checkOwn: " + bluetoothGattService.getUuid());

            if (bluetoothGattService.getUuid().toString().equals(serviceUUID)) {
                mBGC = bluetoothGattService.getCharacteristic(UUID.fromString(characteristicUUID));
                if (mBGC != null) {
                    //开启notify
                    boolean b=mBluetoothLeService.setCharacteristicNotification(mBGC, true);

                    if(b){
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);

                        mLoading.setVisibility(View.GONE);
                        mtvConnect.setText(R.string.connect);
                        jni = new NDKJniUtils();

                        char[] bys = new char[128];
                        int len = 32;
                        // int x=jni.take(bys,len).length;
                        byte[] bytes = jni.take(bys, len);
                        if (bytes != null) {
                            // zhuan(bytes);
                            byte[] headbyts = new byte[20];
                            byte[] footbyts = new byte[12];
                            System.arraycopy(bytes, 0, headbyts, 0, 20);
                            System.arraycopy(bytes, 20, footbyts, 0, 12);
                            Log.e(TAG, "headbyts: " + Arrays.toString(headbyts));
                            Log.e(TAG, "footbyts: " + Arrays.toString(footbyts));
                            // Log.e(TAG, "onCreate: "+ Arrays.toString(bytes));
                            if (headbyts != null && mBluetoothLeService != null) {
                                mBluetoothLeService.writeCharacteristic(mBGC, headbyts);
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                return;
                            }
                            if (footbyts != null && mBluetoothLeService != null) {
                                mBluetoothLeService.writeCharacteristic(mBGC, footbyts);
                            }
                    }

                    }

                }
            }
        }
    }
}

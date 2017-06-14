package com.gizwits.homey.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.homey.R;
import com.gizwits.homey.utils.PullDownElasticImp;
import com.gizwits.homey.view.PullDownScrollView;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 添加设备蓝牙锁房间界面
 * Created by Administrator on 2016/9/13.
 */
public class GosAddDeviceListActivity extends Activity implements PullDownScrollView.RefreshListener{

    /** 蓝牙管理器  */
    private BluetoothManager mBluetoothManager;
    /** 蓝牙适配器  */
    private BluetoothAdapter mBluetoothAdapter;
    /** 自定义蓝牙设备ListView 适配器  */
    private ListView mlist;
    private LockDeviceListAdapter mLeDeviceListAdapter;
    /** 自定义蓝牙ArrayLIst  */
    private ArrayList<BluetoothDevice> mLeDevices;
    /** 处理器  */
    private Handler mHandler;

    /** 1小时后停止查找搜索  */
    private static final long SCAN_PERIOD = 3600000;

    /** 返回  */
    private LinearLayout mback;

    private static final String TAG = "GosAddDeviceListActivity";
    private PullDownScrollView refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setContentView(R.layout.activity_gos_add_devicelist);
        initView();
        initEvent();
    }

    private void initView() {
        mlist = (ListView) findViewById(R.id.list_devices);
        mback = (LinearLayout) findViewById(R.id.locklist_back);
        refresh=(PullDownScrollView)findViewById(R.id.refresh_devicelist);
        refresh.setRefreshListener(this);
        refresh.setPullDownElastic(new PullDownElasticImp(getApplicationContext()));
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initEvent() {
        //获取适配器
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, getResources().getString(R.string.nophone), Toast.LENGTH_SHORT).show();
        }

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.nodevice), Toast.LENGTH_SHORT).show();
        }
        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(GosAddDeviceListActivity.this, GosAddLockActivity.class);
                BluetoothDevice device = mLeDevices.get(i);
                Bundle bundle = new Bundle();
                bundle.putParcelable("BluetoothDevice", device);
                intent.putExtras(bundle);
                Log.e("Test", "点击了" + device.getName());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            // 弹对话框的形式提示用户开启蓝牙
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
            //mBluetoothAdapter.enable(); // 强制开启，不推荐使用
        }
        //Initializes list view adapter.
        mLeDeviceListAdapter = new LockDeviceListAdapter();
        mlist.setAdapter(mLeDeviceListAdapter);
        mLeDeviceListAdapter.clear();
        scanLeDevice(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }



    /**
     * 扫描蓝牙设备
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

    @Override
    public void onRefresh(PullDownScrollView view) {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Date date=new Date();
                DateFormat df = new SimpleDateFormat("HH:mm");
                String time=getResources().getString(R.string.refresh_time)+df.format(date);
                refresh.finishRefresh(time);
            }
        }, 2000);
    }


    // Adapter for holding devices found through scanning.
    private class LockDeviceListAdapter extends BaseAdapter {

        private LayoutInflater mInflator;

        public LockDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = GosAddDeviceListActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }


        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.item_lock, null);
                viewHolder = new ViewHolder();
                viewHolder.lockName = (TextView) view.findViewById(R.id.tv_lockname);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.lockName.setText(deviceName);
            }
            return view;
        }
    }

    /**
     * 搜索到蓝牙设备 添加到适配器
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.e("test", "得到的设备decice名称：" + device.getName());
            Log.e(TAG, "得到的设备decice "+device.toString() );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device != null&&device.getName()!=null) {
                        if (device.getName().length() == 20) {
                            if (device.getName().startsWith("KEY-BOX-")) {
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }
    };

    static class ViewHolder {
        TextView lockName;
    }
}

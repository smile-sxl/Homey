package com.gizwits.homey.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.homey.R;
import com.gizwits.homey.base.GosBaseActivity;
import com.gizwits.homey.utils.NetUtils;
import com.gizwits.homey.view.SlideListView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 蓝牙锁房间列表界面
 * Created by Administrator on 2016/9/9.
 */
public class GosDeviceListActivity extends Activity implements View.OnClickListener {
    /** 设置  */
    private LinearLayout set;
    /** 刷新  */
    private LinearLayout refresh;
    /** 设置弹框  */
    private Dialog setDialog;
    /** 注销弹框  */
    private Dialog logoutDialog;
    private View mDialogView;
    private View mlogoutView;
    /** 蓝牙设备房间集合  */
    private ArrayList<HashMap> mlist;
    /** 蓝牙设备房间适配器  */
    private LeDeviceListAdapter mLeDeviceListAdapter;
    /** 可滑动删除的ListView  */
    private SlideListView mlistView;
    /** 存储器 */
    public SharedPreferences spf;
    private static final String TAG = "GosDeviceListActivity";
    private LinearLayout mLoading;
    private Button mbtnNoDevice;
    private LinearLayout mllDeviceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gos_device_list);
        spf = getSharedPreferences(GosBaseActivity.SPF_Name, Context.MODE_PRIVATE);
        initView();
        initEvent();
    }

    private void initEvent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        boolean b = bundle.getBoolean("isExist", false);
        if (b) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.lock_exist));
            builder.setPositiveButton(getResources().getString(R.string.besure), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }


        Log.e(TAG, "这里执行好吗2");
        mlist = new ArrayList<HashMap>();

        try {
            String phone = spf.getString("UserName", "");
            getRoomList(phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refresh.setOnClickListener(this);
        set.setOnClickListener(this);
        mlistView.initSlideMode(SlideListView.MOD_RIGHT);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                HashMap hashMap = mlist.get(position);
                int i = Integer.parseInt(hashMap.get("id").toString());
                String name=hashMap.get("name").toString();
                String mac = hashMap.get("mac").toString();
                boolean isOwner = Boolean.parseBoolean(hashMap.get("isOwner").toString());
                if (isOwner) {
                    Intent intent = new Intent(GosDeviceListActivity.this, GosDeviceOwnerControlActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", i);
                    bundle.putString("mac", mac);
                    bundle.putString("name",name);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(GosDeviceListActivity.this, GosDeviceGuestControlActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", i);
                    bundle.putString("mac", mac);
                    bundle.putString("name",name);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            }
        });
        mbtnNoDevice.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "这里执行好吗1");

    }

    private void initView() {
        set = (LinearLayout) findViewById(R.id.linear_set);
        refresh = (LinearLayout) findViewById(R.id.linear_refresh);
        mlistView = (SlideListView) findViewById(R.id.listview_devices);
        mLoading=(LinearLayout)findViewById(R.id.linear_devicelistloading);
        mbtnNoDevice=(Button)findViewById(R.id.btn_NoDevice);
        mllDeviceList=(LinearLayout)findViewById(R.id.ll_devicelist);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.linear_set:
                showSetDialog();
                break;
            case R.id.linear_refresh:
                mLoading.setVisibility(View.VISIBLE);
                try {
                    String phone = spf.getString("UserName", "");
                    getRoomList(phone);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_NoDevice:
                startActivity(new Intent(GosDeviceListActivity.this, GosAddDeviceListActivity.class));
                break;
        }
    }

    private void showSetDialog() {

        setDialog = new AlertDialog.Builder(this).create();
        setDialog.show();
        setDialog.setCanceledOnTouchOutside(true);
        mDialogView = View.inflate(getApplicationContext(),
                R.layout.alert_gos_set, null);

        setDialog.getWindow().setContentView(mDialogView);

        WindowManager windowManager = this.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = setDialog.getWindow()
                .getAttributes();
        lp.width = (int) (display.getWidth() * 0.8); // 设置宽度
        setDialog.getWindow().setAttributes(lp);
        bindDialogEvent();

    }

    private void bindDialogEvent() {
        LinearLayout addDevice = (LinearLayout) mDialogView
                .findViewById(R.id.line_adddevice);
        LinearLayout set = (LinearLayout) mDialogView
                .findViewById(R.id.line_set);
        final LinearLayout logout = (LinearLayout) mDialogView
                .findViewById(R.id.line_logout);

        addDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.e("test", "添加设备");
//                mLeDeviceListAdapter.clear();
//                scanLeDevice(true);
                setDialog.dismiss();
                startActivity(new Intent(GosDeviceListActivity.this, GosAddDeviceListActivity.class));
            }
        });
        set.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setDialog.dismiss();
                startActivity(new Intent(GosDeviceListActivity.this, GosSettiingsActivity.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setDialog.dismiss();
                showLogoutDialog();
            }
        });

    }

    private void showLogoutDialog() {

        logoutDialog = new AlertDialog.Builder(this).create();
        logoutDialog.show();
        logoutDialog.setCanceledOnTouchOutside(true);
        mlogoutView = View.inflate(getApplicationContext(),
        R.layout.alert_gos_logout, null);

        logoutDialog.getWindow().setContentView(mlogoutView);

        WindowManager windowManager = this.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = logoutDialog.getWindow()
                .getAttributes();
        lp.width = (int) (display.getWidth() * 0.8); // 设置宽度
        logoutDialog.getWindow().setAttributes(lp);
        bindLogoutDialogEvent();

    }

    private void bindLogoutDialogEvent() {
        LinearLayout no = (LinearLayout) mlogoutView
                .findViewById(R.id.llNo);
        LinearLayout sure = (LinearLayout) mlogoutView
                .findViewById(R.id.llSure);

        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                logoutDialog.dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                logoutDialog.dismiss();
                spf.edit().putString("UserName","").commit();
                spf.edit().putString("PassWord","").commit();

                Intent intent=new Intent(GosDeviceListActivity.this,GosUserLoginActivity.class);
                startActivity(intent);
                GosDeviceListActivity.this.finish();
            }
        });

    }

    /**
     * 发送获取房间列表命令并处理返回结果
     * @param user
     * @throws JSONException
     */
    private void getRoomList(String user) throws JSONException {

        // 创建xUtils的Http（Post）实例
        RequestParams requestParams = new RequestParams();
        // 设置Header
        requestParams.addHeader("user", user);
        requestParams.addHeader("token", "");
        requestParams.addHeader("Content-Type", "application/json");
        // 设置Body
        // {"r" : "landlord", "p" : "13691467160", "o" : "s", "t" : "rooms"}
        JSONObject all = new JSONObject();
        all.put("cmd", 1001);
        try {
            requestParams.setBodyEntity(new StringEntity(all.toString(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST, NetUtils.urlPath, requestParams, new RequestCallBack<String>() {

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                mLoading.setVisibility(View.GONE);
                Log.e("Apptest", "Post Failed " + arg1 + " " + arg0.toString());
                //Toast.makeText(DeviceListActivity.this, "Post Failed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                mLoading.setVisibility(View.GONE);
                Log.e("Apptest", "arg0" + arg0.result);
                try {
                    JSONObject result = new JSONObject(arg0.result);
                    JSONArray items = result.getJSONArray("items");
                    mlist.clear();
                    Log.e(TAG, "onSuccess: " + mlist.size());
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = new JSONObject(items.get(i).toString());
                        HashMap hashMap = new HashMap();
                        hashMap.put("id", item.get("id"));
                        hashMap.put("name", item.get("name"));
                        hashMap.put("mac", item.get("mac"));
                        hashMap.put("isOwner", item.get("isOwner"));
                        mlist.add(hashMap);
                    }
                    Log.e(TAG, "返回的List: " + mlist);
                    if(mlist.size()>0){
                        mllDeviceList.setVisibility(View.GONE);
                    }else {
                        mllDeviceList.setVisibility(View.VISIBLE);
                    }
                    mLeDeviceListAdapter = new LeDeviceListAdapter();

                    mlistView.setAdapter(mLeDeviceListAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    //发送删除房间命令并处理返回结果
    private void deleteRoom(String user,boolean b, int id) throws JSONException {

        // 创建xUtils的Http（Post）实例
        RequestParams requestParams = new RequestParams();
        // 设置Header
        requestParams.addHeader("user", user);
        requestParams.addHeader("token", "");
        requestParams.addHeader("Content-Type", "application/json");
        // 设置Body
        // {"r" : "landlord", "p" : "13691467160", "o" : "s", "t" : "rooms"}
        JSONObject all = new JSONObject();
        if(b) {
            all.put("cmd", 1005);
        }else {
            all.put("cmd", 1017);
        }
        all.put("id", id);
        try {
            requestParams.setBodyEntity(new StringEntity(all.toString(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST, NetUtils.urlPath, requestParams, new RequestCallBack<String>() {

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                Log.e("Apptest", "Post Failed " + arg1 + " " + arg0.toString());
                //Toast.makeText(DeviceListActivity.this, "Post Failed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                Log.e("Apptest", "arg0" + arg0.result);
                try {
                    JSONObject result = new JSONObject(arg0.result);
                    int errno = Integer.parseInt(result.get("errno").toString());
                    if (errno == 0) {
                        try {
                            String phone = spf.getString("UserName", "");
                            getRoomList(phone);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    //Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {

        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();

            mInflator = GosDeviceListActivity.this.getLayoutInflater();
        }


        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int i) {
            return mlist.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (convertView == null) {
                convertView = mInflator.inflate(R.layout.item_device, null);
                viewHolder = new ViewHolder();
                viewHolder.relativeDelete = (RelativeLayout) convertView.findViewById(R.id.relative_delete);
                viewHolder.deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            HashMap hashMap = mlist.get(position);
            final String deviceName = hashMap.get("name").toString();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            }
            final int x = Integer.parseInt(hashMap.get("id").toString());
            final boolean b=(boolean)hashMap.get("isOwner");
            viewHolder.relativeDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("AppTest", "点击了吗");
                    String phone = spf.getString("UserName", "");
                    try {
                        deleteRoom(phone,b, x);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return convertView;
        }
    }



    static class ViewHolder {
        TextView deviceName;
        RelativeLayout relativeDelete;
    }

    /**
    * 菜单、返回键响应
    */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click(); // 调用双击退出函数
        }
        return false;
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            String doubleClick = (String) getText(R.string.double_click);
            Toast.makeText(this, doubleClick, Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            this.finish();
            System.exit(0);
        }
    }
}

package com.gizwits.homey.activity;


import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.gizwits.homey.R;
import com.gizwits.homey.base.GosBaseActivity;
import com.gizwits.homey.utils.NetUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * 添加锁界面
 * Created by Administrator on 2016/9/13.
 */
public class GosAddLockActivity extends GosBaseActivity {
    /**
     * 蓝牙设备
     */
    private BluetoothDevice mDevice;
    /**
     * 返回
     */
    private LinearLayout mback;
    /**
     * 确定分配
     */
    private Button besure;

    private static final String TAG = "GosAddLockActivity";
    /**
     * 房间名称
     */
    private EditText roomname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gos_add_lock);

        initView();
        initEvent();
    }

    private void initView() {
        mback = (LinearLayout) findViewById(R.id.adddevice_back);
        besure = (Button) findViewById(R.id.btn_besure);
        roomname = (EditText) findViewById(R.id.et_roomname);
    }

    private void initEvent() {
        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mDevice = (BluetoothDevice) bundle.get("BluetoothDevice");
        //Log.e("Test","点击"+mDevice.getName());


        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        besure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mac = mDevice.getName().substring(8);
                String name = roomname.getText().toString();
                Log.e(TAG, "onClick: " + mac);
                String phone = spf.getString("UserName", "");
                Log.e(TAG, "phone: " + phone);
                try {
                    sendJson(mac, name, phone);
                    Log.e(TAG, "执行 ");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }


    /**
     * 发送分配房间蓝牙锁的命令并处理返回结果
     *
     * @param mac
     * @param name
     * @param user
     * @throws JSONException
     */
    private void sendJson(String mac, String name, String user) throws JSONException {

        // 创建xUtils的Http（Post）实例
        RequestParams requestParams = new RequestParams();
        Log.e(TAG, "user" + user + "name" + name + "mac" + mac);
        // 设置Header
        requestParams.addHeader("user", user);
        requestParams.addHeader("token", "");
        requestParams.addHeader("Content-Type", "application/json");
        // 设置Body


        JSONObject all = new JSONObject();

        all.put("cmd", 1003);
        all.put("name", name);
        all.put("mac", mac);

        try {
            requestParams.setBodyEntity(new StringEntity(all.toString(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
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
                        Intent intent1 = new Intent(GosAddLockActivity.this, GosDeviceListActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isExist", false);
                        intent1.putExtras(bundle);
                        startActivity(intent1);
                    } else if (errno == 1) {
                        Intent intent1 = new Intent(GosAddLockActivity.this, GosDeviceListActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isExist", true);
                        intent1.putExtras(bundle);
                        startActivity(intent1);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


}

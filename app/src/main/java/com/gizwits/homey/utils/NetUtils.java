package com.gizwits.homey.utils;




import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.gizwits.homey.activity.GosAddLockActivity;
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
 * Created by Administrator on 2016/9/14.
 */
public class NetUtils {

    public  static String urlPath = "http://123.57.217.201/main/homey/op.php";
    public  static String newurlPath = "http://123.57.217.201/main/homey/opnew.php";
    static int code=99;




}

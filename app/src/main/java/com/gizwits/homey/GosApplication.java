package com.gizwits.homey;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizEventType;
import com.gizwits.gizwifisdk.enumration.GizLogPrintLevel;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;

import java.util.concurrent.ConcurrentHashMap;

public class GosApplication extends Application {

	public static int flag = 0;
    /** app_id */
    public static final String APP_ID="00471aa1e1694c47a53f37cc2a5be511";


    /** app_secret */
    public static final String APP_SECRET="b59c383a71734cba848a5edb26c3438d";

	ConcurrentHashMap<String, Object> cloudServiceMap = new ConcurrentHashMap<String, Object>();

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			// 设置日志打印级别 （默认all且生成文件至SD卡）
			GizWifiSDK.sharedInstance().setLogLevel(GizLogPrintLevel.GizLogPrintAll);


			if (!cloudServiceMap.isEmpty()) {
				// 设置云端服务
				//GizWifiSDK.sharedInstance().setCloudService(cloudServiceMap);
			}

		};
	};

	GizWifiSDKListener gizWifiSDKListener = new GizWifiSDKListener() {

		public void didNotifyEvent(GizEventType eventType, Object eventSource,
				GizWifiErrorCode eventID, String eventMessage) {
			if (GizEventType.GizEventSDK == eventType && GizWifiErrorCode.GIZ_SDK_START_SUCCESS == eventID) {

			} else {
				Log.e("Apptest", "SDK UN OPEN/n" + eventMessage);
			}
		};

		public void didGetCurrentCloudService(GizWifiErrorCode result,
				ConcurrentHashMap<String, String> cloudServiceInfo) {

			if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
				Log.e("Apptest", "CloudService Error: " + result.toString());
			}
		};
	};

	public void onCreate() {
		super.onCreate();

		// 读取配置文件
		String AppID = APP_ID;
		String AppSecret = APP_SECRET;

		if (TextUtils.isEmpty(AppID) || AppID.contains("your_app_id") || TextUtils.isEmpty(AppSecret)
				|| AppSecret.contains("your_app_secret")) {
			if (flag == 0) {
			
			}
			flag++;
		} else {

			// 启动SDK
			GizWifiSDK.sharedInstance().startWithAppID(getApplicationContext(), AppID);

			// 设置日志等级和云端服务
			handler.sendEmptyMessageDelayed(0, 3000);

		}
	};

}

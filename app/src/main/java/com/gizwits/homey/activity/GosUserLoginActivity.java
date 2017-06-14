package com.gizwits.homey.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.homey.R;
import com.gizwits.homey.base.GosUserModuleBaseActivity;

import java.util.Timer;
import java.util.TimerTask;

public class GosUserLoginActivity extends GosUserModuleBaseActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener {



	/** The et Name */
	private static EditText etName;

	/** The et Psw */
	private static EditText etPsw;

	/** The btn Login */
	private Button btnLogin;

	/** The tv Register */
	private TextView tvRegister;

	/** The tv Forget */
	private TextView tvForget;


	/** The cb Laws */
	private CheckBox cbLaws;
	private String name;
	private String pass;
	private LinearLayout mLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.AppTheme);

		setContentView(R.layout.activity_gos_user_login);
		initView();
		Toast.makeText(this,"zhende",3000).show;
		initEvent();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		GizWifiSDK.sharedInstance().setListener(gizWifiSDKListener);

	}

	private void autoLogin() {

		if (TextUtils.isEmpty(spf.getString("UserName", ""))) {
			return;
		}
		if (TextUtils.isEmpty(spf.getString("PassWord", ""))) {
			return;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	private void initView() {
		etName = (EditText) findViewById(R.id.etName);
		etPsw = (EditText) findViewById(R.id.etPsw);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		tvRegister = (TextView) findViewById(R.id.tvRegister);
		tvForget = (TextView) findViewById(R.id.tvForget);
		mLoading=(LinearLayout)findViewById(R.id.linear_loginloading);
		cbLaws = (CheckBox) findViewById(R.id.cbLaws);


	}

	private void initEvent() {
		name=spf.getString("UserName",null);
		pass=spf.getString("PassWord",null);
		etName.setText(name);
		etPsw.setText(pass);
		btnLogin.setOnClickListener(this);
		tvRegister.setOnClickListener(this);
		tvForget.setOnClickListener(this);
        cbLaws.setOnCheckedChangeListener(this);
		if(!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(pass)){
			Log.e("Apptest",name+", "+pass);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					GizWifiSDK.sharedInstance().userLogin(name, pass);
					mLoading.setVisibility(View.VISIBLE);
					btnLogin.setEnabled(false);
					tvRegister.setEnabled(false);
					tvForget.setEnabled(false);
					cbLaws.setEnabled(false);
				}
			},500);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
            case R.id.btnLogin:
                if (TextUtils.isEmpty(etName.getText().toString())) {
                    Toast.makeText(GosUserLoginActivity.this, R.string.toast_name_wrong, toastTime).show();
                    return;
                }
                if (TextUtils.isEmpty(etPsw.getText().toString())) {
                    Toast.makeText(GosUserLoginActivity.this, R.string.toast_psw_wrong, toastTime).show();
                    return;
                }
                String name=etName.getText().toString();
                String password=etPsw.getText().toString();
                GizWifiSDK.sharedInstance().userLogin(name, password);
				mLoading.setVisibility(View.VISIBLE);
				btnLogin.setEnabled(false);

                break;

            case R.id.tvRegister:
                Intent intent = new Intent(GosUserLoginActivity.this, com.gizwits.homey.activity.GosRegisterUserActivity.class);
                startActivity(intent);
                break;
            case R.id.tvForget:
                Intent intent1 = new Intent(GosUserLoginActivity.this, com.gizwits.homey.activity.GosForgetPasswordActivity.class);
                startActivity(intent1);
                break;


        }
	}
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b){
            etPsw.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }else{
            etPsw.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }



	/**
	 * 用户登录回调
	 */
	@Override
	protected void didUserLogin(GizWifiErrorCode result, String uid, String token) {

		progressDialog.cancel();

		if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {// 登录失败
			Toast.makeText(GosUserLoginActivity.this, toastError(result), toastTime).show();
			mLoading.setVisibility(View.GONE);
			btnLogin.setEnabled(true);
			tvRegister.setEnabled(true);
			tvForget.setEnabled(true);
			cbLaws.setEnabled(true);
		} else {// 登录成功

            Toast.makeText(GosUserLoginActivity.this, R.string.toast_login_successful, toastTime).show();
            Intent intent = new Intent(GosUserLoginActivity.this, GosDeviceListActivity.class);
            //intent.putExtra("ThredLogin", true);
			Bundle bundle=new Bundle();
			bundle.putBoolean("isExist",false);
			intent.putExtras(bundle);
            startActivity(intent);
			mLoading.setVisibility(View.GONE);
			btnLogin.setEnabled(true);
			tvRegister.setEnabled(true);
			tvForget.setEnabled(true);
			cbLaws.setEnabled(true);
			if (!TextUtils.isEmpty(etName.getText().toString()) && !TextUtils.isEmpty(etPsw.getText().toString())) {
				spf.edit().putString("UserName", etName.getText().toString()).commit();
				spf.edit().putString("PassWord", etPsw.getText().toString()).commit();
			}
			spf.edit().putString("Uid", uid).commit();
			spf.edit().putString("Token", token).commit();
			finish();

		}
	}

	/**
	 * 解绑推送回调
	 *
	 * @param result
	 */
//	protected void didChannelIDUnBind(GizWifiErrorCode result) {
//		if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
//			Toast.makeText(this, toastError(result), toastTime).show();
//		}
//
//		Log.i("Apptest", "UnBind:" + result.toString());
//	};

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
			Toast.makeText(this, doubleClick, toastTime).show();
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

package com.gizwits.homey.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.homey.R;
import com.gizwits.homey.base.GosBaseActivity;
import com.gizwits.homey.utils.NetUtils;
import com.gizwits.homey.utils.ScreenInfo;
import com.gizwits.homey.utils.WheelMain;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 分配房客界面
 * Created by Smile on 2016/9/21.
 */
public class GosAddUserActivity extends GosBaseActivity implements View.OnClickListener {
    /**
     * 返回
     */
    private LinearLayout mback;
    /**
     * 房客的手机号
     */
    private EditText metGuset;
    /**
     * 入住时间
     */
    private TextView mtvIn;
    /**
     * 退房时间
     */
    private TextView mtvOut;
    /**
     * 确定分配
     */
    private Button mbtnAllot;

    private static final String TAG = "GosAddUserActivity";

    private int id;
    /**
     * 时间选择器
     */
    private WheelMain wheelMain;
    private View timepickerview;
    private AlertDialog timeDialog;
    private Calendar calendar;
    /**
     * 年 月 日  时  分 周几
     */
    private int year;
    private int month;
    private int day;
    private int hour;
    private int min;
    private int weekDay;
    private Date in;
    private Date out;
    private AlertDialog setDialog;
    private View mDialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gos_add_user);
        initView();
        initEvent();

    }

    private void initEvent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        id = bundle.getInt("id");
        mback.setOnClickListener(this);
        mbtnAllot.setOnClickListener(this);
        mtvIn.setOnClickListener(this);
        mtvOut.setOnClickListener(this);
    }

    private void initView() {
        mback = (LinearLayout) findViewById(R.id.adduser_back);
        metGuset = (EditText) findViewById(R.id.et_guest);
        mtvIn = (TextView) findViewById(R.id.tv_indatetime);
        mtvOut = (TextView) findViewById(R.id.tv_outdatetime);
        mbtnAllot = (Button) findViewById(R.id.btn_besureallot);
    }


    /**
     * 发送分配房客房间命令并处理返回结果
     *
     * @param user
     * @param guest
     * @param checkInDatetime
     * @param checkOutDatetime
     * @param id
     * @throws JSONException
     */
    private void allotRoom(String user, String guest, String checkInDatetime, String checkOutDatetime, int id) throws JSONException {

        // 创建xUtils的Http（Post）实例
        RequestParams requestParams = new RequestParams();
        // 设置Header
        requestParams.addHeader("user", user);
        requestParams.addHeader("token", "");
        requestParams.addHeader("Content-Type", "application/json");
        // 设置Body


        JSONObject all = new JSONObject();

        all.put("cmd", 1007);
        all.put("guest", guest);
        all.put("checkInDatetime", checkInDatetime);
        all.put("checkOutDatetime", checkOutDatetime);
        all.put("id", id);

        try {
            requestParams.setBodyEntity(new StringEntity(all.toString(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST, NetUtils.urlPath, requestParams, new RequestCallBack<String>() {

            //发送失败
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                Log.e("Apptest", "Post Failed " + arg1 + " " + arg0.toString());
                //Toast.makeText(DeviceListActivity.this, "Post Failed", Toast.LENGTH_LONG).show();
            }

            //发送成功
            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                Log.e("Apptest", "arg0" + arg0.result);
                try {
                    JSONObject result = new JSONObject(arg0.result);
                    int errno = Integer.parseInt(result.get("errno").toString());
                    if (errno == 0) {
                        Intent intent = getIntent();
                        intent.putExtra("isAllot", true);
                        setResult(101, intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.adduser_back:
                finish();
                break;
            case R.id.btn_besureallot:
                String user = spf.getString("UserName", "");
                String guest = metGuset.getText().toString();
                String checkInDatetime = mtvIn.getText().toString();
                String checkOutDatetime = mtvOut.getText().toString();
                if (!TextUtils.isEmpty(guest) && !TextUtils.isEmpty(checkInDatetime) && !TextUtils.isEmpty(checkOutDatetime)) {

                    if (!guest.contains(" ")&&!guest.equals(user)) {

                        try {
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            in = df.parse(checkInDatetime);
                            out = df.parse(checkOutDatetime);
                            if (out.getTime() > in.getTime()) {
                                allotRoom(user, guest, checkInDatetime, checkOutDatetime, id);
                            } else {
                                showSetDialog();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(GosAddUserActivity.this, getResources().getString(R.string.cerror), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(GosAddUserActivity.this, getResources().getString(R.string.cnull), Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.tv_indatetime:
                //设置入住时间
                LayoutInflater inflater = getLayoutInflater();
                timepickerview = inflater.inflate(R.layout.time, null);
                ScreenInfo screenInfo = new ScreenInfo(GosAddUserActivity.this);
                wheelMain = new WheelMain(getApplicationContext(), timepickerview);
                wheelMain.screenheight = screenInfo.getHeight();
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                min = calendar.get(Calendar.MINUTE);
                weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                wheelMain.setInitDateTimePicker(year, month, day, hour, min, weekDay);
                Log.e(TAG, "year: " + year);
                showInDateTimeDialog(timepickerview);
                break;
            case R.id.tv_outdatetime:
                //设置退房时间
                LayoutInflater inflater1 = getLayoutInflater();
                timepickerview = inflater1.inflate(R.layout.time, null);
                ScreenInfo screenInfo1 = new ScreenInfo(GosAddUserActivity.this);
                wheelMain = new WheelMain(getApplicationContext(), timepickerview);
                wheelMain.screenheight = screenInfo1.getHeight();
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                min = calendar.get(Calendar.MINUTE);
                weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                wheelMain.setInitDateTimePicker(year, month, day, hour, min, weekDay);
                showOutDateTimeDialog(timepickerview);
                break;
        }
    }

    /**
     * 显示入住时间弹框
     *
     * @param v
     */
    private void showInDateTimeDialog(View v) {

        timeDialog = new AlertDialog.Builder(this).create();
        timeDialog.show();
        timeDialog.setCanceledOnTouchOutside(true);

        timeDialog.getWindow().setContentView(v);
        timeDialog.getWindow().setGravity(Gravity.BOTTOM);
        WindowManager windowManager = this.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = timeDialog.getWindow()
                .getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        timeDialog.getWindow().setAttributes(lp);
        bindInDateTimeDialogEvent(v);

    }


    /**
     * 处理入住时间弹框事件
     *
     * @param v
     */
    private void bindInDateTimeDialogEvent(View v) {
        Button no = (Button) v
                .findViewById(R.id.time_cancel);
        Button sure = (Button) v
                .findViewById(R.id.time_besure);

        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                timeDialog.dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                timeDialog.dismiss();
                Calendar calendar = Calendar.getInstance();
                int sec = calendar.get(Calendar.SECOND);
                Log.e(TAG, "onTime: " + wheelMain.getTime() + ":" + sec);

                mtvIn.setText(wheelMain.getTime() + ":" + sec);
            }
        });

    }

    /**
     * 弹出对话框
     */
    private void showSetDialog() {

        setDialog = new AlertDialog.Builder(this).create();
        setDialog.show();
        setDialog.setCanceledOnTouchOutside(true);
        mDialogView = View.inflate(this,
                R.layout.alert_gos_hint, null);

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
        LinearLayout allocate = (LinearLayout) mDialogView
                .findViewById(R.id.linear_allocate);
        TextView mtv = (TextView) mDialogView
                .findViewById(R.id.tv_allocate);
        mtv.setText(getResources().getString(R.string.date_error));

        allocate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setDialog.dismiss();
            }
        });

    }

    /**
     * 显示退房时间弹框
     *
     * @param v
     */
    private void showOutDateTimeDialog(View v) {

        timeDialog = new AlertDialog.Builder(this).create();
        timeDialog.show();
        timeDialog.setCanceledOnTouchOutside(true);

        timeDialog.getWindow().setContentView(v);
        timeDialog.getWindow().setGravity(Gravity.BOTTOM);
        WindowManager windowManager = this.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = timeDialog.getWindow()
                .getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        timeDialog.getWindow().setAttributes(lp);
        bindOutDateTimeDialogEvent(v);

    }

    /**
     * 处理退房时间弹框事件
     *
     * @param v
     */
    private void bindOutDateTimeDialogEvent(View v) {
        Button no = (Button) v
                .findViewById(R.id.time_cancel);
        Button sure = (Button) v
                .findViewById(R.id.time_besure);

        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                timeDialog.dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                timeDialog.dismiss();
                Calendar calendar = Calendar.getInstance();
                int sec = calendar.get(Calendar.SECOND);

                Log.e(TAG, "onTime: " + wheelMain.getTime() + ":" + sec);

                mtvOut.setText(wheelMain.getTime() + ":" + sec);
            }
        });

    }
}

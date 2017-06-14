package com.gizwits.homey.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.homey.R;
import com.gizwits.homey.activity.GosAddUserActivity;
import com.gizwits.homey.base.GosBaseActivity;
import com.gizwits.homey.utils.NetUtils;
import com.gizwits.homey.utils.PullDownElasticImp;
import com.gizwits.homey.view.PullDownScrollView;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Smile on 2016/9/20.
 */
public class GosDeviceUserFragment extends Fragment implements PullDownScrollView.RefreshListener {
    /**  fragment_user  */
    private View view;
    /**  房东及房客的集合  */
    private List<HashMap> ownersList;
    private List<HashMap> guestsList;

    /**
     * 存储器
     */
    public SharedPreferences spf;
    /** 房间的id  */
    private int id;
    /** 房东的listview */
    private ListView mlvOwners;
    /** 可滑动删除的房客listView  */
    private SlideListView mlvGuests;
    private static final String TAG = "GosDeviceUserFragment";
    /**  分配 */
    private LinearLayout mAllot;
    /**  房东 及 房客 适配器  */
    private GuestsAdapter mguestsAdapter;
    private OwnersAdapter mownerAdapter;
    /**  是否分配 */
    private boolean isAllot=false;
    /**  弹出对话框 */
    private AlertDialog setDialog;
    private View mDialogView;
    /**  下拉刷新的ScrollView  */
    private PullDownScrollView mPullDownScrollView;
    private LinearLayout mLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, null);
        Log.e("test", "这里是GosDeviceUserFragment");

        id=getArguments().getInt("id");
        mAllot=(LinearLayout)view.findViewById(R.id.linear_allot);
        mlvGuests=(SlideListView) view.findViewById(R.id.lv_guests);
        mLoading=(LinearLayout)view.findViewById(R.id.linear_userloading);
        mPullDownScrollView = (PullDownScrollView) view.findViewById(R.id.refresh_root);
        mPullDownScrollView.setRefreshListener(this);
        mPullDownScrollView.setPullDownElastic(new PullDownElasticImp(getActivity()));
        mlvOwners=(ListView) view.findViewById(R.id.lv_owners);
        mlvGuests.initSlideMode(SlideListView.MOD_RIGHT);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        spf = getActivity().getSharedPreferences(GosBaseActivity.SPF_Name, Context.MODE_PRIVATE);
        ownersList = new ArrayList<HashMap>();
        guestsList = new ArrayList<HashMap>();
        String phone = spf.getString("UserName", "");
        try {
            getRoomUser(phone, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAllot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), GosAddUserActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id",id);
                intent.putExtras(bundle);
                //执行Intent ★使用startActivityForResult来启动
                startActivityForResult(intent,100);

            }
        });
    }
    /**
     * 复写onActivityResult方法
     * 当SecondActivity页面关闭时，接收SecondActiviy页面传递过来的数据。
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 100 && resultCode == 101)
        {
            isAllot=data.getBooleanExtra("isAllot",false);
            if(isAllot){
                showSetDialog();
            }
        }
    }

    /**
     * 弹出对话框
     */
    private void showSetDialog() {

        setDialog = new AlertDialog.Builder(getActivity()).create();
        setDialog.show();
        setDialog.setCanceledOnTouchOutside(true);
        mDialogView = View.inflate(getActivity(),
                R.layout.alert_gos_hint, null);

        setDialog.getWindow().setContentView(mDialogView);

        WindowManager windowManager = getActivity().getWindowManager();
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

        allocate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setDialog.dismiss();
            }
        });

    }

    /**
     * 获取房间用户
     * @param user
     * @param id
     * @throws JSONException
     */
    private void getRoomUser(String user, int id) throws JSONException {

        // 创建xUtils的Http（Post）实例
        RequestParams requestParams = new RequestParams();
        // 设置Header
        requestParams.addHeader("user", user);
        requestParams.addHeader("token", "");
        requestParams.addHeader("Content-Type", "application/json");
        // 设置Body
        // {"r" : "landlord", "p" : "13691467160", "o" : "s", "t" : "rooms"}
        JSONObject all = new JSONObject();
        all.put("cmd", 1013);
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
                Toast.makeText(getActivity(),getResources().getString(R.string.checknetwork),Toast.LENGTH_LONG).show();
                //Toast.makeText(DeviceListActivity.this, "Post Failed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                try {
                    JSONObject result = new JSONObject(arg0.result);
                    Log.e(TAG, "返回的数据 " + result);
                    int errno = Integer.parseInt(result.get("errno").toString());
                    if (errno == 0) {
                        try {
                            JSONArray owners = result.getJSONArray("owners");
                            ownersList.clear();
                            for (int i = 0; i < owners.length(); i++) {
                                JSONObject item = new JSONObject(owners.get(i).toString());
                                HashMap hashMap = new HashMap();
                                hashMap.put("owner", item.get("owner"));
                                ownersList.add(hashMap);
                            }
                            Log.e(TAG, "返回的ownersList: " + ownersList);
                            if(ownersList!=null){
                                Collections.reverse(ownersList);
                                mownerAdapter = new OwnersAdapter();
                                mlvOwners.setAdapter(mownerAdapter);
                            }
                            JSONArray guests = result.getJSONArray("guests");
                            guestsList.clear();
                            for (int i = 0; i < guests.length(); i++) {
                                JSONObject item = new JSONObject(guests.get(i).toString());
                                HashMap hashMap1 = new HashMap();
                                hashMap1.put("distribution_id",item.get("distribution_id"));
                                hashMap1.put("checkOutDatetime", item.get("checkOutDatetime"));
                                hashMap1.put("guest", item.get("guest"));
                                hashMap1.put("checkInDatetime", item.get("checkInDatetime"));
                                //hashMap1.put("guestName", item.get("guestName"));
                                guestsList.add(hashMap1);
                            }
                            Log.e(TAG, "返回的guestsList: " + guestsList);
                            if(guestsList!=null){
                                Collections.reverse(guestsList);
                                mguestsAdapter = new GuestsAdapter();
                                mlvGuests.setAdapter(mguestsAdapter);
                            }

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

    /**
     * 删除房客
     * @param user
     * @param x
     * @throws JSONException
     */
    private void deleteGuest(String user, final int x) throws JSONException {

        // 创建xUtils的Http（Post）实例
        RequestParams requestParams = new RequestParams();
        // 设置Header
        requestParams.addHeader("user", user);
        requestParams.addHeader("token", "");
        requestParams.addHeader("Content-Type", "application/json");
        // 设置Body
        // {"r" : "landlord", "p" : "13691467160", "o" : "s", "t" : "rooms"}
        JSONObject all = new JSONObject();
        all.put("cmd", 1015);
        all.put("id", x);
        try {
            requestParams.setBodyEntity(new StringEntity(all.toString(),"utf-8"));
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
                Log.e("Apptest", "arg1111"+arg0.result);
                try {
                    JSONObject result=new JSONObject(arg0.result);
                    int errno=Integer.parseInt(result.get("errno").toString());
                    if (errno==0){
                        try {
                            String phone=spf.getString("UserName","");
                            Log.e(TAG, "user"+phone+"ID"+id );
                            getRoomUser(phone,id);
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

    @Override
    public void onRefresh(PullDownScrollView view) {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Date date=new Date();
                DateFormat df = new SimpleDateFormat("HH:mm");
                String time=getResources().getString(R.string.refresh_time)+df.format(date);
                mPullDownScrollView.finishRefresh(time);
                String phone = spf.getString("UserName", "");
                try {
                    getRoomUser(phone, id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);
    }

    /**  房客适配器 */
    public class GuestsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return guestsList.size();
        }

        @Override
        public Object getItem(int position) {
            return guestsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder1 viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.item_guests, null);
                viewHolder = new ViewHolder1();
                viewHolder.miv = (ImageView) convertView.findViewById(R.id.iv_guesthead);
                viewHolder.mtvName = (TextView) convertView.findViewById(R.id.tv_guestname);
                viewHolder.mtvTime = (TextView) convertView.findViewById(R.id.tv_guesttime);
                viewHolder.mDelete = (RelativeLayout) convertView.findViewById(R.id.guest_delete);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder1) convertView.getTag();
            }
            HashMap hashMap=guestsList.get(position);
            String guest=hashMap.get("guest").toString();
            final int x=Integer.parseInt(hashMap.get("distribution_id").toString());
            String checkInDatetime=hashMap.get("checkInDatetime").toString();
            String checkOutDatetime=hashMap.get("checkOutDatetime").toString();
            viewHolder.mtvName.setText(guest);
            viewHolder.mtvTime.setText(checkInDatetime+"　—　"+checkOutDatetime);
            viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phone=spf.getString("UserName","");
                    try {
                        mLoading.setVisibility(View.VISIBLE);
                        deleteGuest(phone,x);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return convertView;
        }
    }

    /** 房东适配器 */
    public class OwnersAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ownersList.size();
        }

        @Override
        public Object getItem(int position) {
            Log.e(TAG, "getItem: " );
            return ownersList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.item_user, null);
                viewHolder = new ViewHolder();
                viewHolder.miv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                viewHolder.mtvName = (TextView) convertView.findViewById(R.id.tv_username);
                viewHolder.mtvTime = (TextView) convertView.findViewById(R.id.tv_usertime);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            HashMap hashMap=ownersList.get(position);
            String owner=hashMap.get("owner").toString();
            viewHolder.mtvName.setText(owner);
//            viewHolder.mtvTime.setText("2016-9-20 — 2016-9-20");
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView miv;
        TextView mtvName;
        TextView mtvTime;
    }

    static class ViewHolder1 {
        ImageView miv;
        TextView mtvName;
        TextView mtvTime;
        RelativeLayout mDelete;
    }


}

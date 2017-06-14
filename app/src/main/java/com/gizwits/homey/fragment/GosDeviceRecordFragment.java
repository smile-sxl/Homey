package com.gizwits.homey.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gizwits.homey.R;
import com.gizwits.homey.base.GosBaseActivity;
import com.gizwits.homey.utils.NetUtils;
import com.gizwits.homey.utils.PullDownElasticImp;
import com.gizwits.homey.view.PullDownScrollView;
import com.gizwits.homey.view.SlideListView;
import com.google.android.gms.playlog.internal.LogEvent;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Smile on 2016/9/20.
 */
public class GosDeviceRecordFragment extends Fragment implements PullDownScrollView.RefreshListener{
    /** Earlier集合 */
    private List<HashMap> mlistEarlier;
    /** Former集合 */
    private List<HashMap> mlistFormer;
    /** fragment_record */
    private View view;
    private static final String TAG = "GosDeviceRecordFragment";
    /** 存储器 */
    public SharedPreferences spf;
    /** 可滑动删除ListView  Earlier 及适配器   */
    private SlideListView mlvEarlier;
    private HistoryAdapter mhistoryAdapter;
    /** 蓝牙锁的id   */
    private int id;
    /** 可滑动删除ListView  Earlier 及适配器   */
    private SlideListView mlvBefore;
    private LinearLayout mBefore;
    /**  下拉刷新的ScrollView  */
    private PullDownScrollView mPullDownScrollView;
    private LinearLayout mLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_record, null);
        id=getArguments().getInt("id");
        mlvEarlier = (SlideListView) view.findViewById(R.id.lv_earlier);
        mlvBefore = (SlideListView) view.findViewById(R.id.lv_before);
        mBefore=(LinearLayout)view.findViewById(R.id.line_former);
        mLoading=(LinearLayout)view.findViewById(R.id.linear_recordloading);
        mPullDownScrollView = (PullDownScrollView) view.findViewById(R.id.refresh_history);
        mPullDownScrollView.setRefreshListener(this);
        mPullDownScrollView.setPullDownElastic(new PullDownElasticImp(getActivity()));
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "这里好像不执行 " );
        mlvEarlier.initSlideMode(SlideListView.MOD_RIGHT);
        mlvBefore.initSlideMode(SlideListView.MOD_RIGHT);
        spf = getActivity().getSharedPreferences(GosBaseActivity.SPF_Name, Context.MODE_PRIVATE);
        mlistEarlier=new ArrayList<HashMap>();
        mlistFormer=new ArrayList<HashMap>();
        String phone = spf.getString("UserName", "");
        try {

            getRoomHistory(phone, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取房间历史记录
     * @param user
     * @param id
     * @throws JSONException
     */
    private void getRoomHistory(String user, int id) throws JSONException {

        // 创建xUtils的Http（Post）实例
        RequestParams requestParams = new RequestParams();
        // 设置Header
        requestParams.addHeader("user", user);
        requestParams.addHeader("token", "");
        requestParams.addHeader("Content-Type", "application/json");
        // 设置Body
        // {"r" : "landlord", "p" : "13691467160", "o" : "s", "t" : "rooms"}
        JSONObject all = new JSONObject();
        all.put("cmd", 1009);
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
                try {
                    JSONObject result = new JSONObject(arg0.result);
                    Log.e(TAG, "这里是result: " + result);
                    int errno = Integer.parseInt(result.get("errno").toString());
                    if (errno == 0) {
                        try {
                            JSONArray items = result.getJSONArray("items");
                            Log.e(TAG, "onSuccess: " + items);
                            mlistEarlier.clear();
                            mlistFormer.clear();
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = new JSONObject(items.get(i).toString());
                                HashMap hashMap = new HashMap();

                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                hashMap.put("recordId", item.get("recordId"));
                                hashMap.put("owner", item.get("owner"));
                                hashMap.put("guest", item.get("guest"));
                                hashMap.put("operationType", item.get("operationType"));
                                hashMap.put("datetime", item.get("datetime"));
                                try {
                                    Date dateTime=getTime();
                                    Log.e(TAG, "上个月的这个时候" +dateTime);
                                    Date dateTime1 = dateFormat.parse(item.get("datetime").toString());
                                    //Log.e(TAG, "添加的时间" +dateTime1);
                                    if(dateTime.compareTo(dateTime1)>0){
                                        mlistFormer.add(hashMap);
                                    }else{
                                        mlistEarlier.add(hashMap);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.e(TAG, "历史记录 "+mlistEarlier);
                            if(mlistEarlier!=null){
                                Collections.reverse(mlistEarlier);
                                mhistoryAdapter=new HistoryAdapter(mlistEarlier);
                                mlvEarlier.setAdapter(mhistoryAdapter);
                            }
                            if(mlistFormer.size()!=0){
                                mBefore.setVisibility(View.VISIBLE);
                                Collections.reverse(mlistFormer);
                                mhistoryAdapter=new HistoryAdapter(mlistFormer);
                                mlvBefore.setAdapter(mhistoryAdapter);
                            }else{
                                mBefore.setVisibility(View.GONE);
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
     * 删除房间历史记录
     * @param user
     * @param x
     * @throws JSONException
     */
    private void deleteHistory(String user, final int x) throws JSONException {

        // 创建xUtils的Http（Post）实例
        RequestParams requestParams = new RequestParams();
        // 设置Header
        requestParams.addHeader("user", user);
        requestParams.addHeader("token", "");
        requestParams.addHeader("Content-Type", "application/json");
        // 设置Body
        // {"r" : "landlord", "p" : "13691467160", "o" : "s", "t" : "rooms"}
        JSONObject all = new JSONObject();
        all.put("cmd", 1011);
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
                            getRoomHistory(phone,id);

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

    /** 得到当前的时间 并往前拖一月*/
    private Date getTime() throws ParseException {
        Calendar calendar   =   new GregorianCalendar();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current=df.format(new Date());
        Date date=df.parse(current);
        calendar.setTime(date);
        calendar.add(calendar.MONTH,-1);//把日期往后增加一月.整数往后推,负数往前移动
        Date date1=calendar.getTime();   //这个时间就是日期往前推一月的结果
        return date1;
    }

    @Override
    public void onRefresh(PullDownScrollView view) {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Date date=new Date();
                DateFormat df = new SimpleDateFormat("HH:mm");
                String time="上次刷新时间:"+df.format(date);
                mPullDownScrollView.finishRefresh(time);
                String phone = spf.getString("UserName", "");
                try {
                    getRoomHistory(phone, id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);
    }

    /**
     * 历史记录适配器
     */
    public class HistoryAdapter extends BaseAdapter {
        private List<HashMap> mlist;
        public HistoryAdapter(List<HashMap> list){
            this.mlist=list;
        }
        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int position) {
            return mlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.item_history, null);
                viewHolder = new ViewHolder();
                viewHolder.miv = (ImageView) convertView.findViewById(R.id.iv_history);
                viewHolder.mtvHistory = (TextView) convertView.findViewById(R.id.tv_history);
                viewHolder.mtvTime = (TextView) convertView.findViewById(R.id.tv_time);
                viewHolder.mdelete = (RelativeLayout) convertView.findViewById(R.id.history_delete);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            HashMap hashMap=mlist.get(position);
            String owner=hashMap.get("owner").toString();
            String guest=hashMap.get("guest").toString();
            String datetime=hashMap.get("datetime").toString();
            int operationType=Integer.parseInt(hashMap.get("operationType").toString());
            if(operationType==0){
                viewHolder.mtvHistory.setText(owner+" "+getResources().getString(R.string.accredit)+""+guest);
                viewHolder.miv.setBackgroundResource(R.mipmap.add1);
            }else if(operationType==1){
                viewHolder.mtvHistory.setText(owner+""+getResources().getString(R.string.delete)+""+guest);
                viewHolder.miv.setBackgroundResource(R.mipmap.delete1);
            }
            viewHolder.mtvTime.setText(datetime);
            final int y = Integer.parseInt(hashMap.get("recordId").toString());
            viewHolder.mdelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phone=spf.getString("UserName","");
                    try {
                        mLoading.setVisibility(View.VISIBLE);
                        deleteHistory(phone,y);
                        Log.e(TAG, "要删除的id"+y );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView miv;
        TextView mtvHistory;
        TextView mtvTime;
        RelativeLayout mdelete;
    }

}

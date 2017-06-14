package com.gizwits.homey.activity;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.gizwits.homey.R;
import com.gizwits.homey.fragment.GosDeviceControlFragment;
import com.gizwits.homey.fragment.GosDeviceRecordFragment;
import com.gizwits.homey.fragment.GosDeviceUserFragment;
import com.gizwits.homey.view.CustomViewPager;
import java.util.ArrayList;

/**
 * 房东控制界面
 * Created by Smile on 2016/9/20.
 */
public class GosDeviceOwnerControlActivity extends FragmentActivity implements View.OnClickListener {
    /** 返回 */
    private LinearLayout back;
    /** 自定义ViewPager */
    private CustomViewPager mvgContral;
    /** fragment集合 */
    private ArrayList<Fragment> fragmentList;
    /** 单选按钮组 */
    private RadioGroup mRadioGroup;
    private static final String TAG = "GosDeviceOwnerControlActivity";
    /** 蓝牙锁房间设备的  id  mac name */
    private int id;
    private String mac;
    private String name;
    private TextView mtvTitle;
    private RadioButton mrbControl;
    private RadioButton mrbRecord;
    private RadioButton mrbUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gos_device_owner_control);
        initView();
        initEvent();
    }



    public static GosDeviceControlFragment getControl(String mac){
        GosDeviceControlFragment controlFragment = new GosDeviceControlFragment();
        Bundle bundle = new Bundle();
        bundle.putString("mac", mac);
        controlFragment.setArguments(bundle);
        return controlFragment;
    }

    public static GosDeviceRecordFragment getRecord(int id){
        GosDeviceRecordFragment recordFragment = new GosDeviceRecordFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        recordFragment.setArguments(bundle);
        return recordFragment;
    }

    public static GosDeviceUserFragment getUser(int id){
        GosDeviceUserFragment userFragment = new GosDeviceUserFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        userFragment.setArguments(bundle);
        return userFragment;
    }
    private void initEvent() {
        mvgContral.setNoScroll(true);
        mvgContral.setOffscreenPageLimit(2);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        id = bundle.getInt("id");
        mac = bundle.getString("mac");
        name=bundle.getString("name");
        mtvTitle.setText(name);
        fragmentList = new ArrayList<Fragment>();
        Fragment controlFragment = GosDeviceOwnerControlActivity.getControl(mac);

        Fragment recordFragment = GosDeviceOwnerControlActivity.getRecord(id);
        Fragment userFragment = GosDeviceOwnerControlActivity.getUser(id);


        fragmentList.add(controlFragment);
        fragmentList.add(recordFragment);
        fragmentList.add(userFragment);

        back.setOnClickListener(this);
        mvgContral.setAdapter(new ContentAdapter(getSupportFragmentManager(), fragmentList));
        //mvgContral.setCurrentItem(0);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_control:
                        mvgContral.setCurrentItem(0);//设置当前的界面并去掉切换页面的动画
                        break;
                    case R.id.rb_record:
                        mvgContral.setCurrentItem(1);//设置当前的界面并去掉切换页面的动画
                        break;
                    case R.id.rb_user:
                        mvgContral.setCurrentItem(2);//设置当前的界面并去掉切换页面的动画
                        break;
                }
            }
        });
        mvgContral.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mRadioGroup.check(R.id.rb_control);
                        break;
                    case 1:
                        mRadioGroup.check(R.id.rb_record);
                        break;
                    case 2:
                        mRadioGroup.check(R.id.rb_user);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
//    private void initRadiaButton() {
//        Drawable[] drawable1 = mrbControl.getCompoundDrawables();
//        drawable1[1].setBounds(DensityUtils.dp2px(this, 0), DensityUtils.dp2px(this, 0),
//                DensityUtils.dp2px(this, 30), DensityUtils.dp2px(this, 26));
//        Drawable[] drawable2 = mrbRecord.getCompoundDrawables();
//        drawable2[1].setBounds(DensityUtils.dp2px(this, 0), DensityUtils.dp2px(this, 0),
//                DensityUtils.dp2px(this, 30), DensityUtils.dp2px(this, 26));
//        Drawable[] drawable3 = mrbUser.getCompoundDrawables();
//        drawable3[1].setBounds(DensityUtils.dp2px(this, 0), DensityUtils.dp2px(this,0),
//                DensityUtils.dp2px(this, 30), DensityUtils.dp2px(this, 26));
//    }
    private void initView() {
        back = (LinearLayout) findViewById(R.id.contral_back);
        mvgContral = (CustomViewPager) findViewById(R.id.contral_viewpager);
        mRadioGroup = (RadioGroup) findViewById(R.id.control_radiogroup);
//        mrbControl=(RadioButton)findViewById(R.id.rb_control);
//        mrbRecord=(RadioButton)findViewById(R.id.rb_record);
//        mrbUser=(RadioButton)findViewById(R.id.rb_user);
        mtvTitle=(TextView)findViewById(R.id.tv_title);
//        initRadiaButton();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contral_back:
                finish();
                break;
        }
    }


    class ContentAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> list;

        public ContentAdapter(FragmentManager fm, ArrayList<Fragment> list) {
            super(fm);
            this.list = list;
        }


        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

    }


}

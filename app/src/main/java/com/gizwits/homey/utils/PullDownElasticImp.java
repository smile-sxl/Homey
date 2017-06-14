package com.gizwits.homey.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gizwits.homey.R;

/**
 * Created by Smile on 2016/11/1.
 */
public class PullDownElasticImp implements IPullDownElastic {
    private View refreshView;
    private ImageView arrowImageView;
    private int headContentHeight;
    private ProgressBar progressBar;
    private TextView tipsTextview;
    private TextView lastUpdatedTextView;

    private Context mContext;
    public PullDownElasticImp(Context context) {
        mContext = context;
        init();
    }


    private void init() {
        // 刷新视图顶端的的view
        refreshView = LayoutInflater.from(mContext).inflate(
                R.layout.header_layout, null);
        // 指示器view
        arrowImageView = (ImageView) refreshView
                .findViewById(R.id.head_arrowImageView);
        // 刷新bar
        progressBar = (ProgressBar) refreshView
                .findViewById(R.id.head_progressBar);
        // 下拉显示text
        tipsTextview = (TextView) refreshView.findViewById(R.id.refresh_hint);
        // 下来显示时间
        lastUpdatedTextView = (TextView) refreshView
                .findViewById(R.id.refresh_time);
        float scale = mContext.getResources().getDisplayMetrics().density;
        headContentHeight = (int)(70 * scale + 0.5f);
    }

    /**
     * @return
     *
     */
    @Override
    public View getElasticLayout() {
        return refreshView;
    }

    /**
     * @return
     *
     */
    @Override
    public int getElasticHeight() {
        return headContentHeight;
    }


    @Override
    public void showArrow(int visibility) {
        arrowImageView.setVisibility(visibility);
    }

    /**
     * @param animation
     *
     */
    @Override
    public void startAnimation(Animation animation) {
        arrowImageView.startAnimation(animation);
    }

    /**
     *
     *
     */
    @Override
    public void clearAnimation() {
        arrowImageView.clearAnimation();
    }


    @Override
    public void showProgressBar(int visibility) {
        progressBar.setVisibility(visibility);
    }

    /**
     * @param tips
     *
     */
    @Override
    public void setTips(String tips) {
        tipsTextview.setText(tips);
    }


    @Override
    public void showLastUpdate(int visibility) {
        lastUpdatedTextView.setVisibility(visibility);
    }

    /**
     * @param text
     *
     */
    public void setLastUpdateText(String text) {
        lastUpdatedTextView.setText(text);
    }


    /**
     * @param state
     * @param isBack
     *
     */
    @Override
    public void changeElasticState(int state, boolean isBack) {
        // TODO Auto-generated method stub

    }

}

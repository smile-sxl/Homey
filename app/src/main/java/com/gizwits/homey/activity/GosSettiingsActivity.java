package com.gizwits.homey.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.gizwits.homey.R;
import com.gizwits.homey.base.GosBaseActivity;


public class GosSettiingsActivity extends GosBaseActivity implements OnClickListener {

	/** The ll About */
	private LinearLayout llAbout;

	/** The Intent */
	Intent intent;
	private LinearLayout llBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gos_settings);

		initView();
		initEvent();
	}

	private void initView() {
		llAbout = (LinearLayout) findViewById(R.id.llAbout);
		llBack = (LinearLayout) findViewById(R.id.set_back);
	}

	private void initEvent() {
		llAbout.setOnClickListener(this);
		llBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llAbout:
			intent = new Intent(GosSettiingsActivity.this, GosAboutActivity.class);
			startActivity(intent);
			break;
			case R.id.set_back:
				finish();
				break;
		default:
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}

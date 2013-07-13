package com.airAd.yaqinghui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import com.weibo.PropertiesService;
import com.weibo.WeiboService;
import com.weibo.WeiboUtil;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.FriendshipsAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ShareActivity extends BaseActivity {
	public static Oauth2AccessToken accessToken;
	public static FriendshipsAPI f;
	public static String weiboContent = "";

	private ImageButton mBack, mFriend;
	private Button mSend;
	private PropertiesService pro;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);
		Environment.getDataDirectory();
		Environment.getRootDirectory();
		init();
		saveResources();

	}

	private void saveResources() {
		// TODO Auto-generated method stub
		Resources res = getResources();
		Bitmap bf = BitmapFactory.decodeResource(res, R.drawable.badge);
		try {
			saveMyBitmap("badge",bf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void init() {
		String temp = null;
		// TODO Auto-generated method stub

		Bundle extras = getIntent().getExtras();
		EditText tv1 = (EditText) findViewById(R.id.weiboContent);
		if (extras != null) {
			temp = extras.getString("uName");
			weiboContent += "@" + temp;
			tv1.setText(weiboContent);
			tv1.setSelection(weiboContent.length());
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(tv1, InputMethodManager.RESULT_SHOWN);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
					InputMethodManager.HIDE_IMPLICIT_ONLY);
		}
		// mBack = (ImageButton) findViewById(R.id.main_banner_left_btn);
		// mBack.setOnClickListener(new BackClick());
		mSend = (Button) findViewById(R.id.main_send);
		mSend.setOnClickListener(new SendClick());
		mFriend = (ImageButton) findViewById(R.id.friend);
		mFriend.setOnClickListener(new FriendClick());

		// 判断微薄是否授权，后台获取好友列表

		pro = new PropertiesService(getBaseContext());
		Properties prop = pro.getProperties();

		if (prop != null) {
			WeiboUtil.token = prop.getProperty("token");
			WeiboUtil.expires_in = prop.getProperty("expires_in");
			WeiboUtil util = new WeiboUtil();
			if (WeiboUtil.token != null && WeiboUtil.expires_in != null) {
				util.initToken(WeiboUtil.token, WeiboUtil.expires_in);
			}
		}

		if (WeiboUtil.accessToken == null
				|| !WeiboUtil.accessToken.isSessionValid()) {

			WeiboUtil weiboUtil = new WeiboUtil();
			weiboUtil.login(this, pro);

		} else {
			ShareActivity.this.startService(new Intent(ShareActivity.this,
					WeiboService.class));
		}

	}

	private final class BackClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			ShareActivity.this.finish();
		}
	}// end inner class

	private final class FriendClick implements OnClickListener {
		@Override
		public void onClick(View v) {

			// 设置切换动画，从右边进入，左边退出
			EditText tv1 = (EditText) findViewById(R.id.weiboContent);
			weiboContent = tv1.getText().toString();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(tv1.getWindowToken(), 0);

			Intent intent = new Intent();
			intent.setClass(ShareActivity.this, ShareFriendActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}
	}// end inner class

	private final class SendClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			sendInfo();
		}

	}// end inner class

	public void sendInfo() {
		StatusesAPI statuses = new StatusesAPI(WeiboUtil.accessToken);
		EditText tv1 = (EditText) findViewById(R.id.weiboContent);
		TextView mWeiboInfo = (TextView) findViewById(R.id.weibo_content);
		String content = tv1.getText().toString();
		content = content + mWeiboInfo.getText().toString();

		// statuses.update(content, null, null, new SendListener());
		statuses.upload(content, "/sdcard/badge.png", null, null, new SendListener());

		// statuses.uploadUrlText( content,"", null, null,
		// new SendListener());
	}

	class SendListener implements RequestListener {

		@Override
		public void onComplete(String arg0) {
			// TODO Auto-generated method stub
			String a = arg0;
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(a);
				// f.friends(screen_name, count, cursor, trim_status, listener);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub

		}

	}

	private void saveMyBitmap(String bitName, Bitmap mBitmap)
			throws IOException {
		File f = new File("/sdcard/" + bitName + ".png");
		f.createNewFile();
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
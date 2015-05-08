package com.zhangchh;

import cn.jpush.im.android.api.JMessageClient;

import com.contact.R;
import com.google.zxing.WriterException;
import com.util.AESUtils;
import com.zxing.encoding.EncodingHandler;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class GenerateRQ extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qrcode);
		
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setHomeButtonEnabled(true);
		
		ImageView qrImgImageView = (ImageView) findViewById(R.id.qrcode_img);

		SharedPreferences settings = getSharedPreferences("Tel_info", 0);
		String name = settings.getString("name", "null");

		if (name == "null") {
			// 跳转到编辑界面...
			Intent editTelInfo = new Intent(GenerateRQ.this, EditTelInfo.class);
			startActivity(editTelInfo);
			finish();
		}
		
		String username = JMessageClient.getMyInfo().getUserName();
		String nameString = settings.getString("name", "");
		String telString1 = settings.getString("tel1", "");
		String telString2 = settings.getString("tel2", "");
		String msg = username + "%" + nameString + "%" + telString1 + "%" + telString2;
		
		try {
			msg = AESUtils.encrypt("21a", msg);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		Log.i("GenerateRQ", "[msg] " + msg);
		Log.i("GenerateRQ", "[username] " + username);
		Log.i("GenerateRQ", "[name] " + nameString);
		Log.i("GenerateRQ", "[tel1] " + telString1);
		Log.i("GenerateRQ", "[tel2] " + telString2);
		
		Bitmap qrCodeBitmap;
		
		try {
			qrCodeBitmap = EncodingHandler.createQRCode(msg, 500);
			qrImgImageView.setImageBitmap(qrCodeBitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		
		Button btn = (Button) findViewById(R.id.qrcode_btn);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent editTelInfo = new Intent(GenerateRQ.this, EditTelInfo.class);
				startActivity(editTelInfo);
				finish();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}

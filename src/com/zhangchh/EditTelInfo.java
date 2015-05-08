package com.zhangchh;

import com.contact.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditTelInfo extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setHomeButtonEnabled(true);
		
		setContentView(R.layout.tel_info);
		final EditText name;
		final EditText tel1, tel2;

		SharedPreferences settings = getSharedPreferences("Tel_info", 0);
		
		name = (EditText) findViewById(R.id.tel_info_name);
		tel1 = (EditText) findViewById(R.id.tel_info_tel1);
		tel2 = (EditText) findViewById(R.id.tel_info_tel2);

		name.setText(settings.getString("name", ""));
		tel1.setText(settings.getString("tel1", ""));
		tel2.setText(settings.getString("tel2", ""));

		Button btn = (Button) findViewById(R.id.tel_info_btn);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// do something
				String nameString = name.getText().toString();
				String telString1 = tel1.getText().toString();
				String telString2 = tel2.getText().toString();
				int valid = 1;
				if (nameString.equals("")) {
					showString("名字不能为空");
					valid = 0;
				}
				if (telString1.equals("") && telString2.equals("")) {
					showString("电话号码不能为空");
					valid = 0;
				}
				if (valid == 1) {
					SharedPreferences mySharedPreferences = getSharedPreferences(
							"Tel_info", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = mySharedPreferences
							.edit();
					editor.putString("name", nameString);
					editor.putString("tel1", telString1);
					editor.putString("tel2", telString2);

					editor.commit();
					Intent intent = new Intent(EditTelInfo.this, GenerateRQ.class);
					startActivityForResult(intent, 0);
					finish();
				}
			}

		});
	}

	private void showString(String string) {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(this)
		.setTitle("Error")
		.setMessage(string)
		.setPositiveButton("确定", null)
		.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	
}

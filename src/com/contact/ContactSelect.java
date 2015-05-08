package com.contact;

import java.util.ArrayList;
import java.util.List;

import com.adapter.ContactSelectAdapter;
import com.adapter.ContactSelectAdapter.ContactViewHolder;
import com.contact.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ContactSelect extends Activity {

	private TextView textView;
	private ListView listView;
	private String tagName;
	private long tagID;

	private ContactDbAdapter dbAdapter;
	
	private ProgressDialog pd;

	// String stringArray[];
	private List<String> nameList;
	private List<Long> idList;

	private ContactSelectAdapter contactAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_select);

		this.getActionBar().setHomeButtonEnabled(true);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		tagName = intent.getStringExtra("tag_name");

		dbAdapter = ContactDbAdapter.getInstance(getApplicationContext());
		dbAdapter.open();
		
		listView = (ListView) findViewById(R.id.contact_select_listview);
		textView = (TextView) findViewById(R.id.contact_select_text);
		pd = new ProgressDialog(this);
		pd.setMessage("���ڼ�����...");
		
		MyAsyncTask task = new MyAsyncTask();
		task.execute(0);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	private void renderListView(Cursor cursor) {
		textView.setText("标签：  " + tagName);
		
		if (cursor == null) {
			return;
		}

		nameList = new ArrayList<String>();
		idList = new ArrayList<Long>();

		if (cursor.moveToFirst()) {
			do {
				String contactName = cursor.getString(cursor
						.getColumnIndexOrThrow(ContactDbAdapter.KEY_NAME));
				long contactID = cursor.getLong(cursor
						.getColumnIndexOrThrow(ContactDbAdapter.KEY_ID));
				nameList.add(contactName);
				idList.add(contactID);
			} while (cursor.moveToNext());
		}

		contactAdapter = new ContactSelectAdapter(getApplicationContext(), nameList);

		listView.setAdapter(contactAdapter);
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setBackgroundColor(Color.WHITE);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ContactViewHolder viewHolder = (ContactViewHolder) view.getTag();
				if (viewHolder.checkBox.isChecked()) {
					contactAdapter.checkBoxFlag.put(position, false);
				} else {
					contactAdapter.checkBoxFlag.put(position, true);
				}
				contactAdapter.notifyDataSetChanged();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contact_select, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return false;
		} else if (id == R.id.contact_select_option) {
			pd = new ProgressDialog(this);
			pd.setMessage("正在加载中...");
			MyAsyncTask task = new MyAsyncTask();
			task.execute(1);
			return false;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class MyAsyncTask extends AsyncTask<Integer, Integer, Cursor> {
		private int id_;
		
		@Override
		protected Cursor doInBackground(Integer... params) {
			Log.i("ContactSelect", "tagName: " + tagName);
			
			id_ = params[0];
			Cursor cur = null;
			
			if (id_ == 0) {
				try {
					tagID = dbAdapter.insertTag(tagName);
					cur = dbAdapter.getContact();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Log.i("ContactSelect", "tagId: " + tagID);
			} else {
				for (int i = 0; i < nameList.size(); i++) {
					boolean check = contactAdapter.checkBoxFlag.get(i);
					if (check == true) {
						dbAdapter.attachTagToPerson(idList.get(i), tagID);
						Log.i("ContactSelect", "attach Tag: "+tagID+" to person: "+idList.get(i));
					}
				}
				finish();
			}
			return cur;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			if (id_ == 0) {
				renderListView(result);
			} else {
				ContactSelect.this.finish();
			}
			pd.dismiss();
		}
	}
}

package com.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adapter.TagSelectAdapter;
import com.adapter.TagSelectAdapter.TagViewHolder;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TagSelect extends Activity {

	public static final String CONTACT_ID = "contact_id";
	public static final String CONTACT_NAME = "contact_name";
	public static final String TAG_LIST = "tag_list";
	public static final String TAG_LIST_NAME = "tag_list_n";

	private TextView textView;
	private ListView listView;
	private long contactID;
	private String contactName;
	private List<Map<String, String>> tagList;
	private List<Long> tagMarkedList;

	private ContactDbAdapter dbAdapter;
	private TagSelectAdapter tagAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_select);

		this.getActionBar().setHomeButtonEnabled(true);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		dbAdapter = ContactDbAdapter.getInstance(this);
		dbAdapter.open();

		textView = (TextView) findViewById(R.id.tag_select_text);
		listView = (ListView) findViewById(R.id.tag_select_listview);
		tagList = new ArrayList<Map<String, String>>();

		Intent intent = getIntent();
		contactName = intent.getStringExtra("contact_name");
		contactID = intent.getLongExtra("contact_id", 0);
		tagMarkedList = new ArrayList<Long>();
		ArrayList<String> stringList = intent
				.getStringArrayListExtra("tag_list");
		for (int i = 0; i < stringList.size(); i++) {
			Long temp = Long.parseLong(stringList.get(i));
			tagMarkedList.add(temp);
		}

		Log.i("TagSelect", "contactName: " + contactName);
		Log.i("TagSelect", "id: " + contactID);
		Log.i("TagSelect", "tagList size: " + tagMarkedList.size());
		
		Cursor cur = dbAdapter.getTag();
		renderListView(cur);
	}
	
	private void renderListView(Cursor cursor) {
		textView.setText("联系人：" + contactName);

		if (cursor != null && cursor.moveToFirst()) {
			do {
				String tagID = cursor.getString(cursor
						.getColumnIndexOrThrow(ContactDbAdapter.KEY_TAG_ID));
				String tagName = cursor.getString(cursor
						.getColumnIndexOrThrow(ContactDbAdapter.KEY_TAG_NAME));
				Map<String, String> mapTemp = new HashMap<String, String>();
				mapTemp.put("tag_id", tagID);
				mapTemp.put("tag_name", tagName);
				tagList.add(mapTemp);
			} while (cursor.moveToNext());
		}

		tagAdapter = new TagSelectAdapter(getApplicationContext(), tagList, tagMarkedList);

		listView.setAdapter(tagAdapter);
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setBackgroundColor(Color.WHITE);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TagViewHolder viewHolder = (TagViewHolder) view.getTag();
				if (viewHolder.checkBox.isChecked()) {
					tagAdapter.checkBoxFlag.put(position, false);
				} else {
					tagAdapter.checkBoxFlag.put(position, true);
				}
				tagAdapter.notifyDataSetChanged();
			}
		});
	}

//	@Override
//	protected void onStop() {
//		super.onStop();
//		if (dbAdapter != null)
//			dbAdapter.close();
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tag_select, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return false;
		} else if (id == R.id.tag_select_option) {
			// ȷ��ѡ���ǩ
			ArrayList<String> tag_select = new ArrayList<String>();
			ArrayList<String> tag_select_n = new ArrayList<String>();
			
			for (int i = 0; i < tagList.size(); i++) {
				boolean finalCheck = tagAdapter.checkBoxFlag.get(i);
				String tagID = tagList.get(i).get("tag_id");
				String tagName = tagList.get(i).get("tag_name");
				
				boolean firstCheck = tagMarkedList.contains(tagID);
				if (!firstCheck && finalCheck) {
				  tag_select.add(tagID);
				  tag_select_n.add(tagName);
				}
			}
			
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putStringArrayList(TAG_LIST, tag_select);
			bundle.putStringArrayList(TAG_LIST_NAME, tag_select_n);
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
			return false;
		}
		return super.onOptionsItemSelected(item);
	}

}

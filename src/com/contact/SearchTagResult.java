package com.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.contact.ContactDbAdapter;
public class SearchTagResult extends Activity {

	private ContactDbAdapter adapter;
	private List<HashMap<String, Object>> datalist = new ArrayList<HashMap<String, Object>>();
	private ListView list_res;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_tag_result);
		
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setHomeButtonEnabled(true);
		
		adapter = ContactDbAdapter.getInstance(this);
		adapter.open();

		list_res = (ListView) findViewById(R.id.res_list_searchtag);
		
		Long tag_id = this.getIntent().getLongExtra(ContactDbAdapter.KEY_TAG_ID, -1);
		
		this.setTitle("标签");
		Cursor cur = adapter.getPeopleByTagId(tag_id);
		
		if (cur != null) {
			
			cur.moveToFirst();
			while (cur.isAfterLast() == false) {
				String name_ = cur.getString(cur.getColumnIndex(ContactDbAdapter.KEY_NAME));
				long id_ = cur.getLong(cur.getColumnIndex(ContactDbAdapter.KEY_ID));
				HashMap<String, Object> m = new HashMap<String, Object>();
				m.put(ContactDbAdapter.KEY_NAME, name_);
				m.put(ContactDbAdapter.KEY_ID, id_);
				datalist.add(m);
				cur.moveToNext();
			}
			cur.close();
			
			String[] from = new String[] {ContactDbAdapter.KEY_NAME};
			int[] to = new int[] {R.id.list_row_text};
			SimpleAdapter ad = new SimpleAdapter(this, datalist, R.layout.list_text, from, to);
			list_res.setAdapter(ad);
			list_res.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(getApplicationContext(), ShowContact.class);
					long id_ = (Long) datalist.get(position).get(ContactDbAdapter.KEY_ID);
					intent.putExtra(ContactDbAdapter.KEY_ID, id_);
					startActivity(intent);
					finish();
				}
				
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_tag_result, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}

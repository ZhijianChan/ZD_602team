package com.contact;

import cn.jpush.im.android.demo.view.PullScrollView;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.contact.ContactDbAdapter;
/**
 * ShowContact is used to display the information of the person
 * 
 * @author Chan
 * @since 2015.5.4
 */
public class ShowContact extends Activity {

	private ImageView iv;
	private TextView remark;
	private TableLayout tag_table;
	private TableLayout tel_table;
	private TableLayout addr_table;
	private RelativeLayout tag_rl;
	private RelativeLayout tel_rl;
	private RelativeLayout addr_rl;
	private PullScrollView sv;
	private ProgressBar pb;
	private ContactDbAdapter adapter;
	
	private static long id = -1;
	private static Cursor cur1 = null;
	private static Cursor cur2 = null;
	private static Cursor cur3 = null;
	private static Cursor cur4 = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_contact);

		ActionBar ab = this.getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		
		initView();
		
		final Intent intent = getIntent();
		id = intent.getLongExtra(ContactDbAdapter.KEY_ID, -1);
		adapter = ContactDbAdapter.getInstance(this);
		adapter.open();
		
		if (id < 0) {
			Toast.makeText(this, "Wrong Contact id", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MyAsyncTask task = new MyAsyncTask();
		task.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cur1 != null) cur1.close();
		if (cur2 != null) cur2.close();
		if (cur3 != null) cur3.close();
		if (cur4 != null) cur4.close();
		cur1 = null;
		cur2 = null;
		cur3 = null;
		cur4 = null;
	}

	private void initView() {
		pb = (ProgressBar) findViewById(R.id.show_progress_bar);
		iv = (ImageView) findViewById(R.id.show_contact_avatar_iv);
		remark = (TextView) findViewById(R.id.show_remark);
		
		tag_rl = (RelativeLayout) findViewById(R.id.show_tag_rl);
		tel_rl = (RelativeLayout) findViewById(R.id.show_tel_rl);
		addr_rl = (RelativeLayout) findViewById(R.id.show_addr_rl);
		
		tag_table = (TableLayout) findViewById(R.id.table_show_tag);
		tel_table = (TableLayout) findViewById(R.id.table_show_tel);
		addr_table = (TableLayout) findViewById(R.id.table_show_addr);
		
		sv = (PullScrollView) findViewById(R.id.show_contact_sv);
		sv.setHeader(iv);
	}
	
	private void inflateNameRemark(Cursor cur) {
		if (cur == null) {
			return;
		}
		cur.moveToFirst();
		String s1 = cur.getString(cur.getColumnIndexOrThrow(ContactDbAdapter.KEY_NAME));
		setTitle(s1);
		
		String s2 = cur.getString(cur.getColumnIndexOrThrow(ContactDbAdapter.KEY_REMARK));
		remark.setText(s2);
	}
	
	private void inflateTag(Cursor cur) {
		tag_table.removeAllViews();
		TableRow tr = new TableRow(this);
		
		if (cur == null) {
			TextView tv = new TextView(this);
			tv.setText("暂时没有标签");
			tv.setPadding(20, 5, 20, 5);
			tv.setTextSize(16);
			tv.setTextColor(Color.rgb(100, 100, 100));
			tv.setBackgroundResource(R.drawable.edittext_shape);
			tr.addView(tv);
			tag_table.addView(tr);
			return;
		}
		
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			String tag_tmp = cur.getString(cur.getColumnIndexOrThrow(ContactDbAdapter.KEY_TAG_NAME));
			TextView tv = new TextView(this);
			tv.setText(tag_tmp);
			tv.setPadding(20, 5, 20, 5);
			tv.setTextSize(16);
			tv.setTextColor(Color.rgb(100, 100, 100));
			tv.setBackgroundResource(R.drawable.edittext_shape);
			tr.addView(tv);

			if (cur.isLast() == false) {
				TextView v = new TextView(this);
				v.setText("  ");
				tr.addView(v);
			}
			
			cur.moveToNext();
		}
		tag_table.addView(tr);
	}

	private void inflateTel(Cursor cur) {
		tel_table.removeAllViews();
		if (cur == null) {
			TableRow tr = new TableRow(this);
			TextView tv = new TextView(this);
			tv.setText("（空）");
			tv.setTextSize(18);
			tv.setTextColor(Color.rgb(100, 100, 100));
			tv.setPadding(0, 5, 0, 5);
			tr.addView(tv);
			tel_table.addView(tr);
			return;
		}
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			final String tel_tmp = cur.getString(cur.getColumnIndexOrThrow(ContactDbAdapter.KEY_TEL));
			TableRow tr = new TableRow(this);
			TextView tv = new TextView(this);
			tv.setText(tel_tmp);
			tv.setTextSize(18);
			tv.setTextColor(Color.BLACK);
			tv.setPadding(0, 5, 0, 5);
			tv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tel_tmp));
					startActivity(intent);
				}
			});
			
			tr.addView(tv);
			
			TextView but = new TextView(this);
			but.setText("短信");
			but.setTextSize(16);
			but.setPadding(0, 5, 0, 5);
			but.setTextColor(Color.rgb(100, 100, 100));
			but.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + tel_tmp));
					startActivity(intent);
				}
			});
			tr.addView(but);
			
			tel_table.addView(tr);
			cur.moveToNext();
		}
	}
	
	private void inflateAddr(Cursor cur) {
		addr_table.removeAllViews();
		if (cur == null) {
			TableRow tr = new TableRow(this);
			TextView tv = new TextView(this);
			tv.setText("（空）");
			tv.setTextColor(Color.rgb(100, 100, 100));
			tv.setPadding(0, 5, 0, 5);
			tv.setTextSize(16);
			tr.addView(tv);
			addr_table.addView(tr);
			return;
		}
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			TableRow tr = new TableRow(this);
			
			String addr_tmp = cur.getString(cur.getColumnIndexOrThrow(ContactDbAdapter.KEY_ADDR));
			TextView tv = new TextView(this);
			tv.setText(addr_tmp);
			tv.setTextColor(Color.rgb(100, 100, 100));
			tv.setPadding(0, 5, 0, 5);
			tv.setTextSize(16);
			tr.addView(tv);
			addr_table.addView(tr);
			cur.moveToNext();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.show_contact, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id_ = item.getItemId();
		if (id_ == android.R.id.home) {
			
			finish();
			
		} else if (id_ == R.id.action_settings) {
			
			Intent intent = new Intent(this, EditContact.class);
			intent.setAction(EditContact.EDIT_ACTION);
			intent.putExtra(ContactDbAdapter.KEY_ID, id);
			startActivityForResult(intent, 604);
			
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 604) {
			if (resultCode == 605) {
				finish();
			}
		}
	}

	private class MyAsyncTask extends AsyncTask<Integer, Integer, Cursor> {
		@Override
		protected Cursor doInBackground(Integer... params) {
			cur1 = adapter.getNameRemarkByPersonId(id);
			cur2 = adapter.getTagByPersonId(id);
			cur3 = adapter.getTelByPersonId(id);
			cur4 = adapter.getAddrByPersonId(id);
			return null;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			pb.setVisibility(View.GONE);
			inflateNameRemark(cur1);
			inflateTag(cur2);
			inflateTel(cur3);
			inflateAddr(cur4);
		}
	}
}

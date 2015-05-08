package com.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import cn.jpush.im.android.api.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.Message;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.api.BasicCallback;

import com.contact.ContactDbAdapter;
import com.util.AESUtils;

/**
 * This activity is used to Edit the information of someone
 * 
 * @author ZJ Chan
 * 
 */
@SuppressLint("UseSparseArrays")
public class EditContact extends Activity implements OnClickListener {

	public static final String EDIT_ACTION = "com.contact.EditContact.edit";
	public static final String INSERT_ACTION = "com.contact.EditContact.insert";
	public static final String SCAN_RES_ACTION = "com.contact.EditContact.scanRes";

	public static final String DATA_TARGET = "target";
	public static final String DATA_NAME = "name";
	public static final String DATA_TEL1 = "tel1";
	public static final String DATA_TEL2 = "tel2";

	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;
	private static final int STATE_SCAN_RES = 2;
	
	private static final int EtHeight = 70;

	private ContactDbAdapter adapter;

	private static int mState;
	private static long id = -1;
	private static Cursor cur1;
	private static Cursor cur2;
	private static Cursor cur3;
	private static Cursor cur4;

	private Button but_confirm;
	private Button but_delete;
	private Button but_add_tel;
	private Button but_add_addr;
	private Button but_edit_tag;
	private EditText edit_name;
	private EditText edit_remark;
	private TableLayout table_tel;
	private TableLayout table_addr;
	private ProgressBar pb;
	private ProgressDialog pd;

	// new value
	private ArrayList<EditText> list_tel = new ArrayList<EditText>();
	private ArrayList<EditText> list_addr = new ArrayList<EditText>();

	// old value
	private String old_name;
	private String old_remark;
	private String[] old_tel;
	private String[] old_addr;
	private int old_tel_cnt = 0;
	private int old_addr_cnt = 0;

	private Map<Long, Boolean> old_tag;
	private Map<Long, Boolean> new_tag;

	// JPush
	private String targetId = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_contact);

		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setHomeButtonEnabled(true);

		adapter = ContactDbAdapter.getInstance(this);
		adapter.open();

		old_tag = new HashMap<Long, Boolean>();
		new_tag = new HashMap<Long, Boolean>();

		Intent intent = getIntent();
		String action = intent.getAction();
		initView();

		if (EDIT_ACTION.equals(action)) {

			mState = STATE_EDIT;
			setTitle("编辑联系人");
			id = intent.getLongExtra(ContactDbAdapter.KEY_ID, -1);

			if (id < 0) {
				Toast.makeText(this, "Wrong Contact id", Toast.LENGTH_LONG).show();
				finish();
			}
			
			MyAsyncTask task = new MyAsyncTask();
			task.execute(Long.valueOf(0));

		} else if (INSERT_ACTION.equals(action)) {

			mState = STATE_INSERT;
			setTitle("新增联系人");
			pb.setVisibility(View.GONE);
			inflateTel(null);
			inflateAddr(null);

		} else if (SCAN_RES_ACTION.equals(action)) {

			mState = STATE_SCAN_RES;
			setTitle("新增联系人");
			pb.setVisibility(View.GONE);
			
			targetId = intent.getStringExtra(DATA_TARGET);
			String name = intent.getStringExtra(DATA_NAME);
			String tel1 = intent.getStringExtra(DATA_TEL1);
			String tel2 = intent.getStringExtra(DATA_TEL2);
			
			edit_name.setText(name);
			if ("".equals(tel1) == false) {
				TableRow tr = new TableRow(this);
				EditText et = addEditText();
				et.setText(tel1);
				tr.addView(et);
				list_tel.add(et);
				table_tel.addView(tr);
				if ("".equals(tel2) == false) {
					tr = new TableRow(this);
					et = addEditText();
					et.setText(tel2);
					tr.addView(et);
					table_tel.addView(tr);
					list_tel.add(et);
				}
			} else {
				if ("".equals(tel2) == false) {
					TableRow tr = new TableRow(this);
					EditText et = addEditText();
					et.setText(tel2);
					table_tel.addView(tr);
					list_tel.add(et);
				} else {
					inflateTel(null);
				}
			}
			inflateAddr(null);
		} else {
			Log.i("EditContact", "EditContact: no such action");
			finish();
		}
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
		pb = (ProgressBar) findViewById(R.id.edit_progress_bar);

		edit_name = (EditText) findViewById(R.id.edit_name);
		edit_remark = (EditText) findViewById(R.id.edit_remark);
		table_tel = (TableLayout) findViewById(R.id.edit_table_tel);
		table_addr = (TableLayout) findViewById(R.id.edit_table_addr);
		but_edit_tag = (Button) findViewById(R.id.edit_but_tag);
		but_add_tel = (Button) findViewById(R.id.add_tel);
		but_add_addr = (Button) findViewById(R.id.add_addr);
		but_confirm = (Button) findViewById(R.id.confirm);
		but_delete = (Button) findViewById(R.id.delete);

		but_edit_tag.setOnClickListener(this);
		but_add_tel.setOnClickListener(this);
		but_add_addr.setOnClickListener(this);
		but_confirm.setOnClickListener(this);
		but_delete.setOnClickListener(this);
	}

	private void inflateNameRemark(Cursor cur) {
		if (cur == null) return;
		
		cur.moveToFirst();
		String s1 = cur.getString(cur
				.getColumnIndexOrThrow(ContactDbAdapter.KEY_NAME));
		edit_name.setText(s1);
		old_name = s1;

		String s2 = cur.getString(cur
				.getColumnIndexOrThrow(ContactDbAdapter.KEY_REMARK));
		edit_remark.setText(s2);
		old_remark = s2;
	}

	private void inflateTag(Cursor cur) {
		if (cur == null) return;
		
		cur.moveToFirst();
		String t = "";
		while (!cur.isAfterLast()) {
			String t2 = cur.getString(cur
					.getColumnIndexOrThrow(ContactDbAdapter.KEY_TAG_NAME));
			long t1 = cur.getLong(cur
					.getColumnIndexOrThrow(ContactDbAdapter.KEY_TAG_ID));
			
			old_tag.put(t1, true);
			new_tag.put(t1, true);

			t += t2;
			if (cur.isLast() == false) t += ", ";

			cur.moveToNext();
		}
		if ("".equals(t) == false) but_edit_tag.setText(t);
	}

	private void inflateTel(Cursor cur) {
		if (cur == null) {
			TableRow tr = new TableRow(this);
			EditText et = addEditText();
			et.setHint(R.string.tel);
			tr.addView(et);
			list_tel.add(et);
			table_tel.addView(tr);
			return;
		}
		cur.moveToFirst();
		old_tel = new String[cur.getCount()];
		while (!cur.isAfterLast()) {
			final String s = cur.getString(cur
					.getColumnIndexOrThrow(ContactDbAdapter.KEY_TEL));
			old_tel[old_tel_cnt++] = s;

			Log.i("EditContact", "[tel] " + s);
			
			TableRow tr = new TableRow(this);
			EditText et = addEditText();
			et.setText(s);
			tr.addView(et);
			list_tel.add(et);
			table_tel.addView(tr);
			cur.moveToNext();
		}
	}

	private void inflateAddr(Cursor cur) {
		if (cur == null) {
			TableRow tr = new TableRow(this);
			EditText et = addEditText();
			et.setHint(R.string.addr);
			tr.addView(et);
			list_addr.add(et);
			table_addr.addView(tr);
			return;
		}
		cur.moveToFirst();
		old_addr = new String[cur.getCount()];
		while (!cur.isAfterLast()) {
			String s = cur.getString(cur
					.getColumnIndexOrThrow(ContactDbAdapter.KEY_ADDR));
			
			old_addr[old_addr_cnt++] = s;

			TableRow tr = new TableRow(this);
			EditText et = addEditText();
			et.setText(s);
			tr.addView(et);
			list_addr.add(et);
			table_addr.addView(tr);
			cur.moveToNext();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit_contact, menu);
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

	private void click_confirm() {
		String new_name = edit_name.getText().toString();
		String new_remark = edit_remark.getText().toString();

		if (mState == STATE_EDIT) { /* update contact */
			// Check name
			if (old_name.equals(new_name) == false) {
				Log.i("EditContact", "[name] " + new_name + " (replace) " + old_name);

				if ("".equals(new_name) == false)
					adapter.updateNameByPersonId(id, new_name);
			}

			// Check remark
			if (old_remark.equals(new_remark) == false) {
				Log.i("EditContact", "[remark] " + new_remark + " (replace) " + old_remark);

				if ("".equals(new_remark) == false)
					adapter.updateRemarkByPersonId(id, new_remark);
			}

			// Check tag
			Iterator<Entry<Long, Boolean>> it1 = old_tag.entrySet().iterator();
			/* Remove old tags */
			while (it1.hasNext()) {
				Map.Entry<Long, Boolean> en = it1.next();
				long tag_id = en.getKey();
				if (!new_tag.containsKey(tag_id)) {
					Log.i("EditContact", "[tag] " + tag_id + " (removed)");

					if (!ContactDbAdapter.checkDebugMode())
						adapter.detachTagfromPerson(id, tag_id);
				} else {
					Log.i("EditContact", "[tag] " + tag_id + " (remains)");
				}
			}
			/* Attach new tags */
			Iterator<Entry<Long, Boolean>> it2 = new_tag.entrySet().iterator();
			while (it2.hasNext()) {
				Map.Entry<Long, Boolean> en = it2.next();
				long tag_id = en.getKey();
				if (!old_tag.containsKey(tag_id)) {
					Log.i("EditContact", "[tag] " + tag_id + " (new)");

					if (!ContactDbAdapter.checkDebugMode())
						adapter.attachTagToPerson(id, tag_id);
				}
			}

			// Check tel
			int k = 0;
			for (; k < old_tel_cnt; k++) {
				String new_tel = list_tel.get(k).getText().toString();
				if (old_tel[k].equals(new_tel) == false) {

					if (new_tel.equals("")) { // delete telephone
						Log.i("EditContact", "[tel] " + old_tel[k] + " (removed)");

						adapter.deleteTelByPersonId(id, old_tel[k]);
					} else { // update telephone
						Log.i("EditContact", "[tel] " + new_tel + " (replace) " + old_tel[k]);

						adapter.updateTelByPersonId(id, old_tel[k], new_tel);
					}
				}
			}
			for (; k < list_tel.size(); k++) {
				String new_tel = list_tel.get(k).getText().toString();
				Log.i("EditContact", "[tel] " + new_tel + " (new)");

				if (!"".equals(new_tel)) adapter.addTelToPerson(id, new_tel);
			}

			// Check address
			k = 0;
			for (; k < old_addr_cnt; k++) {
				String new_addr = list_addr.get(k).getText().toString();

				if (old_addr[k].equals(new_addr) == false) {

					if (new_addr.equals("")) { // delete address
						Log.i("EditContact", "[addr] " + old_addr[k] + " (removed)");

						adapter.deleteAddrByPersonId(id, old_addr[k]);
					} else { // update address
						Log.i("EditContact", "[addr] " + new_addr + " (replace) " + old_addr[k]);

						adapter.updateAddressByPersonId(id, old_addr[k], new_addr);
					}
				}
			}
			for (; k < list_addr.size(); k++) {
				String new_addr = list_addr.get(k).getText().toString();
				Log.i("EditContact", "[addr] " + new_addr + " (new)");

				if (!"".equals(new_addr)) adapter.addAddrToPerson(id, new_addr);
			}

		} else { /* new contact */
			// Insert name and remark
			Log.i("EditContact", "[name] " + new_name + " (new)");
			Log.i("EditContact", "[remark] " + new_remark + " (new)");
			id = adapter.insertNameRemark(new_name, new_remark);

			// Insert tag
			Iterator<Entry<Long, Boolean>> it = new_tag.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Long, Boolean> en = it.next();
				long tag_id = en.getKey();
				Log.i("EditContact", "[tag] " + tag_id + " (new)");

				adapter.attachTagToPerson(id, tag_id);
			}

			// Insert tel
			for (int k = 0; k < list_tel.size(); k++) {
				String new_tel = list_tel.get(k).getText().toString();
				Log.i("EditContact", "[tel] " + new_tel + " (new)");

				if (!"".equals(new_tel)) adapter.addTelToPerson(id, new_tel);
			}

			// Insert addr
			for (int k = 0; k < list_addr.size(); k++) {
				String new_addr = list_addr.get(k).getText().toString();
				Log.i("EditContact", "[addr] " + new_addr + " (new)");

				if (!"".equals(new_addr)) adapter.addAddrToPerson(id, new_addr);
			}
		}
	}

	private void click_delete() {
		if (mState == STATE_EDIT) {
			adapter.deleteContactItem(id);
			this.setResult(605);
		}
	}

	private void finish_task() {
	  pd.dismiss();
	  if (mState == STATE_INSERT || mState == STATE_SCAN_RES) {
		  
	    if (mState == STATE_SCAN_RES && "".equals(targetId) == false) {
	    
		  new AlertDialog.Builder(this).setTitle("Notice").setMessage("是否发送个人信息给对方")
		      .setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditContact.this.finish();
				}})
			  .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				  @Override
				  public void onClick(DialogInterface dialog, int which) {
				    SharedPreferences settings = getSharedPreferences("Tel_info", 0);
					String nameString = settings.getString("name", "");
					String telString1 = settings.getString("tel1", "");
					String telString2 = settings.getString("tel2", "");
					String msg_ = "%" + nameString + "%" + telString1 + "%" + telString2;
					
					try {
						msg_ = AESUtils.encrypt("21a", msg_);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					Log.i("EditContact", "[msg] " + msg_);
					
				    if ("".equals(nameString) && "".equals(telString1) && "".equals(telString2)) {
					  new AlertDialog.Builder(EditContact.this).setTitle("Notice")
					      .setMessage("是否发送个人信息给对方")
					      .setPositiveButton(R.string.confirm, null).show();
					  return;
					}
				    pd = new ProgressDialog(EditContact.this);
				    pd.setMessage("正在发送中...");
				    pd.show();
					Conversation conv = JMessageClient.getConversation(ConversationType.single,targetId);
					if (conv == null) 
						conv = Conversation.createConversation(ConversationType.single,targetId);
					conv.resetUnreadCount();
					TextContent tc = new TextContent(msg_);
					final Message msg = conv.createSendMessage(tc);
					msg.setOnSendCompleteCallback(new BasicCallback() {
					  @Override
					  public void gotResult(final int arg0, String arg1) {
					    if (arg0 != 0) {
						  EditContact.this.runOnUiThread(new Runnable() {
						    @Override
							public void run() {
							  HandleResponseCode.onHandle(EditContact.this,arg0);
							}
						  });
						} else {
							pd.dismiss();
							EditContact.this.finish();
						}
						Toast.makeText(EditContact.this, "发送成功", Toast.LENGTH_SHORT).show();
					  }
					});
					JMessageClient.sendMessage(msg);
				}}).show();
			} else {
				finish();
			}
		} else {
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.edit_but_tag) {
			// startActivityForResult to Tag Manager Activity
			
			Intent intent = new Intent(this, TagSelect.class);
			intent.putExtra(TagSelect.CONTACT_ID, id);
			intent.putExtra(TagSelect.CONTACT_NAME, old_name);

			Iterator<Entry<Long, Boolean>> it1 = new_tag.entrySet().iterator();
			
			ArrayList<String> taglist = new ArrayList<String>();
			while (it1.hasNext()) {
				Map.Entry<Long, Boolean> en = it1.next();
				long tag_id = en.getKey();
				taglist.add(String.valueOf(tag_id));
			}
			intent.putStringArrayListExtra(TagSelect.TAG_LIST, taglist);
			startActivityForResult(intent, 606);
			
		} else if (i == R.id.add_tel) {

			TableRow tr = new TableRow(this);
			EditText et = addEditText();
			et.setHint(R.string.tel);
			tr.addView(et);
			list_tel.add(et);
			table_tel.addView(tr);

		} else if (i == R.id.add_addr) {

			TableRow tr = new TableRow(this);
			EditText et = addEditText();
			et.setHint(R.string.addr);
			tr.addView(et);
			list_addr.add(et);
			table_addr.addView(tr);

		} else if (i == R.id.confirm) {

			String new_name = edit_name.getText().toString();
			if ("".equals(new_name)) {
				Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
				return;
			}

			pd = new ProgressDialog(this);
			pd.setMessage("正在更新中...");
			pd.setCancelable(false);
			pd.show();
			MyAsyncTask task = new MyAsyncTask();
			task.execute(Long.valueOf(1));

		} else if (i == R.id.delete) {

			pd = new ProgressDialog(this);
			pd.setMessage("正在更新中...");
			pd.setCancelable(false);
			pd.show();
			MyAsyncTask task = new MyAsyncTask();
			task.execute(Long.valueOf(2));

		}
	}

	private EditText addEditText() {
		EditText et = new EditText(this);
		et.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		et.setHeight(EtHeight);
		et.setTextSize(17);
		return et;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 606 && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			ArrayList<String> res = bundle.getStringArrayList(TagSelect.TAG_LIST);
			ArrayList<String> res_n = bundle.getStringArrayList(TagSelect.TAG_LIST_NAME);
			
			if (res == null) return;
			
			String t = "";
			new_tag.clear();
			
			for (int i = 0; i < res.size(); i ++) {
				String ss = res.get(i);
				String tt = res_n.get(i);
				
				long index = Long.valueOf(ss);
				
				new_tag.put(index, true);
				
				t += tt;
				
				if (i != res.size() - 1) t += ", ";
			}
			if ("".equals(t))
				but_edit_tag.setText(R.string.edit_tag);
			else
				but_edit_tag.setText(t);
		}
	}

	private class MyAsyncTask extends AsyncTask<Long, Integer, Cursor> {
		private long taskid;

		@Override
		protected Cursor doInBackground(Long... params) {
			taskid = params[0];
			if (taskid == 0) { // loading
				cur1 = adapter.getNameRemarkByPersonId(id);
				cur2 = adapter.getTagByPersonId(id);
				cur3 = adapter.getTelByPersonId(id);
				cur4 = adapter.getAddrByPersonId(id);
			} else if (taskid == 1) { // confirm
				click_confirm();
			} else if (taskid == 2) { // delete
				click_delete();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			if (taskid == 0) {
				pb.setVisibility(View.GONE);
				inflateNameRemark(cur1);
				inflateTag(cur2);
				inflateTel(cur3);
				inflateAddr(cur4);
			} else {
				finish_task();
			}
		}
	}

}

package com.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter.LengthFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnQueryTextListener;

import com.contact.ContactDbAdapter;
import com.util.ChineseUtils;

public class SearchableContact extends Activity implements OnQueryTextListener {

	public static String keyword;
	public List<Map<String, Object>> datalist_name = new ArrayList<Map<String, Object>>();
	public List<Map<String, Object>> datalist_remark = new ArrayList<Map<String, Object>>();
	public List<Map<String, Object>> datalist_tag = new ArrayList<Map<String, Object>>();
	public List<Map<String, Object>> datalist_tel = new ArrayList<Map<String, Object>>();
	public List<Map<String, Object>> datalist_addr = new ArrayList<Map<String, Object>>();

	private TextView text;
	private ListView list_name;
	private ListView list_remark;
	private ListView list_tag;
	private ListView list_tel;
	private ListView list_addr;
	private LinearLayout name_ll;
	private LinearLayout remark_ll;
	private LinearLayout tag_ll;
	private LinearLayout tel_ll;
	private LinearLayout addr_ll;
	private SearchView searchView;
	private ContactDbAdapter adapter;
	private ProgressBar pb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchable_contact);
		initActionbar();
		initView();

		searchView.setOnQueryTextListener(this);
		searchView.setSubmitButtonEnabled(true);

		adapter = ContactDbAdapter.getInstance(this);
		adapter.open();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		text = (TextView) findViewById(R.id.res_text);
		pb = (ProgressBar) findViewById(R.id.search_progress_bar);
		list_name = (ListView) findViewById(R.id.res_list_name);
		list_remark = (ListView) findViewById(R.id.res_list_remark);
		list_tag = (ListView) findViewById(R.id.res_list_tag);
		list_tel = (ListView) findViewById(R.id.res_list_tel);
		list_addr = (ListView) findViewById(R.id.res_list_addr);

		name_ll = (LinearLayout) findViewById(R.id.search_name_ll);
		remark_ll = (LinearLayout) findViewById(R.id.search_remark_ll);
		tag_ll = (LinearLayout) findViewById(R.id.search_tag_ll);
		tel_ll = (LinearLayout) findViewById(R.id.search_tel_ll);
		addr_ll = (LinearLayout) findViewById(R.id.search_addr_ll);

		pb.setVisibility(View.GONE);
		name_ll.setVisibility(View.GONE);
		remark_ll.setVisibility(View.GONE);
		tag_ll.setVisibility(View.GONE);
		tel_ll.setVisibility(View.GONE);
		addr_ll.setVisibility(View.GONE);
	}

	@SuppressLint("InflateParams")
	private void initActionbar() {
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayShowCustomEnabled(true);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View myTitle = inflater
				.inflate(R.layout.custom_action_bar_layout, null);
		ActionBar.LayoutParams params = new ActionBar.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ab.setCustomView(myTitle, params);
		searchView = (SearchView) myTitle.findViewById(R.id.search_view);
		searchView.setSubmitButtonEnabled(false);
		int resid = searchView.getContext().getResources()
				.getIdentifier("android:id/search_src_text", null, null);
		TextView tv = (TextView) searchView.findViewById(resid);
		tv.setTextColor(Color.WHITE);
	}

	private void sortResult(Cursor cur) {
		datalist_name.clear();
		datalist_remark.clear();
		datalist_tag.clear();
		datalist_tel.clear();
		datalist_addr.clear();

		if (cur == null)
			return;

		cur.moveToFirst();

		// Pattern pat = Pattern.compile("[#@]");
		// Matcher mat = pat.matcher(keyword);

		// ����ȷ��������ʽ�ҳ�#��@��������ɿջ�ո�
		// keyword = keyword.replaceAll("#|@"," ");

		String[] keywordSplit = keyword.split(" ");
		HashMap<Long, Boolean> checkID = new HashMap<Long, Boolean>();
		
		// Sorting Result
		while (!cur.isAfterLast()) {
			long id = Long.valueOf(cur.getString(cur
					.getColumnIndex(ContactDbAdapter.KEY_ID)));
			String type = cur.getString(cur
					.getColumnIndex(ContactDbAdapter.KEY_TYPE));
			String ss = cur.getString(cur
					.getColumnIndex(ContactDbAdapter.KEY_CONTENT));
			SpannableString value = new SpannableString(ss);

			System.out.println("ss :" + ss);
			System.out.println("type :" + type);

			String[] valueSplit = ss.split(" ");
			int len = keywordSplit.length;

			// record the begnning position of each keywords found in value
			int[] lenSplit = new int[len + 1];

			boolean isMatch = false;
			isMatch = KeywordMatch(keywordSplit, valueSplit, lenSplit);

			System.out.println("isMatch: " + isMatch);

			for (int i = 0; i < len; i++) {
				if (!keywordSplit[i].equals("")) {
					value.setSpan(
							new ForegroundColorSpan(Color.rgb(34, 177, 76)),
							lenSplit[i],
							lenSplit[i] + keywordSplit[i].length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}

			if (ContactDbAdapter.TABLE_NAME.equals(type)) {
				// �˴����ؼ���ƥ���ж�

				if (isMatch) {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put(ContactDbAdapter.KEY_ID, id);
					m.put(ContactDbAdapter.KEY_CONTENT, value);
					if (checkID.get(id)==null) {
						datalist_name.add(m);
						checkID.put(id, true);
					}
				}

				// ����
				// value.setSpan(new ForegroundColorSpan(Color.rgb(34, 177,
				// 76)),
				// 0, // ������ʼλ��
				// 2, // ��������λ�ú�һλ
				// Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			} else if (ContactDbAdapter.TABLE_REMARK.equals(type)) {
				// �˴����ؼ���ƥ���ж�

				// ����
				if (isMatch) {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put(ContactDbAdapter.KEY_ID, id);
					m.put(ContactDbAdapter.KEY_CONTENT, value);
					datalist_remark.add(m);
				}

			} else if (ContactDbAdapter.TABLE_TAG.equals(type)) {
				// �˴����ؼ���ƥ���ж�

				// ����
				if (isMatch) {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put(ContactDbAdapter.KEY_ID, id);
					m.put(ContactDbAdapter.KEY_CONTENT, value);
					datalist_tag.add(m);
				}

			} else if (ContactDbAdapter.TABLE_TEL.equals(type)) {
				// �˴����ؼ���ƥ���ж�

				if (isMatch) {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put(ContactDbAdapter.KEY_ID, id);
					m.put(ContactDbAdapter.KEY_CONTENT, value);
					datalist_tel.add(m);
				}

			} else if (ContactDbAdapter.TABLE_ADDR.equals(type)) {
				// �˴����ؼ���ƥ���ж�

				if (isMatch) {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put(ContactDbAdapter.KEY_ID, id);
					m.put(ContactDbAdapter.KEY_CONTENT, value);
					datalist_addr.add(m);
				}

			} else if ("KEY_PY".equals(type)) {
				// �˴����ؼ���ƥ���ж�

				if (isMatch) {
					Cursor cur_ = adapter.getNameRemarkByPersonId(id);
					String name = cur_.getString(cur_
							.getColumnIndexOrThrow(ContactDbAdapter.KEY_NAME));
					SpannableString displayName = new SpannableString(name);

					System.out.println("Real Name " + name);

					// record the beginning position of each Chinese character
					List<Integer> charLength = new ArrayList<Integer>();
					ChineseUtils tool = new ChineseUtils();
					int tempLen = 0;
					charLength.add(0);
					for (int i = 0; i < name.length(); i++) {
						// calculate length of each Chinese Character
						String tempString = tool.getPinYinChar(name.charAt(i));
						if (tempString == null)
							continue;
						tempLen += tool.getPinYinChar(name.charAt(i)).length();
						charLength.add(tempLen);
					}

					for (int i = 0; i < len; i++) {
						if (!keywordSplit[i].equals("")) {
							int begin = lenSplit[i];
							int end = lenSplit[i] + keywordSplit[i].length()
									- 1;

							for (int j = 0; j < charLength.size() - 1; j++)

								if (begin <= charLength.get(j + 1)
										&& end >= charLength.get(j)) {
									displayName.setSpan(
											new ForegroundColorSpan(Color.rgb(
													34, 177, 76)), j, j + 1,
											Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
									System.out.println("Highlight : " + j
											+ " begin: " + begin + " end: "
											+ end);
								}
						}
					}

					Map<String, Object> m = new HashMap<String, Object>();
					m.put(ContactDbAdapter.KEY_ID, id);
					m.put(ContactDbAdapter.KEY_CONTENT, displayName);
					if (checkID.get(id)==null) {
						datalist_name.add(m);
						checkID.put(id, true);
					}
				}

			} else if ("KEY_PY_SHORT".equals(type)) {
				// �˴����ؼ���ƥ���ж�

				if (isMatch) {
					Cursor cur_ = adapter.getNameRemarkByPersonId(id);
					String name = cur_.getString(cur_
							.getColumnIndexOrThrow(ContactDbAdapter.KEY_NAME));
					SpannableString displayName = new SpannableString(name);

					System.out.println("Short Real Name " + name);

					for (int i = 0; i < len; i++)
						if (!keywordSplit[i].equals("")) {
							displayName.setSpan(
									new ForegroundColorSpan(Color.rgb(34, 177,
											76)), lenSplit[i], lenSplit[i]
											+ keywordSplit[i].length(),
									Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						}

					Map<String, Object> m = new HashMap<String, Object>();
					m.put(ContactDbAdapter.KEY_ID, id);
					m.put(ContactDbAdapter.KEY_CONTENT, displayName);
					if (checkID.get(id)==null) {
						datalist_name.add(m);
						checkID.put(id, true);
					}
				}

			}

			cur.moveToNext();
		}
		cur.close();
	}

	private boolean KeywordMatch(String[] key, String[] value, int[] len) {

		// current match position in valueSplit array
		int posValue = 0;
		int lastPos = -1;
		boolean isMatch = true;
		int length = 0;
		for (int i = 0; i < key.length; i++)
			if (!key[i].equals("")) {

				while (posValue < value.length) {
					if (!value[posValue].equals("")) {
						Pattern keyPattern = Pattern.compile(key[i]);
						Matcher matcher = keyPattern.matcher(value[posValue]);
						if (matcher.find()) {

							if (lastPos >= 0 && matcher.start() > lastPos
									|| lastPos == -1) {
								len[i] = length + matcher.start();
								System.out.println("key: " + i + " match: "
										+ len[i]);
								lastPos = matcher.start();
								break;
							}
						}
					}
					length += value[posValue].length() + 1;
					posValue++;
					lastPos = -1;
				}

				if (posValue >= value.length && i < key.length) {
					isMatch = false;
					break;
				}
			}
		return isMatch;
	}

	private void showResult() {
		int cnt = 0;
		cnt += datalist_name.size();
		cnt += datalist_remark.size();
		cnt += datalist_tag.size();
		cnt += datalist_tel.size();
		cnt += datalist_addr.size();

		if (cnt == 0) {
			text.setText("没有搜索结果");
			return;
		}

		text.setText("总共" + cnt + "条搜索结果");

		HighLightAdapter hAd;

		/* Name */
		if (!datalist_name.isEmpty()) {

			name_ll.setVisibility(View.VISIBLE);

			hAd = new HighLightAdapter(this, datalist_name);
			hAd.notifyDataSetChanged();
			list_name.setDividerHeight(1);
			list_name.setAdapter(hAd);
			list_name.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent(getApplicationContext(),
							ShowContact.class);
					// int value = (Integer)
					// datalist_name.get(position).get(ContactDbAdapter.KEY_ID);
					intent.putExtra(ContactDbAdapter.KEY_ID, id);
					startActivity(intent);
				}
			});
			/* Measure the Height of ListView */
			hAd = (HighLightAdapter) list_name.getAdapter();
			if (hAd != null) {
				int th = 0;
				for (int i = 0; i < hAd.getCount(); ++i) {
					View item = hAd.getView(i, null, list_name);
					item.measure(0, 0);
					th += item.getMeasuredHeight();
				}
				ViewGroup.LayoutParams params = list_name.getLayoutParams();
				params.height = th;
				list_name.setLayoutParams(params);
			}
		}

		/* Remark */
		if (!datalist_remark.isEmpty()) {

			remark_ll.setVisibility(View.VISIBLE);

			hAd = new HighLightAdapter(this, datalist_remark);
			list_remark.setAdapter(hAd);
			list_remark.setDividerHeight(1);
			list_remark.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent(getApplicationContext(),
							ShowContact.class);
					// int value = (Integer)
					// datalist_remark.get(position).get(ContactDbAdapter.KEY_ID);
					intent.putExtra(ContactDbAdapter.KEY_ID, id);
					startActivity(intent);
				}
			});
			/* Measure the Height of ListView */
			hAd = (HighLightAdapter) list_remark.getAdapter();
			if (hAd != null) {
				int th = 0;
				for (int i = 0; i < hAd.getCount(); ++i) {
					View item = hAd.getView(i, null, list_remark);
					item.measure(0, 0);
					th += item.getMeasuredHeight();
				}
				ViewGroup.LayoutParams params = list_remark.getLayoutParams();
				params.height = th;
				list_remark.setLayoutParams(params);
			}
		}

		/* Tag */
		if (!datalist_tag.isEmpty()) {

			tag_ll.setVisibility(View.VISIBLE);

			hAd = new HighLightAdapter(this, datalist_tag);
			list_tag.setAdapter(hAd);
			list_tag.setDividerHeight(1);
			list_tag.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					Intent intent = new Intent(getApplicationContext(),
							SearchTagResult.class);
					// int value = (Integer)
					// datalist_tel.get(position).get(ContactDbAdapter.KEY_ID);
					intent.putExtra(ContactDbAdapter.KEY_TAG_ID, id);
					startActivity(intent);

				}
			});
			/* Measure the Height of ListView */
			hAd = (HighLightAdapter) list_tag.getAdapter();
			if (hAd != null) {
				int th = 0;
				for (int i = 0; i < hAd.getCount(); ++i) {
					View item = hAd.getView(i, null, list_tag);
					item.measure(0, 0);
					th += item.getMeasuredHeight();
				}
				ViewGroup.LayoutParams params = list_tag.getLayoutParams();
				params.height = th;
				list_tag.setLayoutParams(params);
			}
		}

		/* Telephone */
		if (!datalist_tel.isEmpty()) {

			tel_ll.setVisibility(View.VISIBLE);

			hAd = new HighLightAdapter(this, datalist_tel);
			list_tel.setAdapter(hAd);
			list_tel.setDividerHeight(1);
			list_tel.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent(getApplicationContext(),
							ShowContact.class);
					// int value = (Integer)
					// datalist_tel.get(position).get(ContactDbAdapter.KEY_ID);
					intent.putExtra(ContactDbAdapter.KEY_ID, id);
					startActivity(intent);
				}
			});
			/* Measure the Height of ListView */
			hAd = (HighLightAdapter) list_tel.getAdapter();
			if (hAd != null) {
				// if (false){
				int th = 0;
				for (int i = 0; i < hAd.getCount(); ++i) {
					View item = hAd.getView(i, null, list_tel);
					item.measure(0, 0);
					th += item.getMeasuredHeight();
				}
				ViewGroup.LayoutParams params = list_tel.getLayoutParams();
				params.height = th;
				list_tel.setLayoutParams(params);
			}
		}

		/* Address */
		if (!datalist_addr.isEmpty()) {

			addr_ll.setVisibility(View.VISIBLE);

			hAd = new HighLightAdapter(this, datalist_addr);
			list_addr.setAdapter(hAd);
			list_addr.setDividerHeight(1);
			list_addr.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent(getApplicationContext(),
							ShowContact.class);
					// int value = (Integer)
					// datalist_addr.get(position).get(ContactDbAdapter.KEY_ID);
					intent.putExtra(ContactDbAdapter.KEY_ID, id);
					startActivity(intent);
				}
			});
			/* Measure the Height of ListView */
			hAd = (HighLightAdapter) list_addr.getAdapter();
			if (hAd != null) {
				int th = 0;
				for (int i = 0; i < hAd.getCount(); ++i) {
					View item = hAd.getView(i, null, list_addr);
					item.measure(0, 0);
					th += item.getMeasuredHeight();
				}
				ViewGroup.LayoutParams params = list_addr.getLayoutParams();
				params.height = th;
				list_addr.setLayoutParams(params);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.searchable_contact, menu);
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

	private class HighLightAdapter extends SimpleAdapter {

		private List<Map<String, Object>> datalist;
		private LayoutInflater inflater;

		@SuppressWarnings("unchecked")
		public HighLightAdapter(Context context,
				List<? extends Map<String, ?>> data) {
			super(context, data, R.layout.list_text,
					new String[] { ContactDbAdapter.KEY_CONTENT },
					new int[] { R.id.list_row_text });
			datalist = (List<Map<String, Object>>) data;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return datalist.size();
		}

		@Override
		public Object getItem(int position) {
			return datalist.get(position);
		}

		@Override
		public long getItemId(int position) {
			HashMap<String, Object> m = (HashMap<String, Object>) datalist
					.get(position);
			return (Long) m.get(ContactDbAdapter.KEY_ID);
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_text, null);
				holder = new ViewHolder();
				holder.textView = (TextView) convertView
						.findViewById(R.id.list_row_text);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			HashMap<String, Object> m = (HashMap<String, Object>) datalist
					.get(position);
			SpannableString text = (SpannableString) m
					.get(ContactDbAdapter.KEY_CONTENT);

			Log.i("SearchableContact", "[text] " + text);

			holder.textView.setText(text);
			return convertView;
		}
	}

	static class ViewHolder {
		TextView textView;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		pb.setVisibility(View.VISIBLE);
		keyword = query;

		this.name_ll.setVisibility(View.GONE);
		this.tag_ll.setVisibility(View.GONE);
		this.remark_ll.setVisibility(View.GONE);
		this.addr_ll.setVisibility(View.GONE);
		this.tel_ll.setVisibility(View.GONE);

		MyAsyncTask task = new MyAsyncTask();
		task.execute();

		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		pb.setVisibility(View.VISIBLE);
		keyword = newText;

		this.name_ll.setVisibility(View.GONE);
		this.tag_ll.setVisibility(View.GONE);
		this.remark_ll.setVisibility(View.GONE);
		this.addr_ll.setVisibility(View.GONE);
		this.tel_ll.setVisibility(View.GONE);

		MyAsyncTask task = new MyAsyncTask();
		task.execute();

		return true;
	}

	private class MyAsyncTask extends AsyncTask<Integer, Integer, Cursor> {

		@Override
		protected Cursor doInBackground(Integer... params) {
			Cursor cur = null;
			try {
				cur = adapter.getMatched(keyword);
			} catch (Exception e) {
				e.printStackTrace();
			}
			sortResult(cur);
			return cur;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			pb.setVisibility(View.GONE);
			showResult();
		}

	}
}

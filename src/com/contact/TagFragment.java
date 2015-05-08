package com.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.demo.activity.MeFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class TagFragment extends Fragment {

	ListView tagListView;
	TextView tagTitle;
	List<Map<String, Object>> tagList;

	ContactDbAdapter dbAdapter;
	private ProgressDialog pd;

	final String MAP_KEY = "Tag_key";
	final String DISPLAY_KEY = "Display_key";

	public TagFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null)
			return null;
		View rootView = inflater.inflate(R.layout.fragment_tag, container,
				false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		tagListView = (ListView) view.findViewById(R.id.tag_list_view);
		tagTitle = (TextView) view.findViewById(R.id.tag_title);
		dbAdapter = ContactDbAdapter.getInstance(this.getActivity());
		dbAdapter.open();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		pd = new ProgressDialog(this.getActivity());
		pd.setMessage("����Ŭ��������...");
		pd.show();
		MyAsyncTask task = new MyAsyncTask();
		task.execute();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (MeFragment.mposition == 1)
			MeFragment.oldposition = -1;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void renderListview(Cursor tagCursor) {
		final Context ctx = getActivity().getApplicationContext();
		int tagOrder = 0;

		tagList = new ArrayList<Map<String, Object>>();
		if (tagCursor != null && tagCursor.moveToFirst()) {
			
			Log.i("TagFragment", "cursur size:" + tagCursor.getCount());
			
			String lastTagString = new String();

			do {
				Map<String, Object> tagMap = new HashMap<String, Object>();
				String tagString = tagCursor.getString(tagCursor
						.getColumnIndex(ContactDbAdapter.KEY_TAG_NAME));
				Long tag_id = tagCursor.getLong(tagCursor
						.getColumnIndex(ContactDbAdapter.KEY_TAG_ID));

				Log.i("TagFragment", "tag_id:" + tag_id + " tag:" + tagString);

				if (tagList.size() == 0) {
					tagOrder++;
					tagMap.put(DISPLAY_KEY, "" + tagOrder + ".	" + tagString);
					tagMap.put(MAP_KEY, tag_id);
					tagList.add(tagMap);
					lastTagString = tagString;
				} else {
					if (!lastTagString.equals(tagString)) {
						tagOrder++;
						tagMap.put(DISPLAY_KEY, "" + tagOrder + ".	"
								+ tagString);
						tagMap.put(MAP_KEY, tag_id);
						tagList.add(tagMap);
						lastTagString = tagString;
					}
				}
			} while (tagCursor.moveToNext());

			tagTitle.setText("总共" + tagList.size() + "个标签");
		} else {

			Log.i("TagFragment", "cursur size: 0");
			tagTitle.setText("标签管理器");

		}

		String[] from = new String[] { DISPLAY_KEY };
		int[] to = new int[] { R.id.list_row_text };
		SimpleAdapter adapter;
		adapter = new SimpleAdapter(ctx, tagList, R.layout.list_text, from, to);

		tagListView.setAdapter(adapter);

		tagListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				final Context ctx = getActivity().getApplicationContext();
				Intent intent = new Intent();
				intent.setClass(ctx, SearchTagResult.class);
				long tag_id = (Long) tagList.get(position).get(MAP_KEY);
				intent.putExtra(ContactDbAdapter.KEY_TAG_ID, tag_id);
				startActivity(intent);
			}
		});
	}

	private class MyAsyncTask extends AsyncTask<Integer, Integer, Cursor> {
		@Override
		protected Cursor doInBackground(Integer... params) {
			Cursor cur = null;
			try {
				cur = dbAdapter.getTag();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return cur;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			renderListview(result);
			pd.dismiss();
		}
	}
}

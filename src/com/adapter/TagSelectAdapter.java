package com.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.contact.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TagSelectAdapter extends BaseAdapter{
	
	private Context ctx;
	private List<Map<String,String>> tagList;
	private List<Long> tagMarkedList;
	private LayoutInflater mInflater;

	public Map<Integer,Boolean> checkBoxFlag;	
	/**
	 * 
	 * @param ctx
	 * @param List<Map<String,String>> tagList (key: "tag_id" "tag_name")
	 * @param List<Integer> tagMarkedList
	 */
	public TagSelectAdapter(Context ctx, List<Map<String,String>> tagList, List<Long> tagMarkedList) {
		super();
		this.ctx = ctx;
		this.tagList = tagList;
		this.tagMarkedList = tagMarkedList;
		mInflater = LayoutInflater.from(ctx);
		checkBoxFlag = new HashMap<Integer, Boolean>();
		for(int i=0; i<tagList.size(); i++){
			Map<String,String> mapTemp = tagList.get(i); 
			Long tagID = Long.parseLong(mapTemp.get("tag_id"));
			if (tagMarkedList.contains(tagID)){
				checkBoxFlag.put(i, true);
			}
			else{
				checkBoxFlag.put(i, false);
			}
		}
	}

	@Override
	public int getCount() {
		return tagList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final int pos = position;
		TagViewHolder viewHolder;
		
		if (convertView==null){
			viewHolder = new TagViewHolder();
			convertView = mInflater.inflate(R.layout.tag_select_item, null);
			viewHolder.textView = (TextView) convertView.findViewById(R.id.select_tag_name);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.select_tag_cbox);
			convertView.setTag(viewHolder);
		}
		else{
			viewHolder = (TagViewHolder) convertView.getTag();
		}		
		
		viewHolder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					checkBoxFlag.put(pos, true);
				}
				else{
					checkBoxFlag.put(pos, false);
				}
			}
		});
		
		viewHolder.checkBox.setChecked(checkBoxFlag.get(pos));
		String text = tagList.get(pos).get("tag_name");
		viewHolder.textView.setText(text);
		
		return convertView;
	}
	
	public final class TagViewHolder{
		public TextView textView;
		public CheckBox checkBox;
	}

}

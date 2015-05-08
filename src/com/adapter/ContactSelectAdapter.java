package com.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import com.contact.R;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactSelectAdapter extends BaseAdapter{
	
	private Context ctx;
	private List<String> contactList;
	private LayoutInflater mInflater;
	
	public Map<Integer,Boolean> checkBoxFlag;	
	
	public ContactSelectAdapter(Context ctx, List<String>contactList) {
		super();
		this.ctx = ctx;
		this.contactList = contactList;
		mInflater = LayoutInflater.from(this.ctx);
		checkBoxFlag = new HashMap<Integer, Boolean>();
		// to initialize
		for(int i=0; i<contactList.size(); i++){
			checkBoxFlag.put(i, false);
		}
	}
	
	@Override
	public int getCount() {
		return contactList.size();
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
		ContactViewHolder viewHolder;
		
		if (convertView==null){
			viewHolder = new ContactViewHolder();
			convertView = mInflater.inflate(R.layout.contact_select_item, null);
			viewHolder.textView = (TextView) convertView.findViewById(R.id.select_contact_name);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.select_contact_cbox);
			convertView.setTag(viewHolder);
		}
		else{
			viewHolder = (ContactViewHolder) convertView.getTag();
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
		viewHolder.textView.setText(contactList.get(pos));
		
		return convertView;
	}
	
	public final class ContactViewHolder{
		public TextView textView;
		public CheckBox checkBox;
	}

}

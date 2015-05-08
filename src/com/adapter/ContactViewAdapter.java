package com.adapter;

import java.util.List;

import com.contact.ContactItem;
import com.contact.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class ContactViewAdapter extends ArrayAdapter<ContactItem> {
	
	private int resource;
	private SectionIndexer myIndexer;

	public ContactViewAdapter(Context context, int resource,
			List<ContactItem> objects) {
		super(context, resource, objects);
		this.resource = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ContactItem contactItem = getItem(position);
		LinearLayout linearLayout = null;
		
		if (convertView == null){
			linearLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(resource, null);
		}
		else {
			linearLayout = (LinearLayout) convertView;
		}
		
		RelativeLayout sortKeyLayout = (RelativeLayout) linearLayout.findViewById(R.id.sort_key_layout);
		TextView sortKey = (TextView) linearLayout.findViewById(R.id.sort_key);
		TextView name = (TextView) linearLayout.findViewById(R.id.contact_name);
		
		name.setText(contactItem.getName());
		
		int section = myIndexer.getSectionForPosition(position);
		
		if (position == myIndexer.getPositionForSection(section)){
			sortKey.setText(contactItem.getSortKey());
			sortKeyLayout.setVisibility(View.VISIBLE);
		} else {
			sortKeyLayout.setVisibility(View.GONE);
		}
		
		return linearLayout;		
	}
	
	public void setIndexer(SectionIndexer indexer){
		myIndexer = indexer;
	}	

}

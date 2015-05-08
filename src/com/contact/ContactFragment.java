package com.contact;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import cn.jpush.im.android.demo.activity.MeFragment;

import com.contact.ContactDbAdapter;
import com.adapter.ContactViewAdapter;

public class ContactFragment extends Fragment {
	private ListView contactListView;
	private RelativeLayout secToastLayout;
	private TextView secToastText;
	private TextView text;
	private Button alphabetButton;
	
	private AlphabetIndexer mIndexer;
	
	private ContactViewAdapter adapter;
	private ContactDbAdapter madapter;
	
	private ProgressDialog pd;
	
	private List<ContactItem> contactList = new ArrayList<ContactItem>();
	
	private static Cursor cur = null;
	private static final String alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public ContactFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null)
			return null;
		
		madapter = ContactDbAdapter.getInstance(this.getActivity());
		madapter.open();
		
		adapter = new ContactViewAdapter(this.getActivity(),  R.layout.contact_list_view_item, contactList);
		
		View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
		
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		text = (TextView) view.findViewById(R.id.mtext_tot);
		text.setText("目前没有联系人");
		
		contactListView = (ListView) view.findViewById(R.id.contact_list_view);
		
		alphabetButton = (Button) view.findViewById(R.id.alphabet_button);
		alphabetButton.setVisibility(View.GONE);
		
		secToastLayout = (RelativeLayout) view.findViewById(R.id.section_toast_layout);
		secToastText = (TextView) view.findViewById(R.id.section_toast_text);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//madapter.open();
		pd = new ProgressDialog(this.getActivity());
		pd.setMessage("���ڼ�����...");
		pd.show();
		MyAsyncTask task = new MyAsyncTask();
		task.execute();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (cur != null) cur.close();
		cur = null;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (MeFragment.mposition == 0)
			MeFragment.oldposition = -1;
	}

	private void updateData() {
	  contactList.clear();
	  try {

	    cur = madapter.getContact();

	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  
	  if (cur != null) {
	    cur.moveToFirst();
	    while (!cur.isAfterLast()) {
		  String name = cur.getString(cur.getColumnIndex(ContactDbAdapter.KEY_NAME));
		  String spy = cur.getString(cur.getColumnIndex(ContactDbAdapter.KEY_PY_SHORT));
		  long id = cur.getLong(cur.getColumnIndex(ContactDbAdapter.KEY_ID));

		  Log.i("ContactFragment", spy);
		  
		  ContactItem contactItem = new ContactItem();
		  contactItem.setId(String.valueOf(id));
          contactItem.setName(name);
          contactItem.setSortKey(getSortKey(spy));
          contactList.add(contactItem);
          
          cur.moveToNext();
        }
	   }
	}
	
	public void renderListView() {
		final Context ctx = this.getActivity();
		
		if (cur == null || cur.getCount() == 0) {
			text.setText("目前没有联系人");
			alphabetButton.setVisibility(View.GONE);
			return;
		}
		
		text.setText("总共" + cur.getCount() + "个联系人");
		
		contactListView.setVisibility(View.VISIBLE);
		alphabetButton.setVisibility(View.VISIBLE);

	    mIndexer = new AlphabetIndexer(cur, cur.getColumnIndex(ContactDbAdapter.KEY_PY_SHORT), alphabet);
		adapter.setIndexer(mIndexer);
		//adapter.notifyDataSetChanged();
		contactListView.setAdapter(adapter);
		contactListView.setOnItemClickListener(new OnItemClickListener() {
		  @Override
		  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    ContactItem contactItem = new ContactItem();
		    contactItem = (ContactItem) contactList.get(position);
			long contactId = Long.valueOf(contactItem.getId());
			Intent intent = new Intent(ctx, ShowContact.class);
			intent.putExtra(ContactDbAdapter.KEY_ID, contactId);
			startActivity(intent);
		  }
		});
	}
	
	@SuppressLint("ClickableViewAccessibility")
	private void setupAlphabetButton() {
	  alphabetButton.setOnTouchListener(new OnTouchListener() {

	    @Override
	    public boolean onTouch(View v, MotionEvent event) {
	      float height = alphabetButton.getHeight();
	      float y = event.getY();
	      int secPosition = (int) ((y / height) * 27.0);
		  if (secPosition < 0) {
		    secPosition = 0;
		  } else if (secPosition > 26) {
		    secPosition = 26;
	      }

	      String sectionLetter = String.valueOf(alphabet.charAt(secPosition));
	      int position = mIndexer.getPositionForSection(secPosition);
	      switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN:
		      secToastLayout.setVisibility(View.VISIBLE);
		      secToastText.setText(sectionLetter);
		      alphabetButton.setBackgroundResource(R.drawable.alphabet_black);
		      break;
		    case MotionEvent.ACTION_MOVE:
		      secToastText.setText(sectionLetter);
		      contactListView.setSelection(position);
		      break;
            default:
              secToastLayout.setVisibility(View.GONE);
              alphabetButton.setBackgroundResource(R.drawable.alphabet_white);
           }
           return true;
         }
      });
	}
	
	@SuppressLint("DefaultLocale")
	private String getSortKey(String name) {
		String key = name.substring(0, 1).toUpperCase();
		if (key.matches("[A-Z]")) {
			return key;
		}
		return "#";
	}
	
	private class MyAsyncTask extends AsyncTask<Long, Integer, Cursor> {
		
		@Override
		protected Cursor doInBackground(Long... params) {
			updateData();
			return cur;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			renderListView();
			setupAlphabetButton();
			pd.dismiss();
		}
	}
}

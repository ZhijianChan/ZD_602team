package cn.jpush.im.android.demo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.contact.R;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import cn.jpush.im.android.demo.adapter.AlbumListAdapter;
import cn.jpush.im.android.demo.entity.ImageBean;

/*
 * 閺堫剙婀撮崶鍓у闂嗗棛鏅棃锟�
 */
public class PickPictureTotalActivity extends BaseActivity {
    private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();
	private List<ImageBean> list = new ArrayList<ImageBean>();
	private final static int SCAN_OK = 1;
    private final static int SCAN_ERROR = 2;
	private ProgressDialog mProgressDialog;
//	private PickPictureTotalAdapter adapter;
//	private GridView mGroupGridView;
    private AlbumListAdapter adapter;
    private ListView mListView;
	private ImageButton mReturnBtn;
	private TextView mTitle;
	private ImageButton mMenuBtn;
	private Intent mIntent;
	static Context PPTActivity;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_OK:
				//閸忔娊妫存潻娑樺閺夛拷
				mProgressDialog.dismiss();
				
				adapter = new AlbumListAdapter(PickPictureTotalActivity.this, list = subGroupOfImage(mGruopMap), mListView);
				mListView.setAdapter(adapter);
				break;
                case SCAN_ERROR:
                    mProgressDialog.dismiss();
                    Toast.makeText(PPTActivity, PPTActivity.getString(R.string.sdcard_not_prepare_toast), Toast.LENGTH_SHORT).show();
                    break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_picture_total);
		mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
		mTitle = (TextView) findViewById(R.id.title);
		mMenuBtn = (ImageButton) findViewById(R.id.right_btn);
//		mGroupGridView = (GridView) findViewById(R.id.pick_picture_total_grid_view);
		mListView = (ListView) findViewById(R.id.pick_picture_total_list_view);
		mTitle.setText(this.getString(R.string.choose_album_title));
		mMenuBtn.setVisibility(View.GONE);
		PPTActivity = this;
		getImages();
		mIntent = this.getIntent();
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				List<String> childList = mGruopMap.get(list.get(position).getFolderName());
				mIntent.setClass(PickPictureTotalActivity.this, PickPictureActivity.class);
				mIntent.putStringArrayListExtra("data", (ArrayList<String>)childList);
				startActivity(mIntent);
				
			}
		});
		
		mReturnBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		
	}

    @Override
    protected void onPause() {
        mProgressDialog.dismiss();
        super.onPause();
    }

    /**
	 * 閸掆晝鏁ontentProvider閹殿偅寮块幍瀣簚娑擃厾娈戦崶鍓у閿涘本顒濋弬瑙勭《閸︺劏绻嶇悰灞芥躬鐎涙劗鍤庣粙瀣╄厬
	 */
	private void getImages() {
		//閺勫墽銇氭潻娑樺閺夛拷
		mProgressDialog = ProgressDialog.show(this, null, PPTActivity.getString(R.string.loading));
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = PickPictureTotalActivity.this.getContentResolver();

				//閸欘亝鐓＄拠顣恜eg閸滃ng閻ㄥ嫬娴橀悧锟�
				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);
				if(mCursor == null || mCursor.getCount() == 0){
                    mHandler.sendEmptyMessage(SCAN_ERROR);
                }else{
                    while (mCursor.moveToNext()) {
                        //閼惧嘲褰囬崶鍓у閻ㄥ嫯鐭惧锟�
                        String path = mCursor.getString(mCursor
                                .getColumnIndex(MediaStore.Images.Media.DATA));

                        try{
                            //閼惧嘲褰囩拠銉ユ禈閻楀洨娈戦悥鎯扮熅瀵板嫬鎮�
                            String parentName = new File(path).getParentFile().getName();
                            //閺嶈宓侀悥鎯扮熅瀵板嫬鎮曠亸鍡楁禈閻楀洦鏂侀崗銉ュ煂mGruopMap娑擄拷
                            if (!mGruopMap.containsKey(parentName)) {
                                List<String> chileList = new ArrayList<String>();
                                chileList.add(path);
                                mGruopMap.put(parentName, chileList);
                            } else {
                                mGruopMap.get(parentName).add(path);
                            }
                        }catch (Exception e){
                        }
                    }
                    mCursor.close();
                    //闁氨鐓andler閹殿偅寮块崶鍓у鐎瑰本鍨�
                    mHandler.sendEmptyMessage(SCAN_OK);
                }
			}
		}).start();
		
	}
	
	
	/**
	 * 缂佸嫯顥婇崚鍡欑矋閻ｅ矂娼癎ridView閻ㄥ嫭鏆熼幑顔界爱閿涘苯娲滄稉鐑樺灉娴狀剚澹傞幓蹇斿閺堣櫣娈戦弮璺猴拷娆忕殺閸ュ墽澧栨穱鈩冧紖閺�鎯ф躬HashMap娑擄拷
	 * 閹碉拷娴犮儵娓剁憰渚�浜堕崢鍜筧shMap鐏忓棙鏆熼幑顔剧矋鐟佸懏鍨歀ist
	 * 
	 * @param mGruopMap
	 * @return
	 */
	private List<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGruopMap){
		if(mGruopMap.size() == 0){
			return null;
		}
		List<ImageBean> list = new ArrayList<ImageBean>();
		
		Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> entry = it.next();
			ImageBean mImageBean = new ImageBean();
			String key = entry.getKey();
			List<String> value = entry.getValue();
			
			mImageBean.setFolderName(key);
			mImageBean.setImageCounts(value.size());
			mImageBean.setTopImagePath(value.get(0));//閼惧嘲褰囩拠銉х矋閻ㄥ嫮顑囨稉锟藉鐘叉禈閻楋拷
			
			list.add(mImageBean);
		}
		
		return list;
		
	}
}

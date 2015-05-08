package cn.jpush.im.android.demo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import cn.jpush.im.android.api.Message;
import cn.jpush.im.android.api.content.ImageContent;

import com.contact.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;

import cn.jpush.im.android.demo.adapter.PickPictureAdapter;
import cn.jpush.im.android.demo.application.JPushDemoApplication;
import cn.jpush.im.android.demo.tools.BitmapLoader;

public class PickPictureActivity extends BaseActivity {
    private GridView mGridView;
    //濮濄倗娴夐崘灞肩瑓閹碉拷閺堝娴橀悧鍥╂畱鐠侯垰绶為梿鍡楁値
    private List<String> mList;
    //闁鑵戦崶鍓у閻ㄥ嫯鐭惧鍕肠閸氾拷
    private List<String> mPickedList;
    private Button mSendPictureBtn;
    private ImageButton mReturnBtn;
    private boolean mIsGroup;
    private PickPictureAdapter mAdapter;
    private Intent mIntent;
    private String mTargetID;
    private String mSelectedPath;
    private Conversation mConv;
    private static final int REFRESH_CHAT_LISTVIEW = 2000;
    static Context PPActivity;
    private ProgressDialog mDialog;
    private long mGroupID;
    private int[] mMsgIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_picture_detail);
        PPActivity = this;
        mSendPictureBtn = (Button) findViewById(R.id.pick_picture_send_btn);
        mReturnBtn = (ImageButton) findViewById(R.id.pick_picture_detail_return_btn);
        mGridView = (GridView) findViewById(R.id.child_grid);

        mIntent = this.getIntent();
        mIsGroup = mIntent.getBooleanExtra("isGroup", false);
        if (mIsGroup){
            mGroupID = mIntent.getLongExtra("groupID", 0);
            Log.i("PickPictureActivity", "groupID : " + mGroupID);
            mConv = JMessageClient.getConversation(ConversationType.group, mGroupID);
        }
        else {
            mTargetID = mIntent.getStringExtra("targetID");
            Log.i("PickPictureActivity", "mTargetID" + mTargetID);
            mConv = JMessageClient.getConversation(ConversationType.single, mTargetID);
        }
        mList = mIntent.getStringArrayListExtra("data");

        mAdapter = new PickPictureAdapter(this, mList, mGridView);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(onItemListener);
        mSendPictureBtn.setOnClickListener(listener);
        mReturnBtn.setOnClickListener(listener);
    }

    private OnItemClickListener onItemListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> viewAdapter, View view, int position,
                                long id) {
            Intent intent = new Intent();
            intent.putExtra("fromChatActivity", false);
            if(mIsGroup){
                intent.putExtra("groupID", mGroupID);
            }else intent.putExtra("targetID", mTargetID);
            intent.putStringArrayListExtra("pathList", (ArrayList<String>) mList);
            intent.putExtra("position", position);
            intent.putExtra("isGroup", mIsGroup);
            mSelectedPath = mList.get(position);
            intent.putExtra("pathArray", mAdapter.getSelectedArray());
            intent.setClass(PickPictureActivity.this, BrowserViewPagerActivity.class);
            startActivityForResult(intent, JPushDemoApplication.REQUESTCODE_SELECT_PICTURE);
        }
    };

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //閻愮懓鍤崣鎴︼拷浣瑰瘻闁筋噯绱濋崣鎴︼拷渚�锟藉鑵戦惃鍕禈閻楋拷
                case R.id.pick_picture_send_btn:
                    //鐎涙ɑ鏂侀柅澶夎厬閸ュ墽澧栭惃鍕熅瀵帮拷
                    mPickedList = new ArrayList<String>();
                    //鐎涙ɑ鏂侀柅澶夎厬閻ㄥ嫬娴橀悧鍥╂畱position
                    List<Integer> positionList = new ArrayList<Integer>();
                    positionList = mAdapter.getSelectItems();
                    //閹峰灝鍩岄柅澶夎厬閸ュ墽澧栭惃鍕熅瀵帮拷
                    for (int i = 0; i < positionList.size(); i++){
                        mPickedList.add(mList.get(positionList.get(i)));
                        Log.i("PickPictureActivity", "Picture Path: " + mList.get(positionList.get(i)));
                    }
                    if(mPickedList.size() < 1)
                        return;
                    else {
                        mDialog = new ProgressDialog(PPActivity);
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.setMessage(PPActivity.getString(R.string.sending_hint));
                        mDialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final List<String> pathList = new ArrayList<String>();
                                getThumbnailPictures(pathList);
                                android.os.Message msg = handler.obtainMessage();
                                msg.what = 0;
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList("pathList", (ArrayList<String>) pathList);
                                msg.setData(bundle);
                                msg.sendToTarget();
                            }
                        });
                        thread.start();
                    }
                    break;
                case R.id.pick_picture_detail_return_btn:
                    finish();
                    break;
            }
        }

    };

    /**
     * 閼惧嘲绶遍柅澶夎厬閸ュ墽澧栭惃鍕級閻ｃ儱娴樼捄顖氱窞
     *
     * @param pathList
     */
    private void getThumbnailPictures(List<String> pathList) {
        String tempPath;
        Bitmap bitmap;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mMsgIDs = new int[mPickedList.size()];
        for (int i = 0; i < mPickedList.size(); i++) {
            //妤犲矁鐦夐崶鍓у婢堆冪毈閿涘矁瀚㈢亸蹇庣艾720 * 1280閸掓瑧娲块幒銉ュ絺闁礁甯崶鎾呯礉閸氾箑鍨崢瀣級
            if(BitmapLoader.verifyPictureSize(mPickedList.get(i)))
                pathList.add(mPickedList.get(i));
            else {
                bitmap = BitmapLoader.getBitmapFromFile(mPickedList.get(i), 720, 1280);
                tempPath = BitmapLoader.saveBitmapToLocal(bitmap);
                pathList.add(tempPath);
            }

            Log.i("PickPictureActivity", "pathList.get(i) " + pathList.get(i));
            File file = new File(pathList.get(i));
            try {
                    ImageContent content = new ImageContent(file);
                    Message msg = mConv.createSendMessage(content);
                    mMsgIDs[i] = msg.getId();
                } catch (FileNotFoundException e) {
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == JPushDemoApplication.RESULTCODE_SELECT_PICTURE) {
            if (data != null) {
                int[] selectedArray = data.getIntArrayExtra("pathArray");
                int sum = 0;
                for(int i=0; i < selectedArray.length; i++){
                    if(selectedArray[i] > 0)
                        ++sum;
                }
                if(sum > 0)
                    mSendPictureBtn.setText(PPActivity.getString(R.string.send) + "(" + sum + "/" + "9)");
                mAdapter.refresh(selectedArray);
            }

        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Intent intent = new Intent(JPushDemoApplication.REFRESH_CHATTING_ACTION_IMAGE);
                    intent.putExtra("targetID", mTargetID);
                    intent.putExtra("groupID", mGroupID);
                    intent.putExtra("isGroup", mIsGroup);
                    intent.putExtra("msgIDs", mMsgIDs);
                    sendBroadcast(intent);
                    if(mDialog != null)
                        mDialog.dismiss();
                    //finish閹哄ickPictureTotalActivity閿涘瞼娲块幒銉ㄧ箲閸ョ偠浜版径鈺冩櫕闂堬拷
                    ((Activity) PickPictureTotalActivity.PPTActivity).finish();
                    finish();
                    break;
            }
        }
    };
}

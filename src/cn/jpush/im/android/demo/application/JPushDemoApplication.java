package cn.jpush.im.android.demo.application;

import android.app.Application;
import android.util.Log;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.demo.receiver.GroupMemberChangeEventReceiver;
import cn.jpush.im.android.demo.receiver.MessageEventReceiver;

public class JPushDemoApplication extends Application {

	public static final int REQUESTCODE_CONV_LIST = 0;
	public static final int RESULTCODE_CONV_LIST = 2;
	public static final int REQUESTCODE_TAKE_PHOTO = 4;
    public static final int REQUESTCODE_SELECT_PICTURE = 6;
    public static final int RESULTCODE_SELECT_PICTURE = 8;
    public static final int REFRESH_GROUP_NAME = 3000;
    public static final int ADD_GROUP_MEMBER_EVENT = 3001;
    public static final int REMOVE_GROUP_MEMBER_EVENT = 3002;
    public static final int ON_GROUP_EXIT_EVENT = 3003;
	public static String RECEIVE_ACTION = JMessageClient.ACTION_RECEIVE_IM_MESSAGE;
	public static String REFRESH_CHATTING_ACTION = "cn.jpush.im.demo.activity.ACTION_RECEIVER_CHATTING_MESSAGE";
	public static String REFRESH_CONVLIST_ACTION = "cn.jpush.im.demo.activity.ACTION_RECEIVE_CONVERSATION_MESSAGE";
    public static String ADD_GROUP_MEMBER_ACTION = "cn.jpush.im.demo.activity.ACTION_ADD_GROUP_MEMBER";
    public static String REMOVE_GROUP_MEMBER_ACTION = "cn.jpush.im.demo.activity.ACTION_REMOVE_GROUP_MEMBER";
    public static String UPDATE_GROUP_NAME_ACTION = "cn.jpush.im.demo.activity.ACTION_UPDATE_GROUP_NAME";
	public static String REFRESH_CHATTING_ACTION_IMAGE = "refresh_image";
	
	@Override
	public void onCreate() {
		super.onCreate();
        Log.i("JpushDemoApplication", "init");

		JMessageClient.init(getApplicationContext());
        JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_NO_NOTIFICATION);
		new MessageEventReceiver(getApplicationContext());
		new GroupMemberChangeEventReceiver(getApplicationContext());
	}

}

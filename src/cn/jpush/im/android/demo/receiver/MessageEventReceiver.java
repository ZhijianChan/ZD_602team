package cn.jpush.im.android.demo.receiver;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.Message;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;

import com.contact.EditContact;
import com.contact.R;
import com.util.AESUtils;

public class MessageEventReceiver {

    private static final String TAG = MessageEventReceiver.class.getSimpleName();

    private Context context;
    private static int count = 0;
    public static List<String> mNotificationList = new ArrayList<String>();
    private static long mLastTime = 0;

    public MessageEventReceiver(Context context) {
        this.context = context;
        JMessageClient.registerEventReceiver(this);
    }

    @SuppressWarnings("deprecation")
	public void onEvent(MessageEvent event) {
        ConversationType convType = event.getConversationType();
        String targetID = event.getTargetID();
        int messageID = event.getMsgID();
        if (messageID != 0) {
            Conversation conv = JMessageClient.getConversation(convType, targetID);
            Log.d(TAG, "conv.toString() " + conv.toString());
            Message msg = conv.getMessage(messageID);
            Log.i(TAG, "msg = " + msg.toString());
            String content;
            switch (msg.getContentType()) {
                case image:
                    content = context.getString(R.string.noti_content_type_img);
                    break;
                case voice:
                    content = context.getString(R.string.noti_content_type_voice);
                    break;
                case location:
                    content = context.getString(R.string.noti_content_type_location);
                    break;
                default:
                    content = ((TextContent) msg.getContent()).getText();

            }
            
            NotificationManager manager = (NotificationManager) context.
            		getApplicationContext().
            		getSystemService(Context.NOTIFICATION_SERVICE);
            
            Notification notification = new Notification(R.drawable.ic_launcher,
            		context.getText(R.string.noti_new_message_receive),
            		System.currentTimeMillis());
            
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - mLastTime > 5000) {
              notification.defaults = Notification.DEFAULT_ALL;
              mLastTime = System.currentTimeMillis();
             }
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            
            
            try {
				content = AESUtils.decrypt("21a", content);
			} catch (Exception e) {
				e.printStackTrace();
			}
            
            Log.i("MessageEvenReceiver", "[msg] " + content);
            
			int index1 = content.indexOf('%');
			int index2 = content.indexOf('%', index1 + 1);
			int index3 = content.indexOf('%', index2 + 1);
			
			String name = "";
			String tel1 = "";
			String tel2 = "";
			
			if (index2 > index1 + 1) name = content.substring(index1 + 1, index2);
			if (index3 > index2 + 1) tel1 = content.substring(index2 + 1, index3);
			if (content.length() > index3 + 1) tel2 = content.substring(index3 + 1, content.length());
			
            Intent notificationIntent = new Intent(context, EditContact.class);
            notificationIntent.setAction(EditContact.SCAN_RES_ACTION);
            notificationIntent.putExtra(EditContact.DATA_TARGET, "");
            notificationIntent.putExtra(EditContact.DATA_NAME, name);
            notificationIntent.putExtra(EditContact.DATA_TEL1, tel1);
            notificationIntent.putExtra(EditContact.DATA_TEL2, tel2);

            if (!mNotificationList.contains(conv.getTargetId())) {
              if (count < 5) {
                 if (mNotificationList.size() < 5) {
                    mNotificationList.add(conv.getTargetId());
                    count++;
                 } else {
                     mNotificationList.set(count, conv.getTargetId());
                     count++;
                 }
              } else if (count >= 5) {
                   count = 0;
                   mNotificationList.set(count, conv.getTargetId());
                   count++;
              }
            }
            
            notificationIntent.putExtra("count", mNotificationList.indexOf(conv.getTargetId()));
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(context,
            		mNotificationList.indexOf(conv.getTargetId()),
            		notificationIntent,
            		PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setLatestEventInfo(context, "通讯录", "有人向你发送了一个名片", contentIntent);
            manager.notify(mNotificationList.indexOf(conv.getTargetId()), notification);
        }
     }
}

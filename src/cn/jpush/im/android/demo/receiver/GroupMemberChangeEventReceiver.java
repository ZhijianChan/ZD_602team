package cn.jpush.im.android.demo.receiver;

import android.content.Context;
import android.util.Log;

import java.util.List;

import cn.jpush.im.android.api.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.GroupMemberAddedEvent;
import cn.jpush.im.android.api.event.GroupMemberExitEvent;
import cn.jpush.im.android.api.event.GroupMemberRemovedEvent;
import com.contact.R;


public class GroupMemberChangeEventReceiver {
    private static final String TAG = GroupMemberChangeEventReceiver.class.getSimpleName();

    private Context mContext;
    public GroupMemberChangeEventReceiver(Context context){
        mContext = context;
        JMessageClient.registerEventReceiver(this);
    }

    public void onEvent(GroupMemberAddedEvent groupMemberAddedEvent){
        long groupID = groupMemberAddedEvent.getGroupID();
        List<String> members = groupMemberAddedEvent.getMembers();
        Conversation conv = JMessageClient.getConversation(ConversationType.group, groupID);
        if(null == conv){
            return;
        }
        Log.i(TAG, "onGroupMemberAdded");
        for (String member : members) {
            CustomContent content = new CustomContent();
            content.setValue("content", member + mContext.getString(R.string.join_group_toast));
            Log.i(TAG, member + mContext.getString(R.string.join_group_toast));
            conv.createSendMessage(content);
        }
    }

    public void onEvent(GroupMemberRemovedEvent groupMemberRemovedEvent){
        long groupID = groupMemberRemovedEvent.getGroupID();
        List<String> members = groupMemberRemovedEvent.getMembers();
        Conversation conv = JMessageClient.getConversation(ConversationType.group, groupID);
        if(null == conv){
            return;
        }
        Log.i(TAG, "onGroupMemberRemoved");
        for (String member : members) {
            CustomContent content = new CustomContent();
            if(member.equals(JMessageClient.getMyInfo().getUserName())){
                content.setValue("content", mContext.getString(R.string.deleted_by_creator));
            }else {
                content.setValue("content", member + mContext.getString(R.string.exit_group_by_creator));
            }
            conv.createSendMessage(content);
        }
    }

    public void onEvent(GroupMemberExitEvent groupMemberExitEvent){
        long groupID = groupMemberExitEvent.getGroupID();
        boolean isCreator = groupMemberExitEvent.containsGroupOwner();
        List<String> members = groupMemberExitEvent.getMembers();
        Conversation conv = JMessageClient.getConversation(ConversationType.group, groupID);
        if(conv == null){
            return;
        }else {
            CustomContent content = new CustomContent();
            if(isCreator){
                content.setValue("content", mContext.getString(R.string.delete_group_by_creator));
                conv.createSendMessage(content);
            }else {
                for(String userName : members){
                    Log.i(TAG, userName + mContext.getString(R.string.exit_group_event));
                    content.setValue("content", userName + mContext.getString(R.string.exit_group_event));
                    conv.createSendMessage(content);
                }

            }
        }
    }

}

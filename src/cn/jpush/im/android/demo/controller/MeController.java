package cn.jpush.im.android.demo.controller;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import com.contact.R;
import cn.jpush.im.android.demo.activity.MeFragment;
import cn.jpush.im.android.demo.tools.NativeImageLoader;
import cn.jpush.im.android.demo.view.MeView;

public class MeController implements OnClickListener, View.OnTouchListener {

  private MeView mMeView;
  private MeFragment mContext;
  private String mPath;
  private float startY = 0, endY = 0;

  public MeController(MeView meView, MeFragment context) {
    this.mMeView = meView;
    this.mContext = context;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
//      case R.id.my_avatar_iv:
//        Log.i("MeController", "avatar onClick");
//        mContext.startBrowserAvatar();
//        break;
      case R.id.take_photo_iv:
        mContext.showSetAvatarDialog();
        break;
    }
  }

  @SuppressLint("ClickableViewAccessibility")
@Override
  public boolean onTouch(View view, MotionEvent e) {
    switch (e.getAction()){
      case MotionEvent.ACTION_DOWN:
        startY = e.getY();
        return false;
      case MotionEvent.ACTION_MOVE:
        return mMeView.touchEvent(e);
      case MotionEvent.ACTION_UP:
        endY = e.getY();
        if(endY - startY > 10)
          return mMeView.touchEvent(e);
        else return onSingleTapConfirmed(view);
      default: return false;
    }
  }

  private boolean onSingleTapConfirmed(View view) {
    switch (view.getId()){
      case R.id.user_info_rl:
        mContext.StartMeInfoActivity();
        break;
      case R.id.setting_rl:
        mContext.StartSettingActivity();
        break;
      case R.id.logout_rl:
        mContext.Logout();
        mContext.cancelNotification();
        NativeImageLoader.getInstance().releaseCache();
        mContext.getActivity().finish();
      case R.id.contact_info_rl:
    	mContext.StartContactFragment();
    	break;
      case R.id.tag_info_rl:
    	mContext.StartTagFragment();
    	break;
      case R.id.qrcode_info_rl:
    	mContext.StartGenerateRQ();
      break;
    }
    return false;
  }

  public String getPath(){
    return mPath;
  }
}

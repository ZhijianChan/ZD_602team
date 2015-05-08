package cn.jpush.im.android.demo.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import cn.jpush.im.android.api.UserInfo;

import com.contact.R;
import com.zhangchh.GenerateRQ;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.demo.application.JPushDemoApplication;
import cn.jpush.im.android.demo.controller.MeController;
import cn.jpush.im.android.demo.view.MeView;

public class MeFragment extends Fragment {

  private static final String TAG = MeFragment.class.getSimpleName();
  private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

  private NavigationDrawerCallbacks mCallback;
  private ActionBarDrawerToggle mDrawerToggle;
  private DrawerLayout mDrawerLayout;
  private View mFragmentContainerView;
	
  private boolean mFromSavedInstanceState;
  private boolean mUserLearnedDrawer;
	
  private View mRootView;
  private MeView mMeView;
  private MeController mMeController;
  private Context mContext;
  private String mPath;
  
  public static int mposition = 0;
  public static int oldposition = -1;
	
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActionBar actionbar = getActivity().getActionBar();
    actionbar.setDisplayHomeAsUpEnabled(true);
    actionbar.setHomeButtonEnabled(true);

    mContext = this.getActivity();
    
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

    if (savedInstanceState != null) mFromSavedInstanceState = true;
  	
    closeDrawer();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.fragment_me, container, false);
    mMeView = (MeView)mRootView.findViewById(R.id.me_view);
    mMeView.initModule();
    mMeController = new MeController(mMeView, this);
    mMeView.setListeners(mMeController);
    mMeView.setOnTouchListener(mMeController);
    return mRootView;
  }

  @Override
  public void onResume() {
    if (JMessageClient.getMyInfo().getAvatar() != null) {
      File file = JMessageClient.getMyInfo().getAvatar();
      loadUserAvatar(file.getAbsolutePath());
    }
    super.onResume();
  }

  public void setUp(int fragmentId, DrawerLayout drawerLayout) {
    mFragmentContainerView = getActivity().findViewById(fragmentId);
    mDrawerLayout = drawerLayout;
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
      mDrawerLayout,
      R.drawable.ic_drawer,
      R.string.navigation_drawer_open,
      R.string.navigation_drawer_close) {
        @Override
        public void onDrawerClosed(View drawerView) {
          super.onDrawerClosed(drawerView);
          if (!isAdded()) return;
          getActivity().invalidateOptionsMenu();
        }

        @Override
        public void onDrawerOpened(View drawerView) {
          super.onDrawerOpened(drawerView);
          if (!isAdded()) return;
          if (!mUserLearnedDrawer) {
            mUserLearnedDrawer = true;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
          }
          getActivity().invalidateOptionsMenu();
        }
    };

    if (!mUserLearnedDrawer && !mFromSavedInstanceState)
      mDrawerLayout.openDrawer(mFragmentContainerView);

    mDrawerLayout.post(new Runnable() {
      @Override
      public void run() {
        mDrawerToggle.syncState();
      }
    });

    mDrawerLayout.setDrawerListener(mDrawerToggle);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    if (mDrawerLayout != null && isDrawerOpen()) {
      inflater.inflate(R.menu.global, menu);
      showGlobalContextActionBar();
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showGlobalContextActionBar() {
    ActionBar actionBar = getActivity().getActionBar();
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
  }

  public boolean isDrawerOpen() {
    return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
  }

  public void openDrawer() {
    mDrawerLayout.openDrawer(mFragmentContainerView);
  }
  
  public void closeDrawer() {
	if (mDrawerLayout != null) mDrawerLayout.closeDrawer(mFragmentContainerView);
    if (mCallback != null && mposition != oldposition) {
    	oldposition = mposition;
    	mCallback.onNavigationDrawerItemSelected(mposition);
    }
  }
  
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mCallback = (NavigationDrawerCallbacks) activity;
    } catch (ClassCastException e) {
        throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
    }
  }

  public void Logout() {
    Intent intent = new Intent();
    UserInfo info = JMessageClient.getMyInfo();
    if (null != info) {
      intent.putExtra("userName", info.getUserName());
      Log.i("MeFragment", "userName " + info.getUserName());
      JMessageClient.logout();
      intent.setClass(this.getActivity(), ReloginActivity.class);
      startActivity(intent);
    } else {
        Log.d(TAG,"user info is null!");
    }
  }

  public void StartSettingActivity() {
    Intent intent = new Intent();
    intent.setClass(this.getActivity(), SettingActivity.class);
    startActivity(intent);
  }

  public void StartMeInfoActivity() {
    Intent intent = new Intent();
    intent.setClass(this.getActivity(), MeInfoActivity.class);
    startActivity(intent);
  }
  
  public void StartContactFragment() {
	  mposition = 0;
	  closeDrawer();
  }

  public void StartTagFragment() {
	  mposition = 1;
	  closeDrawer();
  }
  
  public void StartGenerateRQ() {
	  Intent intent = new Intent();
	  intent.setClass(this.getActivity(), GenerateRQ.class);
	  startActivity(intent);
  }
  
  public void cancelNotification() {
    NotificationManager manager = (NotificationManager)
    		this.getActivity().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    manager.cancelAll();
  }

  @SuppressLint("InflateParams")
  public void showSetAvatarDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
    final LayoutInflater inflater = LayoutInflater.from(this.getActivity());
    View view = inflater.inflate(R.layout.dialog_set_avatar, null);
    builder.setView(view);
    final Dialog dialog = builder.create();
    dialog.show();
    LinearLayout takePhotoLl = (LinearLayout) view.findViewById(R.id.take_photo_ll);
    LinearLayout pickPictureLl = (LinearLayout) view.findViewById(R.id.pick_picture_ll);
    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        switch (v.getId()){
          case R.id.take_photo_ll:
            dialog.cancel();
            takePhoto();
            break;
          case R.id.pick_picture_ll:
            dialog.cancel();
            selectImageFromLocal();
            break;
        }
      }
    };
    takePhotoLl.setOnClickListener(listener);
    pickPictureLl.setOnClickListener(listener);
  }

  private void takePhoto() {
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      String dir ="sdcard/JPushDemo/pictures/";
      
      File destDir = new File(dir);
      if (!destDir.exists()) destDir.mkdirs();
      
      File file = new File(dir, JMessageClient.getMyInfo().getUserName() + ".jpg");
      mPath = file.getAbsolutePath();
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
      getActivity().startActivityForResult(intent, JPushDemoApplication.REQUESTCODE_TAKE_PHOTO);
    } else {
        Toast.makeText(this.getActivity(), mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
        return;
    }
  }

  public String getPhotoPath(){
    return mPath;
  }

  public void selectImageFromLocal() {
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      Intent intent;
      if (Build.VERSION.SDK_INT < 19) {
        intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
      } else {
          intent = new Intent(Intent.ACTION_PICK,
          android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      }
      getActivity().startActivityForResult(intent, JPushDemoApplication.REQUESTCODE_SELECT_PICTURE);
    } else {
        Toast.makeText(this.getActivity(), mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
        return;
    }
  }

  public void loadUserAvatar(String path){
    if(null != mMeView) mMeView.showPhoto(path);
  }

  public void startBrowserAvatar() {
    File file = JMessageClient.getMyInfo().getAvatar();
    if (file != null && file.exists()) {
      Intent intent = new Intent();
      intent.putExtra("browserAvatar", true);
      intent.putExtra("avatarPath", file.getAbsolutePath());
      intent.setClass(this.getActivity(), BrowserViewPagerActivity.class);
      startActivity(intent);
    }
  }
  
  public static interface NavigationDrawerCallbacks {
    void onNavigationDrawerItemSelected(int position);
  }
}

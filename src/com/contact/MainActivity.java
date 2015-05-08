package com.contact;

import java.io.File;

import com.contact.R;
import com.util.AESUtils;
import com.zxing.activity.CaptureActivity;

import cn.jpush.android.api.JPushInterface;
import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.demo.activity.LoginActivity;
import cn.jpush.im.android.demo.activity.MeFragment;
import cn.jpush.im.android.demo.application.JPushDemoApplication;
import cn.jpush.im.android.demo.tools.BitmapLoader;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.api.BasicCallback;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends Activity implements
		MeFragment.NavigationDrawerCallbacks {

	private MeFragment mMeFragment;
	private ProgressDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (JMessageClient.getMyInfo() == null) {
			Intent intent = new Intent();
			intent.setClass(this, LoginActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		setContentView(R.layout.activity_main);
		mMeFragment = (MeFragment) getFragmentManager().findFragmentById(
				R.id.navigation_drawer);

		// Set up the drawer.
		mMeFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle("菜单Menu");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mMeFragment.isDrawerOpen()) {
			if (mMeFragment.mposition == 0)
				getMenuInflater().inflate(R.menu.main, menu);
			else
				getMenuInflater().inflate(R.menu.tag_menu, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@SuppressLint("InflateParams")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {

			if (!mMeFragment.isDrawerOpen())
				mMeFragment.openDrawer();
			else
				mMeFragment.closeDrawer();
			return false;

		} else if (id == R.id.main_search_option) {

			Intent intent = new Intent(this, SearchableContact.class);
			startActivity(intent);
			return false;

		} else if (id == R.id.main_insert_option) {

			Intent intent = new Intent(this, EditContact.class);
			intent.setAction(EditContact.INSERT_ACTION);
			startActivity(intent);
			return false;

		} else if (id == R.id.main_scan_option) {

			Intent intent = new Intent(this, CaptureActivity.class);
			startActivityForResult(intent, 603);
			return false;

		} else if (id == R.id.tag_insert_option) {
			LayoutInflater inflater = LayoutInflater.from(this);
			LinearLayout textLayout = (LinearLayout) inflater.inflate(R.layout.tag_dialog, null);

			final EditText editText = (EditText) textLayout
					.findViewById(R.id.tag_name_text);
			Builder builder = new AlertDialog.Builder(MainActivity.this);

			builder.setTitle("输入标签名");
			builder.setView(textLayout);

			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent();
							String newTag = editText.getText().toString();
							intent.putExtra(ContactDbAdapter.KEY_TAG_NAME, newTag);
							intent.setClass(MainActivity.this, ContactSelect.class);
							startActivity(intent);
						}
					});
			builder.setNegativeButton("取消", null);
			builder.show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		JPushInterface.onPause(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_CANCELED)
			return;

		if (requestCode == 603) {
			if (resultCode == Activity.RESULT_OK) {

				Bundle bundle = data.getExtras();
				String scanResult = bundle.getString("result");
				
				try {
					scanResult = AESUtils.decrypt("21a", scanResult);
				} catch (Exception e) {
					e.printStackTrace();
				}

				Log.i("MainActivity", "[msg] " + scanResult);
				
				int index1 = scanResult.indexOf('%');
				int index2 = scanResult.indexOf('%', index1 + 1);
				int index3 = scanResult.indexOf('%', index2 + 1);

				if (index1 == -1 || index2 == -1 || index3 == -1) {
					new AlertDialog.Builder(this).setTitle("无法识别以下内容")
							.setMessage(scanResult)
							.setPositiveButton("确定", null).show();
					return;
				}

				String targetID = "";
				String name = "";
				String tel1 = "";
				String tel2 = "";
				if (index1 > 0)
					targetID = scanResult.substring(0, index1);
				if (index2 > index1 + 1)
					name = scanResult.substring(index1 + 1, index2);
				if (index3 > index2 + 1)
					tel1 = scanResult.substring(index2 + 1, index3);
				if (index3 < scanResult.length())
					tel2 = scanResult
							.substring(index3 + 1, scanResult.length());

				Log.i("MainActivity", "[targetname] " + targetID);
				Log.i("MainActivity", "[name] " + name);
				Log.i("MainActivity", "[tel1] " + tel1);
				Log.i("MainActivity", "[tel2] " + tel2);

				Intent intent = new Intent(this, EditContact.class);
				intent.setAction(EditContact.SCAN_RES_ACTION);
				intent.putExtra(EditContact.DATA_TARGET, targetID);
				intent.putExtra(EditContact.DATA_NAME, name);
				intent.putExtra(EditContact.DATA_TEL1, tel1);
				intent.putExtra(EditContact.DATA_TEL2, tel2);
				startActivity(intent);
				return;
			}
		} else if (requestCode == JPushDemoApplication.REQUESTCODE_TAKE_PHOTO) {

			String path = mMeFragment.getPhotoPath();
			if (path != null)
				calculateAvatar(path);

		} else if (requestCode == JPushDemoApplication.REQUESTCODE_SELECT_PICTURE) {

			if (data != null) {
				Uri selectedImg = data.getData();
				if (selectedImg != null) {
					Cursor cursor = this.getContentResolver().query(
							selectedImg, null, null, null, null);
					if (null == cursor || !cursor.moveToFirst()) {
						Toast.makeText(this,
								this.getString(R.string.picture_not_found),
								Toast.LENGTH_SHORT).show();
						return;
					}
					int columnIndex = cursor.getColumnIndex("_data");
					String path = cursor.getString(columnIndex);
					if (path != null) {
						File file = new File(path);
						if (file == null || !file.exists()) {
							Toast.makeText(this,
									this.getString(R.string.picture_not_found),
									Toast.LENGTH_SHORT).show();
							return;
						}
					}
					cursor.close();
					calculateAvatar(path);
				}
			}
		}
	}

	public void calculateAvatar(final String originPath) {
		mDialog = new ProgressDialog(this);
		mDialog.setCancelable(false);
		mDialog.setMessage(this.getString(R.string.updating_avatar_hint));
		mDialog.show();
		if (BitmapLoader.verifyPictureSize(originPath)) {
			updateAvatar(originPath, originPath);
		} else {
			Bitmap bitmap = BitmapLoader.getBitmapFromFile(originPath, 720,
					1280);
			String tempPath = BitmapLoader.saveBitmapToLocal(bitmap);
			updateAvatar(tempPath, originPath);
		}
	}

	private void updateAvatar(final String path, final String originPath) {
		JMessageClient.updateUserAvatar(new File(path),
				new BasicCallback(false) {
					@Override
					public void gotResult(final int status, final String desc) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mDialog.dismiss();
								if (status == 0) {
									Log.i("MeFragment",
											"Update avatar succeed path "
													+ path);
									loadUserAvatar(originPath);
								} else {
									HandleResponseCode.onHandle(
											MainActivity.this, status);
								}
							}
						});
					}
				});
	}

	private void loadUserAvatar(String path) {
		if (path != null)
			mMeFragment.loadUserAvatar(path);
	}

	@Override
	public void onNavigationDrawerItemSelected(int pos) {
		FragmentManager fragmentManager = getFragmentManager();
		if (pos == 0) {
			fragmentManager.beginTransaction()
					.replace(R.id.container, new ContactFragment()).commit();
		} else if (pos == 1) {
			fragmentManager.beginTransaction()
					.replace(R.id.container, new TagFragment()).commit();
		}
	}
}

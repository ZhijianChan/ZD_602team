package cn.jpush.im.android.demo.activity;

import com.contact.MainActivity;
import com.contact.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.demo.controller.LoginController;
import cn.jpush.im.android.demo.view.LoginView;

public class LoginActivity extends BaseActivity {

    private LoginView mLoginView = null;
    private LoginController mLoginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLoginView = (LoginView) findViewById(R.id.login_view);
        mLoginView.initModule();
        mLoginController = new LoginController(mLoginView, this);
        mLoginView.setListener(mLoginController);
        mLoginView.setListeners(mLoginController);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mLoginController.dismissDialog();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mLoginController.dismissDialog();
        finish();
        super.onBackPressed();
    }

    public Context getContext() {
        return this;
    }

    //婵″倹鐏塁urrentUser鐎涙ê婀獮鏈电瑬娴犲骸鍨忛幑銏㈡暏閹撮攱鍨ㄩ懓鍛矤閸忔湹绮惔鏃傛暏閸ョ偞娼甸敍宀勫櫢閺傛壆娅ヨぐ锟�
//    public void SwitchToMainActivity() {
//        Intent intent = new Intent();
//        intent.setClass(getContext(), MainActivity.class);
//        startActivity(intent);
//        finish();
//    }

    public void StartMainActivity() {
        Intent intent = new Intent();
        intent.setClass(getContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void StartMainActivity(boolean hasUserInfo){
        if(!hasUserInfo){
            Intent intent = new Intent();
            intent.setClass(getContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    public void StartRegisterActivity() {
        Intent intent = new Intent();
        intent.setClass(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

}

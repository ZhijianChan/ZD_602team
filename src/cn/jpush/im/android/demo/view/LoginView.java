package cn.jpush.im.android.demo.view;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.contact.R;


public class LoginView extends LinearLayout {

	private EditText mUserId;
	private EditText mPassword;
	private Button mLoginBtn;
	private Button mRegistBtnOnlogin;
	private boolean isSwitch;
	private Dialog mLoadingDialog = null;
	private LoadingDialog mLD = null;
    private Listener mListener;

	public LoginView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	public void initModule() {
		mUserId = (EditText) findViewById(R.id.username);
		mPassword = (EditText) findViewById(R.id.password);
		mLoginBtn = (Button) findViewById(R.id.login_btn);
		mRegistBtnOnlogin = (Button) findViewById(R.id.register_btn);
		// 鐠佸墽鐤嗛弽鍥暯
	}
	
	public void setListeners(OnClickListener onClickListener) {
		mLoginBtn.setOnClickListener(onClickListener);
		mRegistBtnOnlogin.setOnClickListener(onClickListener);
	}

	public String getUserId(){
		return mUserId.getText().toString().trim();
	}
	
	public String getPassword(){
		return mPassword.getText().toString().trim();
	}
	
	public void userNameError(Context context) {
		Toast.makeText(context, "閻€劍鍩涢崥宥勭瑝閼虫垝璐熺粚鐚寸磼", Toast.LENGTH_SHORT).show();
	}
	
	public void passwordError(Context context) {
		Toast.makeText(context, "鐎靛棛鐖滄稉宥堝厴娑撹櫣鈹栭敍锟�", Toast.LENGTH_SHORT).show();
	}

    public void setListener(Listener listener){
        this.mListener = listener;
    }

    public interface Listener {
        public void onSoftKeyboardShown(int softKeyboardHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Rect rect = new Rect();
        Activity activity = (Activity)getContext();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
        int screenHeight = dm.heightPixels;
        int diff = (screenHeight - statusBarHeight) - height;
        if(mListener != null){
            mListener.onSoftKeyboardShown(diff);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

package cn.jpush.im.android.demo.view;

import cn.jpush.im.android.demo.Listener.OnChangedListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.contact.R;

public class SlipButton extends View implements OnTouchListener {
	private boolean enabled = true;
	public boolean flag = true;
	public boolean NowChoose = true;
	private boolean OnSlip = false;
	public float DownX=0f,NowX=0f;
	private Rect Btn_On,Btn_Off;
	
	private boolean isChgLsnOn = false;
	private OnChangedListener ChgLsn;
	private Bitmap bg_on,bg_off, slip_btn;
	private int mId;
	
	public SlipButton(Context context) {
		super(context);
		init();
	}

	public SlipButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public void setChecked(boolean fl){
		if(fl){
			flag = true; NowChoose = true; NowX = 80;
		}else{
			flag = false; NowChoose = false; NowX = 0;
		}
	}
	
	public void setEnabled(boolean b){
		if(b){
			enabled = true;
		}else{
			enabled = false;
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	private void init(){
		bg_on = BitmapFactory.decodeResource(getResources(), R.drawable.slip_on);
		bg_off = BitmapFactory.decodeResource(getResources(), R.drawable.slip_off);
		slip_btn = BitmapFactory.decodeResource(getResources(), R.drawable.slip);
		Btn_On = new Rect(0,0,slip_btn.getWidth(),slip_btn.getHeight());
		Btn_Off = new Rect(
				bg_off.getWidth()-slip_btn.getWidth(),
				0,
				bg_off.getWidth(),
				slip_btn.getHeight());
		setOnTouchListener(this);
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		float x;
		{
			if (flag) {NowX = 80;flag = false;
			}
			if(NowX<(bg_on.getWidth()/2))
				canvas.drawBitmap(bg_off,matrix, paint);
			else
				canvas.drawBitmap(bg_on,matrix, paint);
//			if(NowX >= bg_on.getWidth() - slip_btn.getWidth())
//				canvas.drawBitmap(bg_on, matrix, paint);
//			else if(NowX <= 0)
//				canvas.drawBitmap(bg_off, matrix, paint);
//			else if(0 < NowX && NowX < 80)
//				canvas.drawBitmap(slipping, matrix, paint);
			
			if(OnSlip)
			{
				if(NowX >= bg_on.getWidth())
					x = bg_on.getWidth()-slip_btn.getWidth()/2;
				else
					x = NowX - slip_btn.getWidth()/2;
			}else{
				if(NowChoose)
					x = Btn_Off.left;
				else
					x = Btn_On.left;
			}
		if(x<0)
			x = 0;
		else if(x>bg_on.getWidth()-slip_btn.getWidth())
			x = bg_on.getWidth()-slip_btn.getWidth();
		canvas.drawBitmap(slip_btn,x, 0, paint);
		}
	}


	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouch(View v, MotionEvent event) {
		if(!enabled){
			return false;
		}
		switch(event.getAction())
		{
		case MotionEvent.ACTION_MOVE:
			OnSlip = true;
			NowX = event.getX();
			break;
		case MotionEvent.ACTION_DOWN:
		if(event.getX()>bg_on.getWidth()||event.getY()>bg_on.getHeight())
            return false;
			DownX = event.getX();
			NowX = DownX;
			break;
		case MotionEvent.ACTION_UP:
			OnSlip = false;
			boolean LastChoose = NowChoose;
			if(event.getX() >= (bg_on.getWidth()/2))
				NowChoose = true;
			else
				NowChoose = false;
			if(isChgLsnOn && (LastChoose!=NowChoose))
				ChgLsn.OnChanged(mId, NowChoose);
			break;
		default:
		
		}
		invalidate();
		return true;
	}
	
	public void setOnChangedListener(int id, OnChangedListener l){
        mId = id;
		isChgLsnOn = true;
		ChgLsn = l;
	}
}

package cn.jpush.im.android.demo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.contact.R;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import cn.jpush.im.android.demo.tools.NativeImageLoader;
import cn.jpush.im.android.demo.tools.NativeImageLoader.NativeImageCallBack;
import cn.jpush.im.android.demo.view.MyImageView;
import cn.jpush.im.android.demo.view.MyImageView.OnMeasureListener;

public class PickPictureAdapter extends BaseAdapter {
    /**
     * 閻€劍娼电�涙ê鍋嶉崶鍓у閻ㄥ嫰锟藉鑵戦幆鍛枌
     */
    private HashMap<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
    private GridView mGridView;
    private List<String> mList;
    protected LayoutInflater mInflater;
    private Context mContext;
    private Button mSendBtn;
    private double mDensity;

    public PickPictureAdapter(Context context, List<String> list, GridView mGridView) {
        this.mContext = context;
        this.mList = list;
        this.mGridView = mGridView;
        mInflater = LayoutInflater.from(context);
        Activity activity = (Activity) mContext;
        mSendBtn = (Button) activity.findViewById(R.id.pick_picture_send_btn);
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        String path = mList.get(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pick_picture_detail_grid_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (MyImageView) convertView.findViewById(R.id.child_image);
            viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.child_checkbox);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.mImageView.setImageResource(R.drawable.friends_sends_pictures_no);
        }
        viewHolder.mImageView.setTag(path);
        if (mSelectMap.size() > 0)
            initSelectedPicture();
        viewHolder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.mCheckBox.isChecked()) {
                    if (mSelectMap.size() < 9) {
                        mSelectMap.put(position, true);
                        addAnimation(viewHolder.mCheckBox);
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.picture_num_limit_toast), Toast.LENGTH_SHORT).show();
                        viewHolder.mCheckBox.setChecked(mSelectMap.containsKey(position) ? mSelectMap.get(position) : false);
                    }
                }else if(mSelectMap.size() <= 9){
                    mSelectMap.remove(position);
                }

                if (mSelectMap.size() > 0) {
                    mSendBtn.setText(mContext.getString(R.string.send) + "(" + mSelectMap.size() + "/" + "9)");
                } else {
                    mSendBtn.setText(mContext.getString(R.string.send));
                    mSendBtn.setClickable(false);
                }
            }
        });

        viewHolder.mCheckBox.setChecked(mSelectMap.containsKey(position) ? mSelectMap.get(position) : false);

        //閸掆晝鏁ativeImageLoader缁濮炴潪鑺ユ拱閸︽澘娴橀悧锟�
        Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, (int) (80 * mDensity), new NativeImageCallBack() {

            @Override
            public void onImageLoader(Bitmap bitmap, String path) {
                ImageView mImageView = (ImageView) mGridView.findViewWithTag(path);
                if (bitmap != null && mImageView != null) {
                    mImageView.setImageBitmap(bitmap);
                }
            }
        });

        if (bitmap != null) {
            viewHolder.mImageView.setImageBitmap(bitmap);
        } else {
            viewHolder.mImageView.setImageResource(R.drawable.friends_sends_pictures_no);
        }

        return convertView;
    }

    private void initSelectedPicture() {

    }

    /**
     * 缂佹heckBox閸旂姷鍋ｉ崙璇插З閻紮绱濋崚鈺冩暏瀵拷濠ф劕绨眓ineoldandroids鐠佸墽鐤嗛崝銊ф暰
     *
     * @param view
     */
    private void addAnimation(View view) {
        float[] vaules = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
                ObjectAnimator.ofFloat(view, "scaleY", vaules));
        set.setDuration(150);
        set.start();
    }


    /**
     * 閼惧嘲褰囬柅澶夎厬閻ㄥ嚘tem閻ㄥ埦osition
     *
     * @return
     */
    public List<Integer> getSelectItems() {
        List<Integer> list = new ArrayList<Integer>();
        for (Iterator<Map.Entry<Integer, Boolean>> it = mSelectMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Boolean> entry = it.next();
            if (entry.getValue()) {
                list.add(entry.getKey());
            }
        }

        return list;
    }

    /*
    閼惧嘲绶遍柅澶夎厬閻ㄥ嫬娴橀悧鍥风礉閻€劋绨悙鐟板毊閸ュ墽澧栨潻娑樺弳BrowserViewPagerActivity閻ㄥ嫬鍨垫慨瀣
     */
    public int[] getSelectedArray() {
        int pathArray[] = new int[mList.size()];
        for (int i = 0; i < pathArray.length; i++)
            pathArray[i] = 0;
        for (Iterator<Map.Entry<Integer, Boolean>> it = mSelectMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Boolean> entry = it.next();
            if (entry.getValue()) {
                pathArray[entry.getKey()] = 1;
            }
        }
        return pathArray;
    }

    public void refresh(int[] pathArray) {
        Log.i("PickPictureAdapter", "Refreshing SelectedMap pathArray:" + pathArray.toString());
        mSelectMap.clear();
        for (int i = 0; i < pathArray.length; i++) {
            if (pathArray[i] == 1)
                mSelectMap.put(i, true);
        }
        notifyDataSetChanged();
    }


    public static class ViewHolder {
        public MyImageView mImageView;
        public CheckBox mCheckBox;
    }
}

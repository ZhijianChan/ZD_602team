package cn.jpush.im.android.demo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.contact.R;

import java.util.List;


import cn.jpush.im.android.demo.entity.ImageBean;
import cn.jpush.im.android.demo.tools.NativeImageLoader;
import cn.jpush.im.android.demo.view.MyImageView;

/**
 * Created by Ken on 2015/1/21.
 */
public class AlbumListAdapter extends BaseAdapter{

    private List<ImageBean> list;
    private Point mPoint = new Point(0, 0);//閻€劍娼电亸浣筋棅ImageView閻ㄥ嫬顔旈崪宀勭彯閻ㄥ嫬顕挒锟�
    private ListView mListView;
    private Context mContext;
    protected LayoutInflater mInflater;
    private double mDensity;

    public AlbumListAdapter(Context context, List<ImageBean> list, ListView listView){
        mContext = context;
        this.list = list;
        this.mListView = listView;
        mInflater = LayoutInflater.from(context);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
    }

    @Override
    public int getCount() {
        if(list.size() > 0)
            return list.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        ImageBean mImageBean = list.get(position);
        String path = mImageBean.getTopImagePath();
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.pick_picture_total_list_item, null);
            viewHolder.mImageView = (MyImageView) convertView.findViewById(R.id.group_image);
            viewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.group_title);
            viewHolder.mTextViewCounts = (TextView) convertView.findViewById(R.id.group_count);

            //閻€劍娼甸惄鎴濇儔ImageView閻ㄥ嫬顔旈崪宀勭彯
            viewHolder.mImageView.setOnMeasureListener(new MyImageView.OnMeasureListener() {

                @Override
                public void onMeasureSize(int width, int height) {
                    mPoint.set(width, height);
                }
            });

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.mImageView.setImageResource(R.drawable.friends_sends_pictures_no);
        }

        viewHolder.mTextViewTitle.setText(mImageBean.getFolderName());
        viewHolder.mTextViewCounts.setText("(" + Integer.toString(mImageBean.getImageCounts()) + ")");
        //缂佹┃mageView鐠佸墽鐤嗙捄顖氱窞Tag,鏉╂瑦妲稿鍌涱劄閸旂姾娴囬崶鍓у閻ㄥ嫬鐨幎锟藉锟�
        viewHolder.mImageView.setTag(path);

        //閸掆晝鏁ativeImageLoader缁濮炴潪鑺ユ拱閸︽澘娴橀悧锟�
        Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, (int)(80 * mDensity), new NativeImageLoader.NativeImageCallBack() {

            @Override
            public void onImageLoader(Bitmap bitmap, String path) {
                ImageView mImageView = (ImageView) mListView.findViewWithTag(path);
                if(bitmap != null && mImageView != null){
                    mImageView.setImageBitmap(bitmap);
                }
            }
        });

        if(bitmap != null){
            viewHolder.mImageView.setImageBitmap(bitmap);
        }else{
            viewHolder.mImageView.setImageResource(R.drawable.friends_sends_pictures_no);
        }
        return convertView;
    }

    public static class ViewHolder{
        public MyImageView mImageView;
        public TextView mTextViewTitle;
        public TextView mTextViewCounts;
    }
}

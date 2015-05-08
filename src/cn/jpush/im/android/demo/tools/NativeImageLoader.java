package cn.jpush.im.android.demo.tools;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.UserInfo;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;

/**
 * 閺堫剙婀撮崶鍓у閸旂姾娴囬崳锟�,闁插洨鏁ら惃鍕Ц瀵倹顒炵憴锝嗙�介張顒�婀撮崶鍓у閿涘苯宕熸笟瀣佸蹇撳焺閻⑩暎etInstance()閼惧嘲褰嘚ativeImageLoader鐎圭偘绶�
 * 鐠嬪啰鏁oadNativeImage()閺傝纭堕崝鐘烘祰閺堫剙婀撮崶鍓у閿涘本顒濈猾璇插讲娴ｆ粈璐熸稉锟芥稉顏勫鏉炶姤婀伴崷鏉挎禈閻楀洨娈戝銉ュ徔缁拷
 */
public class NativeImageLoader {
    private LruCache<String, Bitmap> mMemoryCache;
    private static NativeImageLoader mInstance = new NativeImageLoader();
    private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(1);


    private NativeImageLoader() {
        //閼惧嘲褰囨惔鏃傛暏缁嬪绨惃鍕付婢堆冨敶鐎涳拷
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        //閻€劍娓舵径褍鍞寸�涙娈�1/4閺夈儱鐡ㄩ崒銊ユ禈閻楋拷
        final int cacheSize = maxMemory / 4;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            //閼惧嘲褰囧В蹇撶炊閸ュ墽澧栭惃鍕亣鐏忥拷
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    /**
     * 閸掓繂顫愰崠鏍暏閹村嘲銇旈崓蹇曠处鐎涳拷
     *
     * @param userIDList 閻€劍鍩汭D List
     * @param length     婢舵潙鍎氱�逛粙鐝�
     * @param callBack   缂傛挸鐡ㄩ崶鐐剁殶
     */
    public void setAvatarCache(final List<String> userIDList, final int length, final cacheAvatarCallBack callBack) {
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                if (msg.getData() != null) {
                    callBack.onCacheAvatarCallBack(msg.getData().getInt("status", -1));
                }
            }
        };


        for (final String userID : userIDList) {
            //閼汇儰璐烠urrentUser閿涘瞼娲块幒銉ㄥ箯閸欐牗婀伴崷鎵畱婢舵潙鍎氶敍鍦晆rrentUser閺堫剙婀存径鏉戝剼娑撶儤娓堕弬甯礆
            if (userID.equals(JMessageClient.getMyInfo().getUserName())) {
                File file = JMessageClient.getMyInfo().getAvatar();
                if (file == null || !file.exists()) {
                    continue;
                } else {
                    Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), length, length);
                    if (null != bitmap) {
                        mMemoryCache.put(userID, bitmap);
                    }
                    continue;
                }
            } else if (mMemoryCache.get(userID) != null) {
                continue;
            } else {
                JMessageClient.getUserInfo(userID, new GetUserInfoCallback(false) {
                    @Override
                    public void gotResult(int i, String s, UserInfo userInfo) {
                        if (i == 0) {
                            File file = userInfo.getAvatar();
                            if (file != null) {
                                Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), length, length);
                                addBitmapToMemoryCache(userID, bitmap);
                            } else {
//                                Bitmap bitmap = BitmapLoader.getBitmapFromFile(getR.drawable.head_icon, length, length);
                            }
                            android.os.Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putInt("status", 0);
                            msg.setData(bundle);
                            msg.sendToTarget();
                        }
                    }
                });
            }
        }


    }

    /**
     * 濮濄倖鏌熷▔鏇犳纯閹恒儱婀狶ruCache娑擃厼顤冮崝鐘辩娑擃亪鏁崐鐓庮嚠
     *
     * @param targetID 閻€劍鍩涢崥锟�
     * @param path     婢舵潙鍎氱捄顖氱窞
     */
    public void putUserAvatar(String targetID, String path, int size) {
        if (path != null) {
            Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, size, size);
            if (bitmap != null) {
                addBitmapToMemoryCache(targetID, bitmap);
            }
        }
    }

    /**
     * 闁俺绻冨銈嗘煙濞夋洘娼甸懢宄板絿NativeImageLoader閻ㄥ嫬鐤勬笟锟�
     *
     * @return
     */
    public static NativeImageLoader getInstance() {
        return mInstance;
    }


    /**
     * 濮濄倖鏌熷▔鏇熸降閸旂姾娴囬張顒�婀撮崶鍓у閿涘矁绻栭柌宀�娈憁Point閺勵垳鏁ら弶銉ョ殱鐟佸將mageView閻ㄥ嫬顔旈崪宀勭彯閿涘本鍨滄禒顑跨窗閺嶈宓乴ength閺夈儴顥嗛崜鐙焛tmap
     *
     * @param path
     * @param length
     * @param callBack
     * @return
     */
    public Bitmap loadNativeImage(final String path, final int length, final NativeImageCallBack callBack) {
        //閸忓牐骞忛崣鏍у敶鐎涙ü鑵戦惃鍑歩tmap
        Bitmap bitmap = getBitmapFromMemCache(path);

        final Handler handler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                callBack.onImageLoader((Bitmap) msg.obj, path);
            }

        };

        //閼汇儴顕欱itmap娑撳秴婀崘鍛摠缂傛挸鐡ㄦ稉顓ㄧ礉閸掓瑥鎯庨悽銊у殠缁嬪骞撻崝鐘烘祰閺堫剙婀撮惃鍕禈閻楀浄绱濋獮璺虹殺Bitmap閸旂姴鍙嗛崚鐧縈emoryCache娑擄拷
        if (bitmap == null) {
            mImageThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    //閸忓牐骞忛崣鏍ф禈閻楀洨娈戠紓鈺冩殣閸ワ拷
                    Bitmap mBitmap = decodeThumbBitmapForFile(path, length, length);
                    Message msg = handler.obtainMessage();
                    msg.obj = mBitmap;
                    handler.sendMessage(msg);

                    //鐏忓棗娴橀悧鍥у閸忋儱鍩岄崘鍛摠缂傛挸鐡�
                    addBitmapToMemoryCache(path, mBitmap);
                }
            });
        }
        return bitmap;

    }

    /**
     * 濮濄倖鏌熷▔鏇熸降閸旂姾娴囬張顒�婀撮崶鍓у閿涘矁绻栭柌宀�娈憁Point閺勵垳鏁ら弶銉ョ殱鐟佸將mageView閻ㄥ嫬顔旈崪宀勭彯閿涘本鍨滄禒顑跨窗閺嶈宓両mageView閹貉傛閻ㄥ嫬銇囩亸蹇旀降鐟佷礁澹�Bitmap
     *
     * @param path
     * @param point
     * @param mCallBack
     * @return
     */
    public Bitmap loadNativeImage(final String path, final Point point, final NativeImageCallBack mCallBack) {
        //閸忓牐骞忛崣鏍у敶鐎涙ü鑵戦惃鍑歩tmap
        Bitmap bitmap = getBitmapFromMemCache(path);

        final Handler mHander = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                mCallBack.onImageLoader((Bitmap) msg.obj, path);
            }

        };

        //閼汇儴顕欱itmap娑撳秴婀崘鍛摠缂傛挸鐡ㄦ稉顓ㄧ礉閸掓瑥鎯庨悽銊у殠缁嬪骞撻崝鐘烘祰閺堫剙婀撮惃鍕禈閻楀浄绱濋獮璺虹殺Bitmap閸旂姴鍙嗛崚鐧縈emoryCache娑擄拷
        if (bitmap == null) {
            mImageThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    //閸忓牐骞忛崣鏍ф禈閻楀洨娈戠紓鈺冩殣閸ワ拷
                    Bitmap mBitmap = decodeThumbBitmapForFile(path, point.x == 0 ? 0 : point.x, point.y == 0 ? 0 : point.y);
                    Message msg = mHander.obtainMessage();
                    msg.obj = mBitmap;
                    mHander.sendMessage(msg);

                    //鐏忓棗娴橀悧鍥у閸忋儱鍩岄崘鍛摠缂傛挸鐡�
                    addBitmapToMemoryCache(path, mBitmap);
                }
            });
        }
        return bitmap;

    }


    /**
     * 瀵帮拷閸愬懎鐡ㄧ紓鎾崇摠娑擃厽鍧婇崝鐕榠tmap
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public void updateBitmapFromCache(String key, Bitmap bitmap) {
        if (null != bitmap) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public void releaseCache() {
        mMemoryCache.evictAll();
    }

    /**
     * 閺嶈宓乲ey閺夈儴骞忛崣鏍у敶鐎涙ü鑵戦惃鍕禈閻楋拷
     *
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemCache(String key) {
        if (key == null) {
            return null;
        } else {
            return mMemoryCache.get(key);
        }
    }


    /**
     * 閺嶈宓乂iew(娑撴槒顩﹂弰鐤榤ageView)閻ㄥ嫬顔旈崪宀勭彯閺夈儴骞忛崣鏍ф禈閻楀洨娈戠紓鈺冩殣閸ワ拷
     *
     * @param path
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    private Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //鐠佸墽鐤嗘稉绨峳ue,鐞涖劎銇氱憴锝嗙�紹itmap鐎电钖勯敍宀冾嚉鐎电钖勬稉宥呭窗閸愬懎鐡�
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //鐠佸墽鐤嗙紓鈺傛杹濮ｆ柧绶�
        options.inSampleSize = calculateInSampleSize(options, viewWidth, viewHeight);

        //鐠佸墽鐤嗘稉绡篴lse,鐟欙絾鐎紹itmap鐎电钖勯崝鐘插弳閸掓澘鍞寸�涙ü鑵�
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }


    /**
     * 鐠侊紕鐣婚崢瀣級濮ｆ柧绶ラ崐锟�
     *
     * @param options   鐟欙絾鐎介崶鍓у閻ㄥ嫰鍘ょ純顔讳繆閹拷
     * @param reqWidth  閹碉拷闂囷拷閸ュ墽澧栭崢瀣級鐏忓搫顕張锟界亸蹇擃啍鎼达拷
     * @param reqHeight 閹碉拷闂囷拷閸ュ墽澧栭崢瀣級鐏忓搫顕張锟界亸蹇涚彯鎼达拷
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 娣囨繂鐡ㄩ崶鍓у閸樼喎顔旀妯猴拷锟�
        final int height = options.outHeight;
        final int width = options.outWidth;

        // 閸掓繂顫愰崠鏍у竾缂傗晜鐦笟瀣╄礋1
        int inSampleSize = 1;

        // 瑜版挸娴橀悧鍥ь啍妤傛ê锟介棿鎹㈡担鏇氱娑擃亜銇囨禍搴㈠闂囷拷閸樺缂夐崶鍓у鐎逛粙鐝崐鍏兼,鏉╂稑鍙嗗顏嗗箚鐠侊紕鐣荤化鑽ょ埠
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 閸樺缂夊В鏂剧伐閸婂吋鐦″▎鈥虫儕閻滎垯琚遍崐宥咁杻閸旓拷,
            // 閻╂潙鍩岄崢鐔锋禈鐎逛粙鐝崐鑲╂畱娑擄拷閸楀﹪娅庢禒銉ュ竾缂傗晛锟界厧鎮楅柈绲舵径褌绨幍锟介棁锟界�逛粙鐝崐闂磋礋濮濓拷
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /**
     * 閸旂姾娴囬張顒�婀撮崶鍓у閻ㄥ嫬娲栫拫鍐╁复閸欙拷
     *
     * @author xiaanming
     */
    public interface NativeImageCallBack {
        /**
         * 瑜版挸鐡欑痪璺ㄢ柤閸旂姾娴囩�瑰奔绨￠張顒�婀撮惃鍕禈閻楀浄绱濈亸鍜甶tmap閸滃苯娴橀悧鍥熅瀵板嫬娲栫拫鍐ㄦ躬濮濄倖鏌熷▔鏇氳厬
         *
         * @param bitmap
         * @param path
         */
        public void onImageLoader(Bitmap bitmap, String path);
    }

    public interface cacheAvatarCallBack {
        public void onCacheAvatarCallBack(int status);
    }
}

package com.example.imageoptimization;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import androidx.annotation.IntDef;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 图片压缩工具类
 */
public class ImageCompress {

    // 文件的来源，默认为文件
    private @Origin byte mDocumentOrigin = IDocumentOrigin.FILE;

    // 如果文件来源为图片，需要指定文件的路径
    private String originPath = "";

    // 上下文，如果图片来源与app资源，必须获取app的上下文
    private Context mContext;

    // 图片资源ID，如果如果图片来源与app资源，必须设置图片资源ID
    private int resourceID;

    // 最大宽度
    private int maxWidth = 0;

    // 最大高度
    private int maxHeight = 0;

    // 图片压缩的格式
    private Bitmap.CompressFormat defaultFormat = Bitmap.CompressFormat.JPEG;

    // jpeg图片格式
    private static final String JPEG_Format = ".jpg";

    // png图片格式
    private static final String PNG_Format = ".png";

    // webp图片格式
    private static final String WEBP_Format = ".webp";

    // 图片的默认输出名称
    private static final String defaultDestName = "resultImage";

    // 图片输出路径
    private String imageDestPath = "";

    // 图片输出名称
    private String imageDestName = "";

    // 图片压缩质量，区间是：[0, 100]， 默认为100
    private int mQuality = 100;

    /**
     * 设置文件来源
     * @param mDocumentOrigin
     * @return
     */
    public ImageCompress setmDocumentOrigin(@Origin byte mDocumentOrigin) {
        this.mDocumentOrigin = mDocumentOrigin;
        return getInstance();
    }

    /**
     * 设置文件路径
     * @param originPath
     * @return
     */
    public ImageCompress setOriginPath(String originPath) {
        this.originPath = originPath;
        return getInstance();
    }

    /**
     * 设置最大宽度
     * @param maxWidth
     */
    public ImageCompress setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return getInstance();
    }

    /**
     * 设置最大高度
     * @param maxHeight
     */
    public ImageCompress setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return getInstance();
    }

    /**
     * 设置图片压缩格式
     * @param defaultFormat
     */
    public ImageCompress setDefaultFormat(Bitmap.CompressFormat defaultFormat) {
        this.defaultFormat = defaultFormat;
        return getInstance();
    }

    /**
     * 设置股图片的质量因子
     * @param mQuality
     * @return
     */
    public ImageCompress setQuality(int mQuality) {
        this.mQuality = mQuality;
        return getInstance();
    }

    /**
     * 绑定上下文
     * @param mContext
     * @return
     */
    public ImageCompress with(Context mContext) {
        this.mContext = mContext.getApplicationContext();
        return getInstance();
    }

    /**
     * 设置图片资源ID
     * @param resourceID
     */
    public ImageCompress setResourceID(int resourceID) {
        this.resourceID = resourceID;
        return getInstance();
    }

    /**
     * 设置图片的输出路径
     * @param destPath
     * @return
     */
    public ImageCompress setDestPath(String destPath) {
        File destFilePath = new File(destPath);
        if (destFilePath.isFile()) {
            throw new RuntimeException("destPath is error, Iit can only be a directory");
        }
        this.imageDestPath = destPath;
        return getInstance();
    }

    /**
     * 设置图片的输出名称
     * @param destName
     * @return
     */
    public ImageCompress setDestName(String destName) {
        this.imageDestName = destName;
        return getInstance();
    }

    static class ImageCompressHolder {
        public static ImageCompress instance = new ImageCompress();
    }

    public static ImageCompress getInstance() {
        return ImageCompressHolder.instance;
    }

    /**
     * 文件来源常量定义
     */
    interface IDocumentOrigin {
        byte FILE = 0x01; // 来源于文件
        byte RESOURCE = 0x02; // 来源于资源
    }

    //flag=true时，参数可以“|”传递多个参数，flag=false时，只能传递一个参数
    @IntDef(flag = false, value = {IDocumentOrigin.FILE, IDocumentOrigin.RESOURCE})
    //注解作用域参数、方法、成员变量
    @Target({ElementType.PARAMETER,ElementType.METHOD,ElementType.FIELD})
   //仅仅在源码阶段有效
    @Retention(RetentionPolicy.SOURCE)
    private @interface Origin{

    }

    private Bitmap getBitmapFromFile(String pathName, BitmapFactory.Options opts) {
        if (TextUtils.isEmpty(pathName)) {
            throw new RuntimeException("pathName is null");
        }
        return BitmapFactory.decodeFile(pathName, opts);
    }

    private Bitmap getBitmapFromResource(Context mContext, int resourceID, BitmapFactory.Options opts) {
        if (mContext == null) {
            throw new RuntimeException("context is null");
        }
        if (resourceID <= 0) {
            throw new RuntimeException("resource ID is not valid");
        }
        return BitmapFactory.decodeResource(mContext.getResources(), resourceID, opts);
    }


    /**
     * 根据图片来源获取Bitmap
     * @param options
     * @return
     */
    private Bitmap getBitmapByOrigin(BitmapFactory.Options options) {
        if (mDocumentOrigin == IDocumentOrigin.FILE) {
            return getBitmapFromFile(originPath, options);
        } else  if (mDocumentOrigin == IDocumentOrigin.RESOURCE) {
            if (mContext == null) {
                throw new RuntimeException("context is null");
            }
            return getBitmapFromResource(mContext, resourceID, options);
        } else {
            return getBitmapFromFile(originPath, options);
        }
    }

    /**
     * 初始化像素密度缩放的Option
     *
     * @mRatio 图片缩小的倍率，如果mRatio = 3， 图片的横纵各缩小3倍
     *
     * @return
     */
    private void initDensityOption(BitmapFactory.Options options, int mRatio) {
        if (mContext == null) {
            throw new RuntimeException("context is null");
        }
        //设置这个Bitmap是否可以被缩放，默认值是true，表示可以被缩放。
        options.inScaled = true;
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        //表示这个bitmap的像素密度，当inDensity为0时，系统默认赋值为屏幕当前像素密度
        options.inDensity = dm.densityDpi;
        //表示要被画出来时的目标像素密度，当inTargetDensity为0时，系统默认赋值为屏幕当前像素密度
        options.inTargetDensity = options.inDensity / mRatio;
        //表示实际设备的像素密度
        options.inScreenDensity = 0;
    }


    /**
     * 重置bitmap大小
     * @param maxWith 图片最大宽度
     * @param maxHeight 图片最大高度
     * @return
     */
    private Bitmap resizeBitmap(int maxWith, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //需要拿得到系统处理的信息
        options.inJustDecodeBounds = true;
        //把原来的解码参数改了再去生成bitmap
        getBitmapByOrigin(options);
        //取到宽高
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        // 计算图片缩小倍率
        int mRatio = calcuteRatio(outWidth, outHeight, maxWith, maxHeight);
        // 缩小图片分辨率
        initDensityOption(options, mRatio);
        // 将图片的颜色模式设置为RGB_565
        options.inPreferredConfig=Bitmap.Config.RGB_565;
        options.inJustDecodeBounds=false;
        return getBitmapByOrigin(options);
    }

    /**
     * 计算图片的缩放比例(只有当原图横纵都大于max值时，mRatio才会+1)
     *
     * @param srcWidth
     * @param srcHeight
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    private int calcuteRatio(int srcWidth, int srcHeight, int maxWidth, int maxHeight) {
        int mRatio = 1;
        if (srcWidth > maxWidth && srcHeight > maxHeight) {
            mRatio += 1;
            while (srcWidth / mRatio > maxWidth && srcHeight / mRatio > maxHeight){
                mRatio += 1;
            }
        }
        return mRatio;
    }

    /**
     * 质量压缩
     */
    public synchronized void compress() {
        if (mQuality < 0) {
            mQuality = 0;
        }
        if (mQuality > 100) {
            mQuality = 100;
        }
        ByteArrayOutputStream bos = null;
        FileOutputStream fos= null;
        try {
            Bitmap reBitmap = resizeBitmap(maxWidth, maxHeight);
            bos = new ByteArrayOutputStream();
            reBitmap.compress(defaultFormat, mQuality, bos);

            String destPath = "";
            if (TextUtils.isEmpty(imageDestPath)) {
                File originFile = new File(originPath);
                destPath = originFile.getParent(); // 原图路径，默认的输出路径就是原图路径
            } else {
                destPath = imageDestPath;
            }
            String mImageFormat = JPEG_Format; // 图片格式
            if (defaultFormat == Bitmap.CompressFormat.JPEG) {
                mImageFormat = JPEG_Format;
            } else if (defaultFormat == Bitmap.CompressFormat.PNG) {
                mImageFormat = PNG_Format;
            } else if (defaultFormat == Bitmap.CompressFormat.WEBP) {
                mImageFormat = WEBP_Format;
            }

            String destName = "";
            if (TextUtils.isEmpty(imageDestName)) {
                destName = defaultDestName;
            } else {
                destName = imageDestName;
            }
            fos = new FileOutputStream(new File(destPath, destName + mImageFormat));
            fos.write(bos.toByteArray());
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 清空缓存
            clearCache();
            try {
                if (fos != null) {
                    fos.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清空缓存
     */
    private synchronized void clearCache() {
        // 重置默认文件来源
        mDocumentOrigin = IDocumentOrigin.FILE;

        // 重置原文件的路径
        originPath = "";

        // 重置图片资源ID
        resourceID = 0;

        // 重置最大宽度
        maxWidth = 0;

        // 重置最大高度
        maxHeight = 0;

        // 重置图片压缩的格式
        defaultFormat = Bitmap.CompressFormat.JPEG;

        // 重置图片输出路径
        imageDestPath = "";

        // 重置图片输出名称
        imageDestName = "";

        // 重置图片压缩质量，
        mQuality = 100;
    }

    /**
     * 得到bitmap的大小
     */
    public static int getBitmapSize(Bitmap bitmap) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        }
        // 在低版本中用一行的字节x高度
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}

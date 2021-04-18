package com.example.imageoptimization;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File sdFile = Environment.getExternalStorageDirectory();
        File originFile = new File(sdFile, "SrcImg.jpg");
        ImageCompress.getInstance()
                .with(getApplicationContext())
                .setmDocumentOrigin(ImageCompress.IDocumentOrigin.FILE) // 指定文件的来源
                .setOriginPath(originFile.getAbsolutePath()) // 指定原文件路径
                .setDefaultFormat(Bitmap.CompressFormat.JPEG) // 指定文件编码和解码格式
                .setMaxWidth(300) // 指定图片的最大输出宽度
                .setMaxHeight(300) // 指定图片的最大输出高度
                .setDestPath(originFile.getParent()) // 设置默认的输出路径
                .setDestName("resultImage1") // 设置默认的输出名称
                .setQuality(80) // 指定输出的图片质量
                .compress(); // 压缩

        ImageCompress.getInstance()
                .with(getApplicationContext())
                .setmDocumentOrigin(ImageCompress.IDocumentOrigin.RESOURCE) // 指定文件的来源
                .setResourceID(R.drawable.src_img)
                .setDefaultFormat(Bitmap.CompressFormat.JPEG) // 指定文件编码和解码格式
                .setMaxWidth(300) // 指定图片的最大输出宽度
                .setMaxHeight(300) // 指定图片的最大输出高度
                .setDestPath(Environment.getExternalStorageDirectory().getPath()) // 设置默认的输出路径
                .setDestName("resultImage2") // 设置默认的输出名称
                .setQuality(80) // 指定输出的图片质量
                .compress(); // 压缩
    }
}
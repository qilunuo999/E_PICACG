package com.pic603.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.pic603.bean.Image;
import com.pic603.e_picacg.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DisplayBigPicture {
    private ImageView mImageView;
    private Dialog dialog;
    private Context parentContext;
    private Image im;
    private File imageFile;
    private String fileDir = "/sdcard/myFolder";

    public void init(Context context, final Image image) {
        parentContext = context;
        im = image;
        //大图所依附的dialog
        dialog = new Dialog(parentContext, R.style.ThemeOverlay_AppCompat);
        mImageView = getImageView();
        dialog.setContentView(mImageView);
        dialog.show();

        //大图的点击事件（点击让他消失）
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //大图的长按监听
        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //弹出的“保存图片”的Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
                builder.setItems(new String[]{"保存图片","分享图片"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case 0 :
                                DownloadPicture downloadPicture = new DownloadPicture(im.getImage());
                                downloadPicture.start();
                                Toast.makeText(parentContext, "保存图片成功" , Toast.LENGTH_SHORT).show();
                                break;
                            case 1 :
                                /** * 分享图片 */
                                Intent share_intent = new Intent();
                                share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
                                share_intent.setType("image/*");  //设置分享内容的类型
                                share_intent.putExtra(Intent.EXTRA_STREAM,fileDir + im.getImageName());
                                //创建分享的Dialog
                                share_intent = Intent.createChooser(share_intent, "精彩图片分享");
                                parentContext.startActivity(share_intent);
                                break;
                            default : break;
                        }
                    }
                });

                builder.show();
                return true;
            }
        });
    }

    public void init(Context context, File file) {
        parentContext = context;
        imageFile = file;
        //大图所依附的dialog
        dialog = new Dialog(parentContext, R.style.ThemeOverlay_AppCompat);
        mImageView = getImageView(imageFile);
        dialog.setContentView(mImageView);
        dialog.show();

        //大图的点击事件（点击让他消失）
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //大图的长按监听
        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //弹出的“保存图片”的Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
                builder.setItems(new String[]{"分享图片"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {/** * 分享图片 */
                            Intent share_intent = new Intent();
                            share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
                            share_intent.setType("image/*");  //设置分享内容的类型
                            share_intent.putExtra(Intent.EXTRA_STREAM, imageFile.getPath());
                            //创建分享的Dialog
                            share_intent = Intent.createChooser(share_intent, "精彩图片分享");
                            parentContext.startActivity(share_intent);
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    //保存图片
    private void saveCroppedImage(Bitmap mBitmap) {
        System.out.println(mBitmap.getHeight());
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return;
        }
        FileOutputStream b = null;
        File file = new File(fileDir);
        if (!file.exists()){
            file.mkdirs();// 创建文件夹
        }
        String fileName = fileDir + im.getImageName();;// 图片名字
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //动态的ImageView
    private ImageView getImageView(){
        ImageView iv = new ImageView(parentContext);
        //宽高
        iv.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //设置Padding
        iv.setPadding(20,20,20,20);
        //imageView设置图片来源
        Glide.with(parentContext).load(im.getImage()).into(iv);
        Glide.with(parentContext).asBitmap().load(im.getImage()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                saveCroppedImage(resource);
            }
        });
        return iv;
    }

    //直接传入Bitmap生成ImageView
    private ImageView getImageView(File file){
        ImageView iv = new ImageView(parentContext);
        //宽高
        iv.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //设置Padding
        iv.setPadding(20,20,20,20);
        //imageView设置图片来源
        Glide.with(parentContext).load(file.getPath()).into(iv);
        return iv;
    }
}

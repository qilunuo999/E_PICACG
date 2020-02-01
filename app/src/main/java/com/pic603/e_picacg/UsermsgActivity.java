package com.pic603.e_picacg;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pic603.bean.User;
import com.pic603.service.UserService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import cn.bmob.v3.BmobUser;

public class UsermsgActivity extends AppCompatActivity {

    private LinearLayout headlyt;
    private User user = null;
    private View mHeadPicView = null;
    private ImageView headPicture = null;
    private TextView tvUmNickname = null;
    private TextView tvUmEmail = null;
    private TextView tvUmIntro = null;
    private View mNickNameView = null;
    private View mEmailView = null;
    private View mIntroductionView = null;

    private Bitmap head;// 头像Bitmap
    private static String path = "/sdcard/myHead/";// sd路径
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermsg);

        user = BmobUser.getCurrentUser(User.class);
        mHeadPicView = findViewById(R.id.lyt_um_head);
        mEmailView = findViewById(R.id.lyt_um_email);
        mNickNameView = findViewById(R.id.lyt_um_nikname);
        mIntroductionView = findViewById(R.id.lyt_um_intro);
        tvUmNickname = findViewById(R.id.tv_um_nikname);
        tvUmEmail = findViewById(R.id.tv_um_email);
        tvUmIntro = findViewById(R.id.tv_um_intro);
        headPicture = findViewById(R.id.iv_um_head);
        tvUmNickname.setText(user.getUsername());
        tvUmEmail.setText(user.getEmail());
        tvUmIntro.setText(user.getBriefIntro());
        initView();

        mHeadPicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTypeDialog();
            }
        });

        mNickNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UsermsgActivity.this, EditActivity.class);
                intent.putExtra("CHANGE_TYPE",1);
                startActivity(intent);
            }
        });

        mEmailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UsermsgActivity.this, EditActivity.class);
                intent.putExtra("CHANGE_TYPE",2);
                startActivity(intent);
            }
        });

        mIntroductionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UsermsgActivity.this, EditActivity.class);
                intent.putExtra("CHANGE_TYPE",3);
                startActivity(intent);
            }
        });


    }
    private void initView() {
        Bitmap bt = BitmapFactory.decodeFile(path + "head.jpg");// 从SD卡中找头像，转换成Bitmap
        headPicture.setImageBitmap(bt);
    }


    private void showTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this, R.layout.select_headpic_dialog, null);
        TextView tv_select_gallery = (TextView) view.findViewById(R.id.tv_select_gallery);
        tv_select_gallery.setOnClickListener(new View.OnClickListener() {// 在相册中选取
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, 1);
                dialog.dismiss();
            }
        });

        dialog.setView(view);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());// 裁剪图片
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    File temp = new File(Environment.getExternalStorageDirectory() + "/head.jpg");
                    cropPhoto(Uri.fromFile(temp));// 裁剪图片
                }
                break;
            case 3:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    head = Objects.requireNonNull(extras).getParcelable("data");
                    if (head != null) {

                        setPicToView(head);// 保存在SD卡中
                        headPicture.setImageBitmap(head);// 用ImageView显示出来
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 调用系统的裁剪功能
     *
     * @param uri 图片uri
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
// aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
// outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }
    private void setPicToView(Bitmap mBitmap) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return;
        }
        FileOutputStream b = null;
        File file = new File(path);
        file.mkdirs();// 创建文件夹
        String fileName = path + "head.jpg";// 图片名字
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
        UserService userService = new UserService();
        userService.uploadHeadPicture(this, fileName);
    }
}

package com.pic603.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.pic603.bean.User;

import java.io.File;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class UserService {

    public void signUp(final View view,String account,String password) {
        final User user = new User();
        if (account.contains("@")){
            user.setEmail(account);
        }else{
            user.setMobilePhoneNumber(account);
        }
        user.setPassword(password);
        user.setUsername("新用户"+System.currentTimeMillis());
        user.setFollowNum(0);
        user.setHeadPortrait("http://bmob-cdn-27533.bmobpay.com/2019/12/18/4416acb0ff67432ca9f8a05620b138ba.jpg");

        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "注册成功", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "注册失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public void loginByEmailPwd(final View view,String account,String password) {
        //TODO 此处替换为你的邮箱和密码
        BmobUser.loginByAccount(account,password, new LogInListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    downloadHeadPictureFile(user.getHeadPortrait());
                    Snackbar.make(view, "登陆成功", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "登陆失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
    public void loginByPhone(final View view,String account,String password){
        //TODO 此处替换为你的手机号码和密码
        BmobUser.loginByAccount(account, password, new LogInListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if(user!=null){
                    if (e == null) {
                        downloadHeadPictureFile(user.getHeadPortrait());
                        Snackbar.make(view, "登陆成功", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(view, "登陆失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void changeUserEmail(final View view, String email){
        final User user = BmobUser.getCurrentUser(User.class);
        user.setEmail(email);
        user.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "更新用户邮箱成功", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "更新用户邮箱失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    Log.e("error", e.getMessage());
                }
            }
        });
    }

    public void changeUserNickname(final View view, String nickname){
        final User user = BmobUser.getCurrentUser(User.class);
        user.setUsername(nickname);
        user.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "更新用户昵称成功", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "更新用户信息失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    Log.e("error", e.getMessage());
                }
            }
        });
    }

    public void changeUserIntroduction(final View view, String introduction){
        final User user = BmobUser.getCurrentUser(User.class);
        user.setBriefIntro(introduction);
        user.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "更新用户个人签名成功", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "更新用户个人签名失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    Log.e("error", e.getMessage());
                }
            }
        });
    }
    public void uploadHeadPicture(final Context context, String filePath){
        final BmobFile bmobFile = new BmobFile(new File(filePath));
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    //bmobFile.getFileUrl()--返回的上传文件的完整地址
                    Toast.makeText(context,"上传头像成功",Toast.LENGTH_SHORT).show();
                    User user = BmobUser.getCurrentUser(User.class);
                    user.setHeadPortrait(bmobFile.getFileUrl());
                    user.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                        }
                    });
                }else{
                    Toast.makeText(context,"上传头像失败" +e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onProgress(Integer value) {
                // 返回的上传进度（百分比）
            }
        });
    }
    private void downloadHeadPictureFile(String fileUrl){
        BmobFile file = new BmobFile("head.jpg","",fileUrl);
        //允许设置下载文件的存储路径，默认下载文件的目录为：context.getApplicationContext().getCacheDir()+"/bmob/"
        File saveFile = new File("/sdcard/myHead/", file.getFilename());
        file.download(saveFile, new DownloadFileListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void done(String savePath,BmobException e) {

            }

            @Override
            public void onProgress(Integer value, long newworkSpeed) {
                Log.i("bmob","下载进度："+value+","+newworkSpeed);
            }
        });
    }
    /**
     * 同步控制台数据到缓存中
     */
    public void fetchUserInfo() {
        BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    final User myUser = BmobUser.getCurrentUser(User.class);
                } else {
                    Log.e("error",e.getMessage());
                    System.out.println(e.toString());
                }
            }
        });
    }
}

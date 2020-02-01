package com.pic603.e_picacg;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.pic603.bean.Album;
import com.pic603.bean.Following;
import com.pic603.bean.User;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class OtherUserActivity extends AppCompatActivity {

    private List<Pic> picList = new ArrayList<>();
    private ListView lvPicList;
    private Boolean bFollowSwitch = false;
    private Button btfollow;
    private User user;
    private ImageView ivOuHead;
    private TextView tvOuName;
    private TextView tvOuIntro;
    private TextView followNum;
    private TextView fansNum;
    private List<Album> albums;//当前所有相册
    private ExecutorService singleThreadExecutor;
    private static final int ALBUM_COMPLETED = 1;
    private Handler handler = null;//通过handler刷新本地图片
    private PicAdapter picAdapter;
    private List<Bitmap> bitmaps;//相册缩略图
    private User currentUser = null;//当前用户
    private Following following;//关注表
    private Integer fansNumber;

    public static final String ALBUM_INFO = "com.pic603.e_picacg.albumInfo";
    public static final String BITMAP_INFO = "com.pic603.e_picacg.bitmapInfo";

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);
        user = (User) getIntent().getSerializableExtra(ImDetailActivity.USER_INFO);
        ivOuHead = findViewById(R.id.iv_ou_head);
        tvOuName = findViewById(R.id.tv_ou_name);
        tvOuIntro = findViewById(R.id.tv_ou_intro);
        followNum = findViewById(R.id.followNum);
        fansNum = findViewById(R.id.fansNum);
        albums = new ArrayList<>();
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        bitmaps = new ArrayList<>();
        countFansNumber();
        displayUserInfo();
        initData();
        picAdapter = new PicAdapter(OtherUserActivity.this,
                R.layout.list_item, picList);

        lvPicList = findViewById(R.id.lv_ou_list);
        lvPicList .setAdapter(picAdapter);//列表填充
        lvPicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(OtherUserActivity.this, ImDetailActivity.class);
                intent.putExtra(ALBUM_INFO,albums.get(position));
                startActivity(intent);
            }
        });

        btfollow = findViewById(R.id.bt_follow);
        updateFollowSwitch();
        btfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bFollowSwitch = !bFollowSwitch;
                if (bFollowSwitch) {
                    btfollow.setText("已关注");
                    follow(true);
                } else {
                    btfollow.setText("关注");
                    follow(false);
                }
            }
        });
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == ALBUM_COMPLETED){
                    Pic pic = new Pic();
                    pic.setTitle(albums.get(msg.arg1).getTitle());
                    pic.setAuthor(user.getUsername());
                    pic.setPic((Bitmap) msg.obj);
                    bitmaps.add((Bitmap) msg.obj);
                    picList.add(pic);
                    picAdapter.notifyDataSetInvalidated();
                }
            }
        };
    }

    private void initData(){
        BmobQuery<Album> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("uid",user.getObjectId());
        bmobQuery.findObjects(new FindListener<Album>() {
            @Override
            public void done(List<Album> categories, BmobException e) {
                if (e == null) {
                    albums.addAll(categories);
                    for (int i=0; i< albums.size(); i++) {
                        PictureLoader pictureLoader = new PictureLoader(albums.get(i).getThumbnailUrl(),ALBUM_COMPLETED,i);
                        singleThreadExecutor.execute(pictureLoader);
                    }
                } else {
                    Log.e("BMOB", e.toString());
                }
            }
        });
    }

    private void updateFollowSwitch(){
        if (BmobUser.isLogin()){
            currentUser = BmobUser.getCurrentUser(User.class);
        }else{
            Toast.makeText(this,"请先登录，再进行关注操作",Toast.LENGTH_SHORT).show();
            return;
        }
        BmobQuery<Following> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("concernId",currentUser.getObjectId());
        bmobQuery.addWhereEqualTo("followedId",user.getObjectId());
        bmobQuery.findObjects(new FindListener<Following>() {
            @Override
            public void done(List<Following> list, BmobException e) {
                if (e == null){
                    if (list.size() != 0){
                        bFollowSwitch = true;
                        btfollow.setText("已关注");
                    }
                }else {
                    System.out.println(e.toString());
                }
            }
        });
    }

    private void countFansNumber(){
        BmobQuery<Following> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("followedId",user.getObjectId());
        bmobQuery.findObjects(new FindListener<Following>() {
            @Override
            public void done(List<Following> list, BmobException e) {
                if (e == null){
                    if (list.size() == 0){
                        fansNumber = 0;
                    }else {
                        fansNumber = list.size();
                    }
                    fansNum.setText(String.valueOf(fansNumber));
                }else {
                    System.out.println(e.toString());
                }
            }
        });
    }

    private void displayUserInfo(){
        Glide.with(this).load(user.getHeadPortrait()).into(ivOuHead);
        tvOuName.setText(user.getUsername());
        tvOuIntro.setText(user.getBriefIntro());
        followNum.setText(String.valueOf(user.getFollowNum()));
    }

    private void follow(boolean isFollow){
        following = new Following();
        if (BmobUser.isLogin()){
            currentUser = BmobUser.getCurrentUser(User.class);
            if (currentUser.getObjectId().equalsIgnoreCase(user.getObjectId())){
                Toast.makeText(this,"不能自己关注自己哟",Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            Toast.makeText(this,"请先登录，再进行关注",Toast.LENGTH_SHORT).show();
            return;
        }
        int followNum = currentUser.getFollowNum();
        if (isFollow){
            currentUser.setFollowNum(followNum+1);
            following.setFollowedId(user.getObjectId());
            following.setConcernId(currentUser.getObjectId());
            following.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    if (e!=null){
                        System.out.println(e.toString());
                    }else {
                        User tempUser = new User();
                        tempUser.setFollowNum(currentUser.getFollowNum());
                        tempUser.update(currentUser.getObjectId(),new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e != null){
                                    System.out.println(e.toString());
                                }
                            }
                        });
                        fansNumber += 1;
                        fansNum.setText(String.valueOf(fansNumber));
                    }
                }
            });
        }else {
            currentUser.setFollowNum(followNum-1);
            BmobQuery<Following> bmobQuery = new BmobQuery<>();
            bmobQuery.addWhereEqualTo("concernId",currentUser.getObjectId());
            bmobQuery.addWhereEqualTo("followedId",user.getObjectId());
            bmobQuery.findObjects(new FindListener<Following>() {
                @Override
                public void done(List<Following> list, BmobException e) {
                    if (e == null){
                        Following following = list.get(0);
                        following.delete(following.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                User tempUser = new User();
                                tempUser.setFollowNum(currentUser.getFollowNum());
                                tempUser.update(currentUser.getObjectId(),new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e != null){
                                            System.out.println(e.toString());
                                        }
                                    }
                                });
                                fansNumber -= 1;
                                fansNum.setText(String.valueOf(fansNumber));
                            }
                        });
                    }else {
                        System.out.println(e.toString());
                    }
                }
            });
        }


    }

    class PictureLoader extends Thread {

        private String imageUrl;
        private int msgType;
        private int aid;//相册id

        public PictureLoader(String imageUrl, int type, int aid){
            this.imageUrl = imageUrl;
            msgType = type;
            if(type == ALBUM_COMPLETED){
                this.aid = aid;
            }
        }
        @Override
        public void run(){
            HttpURLConnection con = null;
            Bitmap bt = null;
            if(imageUrl != null){
                try {
                    URL url = new URL(imageUrl);
                    con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5 * 1000);
                    con.setReadTimeout(10 * 1000);
                    bt = BitmapFactory.decodeStream(con.getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
            Message msgMessage = Message.obtain();
            msgMessage.obj = bt;
            msgMessage.what = msgType;
            msgMessage.arg1 = aid;
            handler.sendMessage(msgMessage);
        }
    }
}

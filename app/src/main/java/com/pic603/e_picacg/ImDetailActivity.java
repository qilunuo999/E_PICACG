package com.pic603.e_picacg;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.pic603.Utils.DownloadPicture;
import com.pic603.bean.Album;
import com.pic603.bean.Image;
import com.pic603.bean.User;

import org.xmlpull.v1.XmlPullParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class ImDetailActivity extends AppCompatActivity {
    private Boolean bLikeSwitch = false;
    private ImageView ivLikeSwitch;
    private TextView tvAuthor;
    private TextView tvDtTitle;
    private Button btDtRead;
    private ImageView ivDtImage;
    private TextView tvDtTime;
    private TextView tvDtLikenumb;
    private ImageView ivDtHead;
    private TextView tvDtIntro;
    private Button btDtDownload;

    private Album album;
    private User albumUser;
    private ExecutorService singleThreadExecutor;

    public static final String ALBUM_INFO = "com.pic603.e_picacg.ALBUM_INFO";
    public static final String USER_INFO = "com.pic603.e_picacg.USER_INFO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_detail);

        album = (Album) getIntent().getSerializableExtra(MainActivity.ALBUM_INFO);

        tvDtTitle = findViewById(R.id.tv_dt_title);
        ivDtImage = findViewById(R.id.iv_dt_image);
        tvDtTime = findViewById(R.id.tv_dt_time);
        tvDtLikenumb = findViewById(R.id.tv_dt_likenumb);
        ivDtHead = findViewById(R.id.iv_dt_head);
        tvDtIntro = findViewById(R.id.tv_dt_intro);
        btDtDownload = findViewById(R.id.bt_dt_download);

        tvDtTitle.setText(album.getTitle());
        Glide.with(this).load(album.getThumbnailUrl()).into(ivDtImage);
        List<String> date = Arrays.asList(album.getCreatedAt().split(" "));
        tvDtTime.setText(date.get(0));
        tvDtLikenumb.setText(String.valueOf(album.getLikeNum()));
        tvDtIntro.setText(album.getContent());

        ivLikeSwitch = findViewById(R.id.iv_like_switch);
        ivLikeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bLikeSwitch = !bLikeSwitch;
                if (bLikeSwitch) {
                    ivLikeSwitch.setImageResource(R.drawable.ic_like);
                    like(true);
                    tvDtLikenumb.setText(String.valueOf(album.getLikeNum()));
                } else {
                    ivLikeSwitch.setImageResource(R.drawable.ic_unlike);
                    like(false);
                    tvDtLikenumb.setText(String.valueOf(album.getLikeNum()));
                }
            }
        });

        tvAuthor = findViewById(R.id.tv_dt_author);
        tvAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImDetailActivity.this, OtherUserActivity.class);
                intent.putExtra(USER_INFO,albumUser);
                startActivity(intent);
            }
        });

        btDtRead = findViewById(R.id.bt_dt_read);
        btDtRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImDetailActivity.this, ScrollViewActivity.class);
                intent.putExtra(ALBUM_INFO,album);
                startActivity(intent);
            }
        });

        btDtDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadList();
            }
        });
        query();
    }
    /**
     * 查询一个对象
     */
    private void query() {
        BmobQuery<User> bmobQuery = new BmobQuery<>();
        bmobQuery.getObject(album.getUid(), new QueryListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    albumUser = user;
                    tvAuthor.setText(user.getUsername());
                    Glide.with(ImDetailActivity.this).load(user.getHeadPortrait()).into(ivDtHead);
                } else {
                    System.out.println(e.toString());
                }
            }
        });
    }
    /**
     * 点赞/取消点赞
     */
    private void  like(boolean isLike){
        int likesNum = album.getLikeNum();
        if(isLike){
            album.setLikeNum(likesNum+1);
        }else{
            album.setLikeNum(likesNum-1);
        }
        album.update(album.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e != null){
                    System.out.println(e.toString());
                }
            }
        });
    }

    /**
     * 批量下载
     */
    private void downloadList(){
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        BmobQuery<Image> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("aid",album.getObjectId());
        bmobQuery.findObjects(new FindListener<Image>() {
            @Override
            public void done(List<Image> list, BmobException e) {
                for(int i = 0; i < list.size(); i++){
                    Image image = list.get(i);
                    DownloadPicture downloadPicture = new DownloadPicture(image.getImage(),i+1);
                    singleThreadExecutor.execute(downloadPicture);
                }
                Toast.makeText(ImDetailActivity.this,"开始下载，下载会在后台进行，您可以到我的下载去查看下载结果",Toast.LENGTH_LONG).show();
            }
        });
    }
}

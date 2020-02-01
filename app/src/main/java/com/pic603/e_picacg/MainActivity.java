package com.pic603.e_picacg;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pic603.bean.Album;
import com.pic603.bean.User;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Pic> picList = new ArrayList<>();
    private ImageView headpic;
    private ListView imList;
    private User user = null;
    private TextView tvUsername = null;
    private TextView tvIntroduction = null;
    private Handler handler = null;
    PicAdapter picAdapter;
    private List<Album> albums;//相册集合
    private ExecutorService singleThreadExecutor;//用于加载相册
    private String createdAt;//相册创建时间
    private List<Bitmap> bitmaps;//相册缩略图
    private SwipeRefreshLayout swipe;

    private static final int COMPLETED = 0;
    private static final int ALBUM_COMPLETED = 1;
    private final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final String ALBUM_INFO = "com.pic603.e_picacg.albumInfo";
    public static final String BITMAP_INFO = "com.pic603.e_picacg.bitmapInfo";
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA
    };

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isNetworkConnected(this)){
            Toast toast=Toast.makeText(this,
                    "请检查您的网络连接是否正常，无网络连接将无法正常使用本应用", Toast.LENGTH_SHORT);
            toast.show();
        }
        Bmob.initialize(this, "550e45e0e45fc862829b77664397addf");
        albums = new ArrayList<>();
        bitmaps = new ArrayList<>();
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        createdAt = sdf.format(new Date());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //绑定控件
        View headerView = navigationView.getHeaderView(0);
        headpic = headerView.findViewById(R.id.iv_headpic);//点击侧滑抽屉用户头像跳转
        tvUsername = headerView.findViewById(R.id.tv_username);
        tvIntroduction = headerView.findViewById(R.id.tv_introduction);
        if(BmobUser.isLogin()){
            user = BmobUser.getCurrentUser(User.class);
            tvUsername.setText(user.getUsername());
            tvIntroduction.setText(user.getBriefIntro());
            // sd路径
            String path = "/sdcard/myHead/";
            Bitmap bt = BitmapFactory.decodeFile(path + "head.jpg");
            if (bt != null) {
                headpic.setImageBitmap(bt);
            }

        }
        headpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if(!BmobUser.isLogin()){
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                }else {
                    intent = new Intent(MainActivity.this, UsermsgActivity.class);
                }
                startActivity(intent);
            }
        });

        mayRequestContacts();

        picAdapter = new PicAdapter(MainActivity.this,
                R.layout.list_item, picList);

        ListView lvPicList = findViewById(R.id.lv_pic_list);
        lvPicList .setAdapter(picAdapter);

        lvPicList.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent intent = new Intent(MainActivity.this, ImDetailActivity.class);
                intent.putExtra(ALBUM_INFO,albums.get(position));
                startActivity(intent);
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == COMPLETED) {
                    headpic.setImageBitmap((Bitmap) msg.obj);
                }
                if(msg.what == ALBUM_COMPLETED){
                    Pic pic = new Pic();
                    pic.setTitle(albums.get(msg.arg1).getTitle());
                    pic.setAuthor("创建时间\n"+albums.get(msg.arg1).getCreatedAt());
                    pic.setPic((Bitmap) msg.obj);
                    bitmaps.add((Bitmap) msg.obj);
                    picList.add(pic);
                    picAdapter.notifyDataSetChanged();
                }
            }
        };
        swipe = findViewById(R.id.swipe);//下拉刷新列表
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }
    private void mayRequestContacts(){
        for (String i:PERMISSIONS_STORAGE) {
            if (checkSelfPermission(i) == PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(i)) {
                    // TODO: alert the user with a Snackbar/AlertDialog giving them the permission rationale
                    // To use the Snackbar from the design support library, ensure that the activity extends
                    // AppCompatActivity and uses the Theme.AppCompat theme.
                } else {
                    requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
            } else {
                initData();
            }
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initData();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //刷新数据
        if(BmobUser.isLogin()){
            user = BmobUser.getCurrentUser(User.class);
            tvUsername.setText(user.getUsername());
            tvIntroduction.setText(user.getBriefIntro());
            HeadPictureLoader headPictureLoader = new HeadPictureLoader();
            headPictureLoader.start();
        }
    }


    private void initData(){
        BmobQuery<Album> bmobQuery = new BmobQuery<>();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date createdAtDate = null;
        try {
            createdAtDate = sdf.parse(createdAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        BmobDate bmobCreatedAtDate = new BmobDate(createdAtDate);
        bmobQuery.addWhereLessThan("createdAt", bmobCreatedAtDate);
        bmobQuery.setLimit(10);
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(new FindListener<Album>() {
            @Override
            public void done(List<Album> categories, BmobException e) {
                if (e == null) {
                    albums.clear();
                    albums.addAll(categories);
                    createdAt = sdf.format(new Date());

                    for (int i=0; i< albums.size(); i++) {
                        PictureLoader pictureLoader = new PictureLoader(albums.get(i).getThumbnailUrl(),ALBUM_COMPLETED,i);
                        singleThreadExecutor.execute(pictureLoader);
                    }
                    swipe.setRefreshing(false);
                } else {
                    System.out.println(e.toString());
                }
            }
        });
    }

    private void refreshData(){
        picList.clear();
        initData();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_download) {
            Intent intent = new Intent(MainActivity.this, MyDownload.class);
            startActivity(intent);

        } else if (id == R.id.nav_favorite) {
            Intent intent = new Intent(MainActivity.this, MyFollowActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_upload) {
            if(BmobUser.isLogin()){
                Intent intent = new Intent(MainActivity.this, ImUploadActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(this,"请先登陆再进行该项操作",Toast.LENGTH_SHORT).show();
            }
        }  else if (id == R.id.nav_location) {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_exit) {
            tvUsername.setText("用户名");
            tvIntroduction.setText("个性签名");
            headpic.setImageResource(R.mipmap.ic_launcher_round);
            deleteHeadPicture();
            Toast.makeText(this,"当前账号已退出",Toast.LENGTH_SHORT).show();
            BmobUser.logOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    void deleteHeadPicture(){
        File file = new File("/sdcard/myHead/","head.jpg");
        if(file.exists()){
            if(file.isFile()){
                file.delete();
            }
        }
    }

    class HeadPictureLoader extends Thread {

        @Override
        public void run(){
            HttpURLConnection con = null;
            Bitmap bt = null;
            String headPicUrl;
            if (user.getHeadPortrait() != null){
                headPicUrl = user.getHeadPortrait();
            }else{
                headPicUrl = "http://bmob-cdn-27533.bmobpay.com/2019/12/18/4416acb0ff67432ca9f8a05620b138ba.jpg";
            }
            try {
                URL url = new URL(headPicUrl);
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
                Message msgMessage = Message.obtain();
                msgMessage.obj = bt;
                msgMessage.what = COMPLETED;
                handler.sendMessage(msgMessage);
            }
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
            if(imageUrl != null && !imageUrl.trim().isEmpty()){
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
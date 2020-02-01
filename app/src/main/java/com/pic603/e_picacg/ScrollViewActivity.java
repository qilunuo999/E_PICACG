package com.pic603.e_picacg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;

import com.pic603.Utils.DisplayBigPicture;
import com.pic603.bean.Album;
import com.pic603.bean.Image;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ScrollViewActivity extends AppCompatActivity{
    private GridView grid_test;
    private List<Map<String, Object>> dataList;
    private SimpleAdapter simpAdapter;
    private List<Image> imageList;
    private Map<String,Object> data;//单张图片
    private Handler handler = null;//通过handler刷新本地图片
    private static final int DATA_COMPLETED = 0;
    private ExecutorService singleThreadExecutor;
    private Album album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view);
        album = (Album) getIntent().getSerializableExtra(ImDetailActivity.ALBUM_INFO);
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        grid_test = (GridView) findViewById(R.id.gv_album_detail); // 测试图片填充step1
        dataList = new ArrayList<Map<String, Object>>(); // step2
        imageList = new ArrayList<>();
        getData();

        simpAdapter = new SimpleAdapter(ScrollViewActivity.this, dataList, R.layout.grid_item,
                new String[]{"img", "txt"}, new int[]{R.id.img_item, R.id.txt_item});
        simpAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if( (view instanceof ImageView) & (data instanceof Bitmap) ) {
                    ImageView iv = (ImageView) view;
                    Bitmap bm = (Bitmap) data;
                    iv.setImageBitmap(bm);
                    return true;
                }
                return false;
            }
        });
        grid_test.setAdapter(simpAdapter); // step3

        //图片点击事件
        grid_test.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                DisplayBigPicture displayBigPicture = new DisplayBigPicture();
                displayBigPicture.init(ScrollViewActivity.this, imageList.get(position));
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == DATA_COMPLETED) {
                    simpAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    private void getData() {//图片填充
        //从服务器获取
        BmobQuery<Image> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("aid",album.getObjectId());
        bmobQuery.findObjects(new FindListener<Image>() {
            @Override
            public void done(List<Image> list, BmobException e) {
                if(e == null){
                    imageList.addAll(list);
                    int j = 1;
                    for(Image i:list) {
                        ////////
                        ImageLoader imageLoader = new ImageLoader(i.getThumbnailUrl(),DATA_COMPLETED,j++);
                        singleThreadExecutor.execute(imageLoader);
                    }
                }else{
                    System.out.println(e.toString());
                }
            }
        });
    }

    class ImageLoader extends Thread {

        private String imageUrl;
        private int msgType;
        private int id;

        public ImageLoader(String imageUrl, int type, int id){
            this.imageUrl = imageUrl;
            msgType = type;
            this.id = id;
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
                data = new HashMap<>();
                data.put("img",bt);
                data.put("txt",id);
                dataList.add(data);
            }
            Message msgMessage = Message.obtain();
            msgMessage.what = msgType;
            handler.sendMessage(msgMessage);
        }
    }
}

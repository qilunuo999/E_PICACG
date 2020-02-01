package com.pic603.e_picacg;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.pic603.Utils.DisplayBigPicture;
import com.pic603.bean.Album;
import com.pic603.bean.Image;
import com.pic603.bean.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadBatchListener;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import me.iwf.photopicker.PhotoPicker;

public class ImUploadActivity extends AppCompatActivity {
    private AlertDialog.Builder builder;//新增图库对话框
    private ListView lvPicList;
    private PicAdapter picAdapter;
    private GridView grid_test;
    private int isSwitch = -1;//是否选择了相册
    private Map<String,Object> data;//单张图片
    private List<Map<String, Object>> dataList;//图片数据
    private SimpleAdapter simpAdapter;
    private List<Album> albums;//当前所有相册
    private User user;
    private Handler handler = null;//通过handler刷新本地图片
    private List<Pic> picList = new ArrayList<>();//相冊集合
    private List<Image> imageList;
    private ExecutorService singleThreadExecutor;

    private static final int DATA_COMPLETED = 0;
    private static final int ALBUM_COMPLETED = 1;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_upload);
        user = BmobUser.getCurrentUser(User.class);
        albums = new ArrayList<>();
        imageList = new ArrayList<>();
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        picAdapter = new PicAdapter(ImUploadActivity.this,
                R.layout.list_item, picList);
        initData();

        lvPicList = findViewById(R.id.lv_upload_list);
        lvPicList .setAdapter(picAdapter);//测试列表填充
        grid_test = findViewById(R.id.gv_upload); // 测试图片填充step1
        dataList = new ArrayList<>(); // step2

        lvPicList.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                if (id != isSwitch) {
                    picAdapter.setSelectedPosition(position);
                    picAdapter.notifyDataSetInvalidated();
                    isSwitch = position;
                    getData(position);
                }
            }
        });

        simpAdapter = new SimpleAdapter(ImUploadActivity.this, dataList, R.layout.grid_item,
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
                displayBigPicture.init(ImUploadActivity.this,imageList.get(position));
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == DATA_COMPLETED) {
                    simpAdapter.notifyDataSetChanged();
                }
                if(msg.what == ALBUM_COMPLETED){
                    Pic pic = new Pic();
                    pic.setTitle(albums.get(msg.arg1).getTitle());
                    pic.setAuthor(user.getUsername());
                    pic.setPic((Bitmap) msg.obj);
                    picList.add(pic);
                    picAdapter.notifyDataSetInvalidated();
                }
            }
        };
    }

    private void getData(int position) {//图片填充
        clearImageData();
        /////改为从服务器获取
        String albumId = albums.get(position).getObjectId();
        BmobQuery<Image> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("aid",albumId);
        bmobQuery.findObjects(new FindListener<Image>() {
            @Override
            public void done(List<Image> list, BmobException e) {
                imageList.addAll(list);
                if(e == null){
                    int j = 1;
                    for(Image i:list) {
                        ////////
                        ImageLoader imageLoader = new ImageLoader(i.getThumbnailUrl(),DATA_COMPLETED,j++);
                        singleThreadExecutor.execute(imageLoader);
                    }
                }else{
                    Log.e("图片获取错误","图片列表为空。");
                }
            }
        });
    }

    private List<Map<String, Object>> clearImageData(){
        dataList.clear();
        return dataList;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_uploadIm:
                if(isSwitch == -1){
                    Toast.makeText(this,"您还没有选择相册，请先选择相册后再操作。",Toast.LENGTH_LONG).show();
                    return false;
                }
                PhotoPicker.builder()
                        .setPhotoCount(9) //图片最大选择数量
                        .setShowCamera(true)
                        .setShowGif(true)
                        .setPreviewEnabled(false)
                        .start(ImUploadActivity.this, PhotoPicker.REQUEST_CODE);

                Toast.makeText(this, "上传图片完成", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_addAlbum:
                showMyStyle();
                Toast.makeText(this, "新增图库成功", Toast.LENGTH_SHORT).show();
                break;
            default: break;
        }
        return true;
    }
    //相册选择回调函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // get selected images from selector
        if (resultCode == RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            ArrayList<String> photos =
                    data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            final String[] imageUrlList = new String[photos.size()];
            int j = 0;
            for (String i:photos){
                imageUrlList[j++] = i;
            }

            BmobFile.uploadBatch(imageUrlList, new UploadBatchListener() {
                @Override
                public void onSuccess(List<BmobFile> files,List<String> urls) {
                    //1、files-上传完成后的BmobFile集合，是为了方便大家对其上传后的数据进行操作，例如你可以将该文件保存到表中
                    //2、urls-上传文件的完整url地址
                    if(files.size() == imageUrlList.length){
                        ListIterator<BmobFile> it1 = files.listIterator();
                        ListIterator<String> it2 = urls.listIterator();
                        List<BmobObject> imageList = new ArrayList<>();
                        boolean isUploadThumbnail = false;
                        while (it1.hasNext() && it2.hasNext()){
                            BmobFile file = it1.next();
                            String url = it2.next();
                            if (!isUploadThumbnail){//先上传相册缩略图
                                Album album = albums.get(isSwitch);
                                if (album.getThumbnailUrl() == null || album.getThumbnailUrl().isEmpty()){
                                    album.setThumbnailUrl(url);
                                    album.update(album.getObjectId(),new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e != null) {
                                                Log.e("BMOB", e.toString());
                                            }
                                        }
                                    });
                                    Pic pic = picList.get(isSwitch);
                                    try {
                                        pic.setPic(BitmapFactory.decodeStream(new FileInputStream(file.getLocalFile())));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    picList.set(isSwitch,pic);
                                    picAdapter.notifyDataSetInvalidated();
                                }
                                isUploadThumbnail = true;
                            }
                            Image image = new Image();
                            image.setAid(albums.get(isSwitch).getObjectId());
                            image.setUid(user.getObjectId());
                            image.setImage(url);
                            image.setImageName(file.getFilename());
                            image.setThumbnailUrl(url);
                            imageList.add(image);
                        }
                        new BmobBatch().insertBatch(imageList).doBatch(new QueryListListener<BatchResult>() {

                            @Override
                            public void done(List<BatchResult> results, BmobException e) {
                                if (e == null) {
                                    for (int i = 0; i < results.size(); i++) {
                                        BatchResult result = results.get(i);
                                        BmobException ex = result.getError();
                                        if (ex != null){
                                            Log.e("第" + i + "个数据批量添加失败：",ex.getMessage() + "," + ex.getErrorCode());
                                        }
                                        if (i == results.size()-1){
                                            getData(isSwitch);
                                        }
                                    }
                                } else {
                                    Log.e("失败：",e.getMessage() + "," + e.getErrorCode());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(int statuscode, String errormsg) {
                    System.out.println("错误码"+statuscode +",错误描述："+errormsg);
                }

                @Override
                public void onProgress(int curIndex, int curPercent, int total,int totalPercent) {
                    //1、curIndex--表示当前第几个文件正在上传
                    //2、curPercent--表示当前上传文件的进度值（百分比）
                    //3、total--表示总的上传文件数
                    //4、totalPercent--表示总的上传进度（百分比）
                    Toast.makeText(ImUploadActivity.this,"当前已上传"+totalPercent+"%", Toast.LENGTH_LONG).show();
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 新增图库的 dialog
     */
    private void showMyStyle() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_album, null);
        final EditText albumName = view.findViewById(R.id.et_album_name);
        final EditText albumIntro = view.findViewById(R.id.et_album_intro);

        builder = new AlertDialog.Builder(this).setView(view).setTitle("新增图库").setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final Album album = new Album();
                        album.setUid(user.getObjectId());
                        album.setLikeNum(0);
                        album.setShareNum(0);
                        album.setContent(albumIntro.getText().toString());
                        album.setTitle(albumName.getText().toString());
                        album.save(new SaveListener<String>() {
                            @Override
                            public void done(String objectId, BmobException e) {
                                if(e == null){
                                    album.setObjectId(objectId);
                                    albums.add(album);
                                }else{
                                    Log.e("BMOB ERROR",e.toString());
                                }
                            }
                        });
                        Pic pic = new Pic();
                        pic.setTitle(albumName.getText().toString());
                        pic.setAuthor(user.getUsername());
                        picList.add(pic);
                        picAdapter.notifyDataSetInvalidated();
                        Toast.makeText(ImUploadActivity.this,"新建图库成功"
                                , Toast.LENGTH_LONG).show();
                    }
                });

        builder.create().show();
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
                data = new HashMap<>();//用于保存图片数据
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

package com.pic603.e_picacg;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.pic603.Utils.DisplayBigPicture;
import com.pic603.bean.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyDownload extends AppCompatActivity {
    private GridView grid_test;
    private List<Map<String, Object>> dataList;
    private SimpleAdapter simpAdapter;
    private File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydownload);
        getFiles();
        grid_test = (GridView) findViewById(R.id.gv_download); // 测试图片填充step1
        dataList = new ArrayList<Map<String, Object>>(); // step2
        simpAdapter = new SimpleAdapter(this, getData(), R.layout.grid_item,
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
                displayBigPicture.init(MyDownload.this, files[position]);
            }
        });
    }

    private List<Map<String, Object>> getData() {       //测试图片填充
        int j = 1;
        for (File i:files) {
            Bitmap bitmap = BitmapFactory.decodeFile(i.getPath());
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("img", bitmap);
            map.put("txt", j++);
            dataList.add(map);
        }
        return dataList;
    }

    private void getFiles(){
        File file = new File("/sdcard/myFolder");
        files = file.listFiles();
    }
}

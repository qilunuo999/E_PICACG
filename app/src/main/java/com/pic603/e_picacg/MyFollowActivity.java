package com.pic603.e_picacg;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.pic603.bean.Following;
import com.pic603.bean.User;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;

public class MyFollowActivity extends TabActivity {

    private ListView lvUserList;
    private ListView lvFansList;
    private List<User> followUserList = new ArrayList<>();
    private List<User> fansUserList = new ArrayList<>();
    private User user;
    private UserAdapter followAdapter;
    private UserAdapter fansAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_follow);

        final TabHost tabHost = this.getTabHost();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("tab1").setIndicator("我的关注")
                .setContent(R.id.tab1);

        tabHost.addTab(tab1);

        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2").setIndicator("我的粉丝")
                .setContent(R.id.tab2);

        tabHost.addTab(tab2);

        initFollowData();
        initFansData();
        followAdapter = new UserAdapter(MyFollowActivity.this,
                R.layout.user_list, followUserList);
        fansAdapter = new UserAdapter(MyFollowActivity.this,
                R.layout.user_list, fansUserList);

        lvUserList = findViewById(R.id.lv_follow);
        lvUserList .setAdapter(followAdapter);//测试列表填充

        lvFansList = findViewById(R.id.lv_fans);
        lvFansList.setAdapter(fansAdapter);
    }

    private void initFollowData(){
        if(BmobUser.isLogin()){
            user = BmobUser.getCurrentUser(User.class);
        }else{
            Toast.makeText(this,"请先登陆，再进行查看",Toast.LENGTH_SHORT).show();
            return;
        }

        BmobQuery<Following> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("concernId",user.getObjectId());
        bmobQuery.findObjects(new FindListener<Following>() {
            @Override
            public void done(List<Following> list, BmobException e) {
                if (e == null){
                    for(Following i:list){
                        BmobQuery<User> bmobQuery = new BmobQuery<>();
                        bmobQuery.getObject(i.getFollowedId(), new QueryListener<User>() {
                            @Override
                            public void done(User user, BmobException e) {
                                followUserList.add(user);
                                followAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }else {
                    System.out.println(e.toString());
                }
            }
        });
    }
    private void initFansData(){
        if(BmobUser.isLogin()){
            user = BmobUser.getCurrentUser(User.class);
        }else{
            Toast.makeText(this,"请先登陆，再进行查看",Toast.LENGTH_SHORT).show();
            return;
        }

        BmobQuery<Following> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("followedId",user.getObjectId());
        bmobQuery.findObjects(new FindListener<Following>() {
            @Override
            public void done(List<Following> list, BmobException e) {
                if (e == null){
                    for(Following i:list){
                        BmobQuery<User> bmobQuery = new BmobQuery<>();
                        bmobQuery.getObject(i.getConcernId(), new QueryListener<User>() {
                            @Override
                            public void done(User user, BmobException e) {
                                fansUserList.add(user);
                                fansAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }else {
                    System.out.println(e.toString());
                }
            }
        });
    }
}

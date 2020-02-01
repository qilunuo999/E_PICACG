package com.pic603.e_picacg;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pic603.bean.User;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {

    private List<User> mUserData;
    private Context mContext;
    private int resourceId;
    private static final String USER_INFO = "com.pic603.e_picacg.USER_INFO";

    public UserAdapter(@NonNull Context context, int resourceId, @NonNull List<User> data) {
        super(context, resourceId, data);
        this.mContext = context;
        this.mUserData = data;
        this.resourceId = resourceId;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final User user = getItem(position);
        View view;

        UserAdapter.ViewHolder viewHolder;


        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

            viewHolder = new UserAdapter.ViewHolder();

            viewHolder.tvName = view.findViewById(R.id.tv_list_name);
            viewHolder.ivImage = view.findViewById(R.id.iv_list_head);

            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (UserAdapter.ViewHolder) view.getTag();
        }

        viewHolder.tvName.setText(user.getUsername());
        Glide.with(view).load(user.getHeadPortrait()).into(viewHolder.ivImage);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, OtherUserActivity.class);
                intent.putExtra(USER_INFO,user);
                mContext.startActivity(intent);
            }
        });

        return view;
    }

    class ViewHolder{
        TextView tvName;
        ImageView ivImage;
    }
}

package com.pic603.e_picacg;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PicAdapter extends ArrayAdapter<Pic> {
    private List<Pic> mPicData;
    private Context mContext;
    private int resourceId;
    private int selectedPosition = -1;// 选中的位置

    public PicAdapter(@NonNull Context context, int resourceId, @NonNull List<Pic> data) {
        super(context, resourceId, data);
        this.mContext = context;
        this.mPicData = data;
        this.resourceId = resourceId;

    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Pic pic = getItem(position);
        View view;

        ViewHolder viewHolder;

        //view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

            viewHolder = new ViewHolder();

            viewHolder.tvTitle = view.findViewById(R.id.tv_title);
            viewHolder.tvAuthor = view.findViewById(R.id.tv_author);
            viewHolder.ivImage = view.findViewById(R.id.iv_image);
            viewHolder.imCheck = view.findViewById(R.id.im_check);

            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if (selectedPosition == position){
            viewHolder.imCheck.setVisibility(View.VISIBLE);
        }else {
            viewHolder.imCheck.setVisibility(View.INVISIBLE);
        }

        viewHolder.tvTitle.setText(pic.getTitle());
        viewHolder.tvAuthor.setText(pic.getAuthor());
        viewHolder.ivImage.setImageBitmap(pic.getPic());

        return view;
    }

    class ViewHolder{
        TextView tvTitle;
        TextView tvAuthor;
        ImageView ivImage;
        ImageView imCheck;
    }
}

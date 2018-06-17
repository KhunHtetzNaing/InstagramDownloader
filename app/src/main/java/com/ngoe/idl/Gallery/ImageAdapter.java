package com.ngoe.idl.Gallery;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ngoe.idl.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder>  {
    @Override
    public ImageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View photoView = inflater.inflate(R.layout.gallery_images_relative_layout, parent, false);
        ImageAdapter.MyViewHolder viewHolder = new ImageAdapter.MyViewHolder(photoView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ImageAdapter.MyViewHolder holder, int position) {
        ImageView imageView = holder.mPhotoImageView;
        Glide.with(mContext)
                .load(mPhotos.get(position))
                .placeholder(R.drawable.empty_image)
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mPhotoImageView;
        public MyViewHolder(View itemView) {
            super(itemView);
            mPhotoImageView = (ImageView) itemView.findViewById(R.id.iv_photo);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(mContext, ShowActivity.class);
                intent.putExtra("data",mPhotos.get(position));
                mContext.startActivity(intent);
            }
        }
    }

    private ArrayList<String> mPhotos;
    private Context mContext;

    public ImageAdapter(Context context, ArrayList<String> photos) {
        mContext = context;
        mPhotos = photos;
    }
}
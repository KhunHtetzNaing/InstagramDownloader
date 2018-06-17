package com.ngoe.idl;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyListAdapter extends BaseAdapter {
    ArrayList<String> fileName;
    ArrayList<String> fileDate;
    ArrayList<Bitmap> icon;
    Context context;

    public MyListAdapter(ArrayList<String> fileName, ArrayList<String> fileDate, ArrayList<Bitmap> icon, Context context) {
        this.fileName = fileName;
        this.fileDate = fileDate;
        this.icon = icon;
        this.context = context;
    }

    @Override
    public int getCount() {
        return fileName.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.list_item,null);
        TextView title = view.findViewById(R.id.tvFileName);
        TextView date = view.findViewById(R.id.tvFileCreatedDate);
        ImageView image = view.findViewById(R.id.ivThumb);
        title.setText(fileName.get(i));
        date.setText(fileDate.get(i));
        image.setImageBitmap(icon.get(i));
        return view;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
}

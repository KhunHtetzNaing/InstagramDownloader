package com.ngoe.idl.Gallery;


import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ngoe.idl.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 */
public class Photos_Fragment extends Fragment {
    RecyclerView recyclerView;
    TextView textView;

    public Photos_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayList<String> mPhotos = new ArrayList<>();
        File file = new File(Environment.getExternalStorageDirectory()+"/InstagramDownload/");
        File [] files = file.listFiles();
        try {
            Arrays.sort(files, new Comparator() {
                public int compare(Object o1, Object o2) {
                    if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                        return -1;
                    } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                        return +1;
                    } else {
                        return 0;
                    }
                }
            });
        }catch (Exception e){

        }

        for (int i = 0; i < files.length; i++) {
            String photo = files[i].toString();
            if (!photo.endsWith(".mp4")){
                mPhotos.add(files[i].toString());
            }
        }

        if (mPhotos.size()<=0){
            textView.setVisibility(View.VISIBLE);
        }else{
            textView.setVisibility(View.GONE);
        }
        ImageAdapter adapter = new ImageAdapter(getContext(), mPhotos);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        textView = view.findViewById(R.id.tv_no_data);
        return view ;
    }
}

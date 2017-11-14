package net.sunniwell.photowall;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by admin on 17/11/14.
 */

public class PhotoWallAdaptor extends ArrayAdapter<String> implements AbsListView.OnScrollListener {
    private static final String TAG = "jpd-PWApt";
    private Context context;
    private int resourceId;
    private String[] urlList;
    private GridView mPhotoWall;


    public PhotoWallAdaptor(@NonNull Context context, @LayoutRes int resource, @NonNull String[] objects,
                            GridView photoWall) {
        super(context, resource, objects);
        this.context = context;
        this.resourceId = resource;
        this.urlList = objects;
        this.mPhotoWall = photoWall;
        this.mPhotoWall.setOnScrollListener(this);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(resourceId, parent, false);
        } else {
            view = convertView;
        }
        ImageView image = (ImageView)view.findViewById(R.id.grid_image);
        return view;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return urlList[position];
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        Log.d(TAG, "onScrollStateChanged: i:" + i);
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        Log.d(TAG, "onScroll: ");
    }
}

package net.sunniwell.photowall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by admin on 17/11/14.
 */

public class PhotoWallAdaptor extends ArrayAdapter<String> implements AbsListView.OnScrollListener {
    private static final String TAG = "jpd-PWApt";
    private Context context;
    private int resourceId;
    private String[] urlList;
    private GridView mPhotoWall;
    private boolean isFirstLoad = true;
    private LruCache<String, Bitmap> mLruCache;
    private Set<DownloadTask> tastSet;
    private int firstVisibleItem;
    private int visibleCount;

    public PhotoWallAdaptor(@NonNull Context context, @LayoutRes int resource, @NonNull String[] objects,
                            GridView photoWall) {
        super(context, resource, objects);
        this.context = context;
        this.resourceId = resource;
        this.urlList = objects;
        this.mPhotoWall = photoWall;
        this.mPhotoWall.setOnScrollListener(this);
        int maxMem = (int)Runtime.getRuntime().maxMemory();
        Log.d(TAG, "PhotoWallAdaptor: maxMem:" + maxMem);
        mLruCache = new LruCache<String, Bitmap>(maxMem / 8) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                Log.d(TAG, "sizeOf: byteCount:" + value.getByteCount());
                return value.getByteCount();
            }
        };
        tastSet = new HashSet<>();
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
        String url = getItem(position);
        ImageView image = (ImageView)view.findViewById(R.id.grid_image);
        image.setTag(url);
        setImageView(url, image);
        return view;
    }

    private void setImageView(String url, ImageView image) {
        Bitmap bitmap = getBitmapFromCache(url);
        if (bitmap == null) {
            image.setImageResource(R.drawable.empty_photo);
        } else {
            image.setImageBitmap(bitmap);
        }
    }

    private Bitmap getBitmapFromCache(String url) {
        return mLruCache.get(url);
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return urlList[position];
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        Log.d(TAG, "onScrollStateChanged: i:" + i);
        Log.d(TAG, "onScrollStateChanged: i:" + i);
        if (i == SCROLL_STATE_FLING || i == SCROLL_STATE_TOUCH_SCROLL) {
            cancelAllTask();
        }
        if (i == SCROLL_STATE_IDLE) {
            startTask(firstVisibleItem, visibleCount);
        }
    }

    private void startTask(int firstItem, int itemCount) {
        for (int j = firstItem; j < firstItem + itemCount; j++) {
            String picUrl = getItem(j);
            DownloadTask task = new DownloadTask();
            task.execute(picUrl);
            tastSet.add(task);
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        Log.d(TAG, "onScroll: i:" + i + ",i1:" + i1 + ",i2:" + i2);
        firstVisibleItem = i;
        visibleCount = i1;
        if (isFirstLoad && i1 > 0) {
            isFirstLoad = false;
            startTask(i, i1);
        }
    }

    public void cancelAllTask() {
        Iterator it = tastSet.iterator();
        while (it.hasNext()) {
            DownloadTask dt = (DownloadTask)it.next();
            if (dt != null) {
                dt.cancel(true);
                dt = null;
            }
        }
    }

    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (mLruCache.get(url) == null) {
            mLruCache.put(url, bitmap);
        }
    }
    
    public class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        public String url;
        @Override
        protected Bitmap doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: string:" + strings[0] + ",thread:" + Thread.currentThread().getId());
            if (isCancelled()) {
                Log.d(TAG, "doInBackground: cancel....");
                return null;
            }
            url = strings[0];
            Bitmap bitmap = downloadPicFromUrl(url);
            if (bitmap != null) {
                addBitmapToCache(url, bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Log.d(TAG, "onPostExecute: ");
            ImageView image = (ImageView)mPhotoWall.findViewWithTag(url);
            if (image != null && bitmap != null) {
                image.setImageBitmap(bitmap);
            }
            tastSet.remove(this);
        }
    }

    private Bitmap downloadPicFromUrl(String picUrl) {
        HttpURLConnection conn = null;
        Bitmap bitmap = null;
        try {
            URL url = new URL(picUrl);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true);
            Log.d(TAG, "downloadPicFromUrl: before getInputStream");
            bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            Log.d(TAG, "downloadPicFromUrl: after getInputStream");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return bitmap;
    }
}

package lnt.in.geospatial;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by manoj on 21-Jun-17.
 */

public class GeoTagImagesAdapter extends RecyclerView.Adapter<GeoTagImagesAdapter.ViewHolder>{

    private ArrayList<String> mDataset;
    private Context ctx;
    private GeoTagPhotoSelectionListener geoTagPhotoSelectionListener = null;

    public void setGeoTagPhotoSelectionListener(GeoTagPhotoSelectionListener geoTagPhotoSelectionListener) {
        this.geoTagPhotoSelectionListener = geoTagPhotoSelectionListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = new ImageView(this.ctx);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ImageView imgView = (ImageView) holder.v;
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inPreferredConfig = Bitmap.Config.ALPHA_8;
            Bitmap bitmap = BitmapFactory.decodeFile(mDataset.get(position), bmOptions);
            bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100,true);
            imgView.setImageBitmap(bitmap);
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    geoTagPhotoSelectionListener.onItemSelected(mDataset.get(position));
                }
            });
        } catch (Exception e1) {
            Log.d(MainActivity.TAG, "Exception in getting bitmap for image : "+ mDataset.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public View v;
        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }

    public GeoTagImagesAdapter(ArrayList<String> myDataset, Context ctx) {
        mDataset = myDataset;
        this.ctx = ctx;
    }

    public void resetDataset(ArrayList<String> myDataset) {
        this.mDataset.clear();
        this.mDataset = myDataset;
    }
}

package lnt.in.geospatial;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by manoj on 21-Jun-17.
 */

public class GeoTagFragment extends Fragment {

    private RecyclerView mRecyclerView = null;
    private LinearLayoutManager mLayoutManager = null;
    private GeoTagPhotoSelectionListener geoTagPhotoSelectionListener = null;
    private TextView txtv_no_photos = null;

    public void setGeoTagPhotoSelectionListener(GeoTagPhotoSelectionListener geoTagPhotoSelectionListener) {
        this.geoTagPhotoSelectionListener = geoTagPhotoSelectionListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_tool_geotag, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_geotag);
        txtv_no_photos = v.findViewById(R.id.txtv_geotag_no_photo);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
//        Log.d(MainActivity.TAG, "setting recycler view properties to solve stutter problem");
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

//        getGeoTaggedPhotos();
        // specify an adapter (see also next example)
//        String[] myDataset = new String[50];
//        for(int i=0; i<50; i++)
//            myDataset[i] = Integer.toString(i);
//        ArrayList<String> myDataset = getGalleryImages();
        /*ArrayList<String> myDataset = getGeoTaggedPhotos();
        if (myDataset.size() > 0) {
            txtv_no_photos.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            GeoTagImagesAdapter mAdapter = new GeoTagImagesAdapter(myDataset, getActivity().getApplicationContext());
            mAdapter.setGeoTagPhotoSelectionListener(this.geoTagPhotoSelectionListener);
            mRecyclerView.setAdapter(mAdapter);
        }*/
//        new GetGeoTaggedPhotosTask().execute();
        return v;
    }

    @Override
    public void onResume() {
        new GetGeoTaggedPhotosTask().execute();
        super.onResume();
    }

    public void getGeoTaggedPhotos() {
        new GetGeoTaggedPhotosTask().execute();
    }

    public void updateData() {
        txtv_no_photos.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        txtv_no_photos.setText(getString(R.string.msg_progress_geotag_get_photos));
        new GetGeoTaggedPhotosTask().execute();
    }

    private class GetGeoTaggedPhotosTask extends AsyncTask<Void, Integer, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> imagePaths = new ArrayList<>();
            GeoTagDBHelper geoTagDBHelper = new GeoTagDBHelper(getActivity());
            SQLiteDatabase geoTagDB =  geoTagDBHelper.getWritableDatabase();
            Cursor cursor = geoTagDB.rawQuery("select * from "+ GeoTagDBHelper.GeoTagContract.TABLE_NAME, null);
            int imagePathIndex = cursor.getColumnIndex(GeoTagDBHelper.GeoTagContract.COL_NAME_IMAGE_PATH.toString());
            while (cursor.moveToNext()) {
                imagePaths.add(cursor.getString(imagePathIndex));
            }
            cursor.close();
            geoTagDB.close();
            return imagePaths;
        }

        @Override
        protected void onPostExecute(ArrayList<String> imagePaths) {
            super.onPostExecute(imagePaths);
            if (mRecyclerView.getAdapter() == null) {
                GeoTagImagesAdapter mAdapter = new GeoTagImagesAdapter(imagePaths, getActivity());
                mAdapter.setGeoTagPhotoSelectionListener(geoTagPhotoSelectionListener);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                ((GeoTagImagesAdapter) mRecyclerView.getAdapter()).resetDataset(imagePaths);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
            if (imagePaths.size() > 0) {
                txtv_no_photos.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                txtv_no_photos.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                txtv_no_photos.setText(getString(R.string.msg_info_geotag_no_photos));
            }
        }
    }
}

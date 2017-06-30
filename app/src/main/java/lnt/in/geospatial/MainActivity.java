package lnt.in.geospatial;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GeoTagPhotoSelectionListener, OnLongPressListener {

    public static final String TAG = "GEOSPATIAL";
    public static final String TAG_FRAGMENT_MEASURING_TOOL = "MEASURINGTOOL";
    public static final String TAG_FRAGMENT_COORDINATE_CONVERTER = "COORDINATECONVERTER";
    public static final String TAG_FRAGMENT_GEO_TAGGING = "GEOTAGGING";

    private String currentImagePath = null;
    private int currentMenu = 0;
    private int currentNavigationItemID = -1;
    private MapView mapView = null;
    private NavigationView navigationView = null;
    private View homeView = null;
    private Spinner basemapSwitcher = null;
    private View selectedCoordCalloutView = null;
    private HashMap<String, MapOptions.MapType> baseMaps = null;
    private DrawerLayout drawer = null;
    private boolean isNavigationItemSelected = false;
    private boolean isToolFragmentVisible = false;
    private MeasuringToolListener measuringToolListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        initDrawer();
        initMapView();
        initBaseMaps();
        initCallout();
        initBasemapSwitcherSpinner();
        initHomeView();
        initMeasuringToolListener();
    }

    private void initMapView() {
        mapView = (MapView) findViewById(R.id.map);
        mapView.setOnLongPressListener(this);
    }

    private void initHomeView() {
        homeView = navigationView.getHeaderView(0).findViewById(R.id.homeview);
        homeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });
    }

    private void initMeasuringToolListener() {
        measuringToolListener = MeasuringToolListener.getInstance(this, mapView);
        mapView.setOnTouchListener(measuringToolListener);
    }

    private void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        drawer.addDrawerListener(new NavigationDrawerCloseListener());
    }

    private void initBaseMaps() {
        baseMaps = new HashMap<>();
        baseMaps.put("Topo", MapOptions.MapType.TOPO);
        baseMaps.put("Street", MapOptions.MapType.STREETS);
        baseMaps.put("Satellite", MapOptions.MapType.SATELLITE);
    }

    private void initCallout() {
        selectedCoordCalloutView = getLayoutInflater().inflate((R.layout.callout_select_coord), null);
        TextView tv = selectedCoordCalloutView.findViewById(R.id.tv_callout_title_select_coord);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapView.getCallout().hide();
            }
        });
        mapView.getCallout().setContent(selectedCoordCalloutView);

    }

    private void initBasemapSwitcherSpinner(){
        basemapSwitcher = (Spinner) findViewById(R.id.spin_basemap_switcher);
        Spinner basemapswitcherSpinner = (Spinner) findViewById(R.id.spin_basemap_switcher);
        String[] baseMapNames = (String[]) baseMaps.keySet().toArray(new String[0]);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, baseMapNames);
        basemapswitcherSpinner.setAdapter(spinnerAdapter);
        basemapSwitcher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String baseMapName = (String) adapterView.getAdapter().getItem(i);
                MapOptions mapOptions = new MapOptions(baseMaps.get(baseMapName));
                mapView.setMapOptions(mapOptions);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void animateToolFragment(boolean isToolFragmentAvailable) {
        if (!isToolFragmentAvailable) {
            if (!isToolFragmentVisible)
                slideToolsFragmentUp();
        }
        else {
            if (!isToolFragmentVisible)
                slideToolsFragmentUp();
            else
                slideToolsFragmentDown();
        }
    }

    private void setupMeasuringToolFragment(){
        MeasuringToolFragment measuringToolFragment = (MeasuringToolFragment) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_MEASURING_TOOL);
        if (measuringToolFragment == null) {
            measuringToolFragment = new MeasuringToolFragment();
            measuringToolListener.setmEditMode(MeasuringToolListener.EditMode.POLYLINE);
            measuringToolListener.setUpdateMeasurementListener(measuringToolFragment);
            mapView.addLayer(measuringToolListener.getMeasureToolGraphicsLayer());
            FragmentTransaction fragTrans = getFragmentManager().beginTransaction();
            if (isToolFragmentVisible)
                fragTrans.setCustomAnimations(R.animator.frag_fade_in, R.animator.frag_fade_out);
            fragTrans.replace(R.id.frag_container_tool_display, measuringToolFragment, TAG_FRAGMENT_MEASURING_TOOL);
            fragTrans.commit();
            animateToolFragment(false);
        } else
            animateToolFragment(true);
    }

    private void setupCoordinateConverterFragment(){
        CoordinateConverterFragment coordinateConverterFragment = (CoordinateConverterFragment) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_COORDINATE_CONVERTER);
        if (coordinateConverterFragment == null) {
            coordinateConverterFragment = new CoordinateConverterFragment();
            coordinateConverterFragment.setMapViewWeakReference(new WeakReference<MapView>(mapView));
            FragmentTransaction fragTrans = getFragmentManager().beginTransaction();
            if (isToolFragmentVisible)
                fragTrans.setCustomAnimations(R.animator.frag_fade_in, R.animator.frag_fade_out);
            fragTrans.replace(R.id.frag_container_tool_display, coordinateConverterFragment, TAG_FRAGMENT_COORDINATE_CONVERTER);
            fragTrans.commit();
            animateToolFragment(false);
        }
        else
            animateToolFragment(true);
    }

    private void setupGeoTagFragment(){
        GeoTagFragment geoTagFragment = (GeoTagFragment) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_GEO_TAGGING);
        if (geoTagFragment == null) {
            geoTagFragment = new GeoTagFragment();
            geoTagFragment.setGeoTagPhotoSelectionListener(this);
            FragmentTransaction fragTrans = getFragmentManager().beginTransaction();
//            if (isToolFragmentVisible)
//                fragTrans.setCustomAnimations(R.animator.frag_fade_in, R.animator.frag_fade_out);
            fragTrans.replace(R.id.frag_container_tool_display, geoTagFragment, TAG_FRAGMENT_GEO_TAGGING);
            fragTrans.commit();
            animateToolFragment(false);
        }
        else
            animateToolFragment(true);
    }

    private void slideToolsFragmentUp() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        ViewGroup fragParent = (ViewGroup) findViewById(R.id.frag_container_tool_display);
        fragParent.startAnimation(slideUp);
        fragParent.setVisibility(View.VISIBLE);
        isToolFragmentVisible = true;
    }

    private void slideToolsFragmentDown() {
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        ViewGroup fragParent = (ViewGroup) findViewById(R.id.frag_container_tool_display);
        fragParent.startAnimation(slideDown);
        fragParent.setVisibility(View.GONE);
        isToolFragmentVisible = false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (currentMenu == 0)
            getMenuInflater().inflate(R.menu.main, menu);
        else
            getMenuInflater().inflate(currentMenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_geotag_camera){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                if (photoFile != null) {
//                    Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                    Uri photoURI = Uri.fromFile(photoFile);
//                    takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    takePictureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        }
        else if(id == R.id.item_measure_tool_clear)
            measuringToolListener.actionClear();
        else if (id == R.id.item_measure_tool_undo)
            measuringToolListener.actionUndo();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        isNavigationItemSelected = true;
        currentNavigationItemID = item.getItemId();
        return true;
    }

    private void showCurrentLocation() {
        if (mapView.getLocationDisplayManager().isStarted())
            mapView.getLocationDisplayManager().stop();
        else {
            mapView.getLocationDisplayManager().setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
            mapView.getLocationDisplayManager().start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == -1) {
//            GeoTagPhotoCaptureDialog dialog = new GeoTagPhotoCaptureDialog(this, currentImagePath);
//            dialog.show();
            addPhotoToDB();
            GeoTagFragment geoTagFragment = (GeoTagFragment) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_GEO_TAGGING);
            geoTagFragment.updateData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addPhotoToDB() {
        GeoTagDBHelper geoTagDBHelper = new GeoTagDBHelper(this);
        SQLiteDatabase geotTagDB = geoTagDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GeoTagDBHelper.GeoTagContract.COL_NAME_IMAGE_PATH, currentImagePath);
        long id = geotTagDB.insert(GeoTagDBHelper.GeoTagContract.TABLE_NAME, null, values);
        if (id > 0)
            Log.d(TAG, "photo inserted.");
        else
            Log.d(TAG, "photo not inserted.");
        geotTagDB.close();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "GEOTAG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentImagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onItemSelected(String imageFilePath) {
        Log.d(TAG, "selected image path : "+ imageFilePath);
        try {
            ExifInterface exifInterface = new ExifInterface(imageFilePath);
            float[] latLng = new float[2];
            exifInterface.getLatLong(latLng);
            Log.d(TAG, "lat : "+ latLng[0]);
            Log.d(TAG, "lng : "+ latLng[1]);
            MapView mapView = (MapView) findViewById(R.id.map);
            Log.d(TAG, "Spatial reference : "+ mapView.getSpatialReference());
            Point center = new Point();
            center.setX(latLng[1]);
            center.setY(latLng[0]);
//            center = CoordinateConversion.pointToGeoref(center, mapView.getSpatialReference(), 6, true);
            center = GeometryEngine.project(center.getX(), center.getY(), mapView.getSpatialReference());
            Log.d(TAG, "zooming to : "+ center);
            mapView.zoomToScale(center, 12);


        } catch (IOException e) {
            Log.d(TAG, "IO Exception in reading image : "+ e.getMessage());
        }
    }

    @Override
    public boolean onLongPress(float x, float y) {
        if (currentNavigationItemID != R.id.nav_measure_tool)
            showSelectedCoordinate(x, y);
        return true;
    }

    private void showSelectedCoordinate(float screenX, float screenY) {
        TextView tvProjCoord = null;
        TextView tvDDCoord = null;
        TextView tvDMSCoord = null;
        TextView tvUTMCoord = null;
        tvProjCoord = selectedCoordCalloutView.findViewById(R.id.tv_callout_select_coord_proj);
        tvDDCoord = selectedCoordCalloutView.findViewById(R.id.tv_callout_select_coord_dd);
        tvDMSCoord = selectedCoordCalloutView.findViewById(R.id.tv_callout_select_coord_dms);
        tvUTMCoord = selectedCoordCalloutView.findViewById(R.id.tv_callout_select_coord_utm);
        Point mapPoint = mapView.toMapPoint(screenX, screenY);
        tvProjCoord.setText(mapPoint.getX() + " ,"+ mapPoint.getY());
        String ddString = CoordinateConversion.pointToDecimalDegrees(mapPoint, mapView.getSpatialReference(), 4);
        tvDDCoord.setText(ddString);
        String dmsString = CoordinateConversion.pointToDegreesMinutesSeconds(mapPoint, mapView.getSpatialReference(), 4);
        tvDMSCoord.setText(dmsString);
        String utmString = CoordinateConversion.pointToUtm(mapPoint, mapView.getSpatialReference(), CoordinateConversion.UTMConversionMode.DEFAULT, true);
        tvUTMCoord.setText(utmString);
        mapView.getCallout().show(mapPoint);
    }

    private class NavigationDrawerCloseListener extends DrawerLayout.SimpleDrawerListener {
        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            if (isNavigationItemSelected) {
                switch (currentNavigationItemID) {
                    case R.id.nav_measure_tool:
                        currentMenu = R.menu.frag_measure_tool;
                        invalidateOptionsMenu();
                        setupMeasuringToolFragment();
                        break;
                    case R.id.nav_geo_tagging:
                        currentMenu = R.menu.geotag;
                        invalidateOptionsMenu();
                        setupGeoTagFragment();
                        break;
                    case R.id.nav_coordinate_converter:
                        currentMenu = 0;
                        invalidateOptionsMenu();
                        setupCoordinateConverterFragment();
                        break;
                    case R.id.nav_current_location:
                        showCurrentLocation();
                        break;
                    case R.id.nav_trail_point_tracker:
                        break;
                }
                isNavigationItemSelected = false;
            }
        }
    }

    private void reset() {
        currentMenu = 0;
        invalidateOptionsMenu();
        mapView.setOnLongPressListener(this);
        FragmentTransaction fragTrans = getFragmentManager().beginTransaction();
        if (isNavigationItemSelected) {
            switch (currentNavigationItemID) {
                case R.id.nav_measure_tool:
                    fragTrans.remove(getFragmentManager().findFragmentByTag(TAG_FRAGMENT_MEASURING_TOOL));
                    break;
                case R.id.nav_geo_tagging:
                    fragTrans.remove(getFragmentManager().findFragmentByTag(TAG_FRAGMENT_GEO_TAGGING));
                    break;
                case R.id.nav_coordinate_converter:
                    fragTrans.remove(getFragmentManager().findFragmentByTag(TAG_FRAGMENT_COORDINATE_CONVERTER));
                    break;
                case R.id.nav_current_location:
                    showCurrentLocation();
                    break;
                case R.id.nav_trail_point_tracker:
            }
            isNavigationItemSelected = false;
        }
    }
}
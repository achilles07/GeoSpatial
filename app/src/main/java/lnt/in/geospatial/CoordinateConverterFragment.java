package lnt.in.geospatial;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.esri.android.map.MapView;
import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.geometry.Point;

import java.lang.ref.WeakReference;

/**
 * Created by manoj on 21-Jun-17.
 */

public class CoordinateConverterFragment extends Fragment implements View.OnClickListener{

    private static final int MODE_DD_TO_UTM = 0;
    private static final int MODE_DMS_TO_UTM = 1;
    private static final int MODE_UTM_TO_DD = 2;
    private static final int MODE_UTM_TO_DMS = 3;

    Switch switchWgsType = null;
    Switch switchConversionType = null;
    ViewGroup dmsGroup = null;
    ViewGroup ddGroup = null;
    Button btnConvert = null;
    Button btnConvertnGo = null;
    Button btnGo = null;
    EditText etDDLat, etDDLng, etDMSLatDeg, etDMSLatMin, etDMSLatSec, etDMSLngDeg, etDMSLngMin, etDMSLngSec,
            etUtmZone, etUtmNorth, etUtmEast = null;
    WeakReference<MapView> mapViewWeakReference = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_tool_coordinate_converter, container, false);
        init(v);
        setupWGSTypeSwitch(v);
        return v;
    }

    public void setMapViewWeakReference(WeakReference<MapView> mapViewWeakReference) {
        this.mapViewWeakReference = mapViewWeakReference;
    }

    private void init(View v) {
        switchWgsType = v.findViewById(R.id.switch_coord_conv_wgstype);
        switchConversionType = v.findViewById(R.id.switch_coord_conv_convtype);
        dmsGroup = v.findViewById(R.id.linlyt_container_dms);
        ddGroup = v.findViewById(R.id.linlyt_container_dd);
        btnConvert = v.findViewById(R.id.btn_coordconv_convert);
        btnConvert.setOnClickListener(this);
        btnConvertnGo = v.findViewById(R.id.btn_coordconv_convertngo);
        btnConvertnGo.setOnClickListener(this);
        btnGo = v.findViewById(R.id.btn_coordconv_go);
        btnGo.setOnClickListener(this);
        etDDLat = v.findViewById(R.id.et_coordconv_dd_lat);
        etDDLng = v.findViewById(R.id.et_coordconv_dd_lng);
        etDMSLatDeg = v.findViewById(R.id.et_coordconv_dms_lat_deg);
        etDMSLatMin = v.findViewById(R.id.et_coordconv_dms_lat_min);
        etDMSLatSec = v.findViewById(R.id.et_coordconv_dms_lat_sec);
        etDMSLngDeg = v.findViewById(R.id.et_coordconv_dms_lng_deg);
        etDMSLngMin = v.findViewById(R.id.et_coordconv_dms_lng_min);
        etDMSLngSec = v.findViewById(R.id.et_coordconv_dms_lng_sec);
        etUtmZone = v.findViewById(R.id.et_coordconv_utm_zone);
        etUtmNorth = v.findViewById(R.id.et_coordconv_utm_north);
        etUtmEast = v.findViewById(R.id.et_coordconv_utm_east);
    }

    private void setupWGSTypeSwitch(View v){
        switchWgsType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean switchedOn) {
                if(switchedOn){
                    ddGroup.setVisibility(View.GONE);
                    dmsGroup.setVisibility(View.VISIBLE);
                }
                else{
                    dmsGroup.setVisibility(View.GONE);
                    ddGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private int getConversionMode() {
        if (switchWgsType.isChecked()) {
            if (switchConversionType.isChecked())
                return MODE_UTM_TO_DMS;
            else
                return MODE_DMS_TO_UTM;
        }
        else {
            if (switchConversionType.isChecked())
                return MODE_UTM_TO_DD;
            else
                return MODE_DD_TO_UTM;
        }
    }

    private Point getMapPoint() {
        int conversionMode = getConversionMode();
        Point mapPoint = null;
        String ddString, dmsString, utmString, utmZone, utmNorth, utmEast;

        switch (conversionMode) {
            case MODE_DD_TO_UTM:
                String ddLatString = etDDLat.getText().toString().trim();
                String ddLngString = etDDLng.getText().toString().trim();
                ddString = ddLatString+ "N"+ ddLngString+ "E";
                mapPoint = CoordinateConversion.decimalDegreesToPoint(ddString, mapViewWeakReference.get().getSpatialReference());
                break;
            case MODE_DMS_TO_UTM:
                String dmsLatDeg = etDMSLatDeg.getText().toString().trim();
                String dmsLatMin = etDMSLatMin.getText().toString().trim();
                String dmsLatSec = etDMSLatSec.getText().toString().trim();
                String dmsLngDeg = etDMSLngDeg.getText().toString().trim();
                String dmsLngMin = etDMSLngMin.getText().toString().trim();
                String dmsLngSec = etDMSLngSec.getText().toString().trim();
                dmsString = dmsLatDeg+ " "+ dmsLatMin+ " "+ dmsLatSec+ "N"+ dmsLngDeg+ " "+ dmsLngMin+ " "+ dmsLngSec+ "E";
                mapPoint = CoordinateConversion.degreesMinutesSecondsToPoint(dmsString, mapViewWeakReference.get().getSpatialReference());
                break;
            case MODE_UTM_TO_DD:
            case MODE_UTM_TO_DMS:
                utmZone = etUtmZone.getText().toString().trim();
                utmNorth = etUtmNorth.getText().toString().trim();
                utmEast = etUtmEast.getText().toString().trim();
                utmString = utmZone+ " "+ utmNorth+ " "+ utmEast;
                mapPoint = CoordinateConversion.utmToPoint(utmString, mapViewWeakReference.get().getSpatialReference(), CoordinateConversion.UTMConversionMode.DEFAULT);
                break;
        }
        return mapPoint;
    }

    private void convert() {
        String utmString = null;
        String[] utmStringArray = null;
        String ddString;
        String[] ddStringArray;
        String dmsString;
        String[] dmsStringArray;
        int conversionMode = getConversionMode();
        Point mapPoint = getMapPoint();

        switch (conversionMode) {
            case MODE_DD_TO_UTM:
                utmString = CoordinateConversion.pointToUtm(mapPoint, mapViewWeakReference.get().getSpatialReference(), CoordinateConversion.UTMConversionMode.DEFAULT, true);
                utmStringArray = utmString.split(" ");
            case MODE_DMS_TO_UTM:
                if (utmString == null) {
                    utmString = CoordinateConversion.pointToUtm(mapPoint, mapViewWeakReference.get().getSpatialReference(), CoordinateConversion.UTMConversionMode.DEFAULT, true);
                    utmStringArray = utmString.split(" ");
                }
                etUtmZone.setText(utmStringArray[0]);
                etUtmNorth.setText(utmStringArray[1]);
                etUtmEast.setText(utmStringArray[2]);
                break;
            case MODE_UTM_TO_DD:
                ddString = CoordinateConversion.pointToDecimalDegrees(mapPoint, mapViewWeakReference.get().getSpatialReference(), 8);
                ddStringArray = ddString.split(" ");
                etDDLat.setText(ddStringArray[0].substring(0, ddStringArray[0].length()-1));
                etDDLng.setText(ddStringArray[1].substring(0, ddStringArray[1].length()-1));
                break;
            case MODE_UTM_TO_DMS:
                dmsString = CoordinateConversion.pointToDegreesMinutesSeconds(mapPoint, mapViewWeakReference.get().getSpatialReference(), 4);
                dmsStringArray = dmsString.split(" ");
                etDMSLatDeg.setText(dmsStringArray[0]);
                etDMSLatMin.setText(dmsStringArray[1]);
                etDMSLatSec.setText(dmsStringArray[2].substring(0, dmsStringArray[2].length()-1));
                etDMSLngDeg.setText(dmsStringArray[3]);
                etDMSLngMin.setText(dmsStringArray[4]);
                etDMSLngSec.setText(dmsStringArray[5].substring(0, dmsStringArray[5].length()-1));
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_coordconv_convert:
                convert();
                break;
            case R.id.btn_coordconv_convertngo:
                convert();
                mapViewWeakReference.get().zoomToResolution(getMapPoint(), 12);
                break;
            case R.id.btn_coordconv_go:
                mapViewWeakReference.get().zoomToResolution(getMapPoint(), 12);
                break;
        }
    }
}

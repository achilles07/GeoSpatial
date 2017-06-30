package lnt.in.geospatial;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by manoj on 19-Jun-17.
 */

public class MeasuringToolFragment extends Fragment implements UpdateMeasurementListener{

    TextView tvLength, tvArea, tvPerimeter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_tool_measure, container, false);
        return v;
    }

    @Override
    public void onStart() {
        init();
        super.onStart();
    }

    private void init() {
        tvLength = getView().findViewById(R.id.tv_frag_tool_measure_length);;
        tvArea = getView().findViewById(R.id.tv_frag_tool_measure_area);
        tvPerimeter = getView().findViewById(R.id.tv_frag_tool_measure_perimeter);
    }

    @Override
    public void onDestroy() {
        MeasuringToolListener.getInstance(null, null).reset();
        super.onDestroy();
    }

    @Override
    public void updatePolyline(MultiPath multipath) {
        tvLength.setText(String.format(Locale.ENGLISH, "%.4f", multipath.calculateLength2D())+ " m");
        tvArea.setText(getString(R.string.value_dummy_measure_tool));
        tvPerimeter.setText(getString(R.string.value_dummy_measure_tool));
    }

    @Override
    public void updatePolygon(MultiPath multipath) {
        tvLength.setText(getString(R.string.value_dummy_measure_tool));
        tvArea.setText(String.format(Locale.ENGLISH, "%.4f", multipath.calculateArea2D())+ " sq. m");
        tvPerimeter.setText(String.format(Locale.ENGLISH, "%.4f", multipath.calculateLength2D())+ " m");
    }

    @Override
    public void updateNaN() {
        tvLength.setText(getString(R.string.value_dummy_measure_tool));
        tvArea.setText(getString(R.string.value_dummy_measure_tool));
        tvPerimeter.setText(getString(R.string.value_dummy_measure_tool));
    }
}

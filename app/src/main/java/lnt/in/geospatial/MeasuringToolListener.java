package lnt.in.geospatial;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
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

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by manoj on 29-Jun-17.
 */

public class MeasuringToolListener extends MapOnTouchListener {

    public enum EditMode {
        NONE, POLYLINE, POLYGON
    }

    EditMode mEditMode = EditMode.NONE;
    MapView mapView;
    ArrayList<Point> mPoints = new ArrayList<Point>();

    ArrayList<Point> mMidPoints = new ArrayList<Point>();

    boolean mMidPointSelected = false;

    boolean mVertexSelected = false;

    int mInsertingIndex;

    static MeasuringToolListener measuringToolListener = null;

    UpdateMeasurementListener updateMeasurementListener = null;

    GraphicsLayer measureToolGraphicsLayer;// = new GraphicsLayer();

    ArrayList<EditingStates> mEditingStates = new ArrayList<EditingStates>();

    SimpleMarkerSymbol mRedMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

    SimpleMarkerSymbol mBlackMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

    SimpleMarkerSymbol mGreenMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE);

    public static MeasuringToolListener getInstance(Context context, MapView mapview) {
        if (measuringToolListener == null)
            measuringToolListener = new MeasuringToolListener(context, mapview);
        return measuringToolListener;
    }

    private MeasuringToolListener(Context context, MapView view) {
        super(context, view);
        mapView = view;
    }

    public void setUpdateMeasurementListener(UpdateMeasurementListener updateMeasurementListener) {
        this.updateMeasurementListener = updateMeasurementListener;
    }

    public GraphicsLayer getMeasureToolGraphicsLayer() {
        measureToolGraphicsLayer = new GraphicsLayer();
        return measureToolGraphicsLayer;
    }

    public void setmEditMode(EditMode mEditMode) {
        this.mEditMode = mEditMode;
    }

    @Override
    public void onLongPress(MotionEvent point) {
        if (mEditMode != EditMode.NONE) {
            if (mEditMode == EditMode.POLYLINE)
                mEditMode = EditMode.POLYGON;
            else
                mEditMode = EditMode.POLYLINE;
            refresh();
        }
        super.onLongPress(point);
    }

    @Override
    public boolean onSingleTap(final MotionEvent e) {
        if (mEditMode != EditMode.NONE)
            handleTap(e);
        return true;
    }

    /***
     * Handle a tap on the map (or the end of a magnifier long-press event).
     *
     * @param e The point that was tapped.
     */
    private void handleTap(final MotionEvent e) {

        // Ignore the tap if we're not creating a feature just now
            /*if (mEditMode == EditMode.NONE || mEditMode == EditMode.SAVING) {
                return;
            }*/

        Point point = mapView.toMapPoint(new Point(e.getX(), e.getY()));

        // If we're creating a point, clear any existing point
            /*if (mEditMode == EditMode.POINT) {
                mPoints.clear();
            }*/

        // If a point is currently selected, move that point to tap point
        if (mMidPointSelected || mVertexSelected) {
            movePoint(point);
        } else {
            // If tap coincides with a mid-point, select that mid-point
            int idx1 = getSelectedIndex(e.getX(), e.getY(), mMidPoints, mapView);
            if (idx1 != -1) {
                mMidPointSelected = true;
                mInsertingIndex = idx1;
            } else {
                // If tap coincides with a vertex, select that vertex
                int idx2 = getSelectedIndex(e.getX(), e.getY(), mPoints, mapView);
                if (idx2 != -1) {
                    mVertexSelected = true;
                    mInsertingIndex = idx2;
                } else {
                    // No matching point above, add new vertex at tap point
                    mPoints.add(point);
                    mEditingStates.add(new EditingStates(mPoints, mMidPointSelected, mVertexSelected, mInsertingIndex));
                }
            }
        }

        // Redraw the graphics layer
        refresh();
    }

    private int getSelectedIndex(double x, double y, ArrayList<Point> points, MapView map) {
        final int TOLERANCE = 40; // Tolerance in pixels

        if (points == null || points.size() == 0) {
            return -1;
        }

        // Find closest point
        int index = -1;
        double distSQ_Small = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            Point p = map.toScreenPoint(points.get(i));
            double diffx = p.getX() - x;
            double diffy = p.getY() - y;
            double distSQ = diffx * diffx + diffy * diffy;
            if (distSQ < distSQ_Small) {
                index = i;
                distSQ_Small = distSQ;
            }
        }

        // Check if it's close enough
        if (distSQ_Small < (TOLERANCE * TOLERANCE)) {
            return index;
        }
        return -1;
    }

    private void movePoint(Point point) {
        if (mMidPointSelected) {
            // Move mid-point to the new location and make it a vertex
            mPoints.add(mInsertingIndex + 1, point);
        } else {
            // Must be a vertex: move it to the new location
            ArrayList<Point> temp = new ArrayList<Point>();
            for (int i = 0; i < mPoints.size(); i++) {
                if (i == mInsertingIndex) {
                    temp.add(point);
                } else {
                    temp.add(mPoints.get(i));
                }
            }
            mPoints.clear();
            mPoints.addAll(temp);
        }
        // Go back to the normal drawing mode and save the new editing state
        mMidPointSelected = false;
        mVertexSelected = false;
        mEditingStates.add(new EditingStates(mPoints, mMidPointSelected, mVertexSelected, mInsertingIndex));
    }

    void refresh() {
        if (measureToolGraphicsLayer != null) {
            measureToolGraphicsLayer.removeAll();
        }
        drawPolylineOrPolygon();
        drawMidPoints();
        drawVertices();
    }

    private void drawPolylineOrPolygon() {
        Graphic graphic;
        MultiPath multipath;

        if (mPoints.size() == 0) {

        }

        // Create and add graphics layer if it doesn't already exist

        if (mPoints.size() > 1) {

            // Build a MultiPath containing the vertices
            if (mEditMode == EditMode.POLYLINE) {
                multipath = new Polyline();
            } else {
                multipath = new Polygon();
            }
            multipath.startPath(mPoints.get(0));
            for (int i = 1; i < mPoints.size(); i++) {
                multipath.lineTo(mPoints.get(i));
            }

            // Draw it using a line or fill symbol
            if (mEditMode == EditMode.POLYLINE) {
                graphic = new Graphic(multipath, new SimpleLineSymbol(Color.BLACK, 4));
                updateMeasurementListener.updatePolyline(multipath);
            } else {
                SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.YELLOW);
                simpleFillSymbol.setAlpha(100);
                simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 4));
                graphic = new Graphic(multipath, (simpleFillSymbol));
                updateMeasurementListener.updatePolygon(multipath);
            }
            measureToolGraphicsLayer.addGraphic(graphic);
        }
    }

    private void drawMidPoints() {
        int index;
        Graphic graphic;

        mMidPoints.clear();
        if (mPoints.size() > 1) {

            // Build new list of mid-points
            for (int i = 1; i < mPoints.size(); i++) {
                Point p1 = mPoints.get(i - 1);
                Point p2 = mPoints.get(i);
                mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
            }
            if (mEditMode == EditMode.POLYGON && mPoints.size() > 2) {
                // Complete the circle
                Point p1 = mPoints.get(0);
                Point p2 = mPoints.get(mPoints.size() - 1);
                mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
            }

            // Draw the mid-points
            index = 0;
            for (Point pt : mMidPoints) {
                if (mMidPointSelected && mInsertingIndex == index) {
                    graphic = new Graphic(pt, mRedMarkerSymbol);
                } else {
                    graphic = new Graphic(pt, mGreenMarkerSymbol);
                }
                measureToolGraphicsLayer.addGraphic(graphic);
                index++;
            }
        }
    }

    private void drawVertices() {
        int index = 0;
        SimpleMarkerSymbol symbol;

        for (Point pt : mPoints) {
            if (mVertexSelected && index == mInsertingIndex) {
                // This vertex is currently selected so make it red
                symbol = mRedMarkerSymbol;
            } else if (index == mPoints.size() - 1 && !mMidPointSelected && !mVertexSelected) {
                // Last vertex and none currently selected so make it red
                symbol = mRedMarkerSymbol;
            } else {
                // Otherwise make it black
                symbol = mBlackMarkerSymbol;
            }
            Graphic graphic = new Graphic(pt, symbol);
            measureToolGraphicsLayer.addGraphic(graphic);
            index++;
        }
    }

    public void actionUndo() {
        mEditingStates.remove(mEditingStates.size() - 1);
        mPoints.clear();
        if (mEditingStates.size() == 0) {
            mMidPointSelected = false;
            mVertexSelected = false;
            mInsertingIndex = 0;
        } else {
            EditingStates state = mEditingStates.get(mEditingStates.size() - 1);
            mPoints.addAll(state.points);
            mMidPointSelected = state.midPointSelected;
            mVertexSelected = state.vertexSelected;
            mInsertingIndex = state.insertingIndex;
        }
        refresh();
    }

    public void actionClear() {
        clear();
        updateMeasurementListener.updateNaN();
    }

    public void reset() {
        clear();
        mEditMode = EditMode.NONE;
        mapView.removeLayer(measureToolGraphicsLayer);
    }

    private void clear() {
        mPoints.clear();
        mMidPointSelected = false;
        mVertexSelected = false;
        mInsertingIndex = 0;
        mEditingStates.clear();
        mMidPoints.clear();
        measureToolGraphicsLayer.removeAll();
    }

    private class EditingStates {
        ArrayList<Point> points = new ArrayList<Point>();

        boolean midPointSelected = false;

        boolean vertexSelected = false;

        int insertingIndex;

        public EditingStates(ArrayList<Point> points, boolean midpointselected, boolean vertexselected, int insertingindex) {
            this.points.addAll(points);
            this.midPointSelected = midpointselected;
            this.vertexSelected = vertexselected;
            this.insertingIndex = insertingindex;
        }
    }
}

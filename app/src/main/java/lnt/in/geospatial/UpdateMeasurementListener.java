package lnt.in.geospatial;

import com.esri.core.geometry.MultiPath;

/**
 * Created by manoj on 29-Jun-17.
 */

public interface UpdateMeasurementListener {
    public void updatePolyline(MultiPath multiPath);
    public void updatePolygon(MultiPath multiPath);
    public void updateNaN();
}

package lnt.in.geospatial;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.esri.core.geometry.Line;

/**
 * Created by manoj on 22-Jun-17.
 */

public class GeoTagPhotoCaptureDialog extends Dialog {

    public GeoTagPhotoCaptureDialog(Context context, String imagePath) {
        super(context);
        ImageView imgView = new ImageView(getContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(imgView);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        imgView.setImageBitmap(bitmap);
    }
}

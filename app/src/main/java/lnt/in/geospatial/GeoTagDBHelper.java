package lnt.in.geospatial;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by manoj on 22-Jun-17.
 */

public class GeoTagDBHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE = "CREATE TABLE " + GeoTagContract.TABLE_NAME + " (" +
            GeoTagContract._ID + " INTEGER PRIMARY KEY," +
            GeoTagContract.COL_NAME_IMAGE_PATH + " TEXT)";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "geotag.db";


    public GeoTagDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public class GeoTagContract implements BaseColumns{

        public static final String TABLE_NAME = "geotag";
        public static final String COL_NAME_IMAGE_PATH = "imagepath";

        private GeoTagContract() {

        }
    }
}

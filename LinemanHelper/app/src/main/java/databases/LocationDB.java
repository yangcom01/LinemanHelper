package databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by b on 2016/5/26.
 * 创建两张表，经纬度表和多媒体表
 */
public class LocationDB extends SQLiteOpenHelper{

    public static final String TABLE_NAME_LATLNG = "latlng";
    public static final String TABLE_NAME_MEDIA = "media";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_LOCATION_LONGITUDE = "longitude";
    public static final String COLUMN_NAME_LOCATION_LATITUDE = "latitude";

    public static final String COLUMN_NAME_MEDIA_PILE = "pile";
    public static final String COLUMN_NAME_MEDIA_PICTURE = "path_picture";
    public static final String COLUMN_NAME_MEDIA_DESCRIPTION = "description_picture";



    public LocationDB(Context context) {
        super(context, null, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME_LATLNG+"(" +
                COLUMN_NAME_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME_LOCATION_LONGITUDE+" REAL NOT NULL DEFAULT \"\"," +
                COLUMN_NAME_LOCATION_LATITUDE+" REAL NOT NULL DEFAULT \"\"," +
                COLUMN_NAME_DATE+" TEXT NOT NULL DEFAULT \"\"" +
                ")");
        db.execSQL("CREATE TABLE "+TABLE_NAME_MEDIA+"(" +
                COLUMN_NAME_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME_MEDIA_PILE+" INTEGER NOT NULL DEFAULT \"\"," +
                COLUMN_NAME_MEDIA_DESCRIPTION+" TEXT NOT NULL DEFAULT \"\"," +
                COLUMN_NAME_MEDIA_PICTURE+" TEXT NOT NULL DEFAULT 0" +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

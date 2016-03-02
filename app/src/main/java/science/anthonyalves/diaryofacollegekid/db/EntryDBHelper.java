package science.anthonyalves.diaryofacollegekid.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database Table creator and updater.
 * Also contains constants of the Table name and columns
 */
public class EntryDBHelper extends SQLiteOpenHelper {

    public static final String USER_DB_NAME = "entries.sqlite";
    public static int COC_DB_VERSION = 1;

    public static final String TABLE_ENTRIES = "entries";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_KEYWORDS = "keywords";

    public static final String COLUMN_PHOTO_PATH = "photo_path";
    public static final String COLUMN_VIDEO_PATH = "video_path";
    public static final String COLUMN_GALLERY_PATH = "gallery_path";
    public static final String COLUMN_AUDIO_PATH = "audio_path";

    public static final String COLUMN_DATE = "date";

    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";


    Context mContext;

    public EntryDBHelper(Context context) {
        super(context, USER_DB_NAME, null, COC_DB_VERSION);
        mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE \"" + TABLE_ENTRIES + "\" (\n" +
                "\t`" + COLUMN_ID + "`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                "\t`" + COLUMN_TITLE + "`\tTEXT,\n" +
                "\t`" + COLUMN_BODY + "`\tTEXT,\n" +
                "\t`" + COLUMN_KEYWORDS + "`\tTEXT,\n" +
                "\t`" + COLUMN_PHOTO_PATH + "`\tTEXT,\n" +
                "\t`" + COLUMN_VIDEO_PATH + "`\tTEXT,\n" +
                "\t`" + COLUMN_GALLERY_PATH + "`\tTEXT,\n" +
                "\t`" + COLUMN_AUDIO_PATH + "`\tTEXT,\n" +
                "\t`" + COLUMN_DATE + "`\tTEXT,\n" +
                "\t`" + COLUMN_LONGITUDE + "`\tNUMERIC,\n" +
                "\t`" + COLUMN_LATITUDE + "`\tNUMERIC\n" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
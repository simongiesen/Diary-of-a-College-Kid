package science.anthonyalves.diaryofacollegekid.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import science.anthonyalves.diaryofacollegekid.Utils.Constants;


/**
 * Data access object to serve as an interface between the
 * front end of the app and database
 * Contains basic CRUD operations to the database
 */
public class EntryDAO {

    private final Context mContext;
    EntryDBHelper mUserDbHelper;
    SQLiteDatabase db;


    public EntryDAO(Context context) {
        mContext = context;
        mUserDbHelper = new EntryDBHelper(mContext);
        db = mUserDbHelper.getWritableDatabase();
    }

    /**
     * SELECT * FROM TABLE
     * READ all info from the db table
     * @return - list of all entries in the db
     */
    public ArrayList<Entry> getAll() {
        if (!db.isOpen()) {
            db = mUserDbHelper.getWritableDatabase();
        }
        // SELECT th8 FROM MaxLevel where _building = 'building'
        Cursor cursor = db.query(EntryDBHelper.TABLE_ENTRIES, null, null, null, null, null, null, null);

        ArrayList<Entry> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor2Entry(cursor));
        }

        db.close();
        cursor.close();
        return list;
    }

    /**
     * CREATE a new entry to the db
     * @param entry - journal entry to be added to the db
     * @return - true if the insert was successful; else, false
     */
    public boolean createEntry(Entry entry) {
        if (!db.isOpen()) {
            db = mUserDbHelper.getWritableDatabase();
        }

        ContentValues insertValues = entry2ContentValues(entry);

        /**
         * insert Returns the row ID of the newly inserted row, or -1 if an error occurred
         */
        long status = db.insert(EntryDBHelper.TABLE_ENTRIES, null, insertValues);
        return (status >= 0);
    }

    /**
     * UPDATE an existing entry in the db
     * @param entry - journal entry to be updated in the db
     * @return - true if the update was successful; else, false
     */
    public boolean updateEntry(Entry entry) {
        if (!db.isOpen()) {
            db = mUserDbHelper.getWritableDatabase();
        }

        ContentValues insertValues = entry2ContentValues(entry);

        int status = db.update(EntryDBHelper.TABLE_ENTRIES, insertValues, EntryDBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(entry.getId())});
        return (status > 0);
    }

    /**
     * DELETE an existing entry from the db
     * @param entry - journal entry to be deleted to the db
     * @return - true if the delete was successful; else, false
     */
    public boolean deleteEntry(Entry entry) {
        if (!db.isOpen()) {
            db = mUserDbHelper.getWritableDatabase();
        }
        int status = db.delete(EntryDBHelper.TABLE_ENTRIES, EntryDBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(entry.getId())});
        return (status > 0);
    }

    /**
     * Converter method
     * @param entry - Journal entry to be converted
     * @return - Content value pairs of the passed entry object
     */
    private ContentValues entry2ContentValues(Entry entry) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(EntryDBHelper.COLUMN_TITLE, entry.getTitle());
        insertValues.put(EntryDBHelper.COLUMN_BODY, entry.getBody());
        insertValues.put(EntryDBHelper.COLUMN_KEYWORDS, entry.getKeywords());

        insertValues.put(EntryDBHelper.COLUMN_GALLERY_PATH, entry.getGalleryPath());
        insertValues.put(EntryDBHelper.COLUMN_PHOTO_PATH, entry.getPhotoPath());
        insertValues.put(EntryDBHelper.COLUMN_VIDEO_PATH, entry.getVideoPath());
        insertValues.put(EntryDBHelper.COLUMN_AUDIO_PATH, entry.getAudioPath());

        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.SDF_FORMAT);
        String date = dateFormat.format(entry.getDate().getTime());
        insertValues.put(EntryDBHelper.COLUMN_DATE, date);

        Location location = entry.getLocation();
        try {
            insertValues.put(EntryDBHelper.COLUMN_LONGITUDE, location.getLongitude());
            insertValues.put(EntryDBHelper.COLUMN_LATITUDE, location.getLatitude());
        } catch (NullPointerException e) {

        }
        return insertValues;
    }

    /**
     * Converter method
     * @param cursor - row from db to be converted
     * @return - Entry object with the converted fields
     */
    private Entry cursor2Entry(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(EntryDBHelper.COLUMN_ID));

        String title = cursor.getString(cursor.getColumnIndex(EntryDBHelper.COLUMN_TITLE));
        String body = cursor.getString(cursor.getColumnIndex(EntryDBHelper.COLUMN_BODY));
        String keywords = cursor.getString(cursor.getColumnIndex(EntryDBHelper.COLUMN_KEYWORDS));

        String photoPath = cursor.getString(cursor.getColumnIndex(EntryDBHelper.COLUMN_PHOTO_PATH));
        String galleryPath = cursor.getString(cursor.getColumnIndex(EntryDBHelper.COLUMN_GALLERY_PATH));
        String videoPath = cursor.getString(cursor.getColumnIndex(EntryDBHelper.COLUMN_VIDEO_PATH));
        String audioPath = cursor.getString(cursor.getColumnIndex(EntryDBHelper.COLUMN_AUDIO_PATH));

        String date = cursor.getString(cursor.getColumnIndex(EntryDBHelper.COLUMN_DATE));

        double longitude = cursor.getDouble(cursor.getColumnIndex(EntryDBHelper.COLUMN_LONGITUDE));
        double latitude = cursor.getDouble(cursor.getColumnIndex(EntryDBHelper.COLUMN_LATITUDE));

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.SDF_FORMAT);
        try {
            cal.setTime(dateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Location location = null;
        if (!(longitude == 0 && latitude == 0)) {
            location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
        }
        Entry temp = new Entry(title, body, keywords, galleryPath, photoPath, videoPath, audioPath, location, cal);
        temp.setId(id);
        return temp;
    }
}

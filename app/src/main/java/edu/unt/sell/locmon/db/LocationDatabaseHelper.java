package edu.unt.sell.locmon.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SELL-1 on 3/25/2015.
 */
public class LocationDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = LocationDatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "locations.db";
    private static final int DATABASE_VERSION = 1;
    private Context mContext;

    // list of ids that are being synced, store them here so that they can be deleted
    // after successful sync
    public ArrayList<Integer> mSyncList;

    public LocationDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mSyncList = new ArrayList<Integer>();
    }

    // this method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database){
        LocationTable.onCreate(database);
    }

    // this method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        LocationTable.onUpgrade(database, oldVersion, newVersion);
    }

    // compose JSON out of SQLite records, add device id and android version
    public String composeJSONfromSQLite() {
        String deviceId = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
        String androidVersion = Build.VERSION.RELEASE;
        ArrayList<HashMap<String, String>> broadcastList;
        broadcastList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM " + LocationTable.TABLE_NAME;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("_id", cursor.getString(0));
                map.put("latitude", cursor.getString(1));
                map.put("longitude", cursor.getString(2));
                map.put("timestamp", cursor.getString(3));
                map.put("device_id", deviceId);
                map.put("android_version", androidVersion);
                broadcastList.add(map);

                mSyncList.add(Integer.parseInt(cursor.getString(0)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(broadcastList);
        Log.i(TAG, "Here is the json data: " + jsonString);
        return jsonString;
    }

    // delete all entries that have just been synced from the sqlite database
    // takes in an ArrayList of ids
    public void deleteJustSynced() {
        // generate a string that serves as the list of IDs in the query's IN clause
        StringBuffer inClauseBuffer = new StringBuffer();
        int syncListSize = mSyncList.size();
        for (int i=0; i<syncListSize; i++) {
            inClauseBuffer.append(Integer.toString(mSyncList.get(i)));
            if (i != syncListSize - 1) {
                inClauseBuffer.append(",");
            }
        }

        StringBuffer deleteQuery = new StringBuffer();
        deleteQuery.append("DELETE FROM " + LocationTable.TABLE_NAME + " WHERE _id IN (");
        deleteQuery.append(inClauseBuffer);
        deleteQuery.append(")");
        String deleteQueryStr = deleteQuery.toString();
        Log.i(TAG, deleteQueryStr);
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL(deleteQueryStr);
        database.close();
    }

    public boolean checkDatabaseEmpty() {
        int rowCount;
        boolean databaseEmpty;
        String contentQuery = "SELECT * FROM " + LocationTable.TABLE_NAME;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(contentQuery, null);
        rowCount = cursor.getCount();
        if (rowCount == 0) {
            databaseEmpty = true;
        } else {
            databaseEmpty = false;
        }
        return databaseEmpty;
    }
}

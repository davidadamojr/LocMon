package edu.unt.sell.contextmon.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by SELL-1 on 3/25/2015.
 */
public class BroadcastDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "broadcast_table.db";
    private static final int DATABASE_VERSION = 1;

    public BroadcastDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // this method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database){
        BroadcastTable.onCreate(database);
    }

    // this method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        BroadcastTable.onUpgrade(database, oldVersion, newVersion);
    }
}

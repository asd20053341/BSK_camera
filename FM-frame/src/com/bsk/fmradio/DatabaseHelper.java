package com.bsk.fmradio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	
	
	public DatabaseHelper(Context context) {
		super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		 db.execSQL("CREATE TABLE "+ Constants.TABLE_FREQ +" (" +
				 	Constants.COLUMN_FREQUENCY + " integer PRIMARY KEY," +
				 	Constants.COLUMN_TITLE + " TEXT " +
                    ");");
		 
		 db.execSQL("CREATE TABLE "+ Constants.TABLE_FAVOR +" (" +
				 	Constants.COLUMN_FREQUENCY + " integer PRIMARY KEY," +
				 	Constants.COLUMN_TITLE + " TEXT " +
                 ");");
		 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
}

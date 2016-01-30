package com.example.blacklist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {

	/*public static final String CREATE_BOOK = "create table Book ("
			+ "id integer primary key autoincrement, " 
			+ "author text, "
			+ "price real, " 
			+ "pages integer, " 
			+ "name text)";*/
	//创建表的SQL语句
	public static final String CREATE_BLACKLIST = "create table blacklist (" 
			+ "package text primary key)";
	
//	public static final String CREATE_CATEGORY = "create table Category ("
//			+ "id integer primary key autoincrement, "
//			+ "category_name text, "
//			+ "category_code integer)";

	//这里只是把new出MyDatabaseHelper类的那个类传给了mContext，供下面的方法使用
	private Context mContext;

	//构造器方法，new MyDatabaseHelper时，要传入的参数
	//context为在哪个类new出MyDatabaseHelper，就传入哪个类名.this
	//name为要创建的数据库名
	//factory一般传null
	//version为这个数据库的版本号
	public MyDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	//当new出来MyDatabaseHelper后，自动执行onCreate方法，因为MyDatabaseHelper继承了SQLiteOpenHelper
	@Override
	public void onCreate(SQLiteDatabase db) {
		//执行创建表语句
		db.execSQL(CREATE_BLACKLIST);	
		//db.execSQL(CREATE_CATEGORY);
		//mContext这里就是用了上面定义的Context对象
		Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
	}

	//这个方法好像是有更新的时候会执行
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("drop table if exists Book");
		//db.execSQL("drop table if exists Category");
		//onCreate(db);
	}
	

}

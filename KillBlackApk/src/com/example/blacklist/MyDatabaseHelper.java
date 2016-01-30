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
	//�������SQL���
	public static final String CREATE_BLACKLIST = "create table blacklist (" 
			+ "package text primary key)";
	
//	public static final String CREATE_CATEGORY = "create table Category ("
//			+ "id integer primary key autoincrement, "
//			+ "category_name text, "
//			+ "category_code integer)";

	//����ֻ�ǰ�new��MyDatabaseHelper����Ǹ��ഫ����mContext��������ķ���ʹ��
	private Context mContext;

	//������������new MyDatabaseHelperʱ��Ҫ����Ĳ���
	//contextΪ���ĸ���new��MyDatabaseHelper���ʹ����ĸ�����.this
	//nameΪҪ���������ݿ���
	//factoryһ�㴫null
	//versionΪ������ݿ�İ汾��
	public MyDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	//��new����MyDatabaseHelper���Զ�ִ��onCreate��������ΪMyDatabaseHelper�̳���SQLiteOpenHelper
	@Override
	public void onCreate(SQLiteDatabase db) {
		//ִ�д��������
		db.execSQL(CREATE_BLACKLIST);	
		//db.execSQL(CREATE_CATEGORY);
		//mContext��������������涨���Context����
		Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
	}

	//��������������и��µ�ʱ���ִ��
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("drop table if exists Book");
		//db.execSQL("drop table if exists Category");
		//onCreate(db);
	}
	

}

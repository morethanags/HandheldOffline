package com.huntloc.handheldoffline;

import java.util.LinkedList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 3;
	private static final String DATABASE_NAME = "CCUREDB";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_BADGE_TABLE = "CREATE TABLE Journal ( "
				+ "guid TEXT PRIMARY KEY, badge TEXT, log TEXT, door TEXT, time TEXT, sent INTEGER, name TEXT, descLog TEXT )";
		db.execSQL(CREATE_BADGE_TABLE);
		
		String CREATE_PORTRAIT_TABLE = "CREATE TABLE Portrait ( "+ "internalCode TEXT PRIMARY KEY, printedCode TEXT, portrait TEXT, name TEXT, access TEXT, camoExpiration TEXT, expiration, TEXT)";
		db.execSQL(CREATE_PORTRAIT_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS Journal");
		db.execSQL("DROP TABLE IF EXISTS Portrait");
		this.onCreate(db);
	}

	private static final String TABLE_RECORD = "Journal";
	private static final String KEY_GUID = "guid";
	private static final String KEY_BADGE = "badge";
	private static final String KEY_LOG = "log";
	private static final String KEY_DOOR = "door";
	private static final String KEY_TIME = "time";
	private static final String KEY_SENT = "sent";
	private static final String KEY_NAME = "name";
	private static final String KEY_DESC = "descLog";
	//private static final String[] COLUMNS = { KEY_GUID, KEY_BADGE, KEY_LOG, KEY_DOOR, KEY_TIME, KEY_SENT };

	public void addRecord(Journal record) {
		Log.d("addRecord", record.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_GUID, record.getGuid());
		values.put(KEY_BADGE, record.getBadge());
		values.put(KEY_LOG, record.getLog());
		values.put(KEY_DOOR, record.getDoor());
		values.put(KEY_TIME, record.getTime());
		values.put(KEY_SENT, record.isSent() ? 1 : 0);
		values.put(KEY_NAME, record.getName());
		values.put(KEY_DESC, record.getDescLog());
		db.insert(TABLE_RECORD, null, values);
		db.close();
	}

	public List<Journal> getAllRecords(String log) {
		List<Journal> records = new LinkedList<Journal>();
		String query = log!=null?"SELECT  * FROM " + TABLE_RECORD + " where "+ KEY_SENT +" = 0 and " + KEY_DESC + " = '"+log+"'":"SELECT  * FROM " + TABLE_RECORD + " where "+ KEY_SENT +" = 0";
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		Journal record = null;
		if (cursor.moveToFirst()) {
			do {
				record = new Journal(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),Long.parseLong(cursor.getString(4)),cursor.getInt(5) == 1, cursor.getString(6), cursor.getString(7));
				records.add(record);

			} while (cursor.moveToNext());
			Log.d("getAllRecords()", records.size()+" Records");
		}
		db.close();
		return records;
	}

	public void deleteRecords() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from " + TABLE_RECORD);
		Log.d("deleteRecords()", "[]");
		getAllRecords(null);
		db.close();
	}

	public void updateRecord(Journal record) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(KEY_SENT, "1");
		db.update(TABLE_RECORD, cv, KEY_GUID + "='" + record.getGuid()+"'", null);
		db.close();
		Log.d("updateRecord", "[id=" + record.getGuid() + "]");
	}
	
	private static final String TABLE_PORTRAIT = "Portrait";
	private static final String KEY_INTERNAL_CODE = "internalCode";
	private static final String KEY_PRINTED_CODE = "printedCode";
	private static final String KEY_PORTRAIT = "portrait";

	private static final String KEY_ACCESS = "access";
	private static final String KEY_CAMO_EXPIRATION = "camoExpiration";
	private static final String KEY_EXPIRATION = "expiration";

	//private static final String[] COLUMNS1 = { KEY_INTERNAL_CODE, KEY_PRINTED_CODE, KEY_PORTRAIT};
	
	public void addPortrait(Portrait portrait) {
		Log.d("addPortrait", portrait.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(KEY_INTERNAL_CODE, portrait.getInternalCode());
		values.put(KEY_PRINTED_CODE, portrait.getPrintedCode());
		values.put(KEY_PORTRAIT, portrait.getPortrait());
		values.put(KEY_NAME, portrait.getName());

		values.put(KEY_ACCESS, portrait.getAccess());
		values.put(KEY_CAMO_EXPIRATION, portrait.getCamoExpiration());
		values.put(KEY_EXPIRATION, portrait.getExpiration());

		db.insert(TABLE_PORTRAIT, null, values);
		db.close();
	}
	public void deletePortraits() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from " + TABLE_PORTRAIT);
		Log.d("deletePortraits()", "[]");
		db.close();
	}
	public Portrait getPortrait(String code) {
		Portrait portrait = null;
		String query = "SELECT * FROM " + TABLE_PORTRAIT + " where "+ KEY_INTERNAL_CODE +" = '"+code+"' OR "+ KEY_PRINTED_CODE+ "='"+code+"'";
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) {
			portrait = new Portrait(cursor.getString(0),cursor.getString(1),cursor.getString(2), cursor.getString(3), cursor.getString(4),
					cursor.isNull(5)?null:cursor.getString(5),
					cursor.isNull(6)?null:cursor.getString(6));
			Log.d("getPortrait()", portrait.getPrintedCode());
		}
		db.close();
		return portrait;
	}
}

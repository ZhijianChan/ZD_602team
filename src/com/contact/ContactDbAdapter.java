package com.contact;

import android.content.Context;
import net.sqlcipher.Cursor;
import net.sqlcipher.MatrixCursor;
import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import net.sqlcipher.database.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteStatement;
import android.util.Log;
import java.io.File;

import com.util.ChineseUtils;

/**
 * ContactDbAdapter Singleton Version
 *
 * @author YZ Li
 * @since   2015.04.21
 */

public class ContactDbAdapter {
	private static final String DATABASE_NAME = "contact.db";
	private static String DATABASE_PATH = "";
	private static int DATABASE_VERSION = 1;
	
	public static final String TABLE_NAME = "NAME";
    public static final String TABLE_REMARK = "REMARK";
	public static final String TABLE_TEL = "TEL";
	public static final String TABLE_ADDR = "ADDR";
    public static final String TABLE_SEARCH = "SEARCH";
    
    public static final String TABLE_META_TAG = "METATAG";
    public static final String TABLE_TAG = "TAG";
	
	public static final String KEY_ID = "id";
    
	public static final String KEY_NAME = "name";
    public static final String KEY_PY = "py";
	public static final String KEY_PY_SHORT = "spy";
    
	public static final String KEY_REMARK = "remark";
	public static final String KEY_TEL = "tel";
	public static final String KEY_ADDR = "addr";
    
    public static final String KEY_TAG_ID = "tag_id";
    public static final String KEY_TAG_NAME = "tag_name";
   
    public static final String KEY_CONTENT = "content";
    public static final String KEY_TYPE = "type";
    
    private static final String SEL_CONTACT = "SELECT " + KEY_ID + ", " + KEY_NAME + ", " + KEY_PY_SHORT + " FROM " + TABLE_NAME + " ORDER BY " + KEY_PY_SHORT;
    private static final String SEL_NAMEREMARK_ID = "SELECT " + KEY_NAME + ", " + KEY_REMARK + " FROM " + TABLE_NAME + " NATURAL JOIN " + TABLE_REMARK + " WHERE " + KEY_ID + " = ?";
	private static final String SEL_TAG_ID = "SELECT " + KEY_TAG_ID + ", " + KEY_TAG_NAME + " FROM " + TABLE_TAG + " NATURAL JOIN " + TABLE_META_TAG + " WHERE " + KEY_ID + " = ?";
    private static final String SEL_TEL_ID = "SELECT " + KEY_TEL + " FROM " + TABLE_TEL  + " WHERE " + KEY_ID + " = ?";
	private static final String SEL_ADDR_ID = "SELECT " + KEY_ADDR + " FROM " + TABLE_ADDR + " WHERE " + KEY_ID + " = ?";
    private static final String SEL_METATAG = "SELECT * FROM " + TABLE_META_TAG;
	private static final String SEL_CONTACT_TAGID = "SELECT " + KEY_ID + "," + KEY_NAME + ", " + KEY_PY_SHORT + " FROM " + TABLE_TAG + " NATURAL JOIN " + TABLE_NAME + " WHERE " + KEY_TAG_ID + " = ?";
	private static final String SEL_SEARCH = "SELECT * FROM " + TABLE_SEARCH + " WHERE " + KEY_CONTENT + " MATCH \'";
	
	private static final String INS_NAME = "INSERT INTO " + TABLE_NAME + " VALUES(NULL,?,?,?)";
	private static final String INS_REMARK = "INSERT INTO " + TABLE_REMARK + " VALUES(?,?)";
	private static final String INS_TAG = "INSERT INTO " + TABLE_META_TAG + " VALUES(NULL,?)";
	private static final String ATT_TAG = "INSERT INTO " + TABLE_TAG + " VALUES(?,?)";
	private static final String ATT_TEL = "INSERT INTO " + TABLE_TEL + " VALUES(?,?)";
	private static final String ATT_ADDR = "INSERT INTO " + TABLE_ADDR + " VALUES(?,?)";
	
	private static final String UP_NAME = "UPDATE " + TABLE_NAME + " SET " + KEY_NAME + "=?, " + KEY_PY + "=?, " + KEY_PY_SHORT + "=? WHERE " + KEY_ID + "=?";
	private static final String UP_REMARK = "UPDATE " + TABLE_REMARK + " SET " + KEY_REMARK + "=? WHERE " + KEY_ID + "=?";
	private static final String UP_TEL = "UPDATE " + TABLE_TEL + " SET " + KEY_TEL + "=? WHERE " + KEY_ID + "=? AND " + KEY_TEL + "=?";
	private static final String UP_ADDR = "UPDATE " + TABLE_ADDR + " SET " + KEY_ADDR + "=? WHERE " + KEY_ID + "=? AND " + KEY_ADDR + "=?";
	
	private static final String DEL_NAME = "DELETE FROM " + TABLE_NAME + " WHERE " + KEY_ID + "=?";
	private static final String DEL_REMARK = "DELETE FROM " + TABLE_REMARK + " WHERE " + KEY_ID + "=?";
	private static final String DEL_TAG = "DELETE FROM " + TABLE_META_TAG + " WHERE " + KEY_TAG_ID + "=?";
	private static final String DET_TAG = "DELETE FROM " + TABLE_TAG + " WHERE " + KEY_ID + "=? AND " + KEY_TAG_ID + "=?";
	private static final String DEL_TEL = "DELETE FROM " + TABLE_TEL + " WHERE " + KEY_ID + "=? AND " + KEY_TEL + "=?";
	private static final String DEL_ADDR = "DELETE FROM " + TABLE_ADDR + " WHERE " + KEY_ID + "=? AND " + KEY_ADDR + "=?";
	
	private static SQLiteStatement stmtInsName = null;
	private static SQLiteStatement stmtInsRemark = null;
	private static SQLiteStatement stmtInsTag = null;
	private static SQLiteStatement stmtAttTag = null;
	private static SQLiteStatement stmtAttTel = null;
	private static SQLiteStatement stmtAttAddr = null;
	private static SQLiteStatement stmtUpName = null;
	private static SQLiteStatement stmtUpRemark = null;
	private static SQLiteStatement stmtUpTel = null;
	private static SQLiteStatement stmtUpAddr = null;
	private static SQLiteStatement stmtDelName = null;
	private static SQLiteStatement stmtDelRemark = null;
	private static SQLiteStatement stmtDelTag = null;
	private static SQLiteStatement stmtDetTag = null;
	private static SQLiteStatement stmtDelTel = null;
	private static SQLiteStatement stmtDelAddr = null;
	
	private static volatile ContactDbAdapter cDbAdapter = null;
	private static SQLiteDatabase mDb;
	private static DatabaseHelper mDbHelper;
	private static ChineseUtils chUtils;
	private static String initSQL;
	private Context mctx;
	
	private static int Debug_Mode = 0;
	
    /**
	  * Singleton getInstance
	  */
	public static ContactDbAdapter getInstance(Context ctx) {
		if (cDbAdapter == null) {
			synchronized(ContactDbAdapter.class) {
				if (cDbAdapter == null) {
					cDbAdapter = new ContactDbAdapter(ctx);
				}
			}
		}
		return cDbAdapter;
	}
	
	static {
		System.loadLibrary("sqlib");
	}
	private native String initSQL(Context ctx);
	
    /**
	  * Singleton Constructor
	  */
	private ContactDbAdapter(Context ctx) {
		mctx = ctx;
		SQLiteDatabase.loadLibs(mctx);
		
		if (android.os.Build.VERSION.SDK_INT >= 17) {
			DATABASE_PATH = mctx.getApplicationInfo().dataDir + "/databases/";
		}
		else {
			DATABASE_PATH = mctx.getFilesDir().getPath() + mctx.getPackageName() + "/databases/";
		}
		// Create the parent folder of the db
		File dbPath = new File(DATABASE_PATH);
		if (!dbPath.exists()) {
			dbPath.mkdirs();
		}
		chUtils = new ChineseUtils();
		initSQL = initSQL(mctx);
		Log.i("initSQL", initSQL);
	}
	
    public Boolean isOpen() {
		if (mDb != null && mDb.isOpen())
			return true;
		else
			return false;
	}
    
	public ContactDbAdapter open() throws SQLException {
        if (!isOpen()) {
            Log.i("ContactDbAdapter", "[database opens]");
            mDbHelper = DatabaseHelper.getInstance(mctx, DATABASE_NAME, null, DATABASE_VERSION);
            // initSQL is the key
            mDb = mDbHelper.getWritableDatabase(initSQL);
            stmtPreCompile();
        }
        return this;
	}
	
    /**
	  * Pre Compile SQLite Statement for insert, update and delete
	  */
	public void stmtPreCompile() {
		stmtInsName = mDb.compileStatement(INS_NAME);
		stmtInsRemark = mDb.compileStatement(INS_REMARK);
		stmtInsTag = mDb.compileStatement(INS_TAG);
		stmtAttTag = mDb.compileStatement(ATT_TAG);
		stmtAttTel = mDb.compileStatement(ATT_TEL);
		stmtAttAddr = mDb.compileStatement(ATT_ADDR);
		stmtUpName = mDb.compileStatement(UP_NAME);
		stmtUpRemark = mDb.compileStatement(UP_REMARK);
		stmtUpTel = mDb.compileStatement(UP_TEL);
		stmtUpAddr = mDb.compileStatement(UP_ADDR);
		stmtDelName = mDb.compileStatement(DEL_NAME);
		stmtDelRemark = mDb.compileStatement(DEL_REMARK);
		stmtDelTag = mDb.compileStatement(DEL_TAG);
		stmtDetTag = mDb.compileStatement(DET_TAG);
		stmtDelTel = mDb.compileStatement(DEL_TEL);
		stmtDelAddr = mDb.compileStatement(DEL_ADDR);
	}
	
	public static void enableDebugMode() {
		Debug_Mode = 1;
	}
	
	public static void disableDebugMode() {
		Debug_Mode = 0;
	}
	
	 /**
	  * Check the state of DeBug_Mode 
	  * @return  true  under debug mode
	  * @return  false  not under debug mode
	  */
	public static  Boolean checkDebugMode() {
		if (Debug_Mode == 1)
			return true;
		else
			return false;
	}
	
	public void close() {
		Log.i("ContactDbAdapter", "[database closes]");
		mDbHelper.close();
	}
	
	/* ====================================Query Begin====================================*/
	/**
	 * List all contact
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @return  Cursor: {id, name, spy}
	 */
	public Cursor getContact() {
	  if (Debug_Mode == 1) {
		  String[] col = new String[] {KEY_ID, KEY_NAME, KEY_PY_SHORT};
		  MatrixCursor cur_ = new MatrixCursor(col);
		  cur_.addRow(new Object[] {1, "测试", "cs"});
		  cur_.addRow(new Object[] {2, "Test", "Test"});
		  return cur_;
	  }

	  Cursor cur = mDb.rawQuery(SEL_CONTACT, null);
	  
	  if (!cur.moveToFirst()) {
		  cur.close();
		  return null;
	  }
	  
	  return cur;
	}
	
    
	/**
	 * Get name and remark related to person
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @return  Cursor: {name, remark}
	 */
	public Cursor getNameRemarkByPersonId(long id) {
		if(Debug_Mode == 1) {
			String[] col = new String[] {KEY_NAME, KEY_REMARK};
			MatrixCursor cur_ = new MatrixCursor(col);
			
			if (id == 1)
			  cur_.addRow(new Object[] {"测试", "这是一次很认真的测试"});
			else
			  cur_.addRow(new Object[] {"Test", "a serious test"});
			return cur_;
		}
		
		Cursor cur = mDb.rawQuery(SEL_NAMEREMARK_ID, new String[]{Long.toString(id)});
		
		if (!cur.moveToFirst()) {
			cur.close();
			return null;
		}
		
		return cur;
	}
	
	
	/**
	 * Get tags related to person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @return  Cursor: {tag_id, tag_name}
	 */
	public Cursor getTagByPersonId(long id) {
		if (Debug_Mode == 1) {
			String[] col = new String[] {KEY_TAG_ID, KEY_TAG_NAME};
			MatrixCursor cur_ = new MatrixCursor(col);
			
			if (id == 1)
			  cur_.addRow(new Object[] {1, "标签"});
			else
			  cur_.addRow(new Object[] {2, "Tag"});
			
			return cur_;
		}
		
		Cursor cur = mDb.rawQuery(SEL_TAG_ID, new String[]{Long.toString(id)});
		
		if (!cur.moveToFirst()) {
			cur.close();
			return null;
		}
		
		return cur;
	}

	
	/**
	 * Get telephones related to person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @return  Cursor: {tel}
	 */
	public Cursor getTelByPersonId(long id) {
		if (Debug_Mode == 1) {
			String[] col = new String[] {KEY_TEL};
			MatrixCursor cur_ = new MatrixCursor(col);
			cur_.addRow(new Object[] {"4008517517"});
			return cur_;
		}
		
		Cursor cur = mDb.rawQuery(SEL_TEL_ID, new String[]{Long.toString(id)});
		
		if (!cur.moveToFirst()) {
			cur.close();
			return null;
		}
		
		return cur;
	}

	
	/**
	 * Get address related to person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @return  Cursor: {addr}
	 */
	public Cursor getAddrByPersonId(long id) {
		if (Debug_Mode == 1) {
			String[] col = new String[] {KEY_ADDR};
			MatrixCursor cur_ = new MatrixCursor(col);
			
			if (id == 1)
			  cur_.addRow(new Object[] {"广东省广州市番禺区中山大学"});
			else
			  cur_.addRow(new Object[] {"SYSU, Panyu, Guangzhou, Guangdong"});
			
			return cur_;
		}
		
		Cursor cur = mDb.rawQuery(SEL_ADDR_ID, new String[]{Long.toString(id)});
		
		if (!cur.moveToFirst()) {
			cur.close();
			return null;
		}
		
		return cur;
	}
	
	
	/**
	 * List all Tags
	 * 
	 * @return  Cursor: {tag_id, tag}
	 */
	public Cursor getTag() {
		if (Debug_Mode == 1) {
			String[] col = new String[] {KEY_TAG_ID, KEY_TAG_NAME};
			MatrixCursor cur_ = new MatrixCursor(col);
			cur_.addRow(new Object[] {1, "标签"});
			cur_.addRow(new Object[] {2, "Tag"});
			return cur_;
		}
		
		Cursor cur = mDb.rawQuery(SEL_METATAG, null);
		
		if (!cur.moveToFirst()) {
			cur.close();
			return null;
		}
		
		return cur;
	}
	
	
	/**
	 * List all People related to a tag
	 * 
	 * @param   tag_id
	 * @return  Cursor: {id, name, spy}
	 */
	public Cursor getPeopleByTagId(long tag_id) {
		if (Debug_Mode == 1) {
			String[] col = new String[] {KEY_ID, KEY_NAME, KEY_PY_SHORT};
			MatrixCursor cur_ = new MatrixCursor(col);
			if (tag_id == 1)
			  cur_.addRow(new Object[] {1, "测试", "cs"});
			else
			  cur_.addRow(new Object[] {2, "Test", "test"});
			return cur_;
		}
		
		Cursor cur = mDb.rawQuery(SEL_CONTACT_TAGID, new String[]{Long.toString(tag_id)});
		
		if (!cur.moveToFirst()) {
			cur.close();
			return null;
		}
		
		return cur;
	}
	/* ====================================Query End======================================*/

    
	/* ====================================Insert Begin===================================*/
	/**
	 * Insert name and remark into table_name,
	 * and return a specific id associated with the person
	 * 
	 * @author  YZ Li 
	 * @since   2015.5.4
	 * @param   name
	 * @param   remark
	 * @return  id
	 */
	public long insertNameRemark(String new_name, String remark) {
		if (Debug_Mode == 1) {
			return 1;
		}
		
		long id = -1;
		stmtInsName.clearBindings();
		stmtInsName.bindString(1, new_name);
		stmtInsName.bindString(2, chUtils.getPinYinStr(new_name));
		stmtInsName.bindString(3, chUtils.getShortPinYinStr(new_name));
		id = stmtInsName.executeInsert();
		
		stmtInsRemark.clearBindings();
		stmtInsRemark.bindLong(1, id);
		stmtInsRemark.bindString(2, remark);
		stmtInsRemark.executeInsert();
		
		return id;
	}
	
	
	/**
	 * Insert a new tag to meta_tag
	 * 
	 * @author  YZ Li 
	 * @since   2015.5.4
	 * @param   new_tag
	 * @return  tag_id
	 */
	public long insertTag(String new_tag) {
		if (Debug_Mode == 1) {
			return 1;
		}
		
		long tag_id = -1;
		stmtInsTag.clearBindings();
		stmtInsTag.bindString(1, new_tag);
		tag_id = stmtInsTag.executeInsert();
		
		return tag_id;
	}
	
	
	/**
	 * Attach a tag to the person
	 * @author  YZ Li 
	 * @since   2015.5.4
	 * @param   id
	 * @param   tag_id
	 * @return  void
	 */
	public void attachTagToPerson(long id, long tag_id) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtAttTag.clearBindings();
		stmtAttTag.bindLong(1, id);
		stmtAttTag.bindLong(2, tag_id);
		stmtAttTag.executeInsert();
	}
	

	/**
	 * Attach tag to people
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   tag_id
	 * @param   PersonIds
	 * @return  void
	 */
	public void attachTagToPeople(long tag_id, long[] PeopleId) {
		if (Debug_Mode == 1) {
			return;
		}
		mDb.beginTransaction();
		try {
			for (int i = 0; i < PeopleId.length; ++i) {
				attachTagToPerson(PeopleId[i], tag_id);
			}
			mDb.setTransactionSuccessful();
		}
		catch(Exception e){
			Log.i("attTagToPeopleErr", e.getMessage());
		}
		finally {
			mDb.endTransaction();
		}
	}
	
	
	/**
	 * Add a new telephone to the person
	 * 
	 * @author  YZ Li 
	 * @since   2015.5.4
	 * @param   id
	 * @param   tel
	 * @return  void
	 */
	public void addTelToPerson(long id, String tel) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtAttTel.clearBindings();
		stmtAttTel.bindLong(1, id);
		stmtAttTel.bindString(2, tel);
		stmtAttTel.executeInsert();
	}
	
	
	/**
	 * Add a new address to the person
	 * @author  YZ Li 
	 * @since   2015.5.4
	 * @param   id
	 * @param   addr
	 * @return  void
	 */
	public void addAddrToPerson(long id, String addr) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtAttAddr.clearBindings();
		stmtAttAddr.bindLong(1, id);
		stmtAttAddr.bindString(2, addr);
		stmtAttAddr.executeInsert();
	}
	/* ====================================Insert End===================================*/

    
	/* ====================================Update Begin===================================*/
	/**
	 * Update name of person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @param   new_name
	 * @return  void
	 */
	public void updateNameByPersonId(long id, String new_name) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtUpName.clearBindings();
		stmtUpName.bindString(1, new_name);
		stmtUpName.bindString(2, chUtils.getPinYinStr(new_name));
		stmtUpName.bindString(3, chUtils.getShortPinYinStr(new_name));
		Log.i("updateName", chUtils.getPinYinStr(new_name));
		Log.i("updateName", chUtils.getShortPinYinStr(new_name));
		stmtUpName.bindLong(4, id);
		stmtUpName.executeUpdateDelete();
	}
	
	
	/**
	 * Update remark of person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @param   new_remark
	 * @return  void
	 */
	public void updateRemarkByPersonId(long id, String new_remark) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtUpRemark.clearBindings();
		stmtUpRemark.bindString(1, new_remark);
		stmtUpRemark.bindLong(2, id);
		stmtUpRemark.executeUpdateDelete();
	}
	
	
	/**
	 * Update telephone of person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @param   old_tel
	 * @param   new_tel
	 * @return  void
	 */
	public void updateTelByPersonId(long id, String old_tel, String new_tel) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtUpTel.clearBindings();
		stmtUpTel.bindString(1, new_tel);
		stmtUpTel.bindLong(2, id);
		stmtUpTel.bindString(3, old_tel);
		stmtUpTel.executeUpdateDelete();
	}
	
	
	/**
	 * Update address of person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @param   old_addr
	 * @param   new_addr
	 * @return  void
	 */
	public void updateAddressByPersonId(long id, String old_addr, String new_addr) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtUpAddr.clearBindings();
		stmtUpAddr.bindString(1, new_addr);
		stmtUpAddr.bindLong(2, id);
		stmtUpAddr.bindString(3, old_addr);
		stmtUpAddr.executeUpdateDelete();
	}
	/* ====================================Update End=====================================*/

    
	/* ====================================Delete Begin===================================*/
	/**
	 * Delete name, remark, tags, telephone, address related to the person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @return  void
	 */
	public void deleteContactItem(long id) {
		if (Debug_Mode == 1) {
			return;
		}
		String delTags = "DELETE FROM " + TABLE_TAG + " WHERE " + KEY_ID + "=" + Long.toString(id);
		String delTels = "DELETE FROM " + TABLE_TEL + " WHERE " + KEY_ID + "=" + Long.toString(id);
		String delAddrs = "DELETE FROM " + TABLE_ADDR + " WHERE " + KEY_ID + "=" + Long.toString(id); 
		mDb.beginTransaction();
		try {
			stmtDelName.clearBindings();
			stmtDelName.bindLong(1, id);
			stmtDelRemark.clearBindings();
			stmtDelRemark.bindLong(1, id);
			stmtDelName.executeUpdateDelete();
			stmtDelRemark.executeUpdateDelete();
			mDb.execSQL(delTags);
			mDb.execSQL(delTels);
			mDb.execSQL(delAddrs);
			mDb.setTransactionSuccessful();
		}
		catch(Exception e) {
			Log.i("delContactItemErr", e.getMessage());
		}
		finally {
			mDb.endTransaction();
		}
	}
	
	
	/**
	 * Delete tag from meta-tag
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   tag_id
	 * @return  void
	 */
	public void deleteTag(long tag_id) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtDelTag.clearBindings();
		stmtDelTag.bindLong(1, tag_id);
		stmtDelTag.executeUpdateDelete();
	}
	
	
	/**
	 * Detach tag from person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @param   tag_id
	 * @return  void
	 */
	public void detachTagfromPerson(long id, long tag_id) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtDetTag.clearBindings();
		stmtDetTag.bindLong(1, id);
		stmtDetTag.bindLong(2, tag_id);
		stmtDetTag.executeUpdateDelete();
	}
	
	
	/**
	 * Delete telephone related to the person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @param   tel
	 * @return  void
	 */
	public void deleteTelByPersonId(long id, String tel) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtDelTel.clearBindings();
		stmtDelTel.bindLong(1, id);
		stmtDelTel.bindString(2, tel);
		stmtDelTel.executeUpdateDelete();
	}
	
	
	/**
	 * Delete address related to the person
	 * 
	 * @author  YZ Li
	 * @since   2015.5.4
	 * @param   id
	 * @param   addr
	 * @return  void
	 */
	public void deleteAddrByPersonId(long id, String addr) {
		if (Debug_Mode == 1) {
			return;
		}
		
		stmtDelAddr.clearBindings();
		stmtDelAddr.bindLong(1, id);
		stmtDelAddr.bindString(2, addr);
		stmtDelAddr.executeUpdateDelete();
	}
	/* ====================================Delete End=====================================*/
	
    
	/* ====================================Search Begin===================================*/
	/**
	 * Full text search
	 * @author  YZ Li
	 * @since   2015.5.5
	 * @param   query
	 * @return  Cursor: {id, content, type}
	 */
	public Cursor getMatched(String query) {
		if (Debug_Mode == 1) {
			String[] col = new String[] {KEY_ID, KEY_CONTENT, KEY_TYPE};
			MatrixCursor cur_ = new MatrixCursor(col);
			cur_.addRow(new Object[]{1, "测试", TABLE_NAME});
			cur_.addRow(new Object[]{1, "ceshi", KEY_PY});
			cur_.addRow(new Object[]{1, "cs", KEY_PY_SHORT});
			cur_.addRow(new Object[]{1, "这是一次很认真的测试", TABLE_REMARK});
			cur_.addRow(new Object[]{1, "标签", TABLE_TAG});
			cur_.addRow(new Object[]{1, "4008517517", TABLE_TEL});
			cur_.addRow(new Object[]{1, "广东省广州市番禺区中山大学", TABLE_ADDR});
			
			cur_.addRow(new Object[]{2, "Test", TABLE_NAME});
			cur_.addRow(new Object[]{2, "test", KEY_PY});
			cur_.addRow(new Object[]{2, "test", KEY_PY_SHORT});			
			cur_.addRow(new Object[]{2, "a serious test", TABLE_REMARK});
			cur_.addRow(new Object[]{2, "Tag", TABLE_TAG});
			cur_.addRow(new Object[]{2, "4008517517", TABLE_TEL});
			cur_.addRow(new Object[]{2, "SYSU, Panyu, Guangzhou, Guangdong", TABLE_ADDR});
			
			return cur_;
		}
		
		Cursor cur = mDb.rawQuery(SEL_SEARCH + query.trim() + "*\'", null);
		if (!cur.moveToFirst()) {
			cur.close();
			return null;
		}
		
		return cur;
	}
	/* ====================================Search End===================================*/
	
    /**
	  * DatabaseHelper Singleton Version
      * @author  YZ Li
      * @since   2015.5.4
	  */
    static class DatabaseHelper extends SQLiteOpenHelper {
		private volatile static DatabaseHelper mInstance = null;
		
		public static DatabaseHelper getInstance(Context ctx, String name, CursorFactory factory, int version) {
			if (mInstance == null) {
				synchronized(DatabaseHelper.class) {
					if (mInstance == null) {
						mInstance = new DatabaseHelper(ctx, name, factory, version);
					}
				}
			}
			return mInstance;
		}
		
		private DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		private static final String NAME_CREATE = "CREATE TABLE " + TABLE_NAME +
				"(" +
				KEY_ID + " INTEGER PRIMARY KEY, " +
				KEY_NAME + " TEXT, " + 
                KEY_PY + " TEXT, " + 
                KEY_PY_SHORT + " TEXT);";
		
		private static final String TEL_CREATE = "CREATE TABLE " + TABLE_TEL +
				"(" +
				KEY_ID + " INTEGER, " +
				KEY_TEL + " TEXT, PRIMARY KEY(" + 
                KEY_ID + ", " + 
                KEY_TEL + "));";
		
		private static final String ADDR_CREATE = "CREATE TABLE " + TABLE_ADDR +
				"(" +
				KEY_ID + " INTEGER, " +
				KEY_ADDR + " TEXT, PRIMARY KEY(" + 
                KEY_ID + ", " + 
                KEY_ADDR + "));";
        
        private static final String REMARK_CREATE = "CREATE TABLE " + TABLE_REMARK +
				"(" +
				KEY_ID + " INTEGER, " +
				KEY_REMARK + " TEXT, PRIMARY KEY(" + 
                KEY_ID + ", " + 
                KEY_REMARK + "));";
         
        private static final String META_TAG_CREATE = "CREATE TABLE " + TABLE_META_TAG +
				"(" +
				KEY_TAG_ID + " INTEGER PRIMARY KEY, " +
				KEY_TAG_NAME + " TEXT);";
                
        private static final String TAG_CREATE = "CREATE TABLE " + TABLE_TAG +
				"(" +
				KEY_ID + " INTEGER, " +
				KEY_TAG_ID + " TEXT, PRIMARY KEY(" + 
                KEY_ID + ", " + 
                KEY_TAG_ID + "));";
                
        private static final String SEARCH_CREATE = "CREATE VIRTUAL TABLE " + TABLE_SEARCH +
				" USING FTS4(" +
				KEY_ID + ", " +
				KEY_CONTENT + ", " + 
                KEY_TYPE + ", " + 
                "tokenize=icu zh);";
                
        private static final String IDX_NAME_CREATE = "CREATE INDEX id_name ON " + TABLE_NAME +
                "(" + KEY_ID + ", " + KEY_NAME + ");";
         
        private static final String IDX_TEL_CREATE = "CREATE INDEX id_tel ON " + TABLE_TEL +
                "(" + KEY_ID + ", " + KEY_TEL + ");";
        
        private static final String IDX_ADDR_CREATE = "CREATE INDEX id_addr ON " + TABLE_ADDR +
                "(" + KEY_ID + ", " + KEY_ADDR + ");";
        
        private static final String IDX_REMARK_CREATE = "CREATE INDEX id_remark ON " + TABLE_REMARK +
                "(" + KEY_ID + ", " + KEY_REMARK + ");";
        
        private static final String IDX_TAG_ID_CREATE = "CREATE INDEX tag_id_idx ON " + TABLE_TAG +
                "(" + KEY_ID + ");";
        
        private static final String IDX_TAG_TAGID__CREATE = "CREATE INDEX tag_tagid_idx ON " + TABLE_TAG +
                "(" + KEY_TAG_ID + ");";
        
        private static final String IDX_METATAG__CREATE = "CREATE INDEX metatag_idx ON " + TABLE_META_TAG +
                "(" + KEY_TAG_ID + ");";
                
        private static final String TRI_NAME_INSERT_BODY = " BEGIN INSERT INTO " + 
                TABLE_SEARCH + " VALUES(NEW." + KEY_ID + ", NEW." + KEY_NAME + ", \"NAME\");" + 
                "INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_ID + ", NEW." + KEY_PY + ", \"KEY_PY\");" + 
                "INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_ID + ", NEW." + KEY_PY_SHORT + ", \"KEY_PY_SHORT\");"  + 
                "END;";
                
        private static final String TRI_NAME_DELETE_BODY = " BEGIN DELETE FROM " + 
                TABLE_SEARCH + " WHERE " + KEY_TYPE + 
                " MATCH " + "\'" + KEY_ID + ":\'||OLD." + KEY_ID + "||\' NAME\';" + 
                "END;";
                
        private static final String TRI_NAME_AI_CREATE = "CREATE TRIGGER NAME_AI AFTER INSERT ON " + 
                TABLE_NAME + TRI_NAME_INSERT_BODY;
        
        private static final String TRI_NAME_BD_CREATE = "CREATE TRIGGER NAME_BD BEFORE DELETE ON " + 
                TABLE_NAME + TRI_NAME_DELETE_BODY;
        
        private static final String TRI_NAME_BU_CREATE = "CREATE TRIGGER NAME_BU BEFORE UPDATE ON " + 
                TABLE_NAME + TRI_NAME_DELETE_BODY;
        
        private static final String TRI_NAME_AU_CREATE = "CREATE TRIGGER NAME_AU AFTER UPDATE ON " + 
                TABLE_NAME + TRI_NAME_INSERT_BODY;
                
        private static final String TRI_TEL_AI_CREATE = "CREATE TRIGGER TEL_AI AFTER INSERT ON " + 
                TABLE_TEL + " BEGIN INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_ID + ", NEW." + KEY_TEL + ", \"TEL\");" + 
                "END;";
                
        private static final String TRI_TEL_BD_CREATE = "CREATE TRIGGER TEL_BD BEFORE DELETE ON " + 
                TABLE_TEL + " BEGIN DELETE FROM " + TABLE_SEARCH + " WHERE " + KEY_CONTENT + 
                " MATCH " + "\'" + KEY_ID + ":\'||OLD." + KEY_ID + "||\' " + KEY_TYPE + ":TEL \'||OLD." + KEY_TEL + ";" + 
                "END;";
        
        private static final String TRI_TEL_BU_CREATE = "CREATE TRIGGER TEL_BU BEFORE UPDATE ON " + 
                TABLE_TEL + " BEGIN DELETE FROM " + TABLE_SEARCH + " WHERE " + KEY_CONTENT + 
                " MATCH " + "\'" + KEY_ID + ":\'||OLD." + KEY_ID + "||\' " + KEY_TYPE + ":TEL \'||OLD." + KEY_TEL + ";" + 
                "END;";
                
        private static final String TRI_TEL_AU_CREATE = "CREATE TRIGGER TEL_AU AFTER UPDATE ON " + 
                TABLE_TEL + " BEGIN INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_ID + ", NEW." + KEY_TEL + ", \"TEL\");" + 
                "END;";
                
        private static final String TRI_ADDR_AI_CREATE = "CREATE TRIGGER ADDR_AI AFTER INSERT ON " + 
                TABLE_ADDR + " BEGIN INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_ID + ", NEW." + KEY_ADDR + ", \"ADDR\");" + 
                "END;";
                
        private static final String TRI_ADDR_BD_CREATE = "CREATE TRIGGER ADDR_BD BEFORE DELETE ON " + 
                TABLE_ADDR + " BEGIN DELETE FROM " + TABLE_SEARCH + " WHERE " + KEY_CONTENT + 
                " MATCH " + "\'" + KEY_ID + ":\'||OLD." + KEY_ID + "||\' " + KEY_TYPE + ":ADDR \'||OLD." + KEY_ADDR + ";" + 
                "END;";
        
        private static final String TRI_ADDR_BU_CREATE = "CREATE TRIGGER ADDR_BU BEFORE UPDATE ON " + 
                TABLE_ADDR + " BEGIN DELETE FROM " + TABLE_SEARCH + " WHERE " + KEY_CONTENT + 
                " MATCH " + "\'" + KEY_ID + ":\'||OLD." + KEY_ID + "||\' " + KEY_TYPE + ":ADDR \'||OLD." + KEY_ADDR + ";" + 
                "END;";
                
        private static final String TRI_ADDR_AU_CREATE = "CREATE TRIGGER ADDR_AU AFTER UPDATE ON " + 
                TABLE_ADDR + " BEGIN INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_ID + ", NEW." + KEY_ADDR + ", \"ADDR\");" + 
                "END;";

        private static final String TRI_REMARK_AI_CREATE = "CREATE TRIGGER REMARK_AI AFTER INSERT ON " + 
                TABLE_REMARK + " BEGIN INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_ID + ", NEW." + KEY_REMARK + ", \"REMARK\");" + 
                "END;";
                
        private static final String TRI_REMARK_BD_CREATE = "CREATE TRIGGER REMARK_BD BEFORE DELETE ON " + 
                TABLE_REMARK + " BEGIN DELETE FROM " + TABLE_SEARCH + " WHERE " + KEY_CONTENT + 
                " MATCH " + "\'" + KEY_ID + ":\'||OLD." + KEY_ID + "||\' " + KEY_TYPE + ":REMARK \'||OLD." + KEY_REMARK + ";" + 
                "END;";
        
        private static final String TRI_REMARK_BU_CREATE = "CREATE TRIGGER REMARK_BU BEFORE UPDATE ON " + 
                TABLE_REMARK + " BEGIN DELETE FROM " + TABLE_SEARCH + " WHERE " + KEY_CONTENT + 
                " MATCH " + "\'" + KEY_ID + ":\'||OLD." + KEY_ID + "||\' " + KEY_TYPE + ":REMARK \'||OLD." + KEY_REMARK + ";" + 
                "END;";
                
        private static final String TRI_REMARK_AU_CREATE = "CREATE TRIGGER REMARK_AU AFTER UPDATE ON " + 
                TABLE_REMARK + " BEGIN INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_ID + ", NEW." + KEY_REMARK + ", \"REMARK\");" + 
                "END;";
                
        private static final String TRI_METATAG_AI_CREATE = "CREATE TRIGGER METATAG_AI AFTER INSERT ON " + 
                TABLE_META_TAG + " BEGIN INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_TAG_ID + ", NEW." + KEY_TAG_NAME + ", \"TAG\");" + 
                "END;";
                
        private static final String TRI_METATAG_BD_CREATE = "CREATE TRIGGER METATAG_BD BEFORE DELETE ON " + 
                TABLE_META_TAG + " BEGIN DELETE FROM " + TABLE_SEARCH + " WHERE " + KEY_TYPE + 
                " MATCH " + "\'" + KEY_ID + ":\'||OLD." + KEY_TAG_ID + "||\' TAG\';" + 
                " DELETE FROM " + TABLE_TAG + " WHERE " + KEY_TAG_ID + " = " + "OLD." + KEY_TAG_ID + ";" + 
                "END;";
                
        private static final String TRI_METATAG_BU_CREATE = "CREATE TRIGGER METATAG_BU BEFORE UPDATE ON " + 
                TABLE_META_TAG + " BEGIN DELETE FROM " + TABLE_SEARCH + " WHERE " + KEY_TYPE + 
                " MATCH " + "\'" + KEY_ID + ":\'||OLD." + KEY_TAG_ID + "||\' TAG\';" + 
                "END;";
                
        private static final String TRI_METATAG_AU_CREATE = "CREATE TRIGGER METATAG_AU AFTER UPDATE ON " + 
                TABLE_META_TAG + " BEGIN INSERT INTO " + TABLE_SEARCH + " VALUES(NEW." + KEY_TAG_ID + ", NEW." + KEY_TAG_NAME + ", \"TAG\");" + 
                "END;";

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i("info", "(create table)");
			db.beginTransaction();
            try {
                String []sql = new String[]{NAME_CREATE, TEL_CREATE, ADDR_CREATE, REMARK_CREATE, META_TAG_CREATE, TAG_CREATE, SEARCH_CREATE,
                                            IDX_NAME_CREATE, IDX_TEL_CREATE, IDX_ADDR_CREATE, IDX_REMARK_CREATE, IDX_METATAG__CREATE, IDX_TAG_ID_CREATE, IDX_TAG_TAGID__CREATE,
                                            TRI_NAME_AI_CREATE, TRI_NAME_AU_CREATE, TRI_NAME_BD_CREATE, TRI_NAME_BU_CREATE,
                                            TRI_TEL_AI_CREATE, TRI_TEL_AU_CREATE, TRI_TEL_BD_CREATE, TRI_TEL_BU_CREATE,
                                            TRI_ADDR_AI_CREATE, TRI_ADDR_AU_CREATE, TRI_ADDR_BD_CREATE, TRI_ADDR_BU_CREATE,
                                            TRI_REMARK_AI_CREATE, TRI_REMARK_AU_CREATE, TRI_REMARK_BD_CREATE, TRI_REMARK_BU_CREATE,
                                            TRI_METATAG_AI_CREATE, TRI_METATAG_AU_CREATE, TRI_METATAG_BD_CREATE, TRI_METATAG_BU_CREATE};
                                                        
                for (int i = 0; i < sql.length; ++i) {
                    db.execSQL(sql[i]);
                }
                db.setTransactionSuccessful();
            }
            catch(Exception e) {
            	Log.i("initDbErr", e.getMessage());
            }
            finally {
                db.endTransaction();
            }
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("info", "(upgrade database from version:" + oldVersion + "to version:" + newVersion + ")");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEL);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADDR);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMARK);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_META_TAG);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH);
			onCreate(db);
		}
	}
}
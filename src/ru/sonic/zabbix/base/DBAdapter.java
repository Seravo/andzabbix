package ru.sonic.zabbix.base;
 
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter
{
    long id = 0;
    public static final String KEY_ROWID = "_id";
    public static final String KEY_TIMEOUT = "Timeout";
    public static final String KEY_SERVER = "Server";
    public static final String KEY_URL = "ServerUrl";
    public static final String KEY_USER = "User";
    public static final String KEY_PASS = "Pass";
    public static final String KEY_BASE_USE = "Base_auth_use";
    public static final String KEY_BASE_LOGIN = "Base_auth_login";
    public static final String KEY_BASE_PASS = "Base_auth_pass";
    //private static final String TAG = "DBAdapter";

    private static final String DATABASE_NAME = "zabbix";
    private static final String DATABASE_TABLE = "servers";
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_CREATE_TABLE =
      "create table "+ DATABASE_TABLE + " (_id integer primary key autoincrement, "
      + KEY_SERVER + " TEXT, " + KEY_URL + " TEXT, " + KEY_USER + " TEXT, " 
      + KEY_PASS + " TEXT, " + KEY_TIMEOUT + " TEXT, " + KEY_BASE_USE + " INTEGER NOT NULL DEFAULT 0, " 
      + KEY_BASE_LOGIN + " TEXT NOT NULL DEFAULT '', " + KEY_BASE_PASS + " TEXT NOT NULL DEFAULT '')";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
 
	private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
 
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DATABASE_CREATE_TABLE);
			
			ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_SERVER, "Zabbix server");
	        initialValues.put(KEY_URL, "http://example.com/zabbix/api_jsonrpc.php");
	        initialValues.put(KEY_USER, "user");
	        initialValues.put(KEY_PASS, "");
	        initialValues.put(KEY_TIMEOUT, "10");
	        initialValues.put(KEY_BASE_USE, 0);
	        initialValues.put(KEY_BASE_LOGIN, "");
	        initialValues.put(KEY_BASE_PASS, "");
	        db.insert(DATABASE_TABLE, null, initialValues);
        }
 
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,int newVersion)
        {
        	if (oldVersion == 1 && newVersion == 2) {
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD "+KEY_BASE_USE+" INTEGER NOT NULL DEFAULT 0 ;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD "+KEY_BASE_LOGIN+" TEXT NOT NULL DEFAULT '';");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD "+KEY_BASE_PASS + " TEXT NOT NULL DEFAULT '';");
        	} else {
                db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
                onCreate(db);
        	}
        }
    }

    public DBAdapter open() throws SQLException
    {
    	try {
    		db = DBHelper.getWritableDatabase();
    		return this;
    	} catch (Exception e) {
    		return null;
    	}
    }
 
    public void close()
    {
        DBHelper.close();
    }

    public void flush()
    {
    	try {
    		db.execSQL("DELETE FROM " + DATABASE_TABLE);
    	} catch (Exception e) {
    	}
    }
    
    public long insertServer(String server, String url, String user, String pass, String timeout, int base_use, String base_login, String base_pass)
    {
    	try {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_SERVER, server);
	        initialValues.put(KEY_URL, url);
	        initialValues.put(KEY_USER, user);
	        initialValues.put(KEY_PASS, pass);
	        initialValues.put(KEY_TIMEOUT, timeout);
	        initialValues.put(KEY_BASE_USE, base_use);
	        initialValues.put(KEY_BASE_LOGIN, base_login);
	        initialValues.put(KEY_BASE_PASS, base_pass);
	        return db.insert(DATABASE_TABLE, null, initialValues);
    	} catch (Exception e) {
    		return -1;
    	}
    }
    
	public boolean updateServer(String oldname, String server, String url, String user, String pass, String timeout, int base_use, String base_login, String base_pass) {
		try {
				ContentValues initialValues = new ContentValues();
		        initialValues.put(KEY_SERVER, server);
		        initialValues.put(KEY_URL, url);
		        initialValues.put(KEY_USER, user);
		        initialValues.put(KEY_PASS, pass);
		        initialValues.put(KEY_TIMEOUT, timeout);
		        initialValues.put(KEY_BASE_USE, base_use);
		        initialValues.put(KEY_BASE_LOGIN, base_login);
		        initialValues.put(KEY_BASE_PASS, base_pass);
				return db.update(DATABASE_TABLE, initialValues, KEY_SERVER + "= '" + oldname + "'", null) > 0;
		} catch (Exception e) {
			return false;
		}
	}
        
    public long getAllEntries() {
        return DatabaseUtils.queryNumEntries(db,DATABASE_TABLE);
    }

    public List<String> selectServer(String serverName) {
    List<String> list = new ArrayList<String>();
          //Cursor cursor = this.db.query(DATABASE_TABLE, new String[] { KEY_SERVER, KEY_URL, KEY_USER, KEY_PASS, KEY_TIMEOUT },
        	//	  KEY_SERVER, new String[] { serverName }, null, null, "_id");
    	Cursor cursor = this.db.rawQuery("SELECT " + KEY_SERVER + ", "+ KEY_URL + ", "+ KEY_USER + ", "
    			+ KEY_PASS + ", "+ KEY_TIMEOUT + ", "+ KEY_BASE_USE+", "+ KEY_BASE_LOGIN +", "+KEY_BASE_PASS+
    			" FROM "+ DATABASE_TABLE + " WHERE "+ KEY_SERVER + " = '" + serverName+ "'", null);
          int serverCol = cursor.getColumnIndex(KEY_SERVER);
          int urlCol = cursor.getColumnIndex(KEY_URL);
          int userCol = cursor.getColumnIndex(KEY_USER);
          int passCol = cursor.getColumnIndex(KEY_PASS);
          int timeoutCol = cursor.getColumnIndex(KEY_TIMEOUT);
          int base_useCol = cursor.getColumnIndex(KEY_BASE_USE);
          int base_loginCol = cursor.getColumnIndex(KEY_BASE_LOGIN);
          int base_passCol = cursor.getColumnIndex(KEY_BASE_PASS);
          
          if (cursor.moveToFirst()) {
             do {
                list.add(cursor.getString(serverCol));
                list.add(cursor.getString(urlCol));
                list.add(cursor.getString(userCol));
                list.add(cursor.getString(passCol));
                list.add(cursor.getString(timeoutCol));
                if (cursor.getInt(base_useCol) == 0)
                	list.add("false");
                else
                	list.add("true");
                list.add(cursor.getString(base_loginCol));
                list.add(cursor.getString(base_passCol));
             } while (cursor.moveToNext());
          }
          if (cursor != null && !cursor.isClosed()) {
             cursor.close();
          }
          return list;
    }
    
    public String getUrl(String serverName) {
       	Cursor cursor = this.db.rawQuery("SELECT " + KEY_URL + " FROM "+ DATABASE_TABLE + " WHERE "+ KEY_SERVER + " = '" + serverName+ "'", null);
        int urlCol = cursor.getColumnIndex(KEY_URL);
        String serverUrl = "";
              if (cursor.moveToFirst()) {
                 do {
                	 serverUrl = cursor.getString(urlCol);
                 } while (cursor.moveToNext());
              }
              if (cursor != null && !cursor.isClosed()) {
                 cursor.close();
              }
        return serverUrl;
    }
    
    public String getLogin(String serverName) {
       	Cursor cursor = this.db.rawQuery("SELECT " + KEY_USER + " FROM "+ DATABASE_TABLE + " WHERE "+ KEY_SERVER + " = '" + serverName+ "'", null);
        int userCol = cursor.getColumnIndex(KEY_USER);
        String user = "";
              if (cursor.moveToFirst()) {
                 do {
                	 user = cursor.getString(userCol);
                 } while (cursor.moveToNext());
              }
              if (cursor != null && !cursor.isClosed()) {
                 cursor.close();
              }
        return user;
    }
    
    public String getPassword(String serverName) {
       	Cursor cursor = this.db.rawQuery("SELECT " + KEY_PASS + " FROM "+ DATABASE_TABLE + " WHERE "+ KEY_SERVER + " = '" + serverName+ "'", null);
        int passCol = cursor.getColumnIndex(KEY_PASS);
        String pass = "";
              if (cursor.moveToFirst()) {
                 do {
                	 pass = cursor.getString(passCol);
                 } while (cursor.moveToNext());
              }
              if (cursor != null && !cursor.isClosed()) {
                 cursor.close();
              }
        return pass;
    }
    
    public int get_use_base_auth(String serverName) {
       	Cursor cursor = this.db.rawQuery("SELECT " + KEY_BASE_USE + " FROM "+ DATABASE_TABLE + " WHERE "+ KEY_SERVER + " = '" + serverName+ "'", null);
        int passCol = cursor.getColumnIndex(KEY_PASS);
        int pass = 0;
              if (cursor.moveToFirst()) {
                 do {
                	 pass = cursor.getInt(passCol);
                 } while (cursor.moveToNext());
              }
              if (cursor != null && !cursor.isClosed()) {
                 cursor.close();
              }
        return pass;
    }
    
    public List<String> selectServerNames() {
        List<String> list = new ArrayList<String>();
        Cursor cursor = this.db.query(DATABASE_TABLE, new String[] { KEY_SERVER },
          null, null, null, null, "_id");
        int serverCol = cursor.getColumnIndex(KEY_SERVER);
        if (cursor.moveToFirst()) {
           do {
              list.add(cursor.getString(serverCol));;
           } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
           cursor.close();
        }
        return list;
    }
    
    public void deleteServer(String serverName) {
    	db.execSQL("DELETE FROM " + DATABASE_TABLE + " WHERE " + KEY_SERVER + " = '" + serverName+"'");
    }
}

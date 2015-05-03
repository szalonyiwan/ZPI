package pl.mf.zpi.matefinder.helper;

/**
 * Created by root on 22.03.15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Tables names
    private static final String TABLE_LOGIN = "login";
    private static final String TABLE_LOCATIONS = "locations";
    private static final String TABLE_SETTINGS = "settings";
    private static final String TABLE_FRIENDS = "friends";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ID_DATABASE = "userID";
    private static final String KEY_LOGIN = "login";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_NAME = "name";
    private static final String KEY_SURNAME = "surname";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_LOCATION = "location";              // location table + login table + friends table
    private static final String KEY_LAT = "lat";                        // location table
    private static final String KEY_LNG = "lng";                        // location table

    //settings table columns name
    private static final String KEY_INTERNET_LIMIT = "internet";
    private static final String KEY_NOTIFICATION_SOUND = "sound";
    private static final String KEY_USER_NAVIGATION = "navigation";
    private static final String KEY_LAYOUT = "layout";
    private static final String KEY_SEARCH_RADIUS = "radius";

    // Friends table column names
    private static final String KEY_FRIEND_ID = "id";
    private static final String KEY_FRIEND_ID_DATABASE = "userID";
    private static final String KEY_FRIEND_LOGIN = "login";
    private static final String KEY_FRIEND_EMAIL = "email";
    private static final String KEY_FRIEND_PHONE = "phone";
    private static final String KEY_FRIEND_NAME = "name";
    private static final String KEY_FRIEND_SURNAME = "surname";
    private static final String KEY_FRIEND_PHOTO = "photo";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ID_DATABASE + " TEXT," + KEY_LOGIN + " TEXT,"
                + KEY_EMAIL + " TEXT," + KEY_PHONE + " TEXT," + KEY_NAME + " TEXT,"
                + KEY_SURNAME + " TEXT," + KEY_PHOTO + " TEXT," + KEY_LOCATION + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LOCATION + " TEXT," + KEY_LAT
                + " TEXT," + KEY_LNG + " TEXT" + ")";
        db.execSQL(CREATE_LOCATIONS_TABLE);

        String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_SETTINGS + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_INTERNET_LIMIT + " INTEGER, " +
                KEY_NOTIFICATION_SOUND + " INTEGER, " + KEY_USER_NAVIGATION + " INTEGER, " + KEY_LAYOUT + " INTEGER, " + KEY_SEARCH_RADIUS + " INTEGER)";
        db.execSQL(CREATE_SETTINGS_TABLE);

        String CREATE_FRIENDS_TABLE = "CREATE TABLE " + TABLE_FRIENDS + "("
                + KEY_FRIEND_ID + " INTEGER PRIMARY KEY," + KEY_FRIEND_ID_DATABASE + " TEXT," + KEY_FRIEND_LOGIN + " TEXT,"
                + KEY_FRIEND_EMAIL + " TEXT," + KEY_FRIEND_PHONE + " TEXT," + KEY_FRIEND_NAME + " TEXT,"
                + KEY_FRIEND_SURNAME + " TEXT," + KEY_FRIEND_PHOTO + " TEXT," + KEY_LOCATION + " TEXT" + ")";
        db.execSQL(CREATE_FRIENDS_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);

        // Create tables again
        onCreate(db);
    }

    public void addLocation(String locationID, String lat, String lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LOCATION, locationID);
        values.put(KEY_LAT, lat);
        values.put(KEY_LNG, lng);
        long id = db.insert(TABLE_LOCATIONS, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New location inserted into sqlite: " + id);
    }

    public void addSettings(String internet, String notification, String navigation, String layout, String radius) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_INTERNET_LIMIT, internet);
        values.put(KEY_NOTIFICATION_SOUND, notification);
        values.put(KEY_USER_NAVIGATION, navigation);
        values.put(KEY_LAYOUT, layout);
        values.put(KEY_SEARCH_RADIUS, radius);
        long id = db.insert(TABLE_LOCATIONS, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New location inserted into sqlite: " + id);
    }

    /**
     * Storing user details in database
     */
    public void addUser(String userID, String login, String email, String phone, String name, String surname, String photo, String location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID_DATABASE, userID);
        values.put(KEY_LOGIN, login);
        values.put(KEY_EMAIL, email);
        values.put(KEY_PHONE, phone);
        values.put(KEY_NAME, name);
        values.put(KEY_SURNAME, surname);
        values.put(KEY_PHOTO, photo);
        values.put(KEY_LOCATION, location);
        // Inserting Row
        long id = db.insert(TABLE_LOGIN, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    public void addFriend(String userID, String login, String email, String phone, String name, String surname, String photo, String location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FRIEND_ID_DATABASE, userID);
        values.put(KEY_FRIEND_LOGIN, login);
        values.put(KEY_FRIEND_EMAIL, email);
        values.put(KEY_FRIEND_PHONE, phone);
        values.put(KEY_FRIEND_NAME, name);
        values.put(KEY_FRIEND_SURNAME, surname);
        values.put(KEY_FRIEND_PHOTO, photo);
        values.put(KEY_LOCATION, location);
        // Inserting Row
        long id = db.insert(TABLE_FRIENDS, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New friend inserted into sqlite: " + id);
    }

    /**
     * Getting friends data from database
     */
    public List<HashMap<String, String>> getFriendsDetails() {
        List<HashMap<String, String>> friends = new ArrayList<HashMap<String, String>>();

        String selectQuery = "SELECT * FROM " + TABLE_FRIENDS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
           // cursor.moveToFirst();
                do {
                    HashMap<String, String> friend = new HashMap<String, String>();
                    friend.put("userID", cursor.getString(1));
                    friend.put("login", cursor.getString(2));
                    friend.put("email", cursor.getString(3));
                    friend.put("phone", cursor.getString(4));
                    friend.put("name", cursor.getString(5));
                    friend.put("surname", cursor.getString(6));
                    friend.put("photo", cursor.getString(7));
                    friend.put("location", cursor.getString(8));
                    friends.add(friend);

                    Log.d(TAG, "Pętla while ");
                }

                while (cursor.moveToNext());
            }


            cursor.close();
            db.close();
            // return friends
            Log.d(TAG, "Fetching friends from Sqlite: " + friends.toString());
            return friends;
        }

    /**
     * Getting user data from database
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("userID", cursor.getString(1));
            user.put("login", cursor.getString(2));
            user.put("email", cursor.getString(3));
            user.put("phone", cursor.getString(4));
            user.put("name", cursor.getString(5));
            user.put("surname", cursor.getString(6));
            user.put("photo", cursor.getString(7));
            user.put("location", cursor.getString(8));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public HashMap<String, String> getLocationDetails() {
        HashMap<java.lang.String, java.lang.String> locations = new HashMap<java.lang.String, java.lang.String>();
        java.lang.String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            locations.put("locationID", cursor.getString(1));
            locations.put("lat", cursor.getString(2));
            locations.put("lng", cursor.getString(3));

        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + locations.toString());

        return locations;
    }

    public HashMap<String, String> getSettings() {
        HashMap<String, String> settings = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            settings.put("internet", cursor.getString(1));
            settings.put("notification", cursor.getString(2));
            settings.put("navigation", cursor.getString(3));
            settings.put("layout", cursor.getString(4));
            settings.put("radius", cursor.getString(5));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + settings.toString());

        return settings;
    }

    /**
     * Getting user login status return true if rows are there in table
     */
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    public void deleteLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOCATIONS, null, null);
        db.close();

        Log.d(TAG, "Deleted all location info from sqlite");
    }

    /**
     * Re crate database Delete all tables and create them again
     */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }


    public void deleteSettings() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOCATIONS, null, null);
        db.close();

        Log.d(TAG, "Deleted all location info from sqlite");
    }

    public void deleteFriends(){
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete All Rows
        db.delete(TABLE_FRIENDS,null,null);
        db.close();

        Log.d(TAG, "Deleted all friends info from sqlite");
    }

}
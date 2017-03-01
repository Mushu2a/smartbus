package com.m1.lesbuteurs.smartbus.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "android_api";
    private static final String TABLE_USER = "user";

    private static final String KEY_ID = "id";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Création table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + "username TEXT,"
                + "lastname TEXT,"
                + "firstname TEXT,"
                + "password TEXT,"
                + "email TEXT UNIQUE,"
                + "phone TEXT,"
                + "birthday TEXT,"
                + "address1 TEXT,"
                + "address2 TEXT NULL,"
                + "city TEXT,"
                + "zip TEXT,"
                + "country TEXT,"
                + "gender TEXT,"
                + "brandBus TEXT,"
                + "comfort TEXT,"
                + "number TEXT,"
                + "owner TEXT,"
                + "api_token TEXT,"
                + "created_at TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Création base sqlite");
    }

    // Met à jour la base de donnée
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    /**
     * Enregistre l'utilisateur
     * */
    public void addUser(String username, String lastname, String firstname, String password, String email, String phone, String birthday,
                        String address1, String address2, String city, String zip, String country, String gender,
                        String brandBus, String comfort, String number, String owner, String api_token, String created_at) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("lastname", lastname);
        values.put("firstname", firstname);
        values.put("password", password);
        values.put("email", email);
        values.put("phone", phone);
        values.put("birthday", birthday);
        values.put("address1", address1);
        values.put("address2", address2);
        values.put("city", city);
        values.put("zip", zip);
        values.put("country", country);
        values.put("gender", gender);
        values.put("brandBus", brandBus);
        values.put("comfort", comfort);
        values.put("number", number);
        values.put("owner", owner);
        values.put("api_token", api_token);
        values.put("created_at", created_at);

        long id = db.insert(TABLE_USER, null, values);
        db.close();

        Log.d(TAG, "Nouvel utilisateur: " + id);
    }

    /**
     * Récupère utilisateur de la base de donnée
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("username", cursor.getString(1));
            user.put("lastname", cursor.getString(2));
            user.put("firstname", cursor.getString(3));
            user.put("password", cursor.getString(4));
            user.put("email", cursor.getString(5));
            user.put("phone", cursor.getString(6));
            user.put("birthday", cursor.getString(7));
            user.put("address1", cursor.getString(8));
            user.put("address2", cursor.getString(9));
            user.put("city", cursor.getString(10));
            user.put("zip", cursor.getString(11));
            user.put("country", cursor.getString(12));
            user.put("gender", cursor.getString(13));
            user.put("brandBus", cursor.getString(14));
            user.put("comfort", cursor.getString(15));
            user.put("number", cursor.getString(16));
            user.put("owner", cursor.getString(17));
            user.put("api_token", cursor.getString(18));
            user.put("created_at", cursor.getString(19));
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Récupère utilisateur de la base Sqlite: " + user.toString());

        return user;
    }

    /**
     * Met à jour l'utilisateur
     * */
    public int updateUser(int id, String username, String lastname, String firstname, String password, String email, String phone,
                          String birthday, String address1, String address2, String city, String zip,
                          String country, String gender, String brandBus, String comfort, String number, String owner) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("lastname", lastname);
        values.put("firstname", firstname);
        values.put("password", password);
        values.put("email", email);
        values.put("phone", phone);
        values.put("birthday", birthday);
        values.put("address1", address1);
        values.put("address2", address2);
        values.put("city", city);
        values.put("zip", zip);
        values.put("country", country);
        values.put("gender", gender);
        values.put("brandBus", brandBus);
        values.put("comfort", comfort);
        values.put("number", number);
        values.put("owner", owner);
        // updating row
        return db.update(TABLE_USER, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /**
     * Supprime tous dans la base
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Supprime tous les utilisateurs de la base sqlite");
    }

}

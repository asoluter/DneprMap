package com.asoluter.dneprmap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

public class OpenData {

    public static final String DATABASE_NAME="places.sqlite3";
    public static final String TABLE_NAME="places";
    public static final String TABLE_TITLE="place";
    public static final String TABLE_TEXT="description";
    public static final String TABLE_X="tx";
    public static final String TABLE_Y="ty";

    public static SQLiteDatabase database;

    public static SQLiteDatabase database(Context context,String name) throws SQLException {
        ExternalDbOpenHelper helper=new ExternalDbOpenHelper(context,name);
        database=helper.openDataBase();
        return database;
    }

    public static Cursor cursor(Context context,String datan,String name,String[] s) throws SQLException {

        try {
            Cursor c= database(context,datan).query(name, s, null, null, null, null, null);
            return c;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Cursor cursor(Context context) throws SQLException {
        try {
            Cursor c= database(context,DATABASE_NAME).query(TABLE_NAME, new String[]{TABLE_TITLE,TABLE_TEXT,TABLE_X,TABLE_Y}, null, null, null, null, null);
            return c;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final String TTABLE_NAME="travels";
    public static final String TTABLE_TITLE="travel_name";
    public static final String TTABLE_KEY="travel_key";

    public static Cursor tcursor(Context context){
        try {
            return cursor(context,DATABASE_NAME,TTABLE_NAME,new String[]{TTABLE_TITLE,TTABLE_KEY});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void Close(){
        database.close();
    }

}

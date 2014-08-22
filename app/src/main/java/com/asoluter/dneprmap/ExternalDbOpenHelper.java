package com.asoluter.dneprmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public class ExternalDbOpenHelper extends SQLiteOpenHelper {

    public static String DB_PATH;
    public static String DB_NAME;
    public SQLiteDatabase database;
    public final Context context;

    public final SQLiteDatabase getDb(){
        return database;
    }

    public ExternalDbOpenHelper(Context context,String databaseName){
        super(context,databaseName,null,1);
        this.context=context;
        String packageName=context.getPackageName();
        DB_PATH=String.format("//data//data//%s//databases//", packageName);
        DB_NAME=databaseName;
        try{
            openDataBase();
        }catch (SQLException s){

        }

    }

    public void createDataBase() {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database!");
            }
        }
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDb = null;
        String path = DB_PATH + DB_NAME;
        checkDb = SQLiteDatabase.openDatabase(path, null,SQLiteDatabase.OPEN_READONLY);
        if (checkDb != null) {
            checkDb.close();
        }
        return checkDb != null;
    }

    private void copyDataBase() throws IOException {
        // Открываем поток для чтения из уже созданной нами БД
        //источник в assets
        InputStream externalDbStream = context.getAssets().open(DB_NAME);

        // Путь к уже созданной пустой базе в андроиде
        String outFileName = DB_PATH + DB_NAME;

        // Теперь создадим поток для записи в эту БД побайтно
        OutputStream localDbStream = new FileOutputStream(outFileName);

        // Собственно, копирование
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = externalDbStream.read(buffer)) > 0) {
            localDbStream.write(buffer, 0, bytesRead);
        }
        // Мы будем хорошими мальчиками (девочками) и закроем потоки
        localDbStream.close();
        externalDbStream.close();

    }

    public SQLiteDatabase openDataBase() throws SQLException {
        String path = DB_PATH + DB_NAME;
        if (database == null) {
            createDataBase();
            database = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READWRITE);
        }
        return database;
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}

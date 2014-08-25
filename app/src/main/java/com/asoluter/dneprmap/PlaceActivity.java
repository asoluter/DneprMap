package com.asoluter.dneprmap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.SQLException;


public class PlaceActivity extends Activity {

    public static final String TABLE_NAME="places.sqlite3";
    public static String TABLE_ID;
    public static final String TABLE_PIC="picture";
    public static final String TABLE_TEXT="text";

    private SQLiteDatabase database;

    ImageView picture;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        Intent intent=getIntent();

        TABLE_ID=intent.getStringExtra("title");

        ActionBar actionBar=getActionBar();
        try{
            actionBar.setTitle(TABLE_ID);
        }catch (NullPointerException e){
            Log.wtf("info","titlebad");
        }


        picture=(ImageView)findViewById(R.id.picture);
        text=(TextView)findViewById(R.id.text);

        ExternalDbOpenHelper helper=new ExternalDbOpenHelper(getApplicationContext(),TABLE_NAME);
        try{
            database=helper.openDataBase();
            makeActivity();
        }catch (SQLException e){Log.wtf("info","FailDatabase");}
    }

    public void makeActivity(){
        Cursor cursor=database.query(TABLE_NAME,new String[]{TABLE_PIC,TABLE_TEXT},null,null,null,null,TABLE_ID);
        //cursor.moveToFirst();
        //picture.setImageResource(getResources().getIdentifier(cursor.getString(1),"drawable",getPackageName()));
        //text.setText(cursor.getString(2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.place, menu);
        return true;
    }


}

package com.asoluter.dneprmap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.sql.SQLException;


public class SettingsActivity extends Activity {

    public static final String DATABASE_NAME="places.sqlite3";
    public static final String TABLE_NAME="places";
    public static final String TABLE_TITLE="place";
    public static final String TABLE_PIC="background";
    public static final String TABLE_TEXT="description";
    public static final String TABLE_CHECK="checked";
    public static final String TABLE_X="tx";
    public static final String TABLE_Y="ty";

    LinearLayout linearLayout;
    Switch awitch;
    Cursor cursor;
    Boolean b;
    int len;
    ContentValues values;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); int i;
        setContentView(R.layout.activity_settings);

        linearLayout=(LinearLayout)findViewById(R.id.linear);

        try {
            database=OpenData.database(this,DATABASE_NAME);
            cursor=OpenData.cursor(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        len=cursor.getCount();

        cursor.moveToFirst(); i=0;
        while (!cursor.isAfterLast()){

            awitch=new Switch(this);
            if(cursor.getInt(3)==0)b=false;else b=true;
            awitch.setText(cursor.getString(0));
            awitch.setChecked(b);
            awitch.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            awitch.setOnCheckedChangeListener(checkedChangeListener);

            linearLayout.addView(awitch);

            cursor.moveToNext(); i++;
        }

    }

    public CompoundButton.OnCheckedChangeListener checkedChangeListener=new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            cursor.moveToFirst(); int t=1;
            while(!cursor.getString(0).equals(compoundButton.getText())){cursor.moveToNext();t++;}
            values=new ContentValues();
            values.put(TABLE_TITLE,cursor.getString(0));
            values.put(TABLE_PIC,cursor.getString(1));
            values.put(TABLE_TEXT,cursor.getString(2));
            values.put(TABLE_X,cursor.getDouble(4));
            values.put(TABLE_Y,cursor.getDouble(5));
            if(b){
                values.put(TABLE_CHECK,1);
            }else
            {
                values.put(TABLE_CHECK,0);
            }
            String where="_id="+t;
            database.update(TABLE_NAME,values,where,null);
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent=getParentActivityIntent();
        assert intent != null;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }
}

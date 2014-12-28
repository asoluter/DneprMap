package com.asoluter.dneprmap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class FindPlaces extends Activity implements SearchView.OnQueryTextListener {

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
    List<String> placeList=new ArrayList<String>();
    ArrayAdapter<String> dataAdapter;

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); int i;
        setContentView(R.layout.activity_find_places);

        try {
            database=OpenData.database(this,DATABASE_NAME);
            cursor=OpenData.cursor(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        len=cursor.getCount();

        cursor.moveToFirst(); i=0;
        while (!cursor.isAfterLast()){

            placeList.add(cursor.getString(0));

            cursor.moveToNext(); i++;
        }

        dataAdapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.place_list,placeList);

        dataAdapter.sort(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        ListView listView=(ListView)findViewById(R.id.dataList);

        listView.setAdapter(dataAdapter);

        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),PlaceActivity.class);
                intent.putExtra("title",((TextView)view).getText());
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.find_places, menu);
        searchView=(SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Поиск");
        searchView.setOnQueryTextListener(this);
        return true;
    }

    public boolean onQueryTextSubmit(String query) {

        return true;
    }

    public boolean onQueryTextChange(String newText) {
        dataAdapter.getFilter().filter(newText);
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

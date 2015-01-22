package com.asoluter.dneprmap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;


public class Travel extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        fillAll();
    }

    private void fillAll(){
        ExpandableListView expandableListView=(ExpandableListView)findViewById(R.id.expandableListView);
        Cursor cursor=OpenData.tcursor(getApplicationContext());
        cursor.moveToFirst();
        ArrayList<Pair<String,ArrayList<String>>> groups=new ArrayList<>();
        while (!cursor.isAfterLast())
        {
            Pair<String,ArrayList<String>> pair;
            ArrayList<String> childs=new ArrayList<>();
            String s=cursor.getString(1);
            String[] ss=s.split(",");
            try {
                Cursor cursor1=OpenData.cursor(getApplicationContext());
                for(int i=0;i<ss.length;i++)
                {
                    int x=Integer.valueOf(ss[i])-1;
                    cursor1.moveToPosition(x);
                    childs.add(cursor1.getString(0));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            pair=new Pair<>(cursor.getString(0),childs);
            groups.add(pair);
            cursor.moveToNext();
        }
        ExpListAdapter expListAdapter=new ExpListAdapter(this,groups);
        expandableListView.setAdapter(expListAdapter);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_travel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(getApplicationContext(),OwnTravel.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

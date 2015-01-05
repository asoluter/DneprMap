package com.asoluter.dneprmap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


public class Choose extends Activity implements SeekBar.OnSeekBarChangeListener {

    SeekBar seekBar;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    TextView tseek;
    static final String SRadius="sradius";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        Button search=(Button)findViewById(R.id.searchbt);
        Button radius=(Button)findViewById(R.id.radiusbt);
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        seekBar.setMax(50);
        seekBar.setOnSeekBarChangeListener(this);
        tseek=(TextView)findViewById(R.id.tseekView);
        search.setOnClickListener(onClickListener);
        radius.setOnClickListener(onClickListener);
        preferences=getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=preferences.edit();
        seekBar.setProgress(preferences.getInt(SRadius,0));
        tseek.setText(String.valueOf(preferences.getInt(SRadius,0)*100)+" m");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.searchbt:
                {
                    onSearch();
                    break;
                }
                case R.id.radiusbt:
                {
                    onRadius();
                    break;
                }
            }
        }
    };

    public void onSearch(){
        Intent intent=new Intent(getApplicationContext(),FindPlaces.class);
        startActivity(intent);
    }

    public void onRadius(){

        Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
        startActivity(intent);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        editor.putInt(SRadius,progress);
        editor.apply();
        tseek.setText(String.valueOf(progress*100)+" m");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}

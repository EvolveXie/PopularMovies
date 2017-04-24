package com.evolvexie.popularmovies;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.evolvexie.popularmovies.data.CommonPreferences;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private RadioButton mSetSortingByPopular;
    private RadioButton mSetSortingByRated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSetSortingByPopular = (RadioButton) findViewById(R.id.rb_popular);
        mSetSortingByRated = (RadioButton) findViewById(R.id.rb_rated);

        SharedPreferences sharedPreferences = getSharedPreferences(CommonPreferences.SETTING_PREF_NAME,MODE_PRIVATE);
        String curSortingSetting = sharedPreferences.getString(CommonPreferences.SORTING_WAY,"popular");
        if (curSortingSetting.equals("popular")){
            mSetSortingByPopular.setChecked(true);
            mSetSortingByRated.setChecked(false);
        }else{
            mSetSortingByPopular.setChecked(false);
            mSetSortingByRated.setChecked(true);
        }

        mSetSortingByPopular.setOnClickListener(this);
        mSetSortingByRated.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.rb_popular){
            mSetSortingByPopular.setChecked(true);
            mSetSortingByRated.setChecked(false);
            saveSettingsData(CommonPreferences.SORTING_WAY,"popular");
        }else if(viewId == R.id.rb_rated){
            mSetSortingByPopular.setChecked(false);
            mSetSortingByRated.setChecked(true);
            saveSettingsData(CommonPreferences.SORTING_WAY,"rated");
        }
    }

    /**
     * save settting data with SharedPreferences
     * @param key
     * @param value
     */
    public void saveSettingsData(String key,String value){
        SharedPreferences sharedPreferences = getSharedPreferences(CommonPreferences.SETTING_PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }
}

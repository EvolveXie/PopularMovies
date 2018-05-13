package com.evolvexie.popularmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

//    @BindView(R.id.rb_popular)
//    RadioButton mSetSortingByPopular;
//    @BindView(R.id.rb_rated)
//    RadioButton mSetSortingByRated;
    @BindView(R.id.setting_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

//        SharedPreferences sharedPreferences = getSharedPreferences(CommonPreferences.SETTING_PREF_NAME,MODE_PRIVATE);
//        String curSortingSetting = sharedPreferences.getString(CommonPreferences.SORTING_WAY,"popular");
//        if (curSortingSetting.equals("popular")){
//            mSetSortingByPopular.setChecked(true);
//            mSetSortingByRated.setChecked(false);
//        }else{
//            mSetSortingByPopular.setChecked(false);
//            mSetSortingByRated.setChecked(true);
//        }
//
//        mSetSortingByPopular.setOnClickListener(this);
//        mSetSortingByRated.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
//        if(viewId == R.id.rb_popular){
//            mSetSortingByPopular.setChecked(true);
//            mSetSortingByRated.setChecked(false);
//            saveSettingsData(CommonPreferences.SORTING_WAY,"popular");
//        }else if(viewId == R.id.rb_rated){
//            mSetSortingByPopular.setChecked(false);
//            mSetSortingByRated.setChecked(true);
//            saveSettingsData(CommonPreferences.SORTING_WAY,"rated");
//        }
    }

    /**
     * save settting data with SharedPreferences
     * @param key
     * @param value
     */
    public void saveSettingsData(String key,String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

}

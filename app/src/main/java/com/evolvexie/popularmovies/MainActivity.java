package com.evolvexie.popularmovies;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.evolvexie.popularmovies.adapter.MovieSyncAdapter;
import com.evolvexie.popularmovies.data.CommonPreferences;
import com.evolvexie.popularmovies.fragment.MainFragment;
import com.evolvexie.popularmovies.utils.SharedPreferenceUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.main_drawer)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_drawer)
    NavigationView mNavigationView;

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // 设置抽屉导航栏
        drawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,mToolbar,R.string.drawer_open,R.string.drawer_close);
        mDrawerLayout.setDrawerListener(drawerToggle);

        mNavigationView.setNavigationItemSelectedListener(this);
        Account account = MovieSyncAdapter.getAccount(this);
        if (account != null) { // 打开时进行一次数据同步
            syncData(account);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        return mainFragment.navigationItemSelect(item,mDrawerLayout);
    }

    public void syncData(Account account){
        // 最近同步时间
        String lastSyncTime = CommonPreferences.getDefaultSharedPreferenceValue(this,
                BuildConfig.LAST_TIME_SYNC_KEY,"");
        if (!lastSyncTime.equals("")){
            // 用户设定的同步频率(小时)
            int syncFrequency = CommonPreferences.getDefaultSharedPreferenceIntValue(this,
                    this.getResources().getString(R.string.pref_key_sync_frequency),
                    this.getResources().getString(R.string.pref_default_sync_frequency));
            Date nowDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date lastSyncDate = null;
            try {
                lastSyncDate = format.parse(lastSyncTime);
            } catch (ParseException e) {
                Log.e(TAG, "syncData: 转换日期异常",e);
                return;
            }
            long between = nowDate.getTime() - lastSyncDate.getTime();
            Log.d(TAG, "syncData: interval time :" + between / (1 * 60 * 60 * 1000));
            if (between / (1 * 60 * 60 * 1000) > syncFrequency) {
                if (!ContentResolver.isSyncActive(account,MovieSyncAdapter.AUTHORITY)){
                    MovieSyncAdapter.performSync(this,account);
                }
            }
        }else {
            if (!ContentResolver.isSyncActive(account,MovieSyncAdapter.AUTHORITY)){
                MovieSyncAdapter.performSync(this,account);
            }
        }
    }
}

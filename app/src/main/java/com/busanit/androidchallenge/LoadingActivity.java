package com.busanit.androidchallenge;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

public class LoadingActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        Handler handler = new Handler();
        //로딩화면 2초 후 액티비티 종료 (메인화면이 나오게)
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               finish();
            }
        },2000);
    }

    //로딩화면 중 back버튼 막아두기
    @Override
    public void onBackPressed() {}
}

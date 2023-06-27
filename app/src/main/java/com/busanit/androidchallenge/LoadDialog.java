package com.busanit.androidchallenge;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import androidx.annotation.NonNull;

public class LoadDialog extends Dialog {
    public LoadDialog(@NonNull Context context) {
        super(context);
        //타이틀 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_dialog);
    }
}

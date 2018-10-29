package com.example.nb.activitytest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by nb on 2018/10/28.
 */

public class ThirdActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.third_layout);
        Intent intent = getIntent();
        String data = intent.getStringExtra("extra_data");
        Log.d("nb", "extraData:"+data);
    }
}

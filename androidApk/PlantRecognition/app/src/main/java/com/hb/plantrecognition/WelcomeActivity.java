package com.hb.plantrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WelcomeActivity extends AppCompatActivity {

    private boolean mMainActivity;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);

        mMainActivity = false;
        mHandler = new Handler();

        findViewById(R.id.image).setOnClickListener(v -> {
            mHandler.removeCallbacks(mRunnable);
            gotoMainActivity();
        });

        mHandler.postDelayed(mRunnable, 3000);
    }

    private Runnable mRunnable = this::gotoMainActivity;

    private void gotoMainActivity() {
        if (mMainActivity) return;
        mMainActivity = true;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMainActivity = true;
        mHandler.removeCallbacks(mRunnable);
    }
}

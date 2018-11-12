package com.hb.plantrecognition.update;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.hb.plantrecognition.R;

import java.io.File;
import java.util.List;

public class UpdateActivity extends AppCompatActivity implements Handler.Callback {

    private TextView mTextView;
    private ProgressBar mProgressBar;
    private LinearLayout mLayout;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        setTitle(R.string.title_software_update);

        mTextView = findViewById(R.id.text);
        mProgressBar = findViewById(R.id.progress);
        mLayout = findViewById(R.id.layout);

        Button btn = findViewById(R.id.cancel);
        btn.setOnClickListener(v -> finish());

        mHandler = new Handler(this);
        mProgressBar.setMax(100);

        btn =  findViewById(R.id.ok);
        btn.setOnClickListener(v -> {
            checkPermission();
            mProgressBar.setVisibility(View.VISIBLE);
            mLayout.setVisibility(View.GONE);
            mTextView.setText(R.string.prompt_downloading);
            Updater.getInstance().updateApk(mHandler);
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Updater.MSG_START:
                return true;
            case Updater.MSG_FAILED:
                finish();
                return true;
            case Updater.MSG_FINISH:
                update((File) msg.obj);
                finish();
                return true;
            case Updater.MSG_PROGRESS:
                mProgressBar.setProgress(msg.arg1);
                return true;
        }
        return false;
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return;

        int write = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (write != PackageManager.PERMISSION_GRANTED || read != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 300);
        }
    }

    private void update(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, "com.hb.android.fileprovider", file);

            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            uri = Uri.fromFile(file);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }
}

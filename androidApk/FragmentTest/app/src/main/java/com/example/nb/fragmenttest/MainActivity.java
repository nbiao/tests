package com.example.nb.fragmenttest;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.button:
//                        AnotherRightFragment fragment = new AnotherRightFragment();
//                        FragmentManager fragmentManager = getSupportFragmentManager();
//                        FragmentTransaction transaction = fragmentManager.
//                                beginTransaction();
//                        transaction.replace(R.id.right_layout, fragment);
//                        transaction.addToBackStack(null);
//                        transaction.commit();
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
    }
}

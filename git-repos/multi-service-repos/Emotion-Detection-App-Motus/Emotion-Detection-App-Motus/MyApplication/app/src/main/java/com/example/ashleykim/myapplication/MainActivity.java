package com.example.ashleykim.myapplication;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t =(TextView) findViewById(R.id.my_text);
        Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/BPreplay.otf");
        t.setTypeface(myCustomFont);
    }
}

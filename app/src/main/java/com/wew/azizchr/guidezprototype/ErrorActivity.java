package com.wew.azizchr.guidezprototype;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ErrorActivity extends AppCompatActivity {
    private TextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        txtError = findViewById(R.id.txtError);
        String errors = getIntent().getStringExtra("ERRORS");
        txtError.setText(errors);
    }
}

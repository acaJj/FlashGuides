package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class CreateGuideTitle extends AppCompatActivity {

    private static final String NEW_GUIDE = "NEW_GUIDE";
    EditText mGuideTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_guide_title);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        mGuideTitle = (EditText) findViewById(R.id.etGuideTitle);
    }

    public void onContinueClick(View view) {

        Intent intent = new Intent(CreateGuideTitle.this,CreateNewGuide.class);
        intent.putExtra("GUIDE_TITLE", mGuideTitle.getText().toString());
        intent.putExtra("MODE","CREATE");//tells the activity that we are starting from scratch
        startActivity(intent);
        finish();
    }
}

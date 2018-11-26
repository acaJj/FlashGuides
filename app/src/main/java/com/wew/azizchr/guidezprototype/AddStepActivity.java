package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddStepActivity extends AppCompatActivity {

    private EditText mStepTitle;
    private EditText mStepDesc;
    private Button mDone;
    private TextView mStepNumber;

    private String stepTitle;
    private String stepDesc;

    private int stepNumber;
    private static final String STEP_TITLE_WRITTEN = "com.wew.azizchr.guidezprototype.step_title";
    private static final String STEP_DESC_WRITTEN = "com.wew.azizchr.guidezprototype.step_desc";

    public static String getTitle(Intent data){
        return data.getStringExtra(STEP_TITLE_WRITTEN);
    }

    public static String getDesc(Intent data){
        return data.getStringExtra(STEP_DESC_WRITTEN);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_step);
        Intent intent = getIntent();

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        //stepNumber = intent.getIntExtra("CurrStep", 0);
        mStepTitle = (EditText) findViewById(R.id.etNewStepTitle);
        mStepDesc = (EditText) findViewById(R.id.etNewStepDescription);
        mStepNumber = (TextView) findViewById(R.id.txtNewStepHeader);
        mDone = (Button) findViewById(R.id.btnDoneAddStep);

        mStepNumber.setText("Add New Step ");
    }

    public void onDoneClick(View view) {
        try{
            stepTitle = mStepTitle.getText().toString();
            stepDesc = mStepDesc.getText().toString();
            if (stepTitle == "" || stepTitle == null || stepDesc == "" || stepDesc == null){
                Toast.makeText(AddStepActivity.this, "You have to fill out both fields!", Toast.LENGTH_SHORT).show();
            }else{
                setStepResult(stepTitle, stepDesc);
            }
        }catch (NullPointerException ex){
            ex.getMessage();
        }
    }

    private void setStepResult(String title, String desc){
        Intent data = new Intent();
        data.putExtra(STEP_TITLE_WRITTEN,title);
        data.putExtra(STEP_DESC_WRITTEN, desc);
        setResult(RESULT_OK,data);
        finish();
        AddStepActivity.this.overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }
}

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

public class AddDescriptionActivity extends AppCompatActivity {

    private EditText mStepDesc;
    private TextView mHeader;
    private Button mDone;

    private String stepDesc;
    private Boolean isEditing;

    private static final String NEW_DESC_WRITTEN = "com.wew.azizchr.guidezprototype.new_desc";

    public static String getNewDesc(Intent data){
        return data.getStringExtra(NEW_DESC_WRITTEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_description);
        Intent intent = getIntent();
        isEditing = intent.getBooleanExtra("isEditing", false);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        mHeader = (TextView) findViewById(R.id.txtNewDescHeader);
        mStepDesc = (EditText) findViewById(R.id.etNewDesc);
        mDone = (Button) findViewById(R.id.btnDoneAddDesc);

        if(isEditing){
            mStepDesc.setText(intent.getStringExtra("CurrText"));
            mHeader.setText("Edit Description");
        }
    }

    public void onDoneClick(View view) {
        try{
            stepDesc = mStepDesc.getText().toString();

            if (stepDesc == "" || stepDesc == null || stepDesc == "" || stepDesc == null){
                Toast.makeText(AddDescriptionActivity.this, "You have to write something down!", Toast.LENGTH_SHORT).show();
            }else{
                setDescResult(stepDesc);
            }
        }catch (NullPointerException ex){
            ex.getMessage();
        }
    }

    private void setDescResult(String desc){
        Intent data = new Intent();
        data.putExtra(NEW_DESC_WRITTEN, desc);
        setResult(RESULT_OK,data);
        finish();
        AddDescriptionActivity.this.overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }
}
package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TextBlockWriterActivity extends AppCompatActivity {

    private EditText mEditText;
    private Button mbtnDone;
    private String text;
    private static final String TEXT_BLOCK_WRITTEN = "com.wew.azizchr.guidezprototype.text_written";

    public static String getTextBlockWritten(Intent data){
        return data.getStringExtra(TEXT_BLOCK_WRITTEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_block_writer);

        mEditText = findViewById(R.id.newTxtBlock);
        mbtnDone = findViewById(R.id.btnDone);
        if (savedInstanceState != null){
            text = savedInstanceState.getString("Text");
        }

        mbtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text = mEditText.getText().toString();
                setTextResult(text);
            }
        });
    }

    private void setTextResult(String text){
        Intent data = new Intent();
        data.putExtra(TEXT_BLOCK_WRITTEN,text);
        setResult(RESULT_OK,data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("Text", text);
    }
}

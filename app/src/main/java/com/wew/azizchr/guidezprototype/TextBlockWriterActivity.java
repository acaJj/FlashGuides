package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import jp.wasabeef.richeditor.RichEditor;

public class TextBlockWriterActivity extends AppCompatActivity {

    private EditText mEditText;
    private RichEditor mEditor;
    private TextView mPreview;
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
        mEditor = findViewById(R.id.editor);
        mPreview = findViewById(R.id.preview);
        mbtnDone = findViewById(R.id.btnDone);
        text = getIntent().getStringExtra("newtext");

        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(20);
        mEditor.setEditorFontColor(Color.BLACK);
        mEditor.setPlaceholder("Insert text here...");

        if (savedInstanceState != null){
            text = savedInstanceState.getString("Text");
        }

        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text){ }
        });

        findViewById(R.id.btnUndo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.undo();
            }
        });

        findViewById(R.id.btnRedo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.redo();
            }
        });

        findViewById(R.id.btnBold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.setBold();
            }
        });

        findViewById(R.id.btnItalic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.setItalic();
            }
        });

        findViewById(R.id.btnUnderline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.setUnderline();
            }
        });

        findViewById(R.id.btnHeader1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.setHeading(1);
            }
        });

        mbtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text = mEditor.getHtml().toString();
                setTextResult(text);
            }
        });
    }

    private void setTextResult(String text){
        Intent data = new Intent();
        data.putExtra(TEXT_BLOCK_WRITTEN,text);
        setResult(RESULT_OK,data);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("Text", text);
    }
}

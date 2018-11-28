package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import jp.wasabeef.richeditor.RichEditor;


/**
 * Both Chris and Jeffrey have worked on this
 *
 * Chris was responsible for: Creating xml layout, setting button color functionality
 *                            and sending data back to CreateNewGuide.java
 * Jeff was responsible for: implementing the edit text activity
 */
public class TextBlockWriterActivity extends AppCompatActivity {

    private EditText mEditText;
    private RichEditor mEditor;
    private TextView mPreview;
    private Button mbtnDone;
    private String text;

    private Button mbtnUndo;
    private Button mbtnRedo;
    private Button mbtnBold;
    private Button mbtnItalic;
    private Button mbtnUnderline;

    private static final String TEXT_BLOCK_WRITTEN = "com.wew.azizchr.guidezprototype.text_written";

    public static String getTextBlockWritten(Intent data){
        return data.getStringExtra(TEXT_BLOCK_WRITTEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //sets the color for the status bar
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }
        setContentView(R.layout.activity_text_block_writer);

        //mEditText = findViewById(R.id.newTxtBlock);
        mEditor = findViewById(R.id.editor);
        mPreview = findViewById(R.id.preview);
        mbtnDone = findViewById(R.id.btnDone);
        text = getIntent().getStringExtra("newtext");

        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(20);
        mEditor.setEditorFontColor(Color.BLACK);
        mEditor.setPlaceholder("Insert text here...");

        mbtnUndo = (Button) findViewById(R.id.btnUndo);
        mbtnRedo = (Button) findViewById(R.id.btnRedo);
        mbtnUnderline = (Button) findViewById(R.id.btnUnderline);
        mbtnItalic = (Button) findViewById(R.id.btnItalic);
        mbtnBold = (Button) findViewById(R.id.btnBold);

        if (savedInstanceState != null){
            text = savedInstanceState.getString("Text");
        }

        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text){ }
        });

        mbtnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.undo();
            }
        });
        mbtnUndo.setBackgroundResource(R.drawable.style_button_add);

        mbtnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.redo();
            }
        });
        mbtnRedo.setBackgroundResource(R.drawable.style_button_add);

        mbtnBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.setBold();
            }
        });
        mbtnBold.setBackgroundResource(R.drawable.style_button_add);

        mbtnItalic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.setItalic();
            }
        });
        mbtnItalic.setBackgroundResource(R.drawable.style_button_add);

        mbtnUnderline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditor.setUnderline();
            }
        });
        mbtnUnderline.setBackgroundResource(R.drawable.style_button_add);


        mbtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    text = mEditor.getHtml();
                    if (text.equals("") || text == null){
                        Toast.makeText(TextBlockWriterActivity.this, "You have to enter something!", Toast.LENGTH_SHORT).show();
                    }else{
                        setTextResult(text);
                    }
                }catch (NullPointerException ex){
                    ex.getMessage();
                }
            }
        });
    }

    private void setTextResult(String text){
        Intent data = new Intent();
        data.putExtra(TEXT_BLOCK_WRITTEN,text);
        setResult(RESULT_OK,data);
        finish();
        TextBlockWriterActivity.this.overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }

    public static String getNewDesc(Intent data){
        return data.getStringExtra(TEXT_BLOCK_WRITTEN);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("Text", text);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }
}

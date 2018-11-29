package com.wew.azizchr.guidezprototype;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;


/**
 * Both Chris and Jeffrey have worked on this
 *
 * Chris was responsible for: Creating xml layout, adding checked listener and animation
 * Jeff was responsible for: all firebase methods
 */


public class SearchActivity extends AppCompatActivity {

    //private Spinner spinner;
    private CheckBox cbSearchUser;
    private EditText etSearchString;
    private EditText etSearchUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //sets the status bar color
        if (Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        //Gets a handle on all interactable objects
        cbSearchUser = (CheckBox) findViewById(R.id.cbSearchByUser);
        etSearchString = (EditText) findViewById(R.id.etSearchString);
        etSearchUser = (EditText) findViewById(R.id.etSearchUser);
        etSearchUser.setVisibility(View.GONE);

        //Sets an onChecked listener for the checkbox, hides the edittext when appropriate
        cbSearchUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbSearchUser.isChecked()) {
                    etSearchUser.setVisibility(View.VISIBLE);
                } else {
                    etSearchUser.setText("");
                    etSearchUser.setVisibility(View.GONE);
                }
            }
        });
    }

    //Store the first edittext in the bundle, and if the second one is filled out store that too.
    public void onSearchSubmit(View view) {
        Intent intent = new Intent(SearchActivity.this,SearchResultActivity.class);
        intent.putExtra("SearchString", etSearchString.getText().toString());
        if(cbSearchUser.isChecked()){
            intent.putExtra("SearchUser", etSearchUser.getText().toString());
        }else{
            intent.putExtra("SearchUser", "NULL");
        }
        startActivity(intent);
        overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
    }

    //When back is pressed, do an animation
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }

}
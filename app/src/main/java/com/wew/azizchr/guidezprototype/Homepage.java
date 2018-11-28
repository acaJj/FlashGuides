package com.wew.azizchr.guidezprototype;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Locale;

/**
 * Both Chris and Jeffrey have worked on this
 *
 * Chris was responsible for: Creating xml layout, implementing logout,
 */
public class Homepage extends AppCompatActivity {

    private static final String NEW_GUIDE = "NEW_GUIDE";
    private static final String EDIT_MODE = "MODE";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 264;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 265;

    private Button btnMakeGuide, btnSettings, btnCollection, btnSearch;
    private FirebaseAuth mAuth;
    Calendar calendar;
    TextView mGreeting;
    int hour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        checkWriteStoragePermissions();
        checkFineAccessPermissions();

        btnMakeGuide = findViewById(R.id.btnMakeGuide);
        btnSettings = findViewById(R.id.btnSettings);
        btnCollection = findViewById(R.id.btnGuideCollection);
        btnSearch = findViewById(R.id.btnSearch);
        mAuth = FirebaseAuth.getInstance();
        mGreeting = (TextView) findViewById(R.id.txtHomePageGreet);
        calendar = Calendar.getInstance(Locale.getDefault());
        hour = calendar.get(Calendar.HOUR_OF_DAY);

        btnMakeGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Homepage.this,CreateGuideTitle.class);
                intent.putExtra(NEW_GUIDE, true);
                intent.putExtra(EDIT_MODE,"CREATE");
                startActivity(intent);
                overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Homepage.this,SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
            }
        });

        btnCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(Homepage.this, UserCollectionActivity.class);
                Intent intent = new Intent(Homepage.this, UserCollectionActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Homepage.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Checks to see if the user is signed in. If not, start the auth activity
        if(currentUser != null){
            User currUser = new User(mAuth.getCurrentUser().getUid());
            String greet = greeting(hour) + ".";
            mGreeting.setText(greet + ". Signed in as: " + currentUser.getEmail() );
        }else{
            Intent intent = new Intent(Homepage.this, AuthActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //Passes the current hour and returns the appropriate greeting for time of day
    public String greeting(int h){
        String greet = "?";
        if(h > 5 && h < 11){
            greet = "Good morning";
        } else if(h >11 && h < 15){
            greet = "Good afternoon";
        } else if( h > 15 && h < 19){
            greet = "Good evening";
        } else {
            greet = "Good night";
        }
        return greet;
    }

    public void checkWriteStoragePermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return;
            }
        }
    }

    public void checkFineAccessPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
        }
    }

    //Signs the user our and starts the auth activity
    public void OnSignOutClick(View view) {
        mAuth.signOut();
        Intent intent = new Intent(Homepage.this, AuthActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }
}
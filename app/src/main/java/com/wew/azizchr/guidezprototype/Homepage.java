package com.wew.azizchr.guidezprototype;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Locale;

public class Homepage extends AppCompatActivity {

    private static final String NEW_GUIDE = "NEW_GUIDE";
    private static final String EDIT_MODE = "MODE";

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
            }
        });

        btnCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Homepage.this, UserCollectionActivity.class);
                startActivity(intent);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Homepage.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Checks to see if the user is signed in. If not, start the auth activity
        if(currentUser != null){
            String greet = greeting(hour);
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

    //Signs the user our and starts the auth activity
    public void OnSignOutClick(View view) {
        mAuth.signOut();
        Intent intent = new Intent(Homepage.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}
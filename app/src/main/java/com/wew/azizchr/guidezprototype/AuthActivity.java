package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }
    }

    //Starts sign up activity
    public void onClickSignUp(View view) {
        Intent intent = new Intent(AuthActivity.this, SignUpActivty.class);
        startActivity(intent);
        overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
    }

    //Starts sign in activty
    public void onClickSignIn(View view) {
        Intent intent = new Intent(AuthActivity.this, SignInActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
    }
}

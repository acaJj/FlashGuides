package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    TextView mEmail;
    TextView mPassword;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        mEmail = (TextView) findViewById(R.id.txtSignInEmail);
        mPassword = (TextView) findViewById(R.id.txtSignInPass);
        mAuth = FirebaseAuth.getInstance();
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void OnSignInClick(View view) {
        //Checks if the fields are empty
        if( TextUtils.isEmpty(mEmail.getText())){
            mEmail.setError( "Email is required!" );
        }
        else if( TextUtils.isEmpty(mPassword.getText())){
            mPassword.setError( "Password is required!" );
        }
        else{
            String email = mEmail.getText().toString();
            String pass = mPassword.getText().toString();

            //Checks if the email is valid, then signs the user in
            if(isEmailValid(email)){
                signIn(email, pass);
            } else {
                mEmail.setError( "Not valid email format!" );
            }

        }
    }

    private void signIn(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //If the email and password match with an account, it starts the homepage activity
                            Intent intent = new Intent(SignInActivity.this, Homepage.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this, "Sign In failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }
}

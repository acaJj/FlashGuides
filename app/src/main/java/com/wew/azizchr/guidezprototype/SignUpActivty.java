package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Chris created all of this as well as the matching XML file.
 * Signs the user up for an account on Firebase with their email, password and name.
 */


public class SignUpActivty extends AppCompatActivity {


    TextView mEmail;
    TextView mPassword;
    TextView mConfirmPassword;
    TextView mFirstName;
    TextView mLastName;

    private FirebaseConnection mFirebaseConnection;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_activty);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        mFirstName = findViewById(R.id.txtFirstName);
        mLastName = findViewById(R.id.txtLastName);
        mEmail = (TextView) findViewById(R.id.txtSignUpEmail);
        mPassword = (TextView) findViewById(R.id.txtSignUpPassword);
        mConfirmPassword = (TextView) findViewById(R.id.txtConfirmPassword);

        mFirebaseConnection = new FirebaseConnection();
        mAuth = mFirebaseConnection.getFirebaseAuthInstance();
        mFirebaseFirestore = mFirebaseConnection.getFirestoreInstance();
    }

    public void onSignUpClick(View view) {
        //Checks if the fields are empty
        if( TextUtils.isEmpty(mEmail.getText())){
            mEmail.setError( "Email is required!" );
        }
        else if( TextUtils.isEmpty(mPassword.getText())){
            mPassword.setError( "Password is required!" );
        }
        else if( TextUtils.isEmpty(mConfirmPassword.getText())){
            mConfirmPassword.setError( "Password is required!" );
        }
        else{
            String firstName = mFirstName.getText().toString();
            String lastName = mLastName.getText().toString();
            String email = mEmail.getText().toString();
            String pass = mPassword.getText().toString();
            String confPass = mConfirmPassword.getText().toString();

            //Checks if the email is valid, passwords match and are in the proper range
            if(isEmailValid(email)){
                if(pass.length() >= 6 && pass.length() <= 20){
                    if(pass.equals(confPass)) {
                        //Signs the user up using the email and password provided
                        createAccount(firstName,lastName,email, pass);
                    } else{
                        mConfirmPassword.setError("Passwords must match!");
                    }
                }else{
                    mPassword.setError("Password length must be between 6-20 characters!");
                }
            } else {
                mEmail.setError( "Not valid email format!" );
            }
        }
    }

    //Checks to see if the email is a valid format.
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Creates an account using the provided email and password
    private void createAccount(final String fn,final String ln,final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //adds new user info to the database
                            User newUser = new User(mAuth.getCurrentUser().getUid());
                            newUser.setFirstName(fn);
                            newUser.setLastName(ln);
                            newUser.setEmail(email);
                            newUser.setPassword(password);
                            newUser.setNumGuides(0);
                            DocumentReference newUserDoc = mFirebaseFirestore.document("Users/" + mAuth.getUid());
                            newUserDoc.set(newUser);
                            //Account was created, user was signed in, and now switching to homepage
                            Intent intent = new Intent(SignUpActivty.this, Homepage.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivty.this, "Signup failed. Reason:" + task.getException(),
                                    Toast.LENGTH_LONG).show();
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
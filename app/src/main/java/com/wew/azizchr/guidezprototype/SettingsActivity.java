package com.wew.azizchr.guidezprototype;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Both Chris and Jeffrey have worked on this
 *
 * Chris was responsible for: Creating xml layout
 * Jeff was responsible for: Sending data in the edittext fields back to firebase
 */

public class SettingsActivity extends AppCompatActivity {

    private EditText mFirstName;
    private EditText mLastName;
    //private EditText mNickName; we dont use the username for anything so leave out for now
    private EditText mPassword;
    private EditText mConfirmPassword;
    private Button mBack;
    private Button mConfirm;

    private FirebaseConnection mFirebaseConnection;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseFirestore mFirestore;
    private DocumentReference userAccountRef;

    private User mUser = new User();
    private Boolean changesMade = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        mFirstName = findViewById(R.id.eFirstname);
        mLastName = findViewById(R.id.eLastname);
        //mNickName = findViewById(R.id.eUsername);
        mPassword = findViewById(R.id.ePassword);
        mConfirmPassword = findViewById(R.id.ePasswordChangeConfirm);
        mBack = findViewById(R.id.btnBackSettings);
        mConfirm = findViewById(R.id.btnConfirmSettings);

        mFirebaseConnection = new FirebaseConnection();
        mFirebaseAuth = mFirebaseConnection.getFirebaseAuthInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirestore = mFirebaseConnection.getFirestoreInstance();

        userAccountRef = mFirestore.document("Users/" + mFirebaseAuth.getUid());
        //gets user account info from db and sets it to our object
        userAccountRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                mUser.setUserName( documentSnapshot.getString("username"));
                mUser.setPassword(documentSnapshot.getString("password"));
                mUser.setFirstName(documentSnapshot.getString("firstName"));
                mUser.setLastName(documentSnapshot.getString("lastName"));
                int num = documentSnapshot.getLong("numGuides").intValue();
                mUser.setNumGuides(num);
                mUser.setId(documentSnapshot.getString("id"));
                mUser.setEmail("email");

                //Sets the current user information for the user's convenience
                mFirstName.setText(mUser.getFirstName());
                mLastName.setText(mUser.getLastName());
                mPassword.setText(mUser.getPassword());
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (changesMade){
                    new AlertDialog.Builder(SettingsActivity.this)
                            .setMessage("Pressing Yes will save all changes, are you sure you want to change these settings?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    updateUserSettings();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });

        //finish current activity and go back to the home page
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
            }
        });
    }

    public void updateUserSettings(){
        String newFirstName = mFirstName.getText().toString();
        String newLastName = mLastName.getText().toString();
        String newPass = mPassword.getText().toString();
        //String newNick = mNickName.getText().toString();
        if (!newFirstName.equals(mUser.getFirstName())){
            userAccountRef.update("firstName",newFirstName);
        }

        if (!newLastName.equals(mUser.getLastName())){
            userAccountRef.update("lastName",newLastName);
        }

       /* if (!newNick.equals(mUser.getUserName())){
            userAccountRef.update("userName",newNick);
        }*/

        if (!newPass.equals(mUser.getPassword())){
            //if there is a new password typed out, it must be between 6 and 20 chars
            if ((!newPass.isEmpty()) && (newPass.length() >=6 && newPass.length() <= 20)){
                if (newPass.equals(mConfirmPassword.getText().toString())){
                    userAccountRef.update("password", newPass);
                }else{
                    mConfirmPassword.setError("Passwords must match!");
                }
            }else{
                mPassword.setError("Password must be between 6 and 20 characters!");
            }
        }

        Toast.makeText(SettingsActivity.this,"Settings Saved!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }
}

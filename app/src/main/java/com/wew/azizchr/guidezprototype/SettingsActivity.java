package com.wew.azizchr.guidezprototype;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private TextView mCurrFirst;
    private TextView mCurrLast;
    private TextView mCurrNick;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mNickName;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private Button mBack;
    private Button mConfirm;

    private FirebaseUser mCurrentUser;
    private FirebaseFirestore mFirestore;
    private DocumentReference userAccountRef;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        //TODO: this documentPath is a null object reference, makes app crash, need to fix
        userAccountRef = mFirestore.document("Users/" + mFirebaseAuth.getUid());
        userAccountRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mUser = documentSnapshot.toObject(User.class);
            }
        });

        mCurrFirst = findViewById(R.id.txtCurrFirst);
        mCurrLast = findViewById(R.id.txtCurrLast);
        mCurrNick = findViewById(R.id.txtCurrNick);
        mFirstName = findViewById(R.id.eFirstname);
        mLastName = findViewById(R.id.eLastname);
        mNickName = findViewById(R.id.eUsername);
        mPassword = findViewById(R.id.ePassword);
        mConfirmPassword = findViewById(R.id.ePasswordChangeConfirm);
        mBack = findViewById(R.id.btnBackSettings);
        mConfirm = findViewById(R.id.btnConfirmSettings);

        //Sets the current user information for the user's convenience
        mCurrFirst.setText(mUser.getFirstName());
        mCurrLast.setText(mUser.getLastName());
        mCurrNick.setText(mUser.getUserName());
        mPassword.setText(mUser.getPassword());

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Make a menu that prompts the user to confirm their choices before updating firestore

                updateUserSettings();
            }
        });

        //finish current activity and go back to the home page
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void updateUserSettings(){
        String newFirstName = mFirstName.getText().toString();
        String newLastName = mLastName.getText().toString();
        String newPass = mPassword.getText().toString();
        if (!newFirstName.equals(mUser.getFirstName())){
            userAccountRef.update("firstname",newFirstName);
        }

        if (!newLastName.equals(mUser.getLastName())){
            userAccountRef.update("lastname",newLastName);
        }

        if (!newPass.equals(mUser.getPassword())){
            if (newPass.length() >=6 && newPass.length() <= 20){
                if (newPass.equals(mConfirmPassword.getText().toString())){
                    userAccountRef.update("password", newPass);
                }else{
                    mConfirmPassword.setError("Passwords must match!");
                }
            }else{
                mPassword.setError("Password must be between 6 and 20 characters!");
            }
        }
    }
}

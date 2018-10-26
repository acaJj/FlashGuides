package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class UserCollectionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;

    private RecyclerView guideCollection;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    public List<Result> searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_collection);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        //Gets a handle on the firebase modules
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);
        mStorage = FirebaseStorage.getInstance();

        //init the list
        searchResults = new ArrayList<>();

        //Get a handle on the recycler view
        guideCollection = findViewById(R.id.guideCollection);
        guideCollection.setHasFixedSize(true);

        //Creates and sets the layout for the recycler view
        mLayoutManager = new LinearLayoutManager(this);
        guideCollection.setLayoutManager(mLayoutManager);

        //Get all the guides that the user has created
        CollectionReference userGuides = mFirestore.collection("Users/" + mAuth.getUid() +"/guides");
        userGuides.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot doc:task.getResult()){
                        //For each document in the collection, create a Result object and add it to the list
                        Result guide = new Result();
                        guide.setTitle(doc.getString("title"));
                        guide.setName("you!");
                        guide.setDate("October 29th, 2018");
                        searchResults.add(guide);
                    }

                    //Once everything is obtained, it creates and sets the adapter
                    mAdapter = new SearchAdapter(searchResults);
                    guideCollection.setAdapter(mAdapter);
                    Toast.makeText(getApplicationContext(), searchResults.size() + " results." , Toast.LENGTH_SHORT).show();
                } else{

                    //This is included just incase something is wrong
                    testGuides();
                    mAdapter = new SearchAdapter(searchResults);
                    guideCollection.setAdapter(mAdapter);
                }
            }
        });
    }

    public void testGuides(){
        searchResults.add(new Result("Sample guide - Recycling", "you!", "October 29th, 2018"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }

}

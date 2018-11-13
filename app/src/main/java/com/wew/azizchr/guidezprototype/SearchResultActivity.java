package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.view.WindowManager;
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

import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;

    private RecyclerView mYourSearchResults;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private String searchByString;
    private String searchByUser;

    public List<Result> searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        //Get the intent
        Intent intent = getIntent();

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
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
        mYourSearchResults = findViewById(R.id.YourSearchResults);
        mYourSearchResults.setHasFixedSize(true);

        //Creates and sets the layout for the recycler view
        mLayoutManager = new LinearLayoutManager(this);
        mYourSearchResults.setLayoutManager(mLayoutManager);


        //TODO: Jeff can you implement the search function here using the following strings?

        //Retrieves the content entered by the user from the previous activity
        searchByString = intent.getStringExtra("SearchString");
        searchByUser = intent.getStringExtra("SearchUser");

        /*

        //Get all the guides that the user has created
        CollectionReference userGuides = mFirestore.collection("Users/" + mAuth.getUid() + "/guides");
        userGuides.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        //For each document in the collection, create a Result object and add it to the list
                        Result guide = new Result();
                        guide.setTitle(doc.getString("title"));
                        guide.setName("idk man");
                        guide.setDate(doc.getString("dateCreated"));
                        guide.setKey(doc.getId());
                        guide.setId(doc.getString("id"));
                        searchResults.add(guide);
                    }

                    //Once everything is obtained, it creates and sets the adapter
                    mAdapter = new SearchAdapter(searchResults);
                    mYourSearchResults.setAdapter(mAdapter);
                    Toast.makeText(getApplicationContext(), searchResults.size() + " results.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        });* */
    }

    // When back is pressed, do an animation
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }
}
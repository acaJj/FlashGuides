package com.wew.azizchr.guidezprototype;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

public class UserCollectionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;

    private RecyclerView guideCollection;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_collection);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        guideCollection = findViewById(R.id.guideCollection);
        mLayoutManager = new LinearLayoutManager(this);
        guideCollection.setLayoutManager(mLayoutManager);
        //TODO: Create our own adapter class by extending RecyclerView.Adapter

        CollectionReference userGuides = mFirestore.collection("Users/" + mAuth.getUid() +"/guides");
        userGuides.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String[] mDataset = new String[30];
                    int counter = 0;
                    for (QueryDocumentSnapshot doc:task.getResult()){
                        //For each document in the collection, want to populate an array with info for adapter
                        mDataset[counter] = doc.getString("title");
                        counter++;
                    }
                    //set the adapter to the recycler view
                    mAdapter = new CollectionAdapter(mDataset);
                    guideCollection.setAdapter(mAdapter);
                }
            }
        });

    }
}

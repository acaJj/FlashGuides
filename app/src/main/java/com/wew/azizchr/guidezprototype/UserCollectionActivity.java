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
import android.widget.FrameLayout;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_collection);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);
        mStorage = FirebaseStorage.getInstance();

        guideCollection = findViewById(R.id.guideCollection);
        mLayoutManager = new LinearLayoutManager(this);
        guideCollection.setLayoutManager(mLayoutManager);
        //TODO: Create the getter functions for retrieving guide data from firestore

        CollectionReference userGuides = mFirestore.collection("Users/" + mAuth.getUid() +"/guides");
        userGuides.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ArrayList<Guide> guideList = new ArrayList<>();
                    int counter = 0;
                    for (QueryDocumentSnapshot doc:task.getResult()){
                        //For each document in the collection, populate the list with info for adapter
                        Guide guide = new Guide(doc.getString("title"));
                        guide.setId(doc.getString("id"));
                        guideList.add(counter ,guide);
                        counter++;
                    }
                    Log.i("BORBOT LIST",""+guideList.get(0).getId());
                    //set the adapter to the recycler view
                    mAdapter = new CollectionAdapter(guideList, new CollectionAdapter.OnItemClickListener() {
                        @Override
                        public void OnItemClick(Guide item) {
                            Intent intent = new Intent(UserCollectionActivity.this,CreateNewGuide.class);
                            //the guide Id is used for lookup on which specific guide to get while EDIT mode tells the activity we are not making a guide from scratch
                            intent.putExtra("GUIDEID",item.getId());
                            intent.putExtra("MODE","EDIT");
                            startActivity(intent);
                        }
                    });
                    guideCollection.setAdapter(mAdapter);
                }
            }
        });

    }
}

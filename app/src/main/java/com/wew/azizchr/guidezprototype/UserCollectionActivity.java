package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

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
        guideCollection.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            String tag = "";
            String title = "";
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View v = rv.findChildViewUnder(e.getX(),e.getY());
                if (v != null){
                    TextView text = (TextView) v;
                    tag = text.getTag().toString();
                    title = text.getText().toString();
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                //If OnInterceptTouchEvent returns true, then this event is fired
                //create a new intent and start the CreateNewGuide Activity to edit the chosen guide
                Intent intent = new Intent(UserCollectionActivity.this,CreateNewGuide.class);
                intent.putExtra("GUIDEID",tag);//the guide id is sent to so CreateNewGuide knows which guide to get
                intent.putExtra("MODE","EDIT");//tells activity that we are editing one already made
                startActivity(intent);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        guideCollection.setLayoutManager(mLayoutManager);
        //TODO: Create the getter functions for retrieving guide data from firestore

        CollectionReference userGuides = mFirestore.collection("Users/" + mAuth.getUid() +"/guides");
        userGuides.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    Guide[] mDataset = new Guide[30];
                    int counter = 0;
                    for (QueryDocumentSnapshot doc:task.getResult()){
                        //For each document in the collection, want to populate an array with info for adapter
                        Guide guide = new Guide(doc.getString("title"));
                        mDataset[counter] = guide;
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

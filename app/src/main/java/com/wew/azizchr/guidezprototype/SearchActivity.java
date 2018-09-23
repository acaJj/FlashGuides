package com.wew.azizchr.guidezprototype;

import android.app.DownloadManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;

    private Spinner spinner;
    private Button btnSearch;

    private RecyclerView searchCollection;
    private RecyclerView.LayoutManager mLayoutManager;
    private CollectionAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);

        spinner = findViewById(R.id.userIdSpinner);
        btnSearch = findViewById(R.id.btnSearch);

        searchCollection = findViewById(R.id.searchResults);
        mLayoutManager = new LinearLayoutManager(SearchActivity.this);
        searchCollection.setLayoutManager(mLayoutManager);
        final List<String> list = new ArrayList<>();

        CollectionReference users = mFirestore.collection("Users");
        users.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot doc: task.getResult()){
                        list.add(doc.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this,
                            R.layout.support_simple_spinner_dropdown_item,list);
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                }else{
                    Log.i("WHAT:","damn");
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = spinner.getSelectedItem().toString();
                //mAdapter.clear();
                retrievePublishedGuides(id);
            }
        });
    }

    private void retrievePublishedGuides(String id) {
        CollectionReference userGuides = mFirestore.collection("Users/" +id+"/guides");

        userGuides.whereEqualTo("published",true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Guide> guideList = new ArrayList<>();
                    int counter = 0;
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        //For each document in the collection, populate the list with info for adapter
                        Guide guide = new Guide(doc.getString("title"));
                        guide.setKey(doc.getId());
                        guide.setId(doc.getString("id"));
                        guideList.add(counter, guide);
                        counter++;
                    }

                    //set the adapter to the recycler view
                    mAdapter = new CollectionAdapter(guideList, new CollectionAdapter.OnItemClickListener() {
                        @Override
                        public void OnItemClick(Guide item) {
                            Intent intent = new Intent(SearchActivity.this, CreateNewGuide.class);
                            //the guide Id is used for lookup on which specific guide to get while EDIT mode tells the activity we are not making a guide from scratch
                            intent.putExtra("GUIDEID", item.getId());
                            intent.putExtra("Key", item.getKey());
                            intent.putExtra("MODE", "EDIT");
                            startActivity(intent);
                        }
                    });

                    searchCollection.setAdapter(mAdapter);
                }
            }
        });
    }
}
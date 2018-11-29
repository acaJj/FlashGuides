package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeffrey on 2018-11-22.
 * Lets the user search using a string and or a user's name
 */

public class SearchResultActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;
    //Algolia id and api keys for search
    private String searchApiKey = "41400957b43bf3f86fd66e448345860f";//do not use the admin key
    private String applicationId = "031024FLJM";
    private Client client;
    private Index guidesIndex;

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

        //Retrieves the content entered by the user from the previous activity
        searchByString = intent.getStringExtra("SearchString");
        searchByUser = intent.getStringExtra("SearchUser");

        client = new Client(applicationId, searchApiKey);
        guidesIndex = client.getIndex("guides");
        searchDatabase(searchByString,searchByUser);
    }

    /**
     * Begin searching based on the users search params
     * @param searchByString guide should have this in the title
     * @param searchByUser the user whose guides we want
     * @return a list of all guides searched by the user
     */
    public void searchDatabase(String searchByString, String searchByUser){
        //List<Result> results = new ArrayList<>();

        //if both strings aren't empty, search by title and user
        if (!searchByString.equals("NULL") && !searchByUser.equals("NULL")){
            getResultsByTitleAndUser(searchByString,searchByUser);
        }else if (searchByString.equals("NULL") && !searchByUser.equals("NULL")){//else if title is empty, search all guides by specific user
            getResultsByUser(searchByUser);
        }else if (!searchByString.equals("NULL") && searchByUser.equals("NULL")){//else if user is empty, search all guides by title
            getResultsByTitle(searchByString);
        }else{//both are empty, just get all guides
            getAllResults();
        }
    }

    private void getResultsByTitleAndUser(String searchByString, final String searchByUser) {
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(searchByString)
                .setAttributesToRetrieve("title","author","userId","objectID","key")
                .setHitsPerPage(50);
        guidesIndex.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {
                try {
                    JSONArray hits = jsonObject.getJSONArray("hits");
                    for (int i =0;i < hits.length();i++){
                        JSONObject hitsJSONObject = hits.getJSONObject(i);
                        Log.i("SEARCH RESULT:",""+hitsJSONObject.get("title")+"/"+hitsJSONObject.get("author")+"/"+hitsJSONObject.get("objectID"));
                        Log.i("SEARCH RESULT:",""+hitsJSONObject.getString("userId"));
                        //for all the guides return, check if the author is the one we want
                        if (hitsJSONObject.getString("author").equalsIgnoreCase(searchByUser)){
                            Result result = new Result();
                            result.setTitle(hitsJSONObject.getString("title"));
                            result.setName(hitsJSONObject.getString("author"));
                            result.setId(hitsJSONObject.getString("objectID"));
                            result.setUserId(hitsJSONObject.getString("userId"));
                            result.setKey(hitsJSONObject.getString("key"));
                            result.setDestination("View");
                            searchResults.add(result);
                        }
                    }
                    //Once everything is obtained, it creates and sets the adapter
                    mAdapter = new SearchAdapter(searchResults);
                    mYourSearchResults.setAdapter(mAdapter);
                    Toast.makeText(getApplicationContext(), searchResults.size() + " results." , Toast.LENGTH_SHORT).show();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void getResultsByUser(String searchByUser) {
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query( searchByUser)
                .setAttributesToRetrieve("title","author","userId","objectID","key")
                .setHitsPerPage(50);
        guidesIndex.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {
                try {
                    JSONArray hits = jsonObject.getJSONArray("hits");
                    for (int i =0;i < hits.length();i++){
                        JSONObject hitsJSONObject = hits.getJSONObject(i);
                        Log.i("SEARCH RESULT:",""+hitsJSONObject.get("title")+"/"+hitsJSONObject.get("author")+"/"+hitsJSONObject.get("objectID"));
                        Log.i("SEARCH RESULT:",""+hitsJSONObject.getString("userId"));
                        Result result = new Result();
                        result.setTitle(hitsJSONObject.getString("title"));
                        result.setName(hitsJSONObject.getString("author"));
                        result.setId(hitsJSONObject.getString("objectID"));
                        result.setUserId(hitsJSONObject.getString("userId"));
                        result.setKey(hitsJSONObject.getString("key"));
                        result.setDestination("View");
                        searchResults.add(result);
                    }
                    //Once everything is obtained, it creates and sets the adapter
                    mAdapter = new SearchAdapter(searchResults);
                    mYourSearchResults.setAdapter(mAdapter);
                    Toast.makeText(getApplicationContext(), searchResults.size() + " results." , Toast.LENGTH_SHORT).show();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void getResultsByTitle(String searchByString) {
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query( searchByString)
                .setAttributesToRetrieve("title","author","objectID","userId","key")
                .setHitsPerPage(50);
        guidesIndex.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {
                try {
                    JSONArray hits = jsonObject.getJSONArray("hits");
                    Log.i("SEARCH RESULT:",""+hits.length());
                    for (int i =0;i < hits.length();i++){
                        JSONObject hitsJSONObject = hits.getJSONObject(i);
                        Log.i("SEARCH RESULT:",""+hitsJSONObject.get("title")+"/"+hitsJSONObject.get("author")+"/"+hitsJSONObject.get("objectID"));
                        Result result = new Result();
                        result.setTitle(hitsJSONObject.getString("title"));
                        result.setName(hitsJSONObject.getString("author"));
                        result.setId(hitsJSONObject.getString("objectID"));
                        result.setUserId(hitsJSONObject.getString("userId"));
                        result.setKey(hitsJSONObject.getString("key"));
                        result.setDestination("View");
                        searchResults.add(result);
                    }
                    //Once everything is obtained, it creates and sets the adapter
                    mAdapter = new SearchAdapter(searchResults);
                    mYourSearchResults.setAdapter(mAdapter);
                    Toast.makeText(getApplicationContext(), searchResults.size() + " results." , Toast.LENGTH_SHORT).show();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void getAllResults() {
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query()
                .setAttributesToRetrieve("title","author","objectID","userId","key")
                .setHitsPerPage(50);
        guidesIndex.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {
                try {
                    JSONArray hits = jsonObject.getJSONArray("hits");
                    for (int i =0;i < hits.length();i++){
                        JSONObject hitsJSONObject = hits.getJSONObject(i);
                        Log.i("SEARCH RESULT:",""+hitsJSONObject.get("title")+"/"+hitsJSONObject.get("author")+"/"+hitsJSONObject.get("objectID"));
                        Result result = new Result();
                        result.setTitle(hitsJSONObject.getString("title"));
                        result.setName(hitsJSONObject.getString("author"));
                        result.setId(hitsJSONObject.getString("objectID"));
                        result.setUserId(hitsJSONObject.getString("userId"));
                        result.setKey(hitsJSONObject.getString("key"));
                        result.setDestination("View");
                        searchResults.add(result);
                    }
                    //Once everything is obtained, it creates and sets the adapter
                    mAdapter = new SearchAdapter(searchResults);
                    mYourSearchResults.setAdapter(mAdapter);
                    Toast.makeText(getApplicationContext(), searchResults.size() + " results." , Toast.LENGTH_SHORT).show();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    // When back is pressed, do an animation
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
    }
}
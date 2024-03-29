package com.wew.azizchr.guidezprototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Both Chris and Jeffrey have worked on this
 *
 * Chris was responsible for: Creating xml layout, add text, step and image, implementing image editor,
 *                            swapping and deleting images, deleting steps, and reordering steps.
 * Jeff was responsible for: all firebase methods,debugging, guide loading, proper uploading of data
 */

public class CreateNewGuide extends AppCompatActivity {

    private static final String DEBUG_TAG = "BORBOT";//a made up word to make our log statements easier to find
    private static final int SELECT_FILE =0;//used when selecting an image file
    private static final int WRITE_STEP =1;//used when making a new step
    private static final int WRITE_DESC =2;//used when making a new text block
    private static final int EDIT_DESC = 3;//used when editing a text block
    public static final int PESDK_RESULT = 4;//used when getting a picture from the image editor

    private String outputPath;//stores path of an image f
    private String newStepTitle;
    private String newStepDesc;
    private String newDesc;
    private String mode;//used to determine whether we are making a new guide or editing an old one

    private int guideNum = 0;//the current guide index thing
    private int currentPictureSwap;//index of the picture being swapped out for a different/edited pic
    private int indexToPlaceView;//the index in the selected step layout where we place the next text/imageview

    private Boolean isSwapping;//used as check whenw we add an image
    private int stepIndex;//used to determine where the step should be created in the layout feed

    private CameraImagePicker camera;
    public LinearLayout layoutFeed;
    public LinearLayout selectedLayout;
    public WebView selectedWebView; //changed from text to web view for better memory consumption and text formatting
    public ImageView selectedImageView;//used for bitmaps

    TextView mNewGuideTitle;

    //The title of the guide, initialized as NULL so we can easily check through string methods if it hasn't been set
    String guideTitle = "NULL";

    //Firebase Instance Variables
    private FirebaseConnection mFirebaseConnection;
    private FirebaseStorage mStorage;
    private StorageReference imgStorage;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mFirebaseAuth;

    //Cloud Firestore Reference Variables
    private CollectionReference stepData;
    private CollectionReference textData;
    private CollectionReference picData;
    private DocumentReference userRef;

    //ArrayList stores metadata for the guide text and picture components
    private ArrayList<GuideData> mGuideDataArrayList = new ArrayList<>();
    private Guide newGuide; //guide object that stores descriptive info on the guide being made

    //used when saving so we know not to save the copies of the same data objects in db
    private boolean haveSaved;

    //Images used for the step buttons
    public Drawable textIcon;
    public Drawable photoIcon;

    public ArrayList<Bitmap> mBitmaps;

    public LinearLayout uploadGuideProgressBar;
    public TextView uploadGuideText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_guide);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        //newGuide = new Guide();
        mBitmaps = new ArrayList<>();

        //Gets a handle on the progress bar layout and hides it
        uploadGuideProgressBar = (LinearLayout) findViewById(R.id.uploadGuideLL);
        uploadGuideProgressBar.setVisibility(View.GONE);
        uploadGuideText = (TextView) findViewById(R.id.uploadGuideTextView);

        mNewGuideTitle = (TextView) findViewById(R.id.txtNewGuideTitle);

        //Initialize firebase variables
        mFirebaseConnection = new FirebaseConnection();
        mFirebaseAuth = mFirebaseConnection.getFirebaseAuthInstance();
        FirebaseUser mCurrentUser = mFirebaseConnection.getCurrentUser();
        mStorage = mFirebaseConnection.getStorageInstance();
        imgStorage = mFirebaseConnection.getImgStorage();
        mFirestore = mFirebaseConnection.getFirestoreInstance();

        //Gets the guide name variable from previous activity and puts it in the title
        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        if(bundle != null){
            guideTitle = bundle.getString("GUIDE_TITLE");
            mode = bundle.getString("MODE");
        } else{
            guideTitle = "Guide (No title)";
            mode = "CREATE";
        }
        Log.i(DEBUG_TAG+"MODE",mode);
        userRef = mFirestore.document("Users/" + mFirebaseAuth.getUid());

        //gets the number of guides the user has
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                guideNum = documentSnapshot.getLong("numGuides").intValue();

                if (mode.equals("CREATE"))guideNum++;// increments guideNum by 1 because we are making a new guide so there is 1 more than before
                String authorName = ""+documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");
                editorSetup(bundle);
                newGuide.setAuthor(authorName);
                newGuide.setTitle(guideTitle);
                newGuide.setPublishedStatus(false);
            }

        });

        mNewGuideTitle.setText(guideTitle);

        //Sets up the Camera Image Picker values
        camera = new CameraImagePicker(CreateNewGuide.this);
        camera.setImagePickerCallback(new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> list) {
                Uri imagePath = Uri.parse(list.get(0).getQueryUri());
                addImage(imagePath);
            }

            @Override
            public void onError(String s) {

            }
        });

        isSwapping = false;
        layoutFeed = findViewById(R.id.newGuideLayoutFeed);
        Button mSave = findViewById(R.id.btnSaveGuide);
        Button mPublish = findViewById(R.id.btnPublishGuide);

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveGuide();
            }
        });

        mPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (haveSaved){
                    //set guide to published and then index it and update the doc in firestore
                    newGuide.setPublishedStatus(true);
                    setSearchIndex();//index the guide into algolia
                    DocumentReference guideRef = mFirestore.document("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum);
                    guideRef.update("publishedStatus",true);
                    Toast.makeText(CreateNewGuide.this,"You've been published!",Toast.LENGTH_SHORT).show();
                }else{
                    new AlertDialog.Builder(CreateNewGuide.this)
                            .setMessage("You must save your work before publishing, would you like to save now?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //save the guide before adding it to the index so newGuide has values set for Algolia indexing
                                    saveGuide();
                                    newGuide.setPublishedStatus(true);
                                    setSearchIndex();
                                    DocumentReference guideRef = mFirestore.document("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum);
                                    guideRef.update("publishedStatus",true);
                                    Toast.makeText(CreateNewGuide.this,"You've been published!",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(CreateNewGuide.this,"Guide not published",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                }
            }
        });

        //Gets the images from the drawable folder for the step buttons
        textIcon = CreateNewGuide.this.getResources().getDrawable( R.drawable.icon_style_addtext );
        photoIcon = CreateNewGuide.this.getResources().getDrawable( R.drawable.icon_style_addpic );

    }

    /**
     * sets initial guide parameters depending on what mode we enter the editor in
     * @param bundle contains info about the guide like its id.
     */
    private void editorSetup(Bundle bundle){
        if (mode.equals("CREATE")){
            newGuide = new Guide(UUID.randomUUID().toString());
        }else if (mode.equals("EDIT")){
            newGuide = new Guide(bundle.getString("GUIDEID"),bundle.getString("KEY"),bundle.getString("GUIDE_TITLE"));
            newGuide.setDateCreated(bundle.getString("DATE"));
            guideNum = Integer.parseInt(bundle.getString("KEY"));
           // haveLoaded = true;
            loadGuide(newGuide.getId(),newGuide.getKey());
            Log.i(DEBUG_TAG+"EDITs","" + newGuide.getId());
        }
        //sets these 2 collections to point to the folders for the guide data of the new guide. for later uploading
        textData = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum+"/textData");
        picData = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum+"/imageData");
        stepData = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum+"/stepData");
    }

    /**
     * Loads up a guide in storage for further editing
     * @param id of the guide we are editing
     * @param key the name of the guide in the firestore database
     */
    private void loadGuide(String id,String key) {
        //References to the guide we are editing and its component collections
        DocumentReference guideToEdit = mFirestore.document("Users/" + mFirebaseAuth.getUid() +"/guides/"+key);
        CollectionReference guideSteps = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+key + "/stepData");
        final CollectionReference guideText = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+key + "/textData");
        final CollectionReference guideImgs = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+key + "/imageData");

        //get the title
        guideToEdit.get().addOnCompleteListener(CreateNewGuide.this,new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()){

                        String title = doc.get("title").toString();
                        mNewGuideTitle.setText(title);
                    }
                }
            }
        });

        //get the stored guide data
        guideSteps.get().addOnCompleteListener(CreateNewGuide.this,new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int stepNum;
                String stepTitle;
                if (task.isSuccessful()){
                    //for each step in the guide, get its info and recreate the step
                    for (QueryDocumentSnapshot doc: task.getResult()){
                        stepNum = doc.getLong("stepNumber").intValue();
                        stepTitle = doc.get("stepTitle").toString();
                        addStep(stepTitle,"");
                        stepIndex++;//increment stepIndex so steps are placed in order

                        Query stepText = guideText.whereEqualTo("stepNumber",stepNum);
                        Query stepImgs = guideImgs.whereEqualTo("stepNumber", stepNum);
                        stepText.get().addOnCompleteListener(CreateNewGuide.this, new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot snap: task.getResult()){
                                        Blob text = (Blob)snap.get("text");
                                        String id = snap.get("id").toString();
                                        String guideId = snap.get("guideId").toString();
                                        String stepTitle = snap.get("stepTitle").toString();
                                        String type = snap.get("type").toString();
                                        String textType = snap.get("textType").toString();
                                        int num = snap.getLong("stepNumber").intValue();
                                        int placement = snap.getLong("placement").intValue();
                                        TextData data = new TextData(id,type,placement,guideId);
                                        data.setText(text);
                                        data.setTextType(textType);
                                        data.setStep(num,stepTitle);
                                        //data.setId(id);
                                        //Log.i(DEBUG_TAG+"-TEXT EXECUTE","in chained task, data added / " + data.getId() );

                                        addElement(data);
                                    }
                                }
                            }
                        });

                        stepImgs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot snap: task.getResult()){
                                        //get the data from fire store and add a data object to the data list
                                        String id = snap.get("id").toString();
                                        String guideId = snap.get("guideId").toString();
                                        String stepTitle = snap.get("stepTitle").toString();
                                        String type = (String)snap.get("type");
                                        int num = snap.getLong("stepNumber").intValue();
                                        int placement = snap.getLong("placement").intValue();
                                        String imgPath = snap.get("imgPath").toString();
                                        String uri = snap.get("uri").toString();
                                        PictureData data = new PictureData(id,type,placement,guideId);
                                        data.setStep(num,stepTitle);
                                        data.setUri(uri);
                                        data.setImgPath(imgPath);

                                        //Log.i(DEBUG_TAG+"IMG EXECUTE","in chained task, data added / " + data.getId() );
                                        addElement(data);
                                    }
                                }
                            }
                        });

                        //Log.i(DEBUG_TAG+"EXECUTE","in Guide steps / " + doc.getId());
                    }
                }

            }
        });
    }

    public void addElement(GuideData data){
        int stepNumber = data.getStepNumber();

        View elementView = new View(CreateNewGuide.this);
        //create the right view with the objects data and sets it to elementView
        if (data.getType().equals("Text")){
            TextData textData = (TextData) data;
            elementView = loadText(textData);
        }else if (data.getType().equals("Picture")){
            PictureData pictureData = (PictureData)data;
            StorageReference imageToLoad = imgStorage.child(pictureData.getImgPath());
            Log.d(DEBUG_TAG + " check imgPath",pictureData.getImgPath());
            elementView = loadImage(pictureData,imageToLoad);
        }

        LinearLayout stepLayout = (LinearLayout)layoutFeed.getChildAt(stepNumber-1);
        //Log.i(DEBUG_TAG+"-Elements",""+data.getStepNumber()+"/"+data.getPlacement());

        //create data block for the view and the buttons
        LinearLayout newDataBlock = new LinearLayout(CreateNewGuide.this);
        newDataBlock.setOrientation(LinearLayout.VERTICAL);
        newDataBlock.addView(elementView);
        newDataBlock.addView(createAddDataButtonLayout(stepLayout));
        newDataBlock.setTag(R.id.viewType,"DataBlock");

        //goes through the step to find the right location for the element and place it in
        for (int i =0; i<= stepLayout.getChildCount()-1;i++){
            //if there is no object in the step (only the title and buttons), place the element and get out
            if (stepLayout.getChildCount() == 2){
                //if the second child directly in step has viewType tag of 'ButtonBar', then delete it
                if (stepLayout.getChildAt(1).getTag(R.id.viewType).equals("ButtonBar")){
                    stepLayout.removeViewAt(stepLayout.getChildCount()-1);
                    stepLayout.addView(newDataBlock);
                    break;
                }
            }
            int currentViewIndex = i+1;//add 1 because the first element in step is the title layout

            //if we have reached the end of the layout and still haven't added it, put it at the end

            Log.i("PLACEMENTS-END",""+currentViewIndex + "/" + stepLayout.getChildCount());
            if (currentViewIndex == stepLayout.getChildCount()){
                stepLayout.addView(newDataBlock);
                Log.i("PLACEMENTS-END","Adding to the end");
                break;
            }
            int currentViewPlacement = 0;

            LinearLayout dataLayout = (LinearLayout) stepLayout.getChildAt(currentViewIndex);
            View currentView = dataLayout.getChildAt(0);//gets the data block in the first index of the dataLayout
            Log.i("Placements:",""+currentView.getTag(R.id.index)+"/"+(String)elementView.getTag(R.id.index));
            //try to get the placement for the currentView we are comparing to
            try{

                currentViewPlacement = Integer.parseInt((String)currentView.getTag(R.id.index));
            }catch(NumberFormatException ex){
                Log.i(DEBUG_TAG+"EXCEPTION!",ex.getMessage());
                //if we end up here then the element goes at the end of the step
                stepLayout.addView(newDataBlock,currentViewIndex);
                break;
            }

            int elementViewPlacement = Integer.parseInt((String)elementView.getTag(R.id.index));

            //compare the placement of the elementView and currentView
            if (elementViewPlacement<currentViewPlacement){//if less, then it goes before it in the step
                stepLayout.addView(newDataBlock,currentViewIndex);
                break;
            }

            Log.i(DEBUG_TAG,data.getId()+"/"+elementView.getParent());
        }
    }

    public WebView loadText(final TextData text){
        try{
            //Creates a new textview and sets the tag (the tag is the current step number)
            WebView mDescription = new WebView(CreateNewGuide.this);
            String description = text.getStringFromBlob();

            //set tag to the text so we can easily get it when uploading, haven't yet figured out how to get from webview
            String viewTag = "TEXT--" +description;
            mDescription.setTag(R.id.viewId,viewTag);
            mDescription.setTag(R.id.textType,text.getTextType());
            Log.i("Placement","text-"+text.getPlacement());
            mDescription.setTag(R.id.index,""+text.getPlacement());

            mDescription.setPadding(5, 10, 5, 10);
            mDescription.loadData(description,"text/html","UTF-8");
            mDescription.setBackgroundColor(Color.TRANSPARENT);

            mDescription.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_UP:
                            view.performClick();
                            displayTextOptionsMenu(view,text.getTextType());
                    }
                    return true;
                }
            });

            //adds the new text block with the text to the selected step

            haveSaved = false;
            return mDescription;
        }catch (Exception ex){
            ex.getMessage();
            return null;
        }
    }

    /**
     * Loads an image from Firebase Storage into a new image view
     * @param ref of the image we are getting from storage
     */
    public ImageView loadImage(PictureData pictureData,StorageReference ref){
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getApplicationContext());
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        try{
            //Creates the new imageview
            final ImageView newImgView = new ImageView(CreateNewGuide.this);
            GlideApp.with(CreateNewGuide.this).asBitmap().load(ref)
                    .placeholder(circularProgressDrawable).into(newImgView);
            Glide.with(CreateNewGuide.this)
                    .asBitmap()
                    .load(ref)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            //newImgView.setImageBitmap(resource);

                            //addImgBitmap(newImgView,resource);

                            //gets the mediapath of the image and parses it into a uri we can work with
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            String path = MediaStore.Images.Media.insertImage(CreateNewGuide.this.getContentResolver(), resource, UUID.randomUUID().toString(), "drawing");
                            final Uri imageUri = Uri.parse(path);
                            String viewTag = "PICTURE--" + imageUri.toString();
                            //Log.d(DEBUG_TAG+" imageViewTag",viewTag);
                            newImgView.setTag(R.id.viewId,viewTag);
                            newImgView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    displayImageOptionMenu(imageUri, v);
                                }
                            });
                        }
                    });

            //fits the image to the sides, fixes the view bounds, adds padding
            newImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newImgView.setAdjustViewBounds(true);
            newImgView.setPadding(3, 10, 3, 10);

            //add it to the layout
            Log.i("Placement","img-"+pictureData.getPlacement());
            newImgView.setTag(R.id.index,""+pictureData.getPlacement());
            return newImgView;

        }catch(Exception ex){
            Log.i("IMAGE ERROR: ", ex.getMessage());
            return null;
        }
    }

    public void onClickStep(View view) {
        stepIndex = layoutFeed.getChildCount()-1;//if user clicks add step button, new step is added at the end
        createNewStep();
    }

    public void onClickGuideTitle(View view) {
        editGuideTitle(view);
    }

    /**
     * Saves the guide into firestore
     */
    private void saveGuide(){

        //Shows the progress bar
        uploadGuideProgressBar.setVisibility(View.VISIBLE);

        //clear current guide list and populate it with all current block data
        mGuideDataArrayList.clear();
        PopulateUploadList();

        DocumentReference guideRef = mFirestore.document("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum);
        Log.d(DEBUG_TAG+"-SAVE",mode);
        //set the document key to guideNum for upload
        if (mode.equals("CREATE")){
            newGuide.setKey(""+guideNum);
        }

        //if theres no date, set it to the current date
        if (newGuide.getDateCreated() == null || newGuide.getDateCreated().equals("unknown")){
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.CANADA);
            //DateFormat.getDateInstance(DateFormat.LONG, Locale.CANADA);
            String dateFormatted = dateFormat.format(currentDate);
            newGuide.setDateCreated(dateFormatted);
        }

        guideRef.set(newGuide);

        //informs the user that the save process is starting
        Toast.makeText(CreateNewGuide.this,"Saving...\nPlease wait",Toast.LENGTH_LONG).show();


        //delete all the guide data currently in the db and re-populate with the new guide data
        stepData.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        doc.getReference().delete();
                    }
                    textData.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (DocumentSnapshot doc: task.getResult()){
                                    doc.getReference().delete();
                                }
                                //all data is stored in custom objects and added to an array list, we iterate through that to upload.
                                //the order the objects are stored in the array is the order they're laid out in the layout.
                                for (int i = 0; i < mGuideDataArrayList.size(); i++){
                                    //gets the next data object and uploads depending on the type of data we have
                                    GuideData dataToSave = mGuideDataArrayList.get(i);
                                    if (dataToSave.getType().equals("Text")){
                                        TextData textDataPkg = (TextData) dataToSave;
                                        mFirebaseConnection.uploadText(textDataPkg,textData,guideNum);
                                    }
                                }
                            }
                        }
                    });

                    picData.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    doc.getReference().delete();
                                }
                                ArrayList<PictureData> picturesToUpload = new ArrayList<>();
                                //all data is stored in custom objects and added to an array list, we iterate through that to upload.
                                //the order the objects are stored in the array is the order they're laid out in the layout.
                                for (int i = 0; i < mGuideDataArrayList.size(); i++){
                                    //gets the next data object and uploads depending on the type of data we have
                                    GuideData dataToSave = mGuideDataArrayList.get(i);
                                    if (dataToSave.getType().equals("Picture")){
                                        picturesToUpload.add((PictureData) dataToSave);

                                    }
                                }
                                //Log.i("IMAGES",""+picturesToUpload.size());
                                mFirebaseConnection.uploadImages(getApplicationContext(),guideNum,picData,picturesToUpload, uploadGuideProgressBar,uploadGuideText);
                            }
                        }
                    });
                }
            }
        });

        //update the users guide count so the guides can be named differently, 'guide0', 'guide1', etc
        if (mode.equals("CREATE"))userRef.update("numGuides", guideNum);
        Toast.makeText(CreateNewGuide.this, "Guide Saved!", Toast.LENGTH_SHORT).show();

        //after guide is saved, wait 5 seconds for the last uploading to finish up then ask the user if they want to leave or go back
        //pops up too early sometimes, leave out until we can figure out a better solution
       /* Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                new AlertDialog.Builder(CreateNewGuide.this)
                        .setMessage("Do you want to continue editing, or go back?")
                        .setCancelable(false)
                        .setPositiveButton("Go Back", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                CreateNewGuide.this.finish();
                                overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
                            }
                        })
                        .setNegativeButton("Stay", null)
                        .show();
            }
        }, 5000);
*/
        haveSaved = true;
    }

    public void setSearchIndex(){
        //Algolia id and api keys for search
        String updateApiKey = "132c50036e3241722083caa0a25393e2";//do not use the admin key
        String applicationId = "031024FLJM";
        Client client = new Client(applicationId, updateApiKey);
        Index algoliaIndex = client.getIndex("guides");

        //index the guide title with Algolia so we can search it later
        try{
            //List<JSONObject> guideToIndex = new ArrayList<>();
            JSONObject guideObject = new JSONObject().//guide object
                    put("title",newGuide.getTitle()).put("author",newGuide.getAuthor())
                    .put("userId",mFirebaseAuth.getUid()).put("key",newGuide.getKey())
                    .put("date",newGuide.getDateCreated()).put("publishedStatus",newGuide.getPublishedStatus());
            //guideToIndex.add(new JSONObject(guideObject));
            algoliaIndex.addObjectAsync(guideObject, newGuide.getId(),null);
            Log.d(DEBUG_TAG,"indexing completed");
        }catch(Exception ex){
            Log.d(DEBUG_TAG, ex.getMessage());
        }
    }

    /**
     * Builds and displays a menu of options for placing a picture
     */
    private void SelectImage(){
        final CharSequence[] items = {"Camera","Gallery","Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("Add an Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Camera")){
                    outputPath = camera.pickImage();
                    overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                    //Intent intent = new Intent(CreateNewGuide.this,CameraViewActivity.class);
                    //startActivityForResult(intent,PESDK_RESULT);
                }else if(items[i].equals("Gallery")){
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select File"),SELECT_FILE);
                }else if(items[i].equals("Cancel")){
                    if(isSwapping){isSwapping = false;}
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Creates and adds a new ImageView to the guide's layoutFeed
     * @param imageUri the uri of the image being added
     * @return true if success, otherwise false
     */
    public boolean addImage(final Uri imageUri){
        try{
            //Creates the new imageview

            final ImageView newImgView = new ImageView(CreateNewGuide.this);
            String viewTag = "PICTURE--" + imageUri.toString();
            newImgView.setTag(R.id.viewId,viewTag);
            //Glide.with(CreateNewGuide.this).load(imageUri).into(newImgView);
            //final Boolean checkSwapping = isSwapping;
            Glide.with(CreateNewGuide.this)
                    .asBitmap()
                    .load(imageUri)
                    .transition(GenericTransitionOptions.with(R.anim.fui_slide_in_right))
                    .into(newImgView);

            newImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayImageOptionMenu(imageUri, v);
                }
            });

            //fits the image to the sides, fixes the view bounds, adds padding
            newImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newImgView.setAdjustViewBounds(true);
            newImgView.setPadding(3, 10, 3, 10);

            LinearLayout newDataBlock = new LinearLayout(CreateNewGuide.this);
            newDataBlock.setOrientation(LinearLayout.VERTICAL);
            newDataBlock.addView(newImgView);
            newDataBlock.addView(createAddDataButtonLayout(selectedLayout));
            newDataBlock.setTag(R.id.viewType,"DataBlock");
            if(isSwapping){
                selectedLayout.addView(newDataBlock, currentPictureSwap);
                selectedLayout.removeViewAt(currentPictureSwap + 1);
                currentPictureSwap = 0;
                isSwapping = false;
            }else {

                if (selectedLayout.getChildCount() == 2){
                    if (selectedLayout.getChildAt(1).getTag(R.id.viewType).equals("ButtonBar")){
                        selectedLayout.removeViewAt(selectedLayout.getChildCount()-1);
                    }
                    selectedLayout.addView(newDataBlock);
                }else{
                    Log.i(DEBUG_TAG,""+indexToPlaceView);
                    selectedLayout.addView(newDataBlock,indexToPlaceView);
                }

                //currentIndex++;
            }
            haveSaved = false;

        }catch(Exception ex){
            Log.i("IMAGE ERROR: ", ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Creates and adds a new text block to the layoutFeed
     * @param title of the new step block
     * @param desc of the new step block
     * @return true if success, otherwise false
     */
    public boolean addStep(String title, String desc){
        try{
            final LinearLayout newStepBlock = new LinearLayout(CreateNewGuide.this);
            newStepBlock.generateViewId();
            final LinearLayout newTitleBlock = new LinearLayout(CreateNewGuide.this);
            newStepBlock.setOrientation(LinearLayout.VERTICAL);
            newTitleBlock.setOrientation(LinearLayout.HORIZONTAL);

            //Creates the various text views and changes their visual properties
            TextView mStepNumber = new TextView(CreateNewGuide.this);
            mStepNumber.setTypeface(null, Typeface.BOLD);
            mStepNumber.setTextSize(24);
            mStepNumber.setTextColor(Color.BLACK);
            TextView mStepTitle = new TextView(CreateNewGuide.this);
            mStepTitle.setTypeface(null, Typeface.BOLD);
            mStepTitle.setTextSize(24);
            mStepTitle.setTextColor(Color.BLACK);

            newTitleBlock.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    displayStepOptionsMenu(newTitleBlock.getChildAt(1));
                }
            });

            int num = layoutFeed.getChildCount();

            //Sets the tag for the step block and step description textview
            //The tag is the step number
            newStepBlock.setTag(num);
//            mStepDesc.setTag(num);

            //add the title and button views to the stepBlock, an "empty" stepBlock has those 2 children
            String newStepNum = "Step " +num + " : ";
            mStepNumber.setText(newStepNum);
            mStepTitle.setText(title);
            newTitleBlock.addView(mStepNumber);
            newTitleBlock.addView(mStepTitle);
            newStepBlock.addView(newTitleBlock);
            newStepBlock.addView(createAddDataButtonLayout(newStepBlock));

            //newStepBlock.setBackgroundResource(R.drawable.border_new_step);
            //Adds the new step block to the index specified by the stepIndex variable
            layoutFeed.addView(newStepBlock, stepIndex);
            selectedLayout = newStepBlock;

            //adds the starting description to the step block if not null/empty
            if (!desc.equals("")){
                //indexToPlaceView = 1;//to ensure that it is placed in the first empty spot
                addDescription(desc,"Text");
            }
            //currentIndex++;
            haveSaved = false;

        }catch (Exception ex){
            ex.getMessage();
            return false;
        }
        return true;
    }

    private LinearLayout createAddDataButtonLayout(final LinearLayout stepBlock){
        //Creates the Layout Parameters for the buttons
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.weight = 0.5f;

        //Creates the add image button and its on click listener
        final Button mAddImage = new Button(CreateNewGuide.this);
        mAddImage.setBackgroundResource(R.drawable.style_button_add);
        mAddImage.setCompoundDrawablesWithIntrinsicBounds(photoIcon, null, null, null);
        String addImageBtnDesc = "Add Image";
        mAddImage.setText(addImageBtnDesc);
        mAddImage.setLayoutParams(params);

        //Creates the add description button and its on click listener
        Button mAddDesc = new Button (CreateNewGuide.this);
        mAddDesc.setBackgroundResource(R.drawable.style_button_add);
        mAddDesc.setCompoundDrawablesWithIntrinsicBounds(textIcon, null, null, null);
        String addTextBtnDesc = "Add Text";
        mAddDesc.setText(addTextBtnDesc);
        mAddDesc.setLayoutParams(params);

        //create button that will hold reference to a menu of other options
        Button mAddOther = new Button(CreateNewGuide.this);
        mAddOther.setBackgroundResource(R.drawable.style_button_add);
        mAddOther.setCompoundDrawablesWithIntrinsicBounds(textIcon, null, null, null);
        String addOtherBtnDesc = "Add Other";
        mAddOther.setText(addOtherBtnDesc);
        mAddOther.setLayoutParams(params);

        LinearLayout.LayoutParams buttonLP;
        //Sets the layout parameters for the buttonHolder layout
        buttonLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLP.setMargins(0,0,0, 75);

        //Creates the linear layout to hold the two buttons together
        final LinearLayout buttonHolder = new LinearLayout(CreateNewGuide.this);
        buttonHolder.setBackgroundResource(R.drawable.border_new_content);
        buttonHolder.setOrientation(LinearLayout.HORIZONTAL);
        buttonHolder.setLayoutParams(buttonLP);
        buttonHolder.setPadding(0,0,0,75);
        buttonHolder.addView(mAddDesc);
        buttonHolder.addView(mAddImage);
        buttonHolder.addView(mAddOther);

        mAddImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectedLayout = stepBlock;
                LinearLayout dataLayout = (LinearLayout)buttonHolder.getParent();
                indexToPlaceView = ((LinearLayout)dataLayout.getParent()).indexOfChild(dataLayout) + 1;
                SelectImage();
            }
        });
        mAddDesc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectedLayout = stepBlock;
                LinearLayout dataLayout = (LinearLayout)buttonHolder.getParent();
                indexToPlaceView = ((LinearLayout)dataLayout.getParent()).indexOfChild(dataLayout) + 1;
                Intent intent = new Intent(CreateNewGuide.this,TextBlockWriterActivity.class);
                intent.putExtra("CurrStep", layoutFeed.getChildCount());
                intent.putExtra("isEditing", false);
                startActivityForResult(intent,WRITE_DESC);
                overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
            }
        });
        final PopupMenu options = new PopupMenu(CreateNewGuide.this,mAddOther);
        options.getMenu().add(0,0, Menu.NONE,"Add Link");
        options.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int i = menuItem.getItemId();
                //set the current stepBlock and the proper index for when we place the link
                selectedLayout = stepBlock;
                LinearLayout dataLayout = (LinearLayout)buttonHolder.getParent();
                indexToPlaceView = ((LinearLayout)dataLayout.getParent()).indexOfChild(dataLayout) + 1;
                if (i == 0){
                    //set the linkHtml with a modal and add the link to the layout if the user didn't leave it empty or cancel
                    displayModalForLink("Add");

                }
                return true;
            }
        });
        mAddOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                options.show();
            }
        });

        buttonHolder.setTag(R.id.viewType,"ButtonBar");

        return buttonHolder;
    }

    /**
     * Builds and displays a modal to take user input for a google link they want to add to their guide
     * @param type of operation you are performing: "Add", add a new link; "Edit", edit an existing link based on which is the selectedWebview
     */
    private void displayModalForLink(final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter what you want a google link for");
        final EditText input = new EditText(this);
        input.setHint("ex. threading a needle");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String linkHtml = "<a href='http://lmgtfy.com/?q="+input.getText()+"'>"+input.getText()+"</a>";
                //Add or edit th textblock based on the type
                if (type.equals("Add"))addDescription(linkHtml,"Link");
                else if (type.equals("Edit"))editDescription(linkHtml);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * Builds and displays a menu of options for selecting a photo in the guide
     */
    private void displayImageOptionMenu(final Uri imageUri, final View v){
        final CharSequence[] items = {"Delete Picture","View Picture","Edit Picture", "Swap Picture", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Delete Picture")){
                    //Remove picture, indexes shift up
                    LinearLayout dataLayout = (LinearLayout) v.getParent();
                    LinearLayout stepLayout = (LinearLayout)dataLayout.getParent();
                    int indexToRemove = stepLayout.indexOfChild(dataLayout);
                    //if (stepLayout.getChildCount() > 3) stepLayout.removeViewAt(indexToRemove+1);
                    stepLayout.removeViewAt(indexToRemove);
                    if (stepLayout.getChildCount() <= 1){
                        //LinearLayout newDatBlock = new LinearLayout(CreateNewGuide.this);
                        //newDatBlock.addView(createAddDataButtonLayout(stepLayout));
                        stepLayout.addView(createAddDataButtonLayout(stepLayout));
                    }
                }else if(items[i].equals("View Picture")){
                    //calls the activty to view the picture and passes the URI
                    Intent intent = new Intent(CreateNewGuide.this, ViewPhoto.class);
                    intent.putExtra("imageUri", imageUri);
                    startActivity(intent);
                }else if(items[i].equals("Edit Picture")){
                    isSwapping = true;
                    //selectedLayout = ((LinearLayout) v.getParent());
                    //currentPictureSwap = ((LinearLayout) v.getParent()).indexOfChild(v);
                    LinearLayout dataLayout = (LinearLayout) v.getParent();
                    selectedLayout = (LinearLayout) dataLayout.getParent();
                    currentPictureSwap = selectedLayout.indexOfChild(dataLayout);
                    selectedImageView = (ImageView)v;
                    Intent intent = new Intent(CreateNewGuide.this, EditorActivity.class);
                    intent.putExtra("imageUri", imageUri);
                    intent.putExtra("isNewPic", false);
                    startActivityForResult(intent, PESDK_RESULT);
                    overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                }else if(items[i].equals("Swap Picture")){
                    //sets isSwapping check to true and then calls SelectImage to replace the image we want in the layout hierarchy
                    isSwapping = true;
                    LinearLayout dataLayout = (LinearLayout) v.getParent();
                    selectedLayout = (LinearLayout) dataLayout.getParent();
                    currentPictureSwap = selectedLayout.indexOfChild(dataLayout);
                    selectedImageView = (ImageView)v;
                    SelectImage();
                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Builds and displays a menu of options for selecting a text block in the guide
     */
    private void displayTextOptionsMenu(final View v, final String type){
        List<String> itemList = new ArrayList<>(Arrays.asList("Delete Text","Edit Text","Cancel"));
        if (type.equals("Link")){
            itemList.add(2,"View Link");
        }
        final CharSequence[] items = itemList.toArray(new CharSequence[itemList.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Stores the selected text view to edit later
                selectedWebView = (WebView) v;
                String tag = v.getTag(R.id.viewId).toString();
                String[] strings = tag.split("--");
                if(items[i].equals("Delete Text")) {
                    //Removes the selected textviews dataLayout from the layout
                    LinearLayout dataLayout = (LinearLayout) v.getParent();
                    LinearLayout stepLayout = (LinearLayout)dataLayout.getParent();
                    int indexToRemove = stepLayout.indexOfChild(dataLayout);
                    stepLayout.removeViewAt(indexToRemove);
                    if (stepLayout.getChildCount() <= 1){//if removing the layout would mean there is no more elements, add a button bar
                        stepLayout.addView(createAddDataButtonLayout(stepLayout));
                    }
                }else if(items[i].equals("Edit Text")){
                    //Starts a new activity
                    if (type.equals("Text")){
                        String currText = strings[1];
                       /* TextView contentView = new TextView(getApplicationContext());
                        selectedWebView.getSettings().setJavaScriptEnabled(true); //someone can use an XSS attack to screw us while js is enabled
                        selectedWebView.addJavascriptInterface(new WebViewJavascriptInterface(currText,contentView),"INTERFACE");
                        selectedWebView.setWebViewClient(new WebViewClient(){
                            @Override
                            public void onPageFinished(WebView view, String url){
                                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
                            }
                        });
                        selectedWebView.getSettings().setJavaScriptEnabled(false);// disable js to remove possibility of XSS attack
*/
                        Intent intent = new Intent(CreateNewGuide.this,TextBlockWriterActivity.class);
                        intent.putExtra("CurrText", currText);
                        //intent.putExtra("isEditing", true);
                        startActivityForResult(intent,EDIT_DESC);
                    }else if (type.equals("Link")){
                        displayModalForLink("Edit");
                        //if (!linkHtml.equals(""))editDescription(linkHtml);
                    }

                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }else if(items[i].equals("View Link")){
                    //get the html text and split it by "'" in order to get the link so we can parse it
                    String[] linkHtml = strings[1].split("'");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkHtml[1]));
                    startActivity(browserIntent);
                }
            }
        });
        builder.show();
    }

    /**
     * Builds and displays a menu of options for selecting the step title
     */
    private void displayStepOptionsMenu(final View v){
        final CharSequence[] items = {"Edit step title","Add step above","Add step below","Delete entire step", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Edit step title")) {
                    editStepTitle(v);
                }else if(items[i].equals("Add step above")){
                    //get the index of the step in the overall guide layout feed
                    LinearLayout titleLayout = ((LinearLayout) v.getParent());
                    LinearLayout stepLayout = ((LinearLayout) titleLayout.getParent());
                    stepIndex = layoutFeed.indexOfChild(stepLayout);

                    Log.i(DEBUG_TAG,"NUMBER: " + stepIndex);
                    createNewStep();
                    reorderSteps();
                }else if(items[i].equals("Add step below")){
                    //get the index of the step in the overall guide layout feed
                    LinearLayout titleLayout = ((LinearLayout) v.getParent());
                    LinearLayout stepLayout = ((LinearLayout) titleLayout.getParent());
                    stepIndex = layoutFeed.indexOfChild(stepLayout) + 1;
                    Log.i(DEBUG_TAG,"NUMBER: " + stepIndex);
                    createNewStep();
                }else if(items[i].equals("Delete entire step")){
                    deleteStep(v);
                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Edits the text of a chosen text block
     * @param newStepDesc of the text block that we are editing
     * @return true if success, otherwise false
     */
    private boolean editDescription(String newStepDesc){
        try {

            selectedWebView.loadData(newStepDesc,"text/html","UTF-8");
            String viewTag = "TEXT--" +newStepDesc;
            selectedWebView.setTag(R.id.viewId,viewTag);
            haveSaved = false;
        }catch (Exception ex){
            Log.e("BORBOT edit error: ",ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Adds a text block to the editor
     * @param newStepDesc The text of the new text block
     * @param type of text block we are adding (default text or link text)
     * @return true if success, otherwise false
     */
    private boolean addDescription(String newStepDesc,final String type) {
        try{
            //Creates a new textview and sets the tag (the tag is the current step number)
            WebView mDescription = new WebView(CreateNewGuide.this);
            //set tag to the stepNum as the first section for proper placement in data list
            //set second section of tag to data id, so we can get the view to manipulate it with the data object simultaneously
            String viewTag = "TEXT--" +newStepDesc;
            mDescription.setTag(R.id.viewId,viewTag);
            mDescription.setTag(R.id.textType,type);

            mDescription.setPadding(5, 10, 5, 10);
            mDescription.loadData(newStepDesc,"text/html","UTF-8");
            mDescription.setBackgroundColor(Color.TRANSPARENT);
            mDescription.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_UP:
                            view.performClick();
                            displayTextOptionsMenu(view,type);
                    }
                    return true;
                }
            });
            LinearLayout newDataBlock = new LinearLayout(CreateNewGuide.this);
            newDataBlock.setOrientation(LinearLayout.VERTICAL);
            newDataBlock.addView(mDescription);
            newDataBlock.addView(createAddDataButtonLayout(selectedLayout));
            newDataBlock.setTag(R.id.viewType,"DataBlock");
            if (selectedLayout.getChildCount() == 2){
                if (selectedLayout.getChildAt(1).getTag(R.id.viewType).equals("ButtonBar")){
                    selectedLayout.removeViewAt(selectedLayout.getChildCount()-1);
                }
                //adds the new text block with the text to the selected step
                selectedLayout.addView(newDataBlock);
            }else{
                Log.i(DEBUG_TAG,""+indexToPlaceView);
                selectedLayout.addView(newDataBlock,indexToPlaceView);
            }

            //addObjectToDataListInOrder(dataToAdd);
            haveSaved = false;
        }catch (Exception ex){
            ex.getMessage();
            return false;
        }

        return true;
    }

    /**
     * Starts activity to create and add a new step to the guide
     */
    public void createNewStep(){
        Intent intent = new Intent(CreateNewGuide.this,AddStepActivity.class);
        //intent.putExtra("CurrStep", layoutFeed.getChildCount());
        startActivityForResult(intent,WRITE_STEP);
        overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
    }
    /**
     * Builds and displays a edittext to allow the user to edit the title of the guide
     */
    public void editGuideTitle(View v){
        final TextView textview = (TextView) v;
        String currentText = textview.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Title");
        final EditText input = new EditText(this);
        input.setText(currentText);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(input.getText().toString().isEmpty()){
                    new AlertDialog.Builder(CreateNewGuide.this)
                            .setMessage("You can't leave it blank! Please try again.")
                            .setCancelable(false)
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                }else {
                    textview.setText(input.getText().toString());
                    newGuide.setTitle(input.getText().toString());
                    haveSaved = false;
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    //Lets users edit the title of a step
    public void editStepTitle(View v){
        final TextView textview = (TextView) v;
        String currentText = textview.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Title");
        final EditText input = new EditText(this);
        input.setText(currentText);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(input.getText().toString().isEmpty()){
                    new AlertDialog.Builder(CreateNewGuide.this)
                            .setMessage("You can't leave it blank! Please try again.")
                            .setCancelable(false)
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                }else{
                    textview.setText(input.getText().toString());
                    haveSaved = false;
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * Iterates through the whole layout, gets each step's contents and stores them inside a list.
     */
    public void PopulateUploadList(){

        //This loop iterates through each step.
        //We do layoutFeed.getChildCount() - 1 since we do not want to access the "add step" button
        for(int i = 0; i < layoutFeed.getChildCount() - 1; i++) {

            //This gets the layout where the entire step are held
            // Index 0 is the title layout, 1 to n-1 are either a WebView block or an ImageView block
            LinearLayout stepLayout = (LinearLayout) layoutFeed.getChildAt(i);

            //This gets the layout where the step number and title are held
            //Index 0 is the step and step number, 1 is the title of the step
            LinearLayout titleLayout = (LinearLayout) stepLayout.getChildAt(0);

            //This gets the textviews of the title number and description
            TextView TitleNum = (TextView) titleLayout.getChildAt(0);
            TextView TitleDesc = (TextView) titleLayout.getChildAt(1);

            int stepNumber = -1; //default is -1, if we get that in the data obj there was a mistake
            Pattern pattern = Pattern.compile("\\d");
            Matcher matcher = pattern.matcher(TitleNum.getText().toString());
            while (matcher.find()){
                stepNumber = Integer.parseInt(matcher.group());
                //Log.i("REGEX: ", ""+stepNumber);
            }

            // loops through the content of each step. We start at 1 since 0 is the title
            for (int j = 1; j < stepLayout.getChildCount(); j++) {
                //Get each data block, create proper data object for it, and add to data list for upload
                LinearLayout dataLayout = (LinearLayout) stepLayout.getChildAt(j);//get layout
                View dataView = dataLayout.getChildAt(0);//get the view containing our data in the layout
                Log.d("GET TAG","");
                String tag = dataView.getTag(R.id.viewId).toString();
                String[] strings = tag.split("--");
                String blockType = strings[0];

                if (blockType.equals("TEXT")){
                    //Log.i("BORBOT text blob: ","getting text");
                    //make the text data object
                    String textId = UUID.randomUUID() +"TEXT"+mGuideDataArrayList.size();
                    TextData dataToAdd = new TextData(textId,"Text",j-1,newGuide.getId());
                    dataToAdd.setStep(stepNumber,TitleDesc.getText().toString());
                    String currText = strings[1];
                    dataToAdd.stringToBlob(currText);
                    dataToAdd.setTextStyle(false, false, Color.DKGRAY, 17);
                    String textType = dataView.getTag(R.id.textType).toString();
                    dataToAdd.setTextType(textType);
                    //dataToAdd.setId();
                    mGuideDataArrayList.add(dataToAdd);
                }else if (blockType.equals("PICTURE")){
                    //make picture obj
                    String pictureId = UUID.randomUUID()+"IMG"+mGuideDataArrayList.size();
                    PictureData picData = new PictureData(pictureId,"Picture",j-1,newGuide.getId());
                    //picData.setType("Picture");
                    picData.setStep(stepNumber,TitleDesc.getText().toString());
                    //picData.setStepNumber(stepNumber);
                    //picData.setStepTitle(TitleDesc.getText().toString());
                    //picData.setPlacement(j-1);
                    String picUri = strings[1];
                    picData.setUri(picUri);
                    mGuideDataArrayList.add(picData);
                }
            }
        }
    }

    /**
     * Re-organizes step numbers so that they are in the proper order
     */
    private void reorderSteps(){
        //int dataListPointer = 0;//used to iterate through the datalist by stepCount instead of one by one
        //Loops through the layout Feed and its children to set the step number to the correct step
        for(int i = 0; i < layoutFeed.getChildCount() - 1; i++){
            LinearLayout stepLayout = (LinearLayout) layoutFeed.getChildAt(i);
            LinearLayout titleLayout = (LinearLayout) stepLayout.getChildAt(0);
            TextView stepTitle = (TextView) titleLayout.getChildAt(0);
            String newStepTitle = "Step " + (i + 1) + " : ";
            stepTitle.setText(newStepTitle);
            reorderTags(stepLayout,(i+1));//reorders the tags for each view in each step's linear layout
        }
    }

    /**
     * Re-sets the tags of the guide data views to their new proper step values.
     * @param stepLayout that we are editing the view tags of
     * @param stepNum the new step number that we are changing the tags to
     */
    private void reorderTags(LinearLayout stepLayout, int stepNum){
        //we start at 1 because the view at index 0 is the title of the step
        for (int i = 1; i < stepLayout.getChildCount();i++){
            View view = stepLayout.getChildAt(i);
            view.setTag(stepNum);//set the tag of the view to the new stepNum
        }
    }
    /**
     * Asks the user if they want to delete the selected step, then does it.
     */
    public void deleteStep(final View v){
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this step? All unsaved changes will be lost.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //IT WORKS NOW TRUST ME - CA (July 23. 2018)
                        
                        //Gets the parent of the parent layout and deletes the whole parent (step layout)
                        LinearLayout titleLayout = ((LinearLayout) v.getParent());
                        LinearLayout stepLayout = ((LinearLayout) titleLayout.getParent());

                        ((LinearLayout) stepLayout.getParent()).removeView(stepLayout);
                        reorderSteps();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Adds an image or some text
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_CAMERA){
                if (camera == null){
                    camera = new CameraImagePicker(CreateNewGuide.this,outputPath);
                    camera.setImagePickerCallback(new ImagePickerCallback() {
                        @Override
                        public void onImagesChosen(List<ChosenImage> list) {
                            Uri imagePath = Uri.parse(list.get(0).getQueryUri());
                            addImage(imagePath);
                        }

                        @Override
                        public void onError(String s) {

                        }
                    });
                }
                camera.submit(data);
            }
            else if (requestCode == SELECT_FILE){
                Uri selectedImageUri = data.getData();
                addImage(selectedImageUri);
            } else if (requestCode == WRITE_STEP){
                if (data == null){
                    return;
                }
                newStepTitle = AddStepActivity.getTitle(data);
                newStepDesc = AddStepActivity.getDesc(data);
                addStep(newStepTitle, newStepDesc);
                reorderSteps();
            } else if (requestCode == WRITE_DESC){
                if (data == null){
                    return;
                }
                newDesc = TextBlockWriterActivity.getNewDesc(data);
                addDescription(newDesc,"Text");
            }else if (requestCode == EDIT_DESC){
                if (data == null){
                    return;
                }
                newDesc = TextBlockWriterActivity.getNewDesc(data);
                editDescription(newDesc);
            }else if (requestCode == PESDK_RESULT){
                String editedImage = data.getStringExtra("resultURI");
                Uri editedUri = Uri.parse(editedImage);
                isSwapping = true;
                addImage(editedUri);
            }
        }
    }

    /**
     * Asks the user if they really want to go back and erase all unsaved data
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to go back? Any unsaved changes will be lost.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CreateNewGuide.this.finish();
                        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outstate){
        //need a save path in case the activity is killed
        //will need to re-initialize cameraimagepicker
        outstate.putString("ImagePath",outputPath);
        outstate.putInt("guideNum",guideNum);
        super.onSaveInstanceState(outstate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        //after activity is restored, need to re-initialize these 2 values
        // in order to re-initialize CameraImagePicker
        if(savedInstanceState != null){
            outputPath = savedInstanceState.getString("ImagePath", null);
            guideNum = savedInstanceState.getInt("guideNum",0);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}

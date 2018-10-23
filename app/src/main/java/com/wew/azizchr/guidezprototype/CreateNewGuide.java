package com.wew.azizchr.guidezprototype;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

import io.grpc.Context;

public class CreateNewGuide extends AppCompatActivity {

    //private static final String TEXT_TAG = "TEXT";
    //private static final String IMG_TAG = "IMG";
    private static final int SELECT_FILE =0;//used when selecting an image file
    private static final int WRITE_STEP =1;//used when making a new step
    private static final int WRITE_DESC =2;//used when making a new text block
    private static final int EDIT_DESC = 3;//used when editing a text block
    public static final int PESDK_RESULT = 4;

    private String outputPath;
    private String newStepTitle;
    private String newStepDesc;
    private String newDesc;
    private String mode;//used to determine whether we are making a new guide or editing an old one

    private int guideNum = 0;//the current guide index thing
    private int textBlockNum; //the current number of textBlocks
    private int imgBlockNum;//the current number of image blocks
    private int currentIndex;
    private int currentPictureSwap;

    private Boolean isSwapping;

    private CameraImagePicker camera;
    public LinearLayout layoutFeed;
    public LinearLayout selectedLayout;
    //public TextView selectedTextView;
    public WebView selectedWebView; //changed from text to web view for better memory consumption and text formatting

    TextView mNewGuideTitle;

    //The title of the guide, initialized as NULL so we can easily check through string methods if it hasn't been set
    String guideTitle = "NULL";

    //Firebase Instance Variables
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
    //stores all documents that we are going to delete when we save a guide
    private ArrayList<DocumentReference> deletedDataList = new ArrayList<>();
    private Guide newGuide; //guide object that stores descriptive info on the guide being made

    //used when saving so we know not to save the copies of the same data objects in db
    private boolean haveSaved;

    //Images used for the step buttons
    public Drawable textIcon;
    public Drawable photoIcon;

    public LinearLayout.LayoutParams buttonLP;

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

        newGuide = new Guide();

        mNewGuideTitle = (TextView) findViewById(R.id.txtNewGuideTitle);

        //Initialize firebase variables
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentUser = mFirebaseAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        imgStorage = mStorage.getReference();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);

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
        Log.i("BORBOT MODE",mode);
        userRef = mFirestore.document("Users/" + mFirebaseAuth.getUid());

        //gets the number of guides the user has
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                guideNum = documentSnapshot.getLong("numGuides").intValue();

                if (mode.equals("CREATE"))guideNum++;// increments guideNum by 1 because we are making a new guide so there is 1 more than before

                //sets these 2 collections to point to the folders for the guide data of the new guide. for later uploading
                textData = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum+"/textData");
                picData = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum+"/imageData");

                editorSetup(bundle);
            }

        });

        mNewGuideTitle.setText(guideTitle);

        newGuide.setAuthor(mCurrentUser.getUid());
        newGuide.setTitle(guideTitle);
        //newGuide.setDateCreated();
        newGuide.setPublishedStatus(false);

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

        currentIndex = 0;
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
                    newGuide.setPublishedStatus(true);
                }else{
                    new AlertDialog.Builder(CreateNewGuide.this)
                            .setMessage("You must save your work before publishing, would you like to save now?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    saveGuide();
                                    newGuide.setPublishedStatus(true);
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

        //Sets the layout parameters for the buttonHolder layout
        buttonLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLP.setMargins(0,0,0, 75);
    }


    //JavaScript interface used to get html content from our webviews so that we can do stuff with them like editing
    public class WebViewJavascriptInterface{
        private String Html;

        public WebViewJavascriptInterface(String html){
            this.Html = html;
        }

        @JavascriptInterface
        public void processContent(String htmlContent){
            Html = htmlContent;
        }
    }

    /**
     * sets initial guide parameters depending on what mode we enter the editor in
     * @param bundle contains info about the guide like its id.
     */
    private void editorSetup(Bundle bundle){
        if (mode.equals("CREATE")){
            newGuide.setId(UUID.randomUUID().toString());
        }else if (mode.equals("EDIT")){
            newGuide.setId(bundle.getString("GUIDEID"));
            newGuide.setKey(bundle.getString("Key"));
            loadGuide(newGuide.getId(),newGuide.getKey());
            Log.i("BORBOT EDITs","" + newGuide.getId());
        }
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

        /*Task guideLoader = mFirebaseAuth.signInAnonymously();

        guideLoader.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                return null;
            }
        });

        Executor executor = new Executor() {
            @Override
            public void execute(@NonNull Runnable runnable) {

            }
        };
*/
        //get the stored guide data
        guideSteps.get().addOnCompleteListener(CreateNewGuide.this,new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int stepNum;
                if (task.isSuccessful()){
                    //for each step in the guide, get its info and recreate the step
                    for (QueryDocumentSnapshot doc: task.getResult()){
                        stepNum = doc.getLong("stepNumber").intValue();
                        String stepTitle = doc.get("stepTitle").toString();
                        addStep(stepTitle,"");

                        Query stepText = guideText.whereEqualTo("stepNumber",stepNum);
                        Query stepImgs = guideImgs.whereEqualTo("stepNumber", stepNum);

                        //get the step's text from the db and add it to the data list in the proper order
                        stepText.get().addOnCompleteListener(CreateNewGuide.this,new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    //get each text's data, make an object for it, and add it to the data list
                                    for (QueryDocumentSnapshot snap: task.getResult()){
                                        Blob text = (Blob)snap.get("text");
                                        String id = snap.get("id").toString();
                                        String guideId = "";
                                        String stepTitle = snap.get("stepTitle").toString();
                                        String type = snap.get("type").toString();
                                        int num = snap.getLong("stepNumber").intValue();
                                        int placement = snap.getLong("placement").intValue();
                                        int size = snap.getLong("size").intValue();
                                        int color = snap.getLong("color").intValue();
                                        boolean bold = (boolean)snap.get("bold");
                                        boolean italic = (boolean)snap.get("italic");
                                        TextData data = new TextData(type,placement,guideId,stepTitle,num);
                                        //data.stringToBlob(text);
                                        data.setText(text);
                                        data.setId(id);
                                        //selectedLayout = (LinearLayout) layoutFeed.getChildAt(num-1);
                                        addObjectToDataListInOrder(data);
                                        addDescription(data);
                                    }
                                }
                            }
                        });

                        //get the step's images from the db and add it to the data list in the proper order
                        stepImgs.get().addOnCompleteListener(CreateNewGuide.this,new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    //get each pic's data, make an object for it, add it to the data list, then load the image
                                    for (QueryDocumentSnapshot snap: task.getResult()){
                                        //get the data from fire store and add a data object to the data list
                                        String id = snap.get("id").toString();
                                        String guideId = "";
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
                                       // selectedLayout = (LinearLayout) layoutFeed.getChildAt(num-1);
                                        addObjectToDataListInOrder(data);

                                        //load the image from storage into the layout
                                        String path = "guideimages/users/" + mFirebaseAuth.getUid() + "/guide"+guideNum+"/" + id + ".png";
                                        StorageReference imageToLoad = imgStorage.child(path);
                                        //get reference to the layout we are adding the picture to
                                        int dataStep = data.getStepNumber() - 1;
                                        LinearLayout theSelectedLayout = (LinearLayout) layoutFeed.getChildAt(dataStep);
                                        loadImage(imageToLoad, theSelectedLayout);
                                    }
                                }
                            }
                        });
                    }
                }else{
                    //sheeeeit
                    Log.i("SOMEONE FUCKED UP: ","IT WAS JEFFREY!");
                    Log.i("ITS ALRIGHT: ","BE NICE TO JEFF");
                }


            }
        });
    }

    /**
     * Loads an image from Firebase Storage into a new image view
     * @param ref of the image we are getting from storage
     */
    public void loadImage(StorageReference ref, LinearLayout layout){
        try{
            //Creates the new imageview
            final ImageView newImgView = new ImageView(CreateNewGuide.this);
            //Glide.with(CreateNewGuide.this).load(imageUri).into(newImgView);
            Glide.with(CreateNewGuide.this)
                    .asBitmap()
                    .load(ref)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            newImgView.setImageBitmap(resource);

                            //gets the mediapath of the image and parses it into a uri we can work with
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            String path = MediaStore.Images.Media.insertImage(CreateNewGuide.this.getContentResolver(), resource, UUID.randomUUID().toString() + ".png", "drawing");
                            final Uri imageUri = Uri.parse(path);
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
            layout.addView(newImgView, layout.getChildCount() - 1);
            currentIndex++;

        }catch(Exception ex){
            Log.i("IMAGE ERROR: ", ex.getMessage());
        }
    }

    public void onClickStep(View view) {
        Intent intent = new Intent(CreateNewGuide.this,AddStepActivity.class);
        intent.putExtra("CurrStep", layoutFeed.getChildCount());
        startActivityForResult(intent,WRITE_STEP);
        overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
    }

    public void onClickGuideTitle(View view) {
        editTitle(view);
    }

    /**
     * Saves the guide into firestore
     */
    private void saveGuide(){

        DocumentReference guideRef = mFirestore.document("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum);
        guideRef.set(newGuide);

        //informs the user that the save process is starting
        Toast.makeText(CreateNewGuide.this,"Saving...\nPlease wait",Toast.LENGTH_LONG).show();

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
                            //textDataPkg.setPlacement(i);//sets the placement to the current index
                            uploadText(textDataPkg);
                        }
                    }
                }
            }
        });
        //TODO: Modify uploadImage method to take in an array of all pictures in the data list rather than one by one
        //TODO: This array will be passed to the async task where the processing and uploading will be carried out
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

                           // PictureData picDataPkg = (PictureData)dataToSave;
                            //picDataPkg.setPlacement(i);
                           // String picDataPkgUri = picDataPkg.getUri();
                            //uploadImage(picDataPkg,picDataPkgUri);
                        }
                    }

                    uploadImages(picturesToUpload);
                }
            }
        });

        //delete any documents from the db that we no longer want
        if (!deletedDataList.isEmpty()){
            for (DocumentReference doc: deletedDataList){
                doc.delete();
            }
        }

        //update the users guide count so the guides can be named differently, 'guide0', 'guide1', etc
        userRef.update("numGuides", guideNum);
        userRef.update("key", userRef.getId());
        Toast.makeText(CreateNewGuide.this, "Guide Saved!", Toast.LENGTH_SHORT).show();
        for (int i = 0; i< mGuideDataArrayList.size();i++){
            Log.i("GUIDEDATA OBJ: ",mGuideDataArrayList.get(i).getType());
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
            //Glide.with(CreateNewGuide.this).load(imageUri).into(newImgView);
            Glide.with(CreateNewGuide.this)
                    .asBitmap()
                    .load(imageUri)
                    .transition(GenericTransitionOptions.with(R.anim.fui_slide_in_right))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                            //creates object to hold picture data
                            newImgView.setImageBitmap(resource);
                            LinearLayout titleBlock = (LinearLayout) selectedLayout.getChildAt(0);
                            TextView title = (TextView)titleBlock.getChildAt(1);
                            PictureData picData = new PictureData();
                            picData.setUri(imageUri.toString());
                            picData.setType("Picture");
                            picData.setStepNumber((int)selectedLayout.getTag());
                            picData.setStepTitle(title.getText().toString());
                            picData.setId(newGuide.getId()+"IMG"+mGuideDataArrayList.size());

                            //Adds the tag to the new imageview (the tag is the step number + dataId)
                            String viewTag = selectedLayout.getTag().toString() + "--" + picData.getId();
                            newImgView.setTag(viewTag);
                            Log.i("IMG TAG", ""+newImgView.getTag());
                            //if we are swapping out a picture, replace the old location with new pic, otherwise just add it to end
                            if (isSwapping){
                                //picData.setPlacement(currentPictureSwap);
                                mGuideDataArrayList.add(currentPictureSwap,picData);
                                mGuideDataArrayList.remove(currentPictureSwap + 1);
                            }else{
                                //picData.setPlacement(currentIndex);
                                //mGuideDataArrayList.add(picData);
                                addObjectToDataListInOrder(picData);
                            }
                        }
                    });

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

            if(isSwapping){
                selectedLayout.addView(newImgView, currentPictureSwap);
                selectedLayout.removeViewAt(currentPictureSwap + 1);
                currentPictureSwap = 0;
                isSwapping = false;
            }else {
                selectedLayout.addView(newImgView, selectedLayout.getChildCount() - 1);
                currentIndex++;
            }

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

            //Creates the Layout Parameters for the buttons
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.weight = 0.5f;

            //Creates the add image button and its on click listener
            Button mAddImage = new Button(CreateNewGuide.this);
            mAddImage.setBackgroundResource(R.drawable.style_button_add);
            mAddImage.setCompoundDrawablesWithIntrinsicBounds(photoIcon, null, null, null);
            String addImageBtnDesc = "Add Image";
            mAddImage.setText(addImageBtnDesc);
            mAddImage.setLayoutParams(params);
            mAddImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectedLayout = newStepBlock;
                    SelectImage();
                }
            });

            //Creates the add description button and its on click listener
            Button mAddDesc = new Button (CreateNewGuide.this);
            mAddDesc.setBackgroundResource(R.drawable.style_button_add);
            mAddDesc.setCompoundDrawablesWithIntrinsicBounds(textIcon, null, null, null);
            String addTextBtnDesc = "Add Text";
            mAddDesc.setText(addTextBtnDesc);
            mAddDesc.setLayoutParams(params);
            mAddDesc.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectedLayout = newStepBlock;
                    Intent intent = new Intent(CreateNewGuide.this,TextBlockWriterActivity.class);
                    intent.putExtra("CurrStep", layoutFeed.getChildCount());
                    intent.putExtra("isEditing", false);
                    startActivityForResult(intent,WRITE_DESC);
                    overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                }
            });

            //Creates the linear layout to hold the two buttons together
            LinearLayout buttonHolder = new LinearLayout(CreateNewGuide.this);
            buttonHolder.setBackgroundResource(R.drawable.border_new_content);
            buttonHolder.setOrientation(LinearLayout.HORIZONTAL);
            buttonHolder.setLayoutParams(buttonLP);
            buttonHolder.setPadding(0,0,0,75);


            int num = layoutFeed.getChildCount();

            //Sets the tag for the step block and step description textview
            //The tag is the step number
            newStepBlock.setTag(num);
//            mStepDesc.setTag(num);

            String newStepNum = "Step " +num + " : ";
            mStepNumber.setText(newStepNum);
            mStepTitle.setText(title);
            newTitleBlock.addView(mStepNumber);
            newTitleBlock.addView(mStepTitle);
            newStepBlock.addView(newTitleBlock);
            newStepBlock.addView(buttonHolder);
            buttonHolder.addView(mAddDesc);
            buttonHolder.addView(mAddImage);

            //newStepBlock.setBackgroundResource(R.drawable.border_new_step);
            //Adds the new step block to the end of the main layout, before the button
            layoutFeed.addView(newStepBlock, layoutFeed.getChildCount()-1);
            selectedLayout = newStepBlock;

            //adds the starting description to the step block if not null/empty
            if (!desc.equals("")){
                addDescription(desc);
            }
            currentIndex++;

        }catch (Exception ex){
            ex.getMessage();
            return false;
        }
        return true;
    }

    /**
     * Edits the text of a chosen text block
     * @param newStepDesc of the text block that we are editing
     * @return true if success, otherwise false
     */
    private boolean editDescription(String newStepDesc){
        try {
            selectedWebView.loadData(newStepDesc,"text/html","UTF-8");
            String dataId = (String)selectedWebView.getTag();
            String[] strings = dataId.split("--");
            int num = Integer.parseInt(strings[0]);//get the tag of the textview, which is the view's id
            String id = strings[1];
            //iterate through the data list and update the view in it with the new text
            for (int i = 0; i < mGuideDataArrayList.size();i++){
                //we are only updating text data here, if the type is picture then go to next iteration
                if (mGuideDataArrayList.get(i).getType() == "Picture") continue;

                //Cast the GuideData as TextData so we can change the text
                TextData data = (TextData)mGuideDataArrayList.get(i);
                if (data.getId().equals(id)){
                    data.stringToBlob(newStepDesc);
                    break;
                }
            }
        }catch (Exception ex){
            Log.e("BORBOT error: ",ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Overloaded method is used if we have a TextData object that we want to create in the editor.
     * Used when we are loading a prior guide for editing
     * @param data representing the text block we want to load into the editor
     * @return true if success, otherwise false
     */
    private boolean addDescription(TextData data){
        try{
            //Creates a new textview and sets the tag (the tag is the current step number)
            WebView mDescription = new WebView(CreateNewGuide.this);
            int dataStep = data.getStepNumber() - 1;
            LinearLayout theSelectedLayout = (LinearLayout) layoutFeed.getChildAt(dataStep);
            String viewTag = theSelectedLayout.getTag().toString() + "--" + data.getId();
            mDescription.setTag(viewTag);

            //mDescription.setTextSize(data.getSize());
            //mDescription.setTextColor(data.getColor());
            mDescription.setPadding(5, 10, 5, 10);
            mDescription.loadData(data.getStringFromBlob(),"text/html","UTF-8");
            mDescription.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_UP:
                            view.performClick();
                            displayTextOptionsMenu(view);
                    }
                    return true;
                }
            });
            mDescription.setBackgroundColor(Color.TRANSPARENT);

            //adds the new textblock with the text to the selected step
            theSelectedLayout.addView(mDescription, theSelectedLayout.getChildCount() - 1);

        }catch (Exception ex){
            ex.getMessage();
            return false;
        }

        return true;
    }

    /**
     * Adds a text block to the editor, then creates a data object representing the block and
     * calls the function to add it to the data list
     * @param newStepDesc The text of the new text block
     * @return true if success, otherwise false
     */
    private boolean addDescription(String newStepDesc) {
        try{
            LinearLayout titleBlock = (LinearLayout) selectedLayout.getChildAt(0);
            TextView title = (TextView)titleBlock.getChildAt(1);
            TextData dataToAdd = new TextData();
            dataToAdd.setType("Text");
            //dataToAdd.setPlacement(mGuideDataArrayList.size());
            dataToAdd.setStepNumber((int) selectedLayout.getTag());
            dataToAdd.setStepTitle(title.getText().toString());
            dataToAdd.stringToBlob(newStepDesc);
            dataToAdd.setTextStyle(false, false, Color.DKGRAY, 17);

            dataToAdd.setId(newGuide.getId() +"TEXT"+mGuideDataArrayList.size());
            //Creates a new textview and sets the tag (the tag is the current step number)
            WebView mDescription = new WebView(CreateNewGuide.this);
            //set tag to the stepNum as the first section for proper placement in data list
            //set second section of tag to data id, so we can get the view to manipulate it with the data object simultaneously
            String viewTag = selectedLayout.getTag().toString() + "--" + dataToAdd.getId();
            mDescription.setTag(viewTag);

           // mDescription.setTextSize(17);
            //mDescription.setTextColor(Color.DKGRAY);
            mDescription.setPadding(5, 10, 5, 10);
            mDescription.loadData(newStepDesc,"text/html","UTF-8");
            mDescription.setBackgroundColor(Color.TRANSPARENT);

            mDescription.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_UP:
                            view.performClick();
                            displayTextOptionsMenu(view);
                    }
                    return true;
                }
            });

            //adds the new text block with the text to the selected step
            selectedLayout.addView(mDescription, selectedLayout.getChildCount() - 1);

            addObjectToDataListInOrder(dataToAdd);

        }catch (Exception ex){
            ex.getMessage();
            return false;
        }

        return true;
    }

    /**
     * this function takes a GuideData object and adds it to our overall guide data list
     * in the correct index according to the object's position in the actual guide
     * @param data the object we are adding to the data list
     * @return true if successfully added
     */
    private boolean addObjectToDataListInOrder(GuideData data){
        try{
            int dataStep = data.getStepNumber() - 1;
            LinearLayout theSelectedLayout = (LinearLayout) layoutFeed.getChildAt(dataStep);
            int num = (int)theSelectedLayout.getTag();//get the tag of the stepLayout, which is the view's id
            boolean foundCorrectStep = false;//when we found the step we want to add text to, set to true
            //add the new data object to the data list in the appropriate spot
            for (int i = 0; i < mGuideDataArrayList.size();i++){
                GuideData currentObj = mGuideDataArrayList.get(i);

                //set to true when we have found the step we are adding text to
                if (currentObj.getStepNumber() == num){
                    foundCorrectStep = true;
                }

                //if we found the items proper placement in the step, i.e. its right before the current block in the guide layout
                if ((data.getPlacement() != 0 ) && (currentObj.getPlacement() >= data.getPlacement())){
                    //String newDataId = "";
                    //if (data.getType().equals("Text"))newDataId=newGuide.getId()+"TEXT"+mGuideDataArrayList.size();
                    //else if (data.getType().equals("Picture"))newDataId=newGuide.getId()+"IMG"+mGuideDataArrayList.size();
                    //data.setId(newDataId);
                    mGuideDataArrayList.add(i-1,data);
                    break;
                }

                //new text is always added to the end of the step by default, we can only know when we reached it when
                //we iterate onto the first object of the next step.
                if (foundCorrectStep && (currentObj.getStepNumber() > num)){//first object that isn't equal to id of the new view, which is the first element THIS NEEDS TO BE FIXED
                    data.setPlacement(i);
                    mGuideDataArrayList.add(i,data);
                    mGuideDataArrayList.remove(currentObj);
                    currentObj.setPlacement(i+1);
                    mGuideDataArrayList.add(i+1,currentObj);

                    break;
                }
            }
            //if we didn't find the next step we haven't added the data to the list
            //that means the step we are adding to is the last step, put the data object at the end of the data list
            if (!mGuideDataArrayList.contains(data)){
                data.setStepNumber(num);
                data.setPlacement(mGuideDataArrayList.size());
                mGuideDataArrayList.add(data);
            }
        }catch (Exception ex){
            return false;
        }
        return true;
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
                    String dataId = (String)v.getTag();
                    String[] strings = dataId.split("--");
                    removeFromDataList(strings[1]);
                    ((LinearLayout) v.getParent()).removeView(v);
                }else if(items[i].equals("View Picture")){
                    //calls the activty to view the picture and passes the URI
                    Intent intent = new Intent(CreateNewGuide.this, ViewPhoto.class);
                    intent.putExtra("imageUri", imageUri);
                    startActivity(intent);
                }else if(items[i].equals("Edit Picture")){
                    isSwapping = true;
                    selectedLayout = ((LinearLayout) v.getParent());
                    currentPictureSwap = ((LinearLayout) v.getParent()).indexOfChild(v);
                    Intent intent = new Intent(CreateNewGuide.this, EditorActivity.class);
                    intent.putExtra("imageUri", imageUri);
                    intent.putExtra("isNewPic", false);
                    startActivityForResult(intent, PESDK_RESULT);
                    overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                }else if(items[i].equals("Swap Picture")){
                    //sets isSwapping check to true and then calls SelectImage to replace the image we want in the layout hierarchy
                    isSwapping = true;
                    selectedLayout = ((LinearLayout) v.getParent());
                    currentPictureSwap = ((LinearLayout) v.getParent()).indexOfChild(v);
                    SelectImage();
                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void removeFromDataList(String dataId) {
        boolean elementDeleted = false;
        GuideData dataToDelete = new GuideData();
        //go through list until we have found the element we want to delete
        //once it is deleted, we need to lower the placement # of all elements after it by 1
        for (GuideData data: mGuideDataArrayList){
            if (elementDeleted){
                int newPlacement = data.getPlacement()-1;
                data.setPlacement(newPlacement);
            }else{
                if (data.getId().equals(dataId)){
                    //mGuideDataArrayList.remove(data);
                    dataToDelete = data;//get reference to the object we want to remove
                    elementDeleted = true;
                }
            }
        }

        //add the ref to the list, if we are editing a guide previously saved, then we will delete any docs that we no longer want in the db
        DocumentReference deletedDataRef = mFirestore.document(textData+"/textBlock-"+dataToDelete.getId());
        deletedDataList.add(deletedDataRef);

        mGuideDataArrayList.remove(dataToDelete);
    }

    /**
     * Builds and displays a menu of options for selecting a text block in the guide
     */
    private void displayTextOptionsMenu(final View v){
        final CharSequence[] items = {"Delete Text","Edit Text", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Delete Text")) {
                    //Removes the selected Textview from the layout and its corresponding data obj from our data list
                    //TODO: After deleting a step, the data list disappears, and the tag just becomes the step number, WTF
                    Log.i("BORBOT TAG",v.getTag().toString());
                    String dataId = (String)v.getTag();
                    String[] strings = dataId.split("--");
                    removeFromDataList(strings[1]);
                    ((LinearLayout) v.getParent()).removeView(v);
                }else if(items[i].equals("Edit Text")){
                    //Stores the selected text view to edit later
                    selectedWebView = (WebView) v;
                    //Starts a new activity
                    //TODO: Have to find way to get text from a web view
                    String currText = "";
                    selectedWebView.getSettings().setJavaScriptEnabled(true); //someone can use an XSS attack to screw us while js is enabled
                    selectedWebView.addJavascriptInterface(new WebViewJavascriptInterface(currText),"INTERFACE");
                    selectedWebView.setWebViewClient(new WebViewClient(){
                        @Override
                        public void onPageFinished(WebView view, String url){
                            view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
                        }
                    });
                    selectedWebView.getSettings().setJavaScriptEnabled(false);// disable js to remove possibility of XSS attack

                    Intent intent = new Intent(CreateNewGuide.this,TextBlockWriterActivity.class);
                    intent.putExtra("CurrText", currText);
                    //intent.putExtra("isEditing", true);
                    startActivityForResult(intent,EDIT_DESC);
                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Builds and displays a menu of options for selecting the step title
     */
    private void displayStepOptionsMenu(final View v){
        final CharSequence[] items = {"Edit step title","Delete entire step", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Edit step title")) {
                    editTitle(v);
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
     * Builds and displays a edittext to allow the user to edit the title
     */
    public void editTitle(View v){
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
                textview.setText(input.getText().toString());
                newGuide.setTitle(input.getText().toString());
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

    //TODO:These 3 upload functions should be put into a firestore connection factory class

    /**
     * Uploads a text block to the firestore database
     * @param text object to be uploaded
     */
    public void uploadText(TextData text){
        if (text.getStringFromBlob().isEmpty()){return;}

        //Check to see if the current step has an object saved in the db
        uploadStep(text);

        //if the text object does not have an id, that means that its new and hasn't been saved into the db
        //give it an id so that in future saves we won't create a new doc in db and will instead overwrite old save
        if (text.getId() == null || text.getId().equals("")){
            text.setId(UUID.randomUUID().toString());
        }
        DocumentReference textBlockRef = textData.document("textBlock-" + text.getId());
        textBlockRef.set(text);
        textBlockNum++;
    }

    private void uploadStep(final GuideData data) {
        //if the current step has not had an object representing it stored in the db
        //then we will make one here before saving the text
        final DocumentReference step = mFirestore.document(
                "Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum+"/stepData/step" + data.getStepNumber());
        step.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Map<String,Object> dataToSave = new HashMap<>();
                    dataToSave.put("stepNumber",data.getStepNumber());
                    dataToSave.put("stepTitle",data.getStepTitle());
                    if (!documentSnapshot.exists()){
                        //step doesn't exist so we create it
                        step.set(dataToSave);
                    }else{
                        step.update(dataToSave);
                    }
                }
            }
        });
    }


    public void uploadImages(ArrayList<PictureData> images){
        final ArrayList<Bitmap> bitmaps = new ArrayList<>();
        final ArrayList<String> paths = new ArrayList<>();

        //get each PictureData obj in guide and extract bitmaps/file paths for async uploading;then store the data in firestore
        for(PictureData image: images){
            //Check to see if the current step has an object saved in the db
            uploadStep(image);
            Uri imageUri = Uri.parse(image.getUri());
            Glide.with(CreateNewGuide.this)
                    .asBitmap()
                    .load(imageUri)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            bitmaps.add(resource);
                        }
                    });
            String path = "guideimages/users/" + mFirebaseAuth.getUid() + "/guide"+guideNum+"/" + image.getId() + ".png";
            image.setImgPath(path);
            paths.add(path);

            //Can create a hashmap to upload but instead we use custom objects
            DocumentReference imgBlockRef = picData.document("imgBlock-" + image.getId());
            imgBlockRef.set(image);
        }
        Log.d("BORBOT SIZE",""+bitmaps.size());
        Log.d("BORBOT","Uploading images asynchronously");
        //send all the bitmaps to the async task
        new ImageUploadAsyncTask(paths).execute(bitmaps);
    }

    private static class ImageUploadAsyncTask extends AsyncTask<ArrayList<Bitmap>, Void, Long>{
        private static ArrayList<String> storagePaths;
        private final FirebaseStorage mStorageReference;

        private ImageUploadAsyncTask(ArrayList<String> paths){
            storagePaths = paths;
            mStorageReference = FirebaseStorage.getInstance();
        }

        @Override
        protected Long doInBackground(ArrayList<Bitmap>[] bitmaps) {
            int storageindex = 0;
            for (Bitmap bmap: bitmaps[0]){
                //create a byte array output stream to prepare the image bitmap for upload
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //resource bitmap is compressed and stored into baos
                bmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                byte[] data = baos.toByteArray();//outstream is converted into byte array for upload

                String path = storagePaths.get(storageindex);
                Log.d("BORBOT PATH",path);
                StorageReference imgRef = mStorageReference.getReference(path);

                //image byte array is uploaded with our metadata
                UploadTask uploadTask = imgRef.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("ASYNCIMAGEUPLOADA", "Upload Success: " + taskSnapshot.getUploadSessionUri());
                        StorageMetadata storageMetadata = taskSnapshot.getMetadata();
                        Log.d("BORBOT","Image upload successful");
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //display the progress to the user
                        //TODO: TO CHRIS, this is where we can get the upload progress to show the user, dont know how tho
                        long progress = taskSnapshot.getBytesTransferred();
                    }
                });

                storageindex++;
            }

            Log.d("BORBOT","Uploading Complete");
            return null;
        }
    }

    /**
     * This iterates through the whole layout, and gets each step and its contents and stores them
     * inside a list. That list is then uploaded.
     */
    public void PopulateUploadList(){

        //This loop iterates through each step.
        //We do layoutFeed.getChildCount() - 1 since we do not want to access the "add step" button
        for(int i = 0; i < layoutFeed.getChildCount() - 1; i++) {

            //This gets the layout where the entire step are held
            // Index 0 is the title layout
            // Index 1 to n-1 are either a WebView block or an ImageView block
            // Index n is the layout where the two "add" buttons are held
            LinearLayout stepLayout = (LinearLayout) layoutFeed.getChildAt(i);

            //This gets the layout where the step number and title are held
            //Index 0 is the step and step number (Step x:)
            //Index 1 is the title of the step
            LinearLayout titleLayout = (LinearLayout) stepLayout.getChildAt(0);

            //This gets the textviews of the title number and description
            TextView TitleNum = (TextView) titleLayout.getChildAt(0);
            TextView TitleDesc = (TextView) titleLayout.getChildAt(1);

            // This loops through the content of each step. We start at 1 since 0 is the title
            // and we -1 since we do not need to access the end which just holds the buttons
            for (int j = 1; j < stepLayout.getChildCount() - 1; j++) {
                    //This is where you can access each text or picture block and do whatever with it
                    //You can access it by using
                    // stepLayout.getChildAt(j);
                    //TODO: decide if its an image or text from here (maybe using tags?)
                    //TODO: add the image or text to the list
                    //TODO: set any variables/modifiers that are needed in the back end
                    //You can get URI from glide using
                    //https://stackoverflow.com/questions/42200448/how-to-get-uri-on-imageview-with-glide
            }
        }
    }
    /*
    public void uploadImage(final PictureData img, String picUri){

        //Check to see if the current step has an object saved in the db
        uploadStep(img);
        Uri newUri = Uri.parse(picUri);

        Glide.with(CreateNewGuide.this)
                .asBitmap()
                .load(newUri)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        //new ImageUploadAsyncTask().execute(resource);
                        //create a byte array output stream to prepare the image bitmap for upload
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //resource bitmap is compressed and stored into baos
                        resource.compress(Bitmap.CompressFormat.PNG,100,baos);
                        byte[] data = baos.toByteArray();//outstream is converted into byte array for upload

                        //the path of the image in firebase storage, is set as the imgPath for our PictureData obj for retrieval purposes
                        String path = "guideimages/users/" + mFirebaseAuth.getUid() + "/guide"+guideNum+"/" + img.getId() + ".png";
                        img.setImgPath(path);
                        StorageReference imgRef = mStorage.getReference(path);

                        //metadata is set for the image to be uploaded
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setCustomMetadata(mFirebaseAuth.getUid(),"guide"+guideNum+"/imgBlock-" + img.getId())
                                .build();

                        //image byte array is uploaded with our metadata
                        UploadTask uploadTask = imgRef.putBytes(data,metadata);

                        //on success, upload to database, otherwise go to the error page and tell us what went wrong
                        uploadTask.addOnSuccessListener(CreateNewGuide.this,new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //Can create a hashmap to upload but instead we use custom objects
                                DocumentReference imgBlockRef = picData.document("imgBlock-" + img.getId());
                                imgBlockRef.set(img);
                                //imgBlockNum++;
                            }
                        }).addOnFailureListener(CreateNewGuide.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Intent intent = new Intent(CreateNewGuide.this, ErrorActivity.class);
                                intent.putExtra("ERRORS", e.getLocalizedMessage());
                                startActivity(intent);
                            }
                        }).addOnProgressListener(CreateNewGuide.this, new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //display the progress to the user
                                long progress = taskSnapshot.getBytesTransferred();
                            }
                        });
                    }
                });

    }*/



    /**
     * Fixes the step numbers when a step is deleted
     * When the steps are reordered that means we have to update the step numbers for each item
     * in the data list and the tags for each data view so those edits are done too
     */
    private void reorderSteps(){
        //CRASHES AFTER PUTTING BOTH BUTTONS ON THE SAME LINE
        //int dataListPointer = 0;//used to iterate through the datalist by stepCount instead of one by one

        //Loops through the layout Feed and its children to set the step number to the correct step

        for(int i = 0; i < layoutFeed.getChildCount() - 1; i++){
            LinearLayout stepLayout = (LinearLayout) layoutFeed.getChildAt(i);
            LinearLayout titleLayout = (LinearLayout) stepLayout.getChildAt(0);
            TextView stepTitle = (TextView) titleLayout.getChildAt(0);
            String newStepTitle = "Step " + (i + 1) + " : ";
            stepTitle.setText(newStepTitle);
            reorderTags(stepLayout,(i+1));//reorders the tags for each view in each step's linear layout

            //CRASHES AFTER PUTTING BOTH BUTTONS ON THE SAME LINE
            //get the first object of the current step
            //GuideData data = mGuideDataArrayList.get(i+ dataListPointer);

            //CRASHES AFTER PUTTING BOTH BUTTONS ON THE SAME LINE
            //if data's number is more than the number we are changing to, then correct it
            //ex. removed step 1, step 2 is now the new step 1, therefore we must decrement all blocks from step 2,
            //otherwise its already correct so leave it
//            int currStepNum = data.getStepNumber();//the step num of the current data block we are on
//            int stepToCheck = currStepNum;//the current step whose data blocks we are working on
//            while (currStepNum > (i+1)){
//                data.setStepNumber(currStepNum-1);//lower the step number by 1 to its proper number
//                //get the next data object in the list so we can see if its in the same step as our current object
//               dataListPointer++;//we are moving to the next data object in the list
//                //if we have reached the last element, break the loop
//                if ((i+dataListPointer) >= mGuideDataArrayList.size()){
//                    break;
//                }
//                data = mGuideDataArrayList.get(i+dataListPointer);
//                currStepNum = data.getStepNumber();
//                //if this is not true, then we have reached a data object of the next step, break and move on
//                if (currStepNum != stepToCheck){
//                    dataListPointer--;
//                    break;
//                }
//            }


            //THIS IS NOT USED SINCE WE CHANGED THE BUTTONS TO BE IN ONE LINE
            //Changes the buttons text to the correct step number
            /*
            Button addStep = (Button) stepLayout.getChildAt(stepLayout.getChildCount() - 2);
            String newImageBtnDesc = "Add Image to step " + (i + 1);
            String newTextBtnDesc = "Add Text to Step " + (i + 1);
            addStep.setText(newImageBtnDesc);
            Button addDesc = (Button) stepLayout.getChildAt(stepLayout.getChildCount() - 1);
            addDesc.setText(newTextBtnDesc);
            */
        }
    }

    /**
     * Called whenever we have to re-order the views in the guide layout
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

                        ArrayList<GuideData> newDataList = new ArrayList<GuideData>();

                        for (int i =0;i<mGuideDataArrayList.size();i++){
                            GuideData data = mGuideDataArrayList.get(i);
                            if (data.getStepNumber() !=(int)stepLayout.getTag()){
                                newDataList.add(data);
                            }
                        }

                        mGuideDataArrayList = newDataList;

                        //iterates through the data list and removes all elements of the deleted step
                        /*Iterator<GuideData> iterator = mGuideDataArrayList.iterator();
                        try{
                            while (iterator.hasNext()){
                                GuideData data = iterator.next();
                                if (data.getStepNumber() == (int)stepLayout.getTag()){
                                    iterator.remove();
                                }
                            }
                        }catch(Exception ex){
                            Log.e("FUCKING ERRORS:",ex.getMessage());
                        }
*/
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
            } else if (requestCode == WRITE_DESC){
                if (data == null){
                    return;
                }
                newDesc = TextBlockWriterActivity.getNewDesc(data);
                addDescription(newDesc);
            }else if (requestCode == EDIT_DESC){
                if (data == null){
                    return;
                }
                newDesc = TextBlockWriterActivity.getNewDesc(data);
                editDescription(newDesc);
            }else if (requestCode == PESDK_RESULT){
                String editedImage = data.getStringExtra("resultURI");
                Uri editedUri = Uri.parse(editedImage);
                addImage(editedUri);
            }
        }
    }

    /**
     * Asks the user if they really want to go back and erease all unsaved data
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
        //outstate.putInt("TEXTBLOCKNUM",textBlockNum);
       // outstate.putInt("IMGBLOCKNUM",imgBlockNum);
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

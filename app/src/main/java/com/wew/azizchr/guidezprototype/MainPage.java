package com.wew.azizchr.guidezprototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainPage extends AppCompatActivity {


    //PROBLEMS:
    //guideNum resets everytime main page is loaded, needs to be saved - FIXED I THINK, NO WAY OF KNOWING UNTIL LATER
    //textBlockNUm and imgBlockNum don't work properly i think - FIXED
    //pictures taken with the camera aren't saved to firebase storage - FIXED
    //the instances may be different each time, logging out and logging back in generates a new instance - NOT A BUG BUT RELATED PROBLEM FIXED
    //probably more - DONT KNOW




    private static final String NAME_KEY = "name";
    private static final String PLACEMENT_KEY = "placement";
    private static final String TEXT_VALUE_KEY = "text";
    private static final String IMG_URL_KEY = "imgUrl";
    private static final String IMG_TAG = "IMG";
    private static final String TEXT_TAG = "TEXT";
    private static final String NEW_GUIDE = "NEW_GUIDE";

    LinearLayout layoutFeed;
    int index;
    private int guideNum;//the current guide index thing
    private int textBlockNum;
    private int imgBlockNum;
    private boolean isNew;

    //Firebase Instance Variables
    //private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mFirebaseAuth;

    //Cloud Firestore Reference Variables
    private DocumentReference mDocRef;
    private CollectionReference guideData;
    private CollectionReference picData;

    private ImagePicker imgPicker;
    private CameraImagePicker camera;
    private static final int SELECT_FILE =0;
    private static final int WRITE_TEXT =1;
    private String outputPath;
    private String newText;
    private List<Spinner> mSpinners = new ArrayList<Spinner>();
    private TextView display;
    private Button btnSaveGuide;

    User userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        display = findViewById(R.id.txtDisplay);

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentUser = mFirebaseAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        if (savedInstanceState != null){
            textBlockNum = savedInstanceState.getInt("TEXTBLOCKNUM");
            imgBlockNum = savedInstanceState.getInt("IMGBLOCKNUM");
        }else{
            textBlockNum = 0;
            imgBlockNum = 0;
        }

        Log.i("USER ID: ",mFirebaseAuth.getUid());
        DocumentReference userRef = mFirestore.document("Users/" + mFirebaseAuth.getUid());
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userData = documentSnapshot.toObject(User.class);
                String dbString = "Users/" + mFirebaseAuth.getUid() + "/guides/guide" + userData.getNumGuides();
                guideData = mFirestore.collection(dbString + "/textData");
                picData = mFirestore.collection(dbString + "/imageData");
                Log.i("","SUCCESSFULLY RETRIEVED DATA");

                mDocRef = guideData.document("textBlock"+textBlockNum);
                mDocRef.addSnapshotListener(MainPage.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()){
                            String text = documentSnapshot.getString(TEXT_VALUE_KEY);
                            //int placement = (int)documentSnapshot.get(PLACEMENT_KEY);
                            display.setText(text);
                        }else if(e!= null){
                            Log.i("GOT AN EXcEPTION: ", "onEvent: ", e);
                        }
                    }
                });
            }
        });

        layoutFeed = (LinearLayout) findViewById(R.id.layoutFeed);
        index = 0;
        camera = new CameraImagePicker(MainPage.this);
        camera.setImagePickerCallback(new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> list) {
                //Log.i("THUMBNAIL PATH: ",list.get(0).getQueryUri());
                Uri imagePath = Uri.parse(list.get(0).getQueryUri());
                addImage(imagePath);
            }

            @Override
            public void onError(String s) {

            }
        });

        btnSaveGuide = findViewById(R.id.btnSaveGuide);
        btnSaveGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveGuide();
            }
        });
    }

    public void saveToDevice(){

    }
//create a new object that models the data for the new text block
    //contains attributes of placement and type and stuff
    public boolean saveGuide(){
        for (int i =0; i< layoutFeed.getChildCount()-3;i++){
            String tag = (String) layoutFeed.getChildAt(i).getTag();
            switch (tag){
                case IMG_TAG:

                    break;
                case TEXT_TAG:

                    break;
            }
        }

        boolean isNew = getIntent().getBooleanExtra(NEW_GUIDE,false);
        if (isNew){
            guideNum++;
            DocumentReference newUserDoc = mFirestore.document("Users/" + mFirebaseAuth.getUid());
            newUserDoc.update("guideNum",guideNum);
            Log.i("SAVE_GUIDE: ","NEW GUIDE SAVED TO DB");
        }
        return true;
    }

    public void getTextBlock(){
        mDocRef = guideData.document("textBlock" + textBlockNum);
        mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String text = documentSnapshot.getString(TEXT_VALUE_KEY);
                    int placement = (int)documentSnapshot.get(PLACEMENT_KEY);
                    display.setText(text);
                }
            }
        });
    }

    public void uploadText(String text, int place){
        Log.i("UPLOADTEXT: ", "STARTING UPLOAD");
        if (text.isEmpty()){return;}
        Map<String,Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put(TEXT_VALUE_KEY,text);
        dataToSave.put(PLACEMENT_KEY,place);
        mDocRef = guideData.document("textBlock" + textBlockNum);
        mDocRef.set(dataToSave).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d("UPLOADTEXT: ", "onComplete: Text Block has been saved");
                }else{
                    Log.e("UPLOADTEXT: ", "onComplete: ",task.getException() );
                }
            }
        });

        textBlockNum++;
    }

    public void uploadImage(Bitmap img, final int place){
        Log.i("UPLOADIMAGE: ","Starting upload");
       // img.setDrawingCacheEnabled(true);
       // img.buildDrawingCache(true);
      //  Bitmap bitmap = img.getDrawingCache();
       // img.destroyDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG,100,baos);
      //  img.setDrawingCacheEnabled(false);
        byte[] data = baos.toByteArray();

        final String path = "guideimages/users/" + mFirebaseAuth.getUid() + "/guide"+guideNum+"/" + UUID.randomUUID() + ".png";
        StorageReference imgRef = mStorage.getReference(path);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("text","TESTING")
                .build();

        UploadTask uploadTask = imgRef.putBytes(data,metadata);
        Log.i("UPLOADIMAGE: ","uploadtask");
        uploadTask.addOnSuccessListener(MainPage.this,new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Map<String,Object> dataToSave = new HashMap<String, Object>();
                dataToSave.put(IMG_URL_KEY,path);
                dataToSave.put(PLACEMENT_KEY, place);
                mDocRef = picData.document("imgBlock" + imgBlockNum);
                mDocRef.set(dataToSave).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("UPLOADIMAGE: ", "onComplete: Image Block URL has been saved");
                        }else{
                            Log.e("UPLOADIMAGE: ", "onComplete: ",task.getException() );
                        }
                    }
                });
                Toast.makeText(MainPage.this, "Upload Complete", Toast.LENGTH_SHORT).show();
            }
        });
        Log.i("UPLOADIMAGE: ","please be done");
        imgBlockNum++;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        //FirebaseUser currentUser = mAuth.getCurrentUser();

    }


    /**
     * Brings up a menu of image options for the user on button press
     * @param view the current view of the object that ran the function
     */
    public void onClickGallery(View view) {
        SelectImage();
    }

    /**
     * Start an activity where the user can edit and retun the data of a text field
     * @param view current view of object
     */
    public void onClickText(View view) {
        Intent intent = new Intent(MainPage.this,TextBlockWriterActivity.class);
        startActivityForResult(intent,WRITE_TEXT);
    }

    /**
     * Builds and displays a menu of options for placing a picture
     */
    private void SelectImage(){
        final CharSequence[] items = {"Camera","Gallery","Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Camera")){
                    //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //startActivityForResult(intent,REQUEST_CAMERA);
                    outputPath = camera.pickImage();
                    //result.toBitmap().whenAvailable();
                }else if(items[i].equals("Gallery")){
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select File"),SELECT_FILE);
                    //imgPicker.pickImage();
                }else if(items[i].equals("Cancel")){
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
            LinearLayout newPicBlock = new LinearLayout(MainPage.this);
            final Spinner spinner = new Spinner(MainPage.this);
            setSpinnerListeners(spinner);
            ImageView newImgView = new ImageView(MainPage.this);
            Glide.with(this).load(imageUri).into(newImgView);
            Glide.with(MainPage.this)
                    .asBitmap()
                    .load(imageUri)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                            uploadImage(resource, index);
                        }
                    });

            newImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainPage.this, ViewPhoto.class);
                    intent.putExtra("imageUri", imageUri);
                    startActivity(intent);
                }
            });
            newImgView.setTag(IMG_TAG);

            //fits the image to the sides, fixes the view bounds, adds padding
            newImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newImgView.setAdjustViewBounds(true);
            newImgView.setPadding(3, 10, 3, 10);
            mSpinners.add(spinner);
            newPicBlock.addView(spinner);
            newPicBlock.addView(newImgView);
            layoutFeed.addView(newPicBlock, index);
            index++ ;
            updateSpinnerLists();
            Log.i("UPLOAD TO FB: ","BEFORE UPLOAD");
          //  uploadImage(newImgView);
            Log.i("UPLOAD TO FB: ","After UPLOAD");

        }catch(Exception ex){
            Log.i("IMAGE ERROR: ", ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Creates and adds a new text block to the layoutFeed
     * @param text of the new text block
     * @return true if success, otherwise false
     */
    public boolean addText(String text){
        try{
            LinearLayout newTextBlock = new LinearLayout(MainPage.this);
            final Spinner spinner = new Spinner(MainPage.this);
            setSpinnerListeners(spinner);

            TextView textView = new TextView(MainPage.this);
            textView.setText(Html.fromHtml(text));
            textView.setTag(TEXT_TAG);

            //modifies the texts size, color and padding
            textView.setTextSize(18);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(5,10,5,10);

            mSpinners.add(spinner);
            newTextBlock.addView(spinner);
            newTextBlock.addView(textView);
            layoutFeed.addView(newTextBlock, index);

            uploadText(textView.getText().toString(),index);
            index++;
            updateSpinnerLists();
        }catch (Exception ex){
            ex.getMessage();
            return false;
        }
        return true;
    }

    /**
     * Updates the default number settings of the spinners to match
     * their current place
     */
    public void updateSpinnerLists(){
        Log.d("LAYOUT FEED LENGTH: ",""+layoutFeed.getChildCount());
        for (int i = 0;i<layoutFeed.getChildCount()-1;i++){
            List<String> options = new ArrayList<String>();
            int o = 0;
            for (int j = 0;j<index;j++){
                o = j + 1;
                options.add(Integer.toString(o));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainPage.this,
                    R.layout.support_simple_spinner_dropdown_item,options);
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

            mSpinners.get(i).setAdapter(adapter);
            mSpinners.get(i).setSelection(i);//sets default selection to its current spot in the block hierarchy
            mSpinners.get(i).setTag(i);//set the current value as its tag for possible rearranging
        }
    }

    /**
     * The spinner associated with the moved block will need to be moved in the list to the
     * proper corresponding index so that it is aware of its new position
     * @param index of the spinner we're moving
     * @param indexToMove index that we are moving the spinner to
     */
    public void rearrangeSpinnerList(int index, int indexToMove){
        Spinner prev = mSpinners.get(index);
        mSpinners.remove(prev);
        mSpinners.add(indexToMove,prev);
    }

    /**
     * Rearranges the elements of the layout feed, then calls the rearrangeSpinnerList method
     * so the spinnerList mirrors the layout feed
     * @param indexBlock the index of the block that we're moving
     * @param indexToMove the index that we are moving the block to
     */
    public void reOrderBlocks(int indexBlock,int indexToMove){

        LinearLayout block = (LinearLayout) layoutFeed.getChildAt(indexBlock);
        View emptyBlock = new View(this);
        layoutFeed.removeViewAt(indexBlock);
        layoutFeed.addView(block,indexToMove);

        rearrangeSpinnerList(indexBlock,indexToMove);
        index = layoutFeed.getChildCount() - 1;
        updateSpinnerLists();
    }

    public boolean setSpinnerListeners(final Spinner spinner){
        try{
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i != (int)spinner.getTag()){
                        Log.d("ADAPTER PARENT:",""+spinner.getTag());
                        Log.d("INTS: ", ""+i + "/" + l);
                        reOrderBlocks((int)spinner.getTag(),i);
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Log.d("NOTHING ADAPTER PARENT:",""+spinner.getTag());
                }
            });
        }catch (Exception ex){
            ex.getMessage();
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_CAMERA){
                if (camera == null){
                    camera = new CameraImagePicker(MainPage.this,outputPath);
                    camera.setImagePickerCallback(new ImagePickerCallback() {
                        @Override
                        public void onImagesChosen(List<ChosenImage> list) {
                            //Log.i("THUMBNAIL PATH: ",list.get(0).getQueryUri());
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
            } else if (requestCode == WRITE_TEXT){
                if (data == null){
                    return;
                }
                newText = TextBlockWriterActivity.getTextBlockWritten(data);
                addText(newText);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outstate){
        //need a save path in case the activity is killed
        //will need to re-initialize cameraimagepicker
        outstate.putString("ImagePath",outputPath);
        outstate.putInt("guideNum",guideNum);
        outstate.putInt("TEXTBLOCKNUM",textBlockNum);
        outstate.putInt("IMGBLOCKNUM",imgBlockNum);
        super.onSaveInstanceState(outstate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        //after activity is restored, need to re-initialize these 2 values
        // in order to re-initialize CameraImagePicker
        if(savedInstanceState != null){
            outputPath = savedInstanceState.getString("ImagePath", null);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}

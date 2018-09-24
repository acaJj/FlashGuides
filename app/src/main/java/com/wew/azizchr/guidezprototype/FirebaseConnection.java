package com.wew.azizchr.guidezprototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class contains all the methods that interact with firebase
 * Created by Jeffrey on 2018-09-23.
 */

public class FirebaseConnection {
    //Firebase Instance Variables
    private FirebaseStorage mStorageInstance;
    private StorageReference imgStorage;
    private FirebaseFirestore mFirestoreInstance;
    private FirebaseAuth mFirebaseAuthInstance;
    private FirebaseUser mCurrentUser;

    private CollectionReference textData;
    private CollectionReference picData;
    private DocumentReference userRef;
    private int guideNum = 0;//the current guide index thing

    public FirebaseConnection(){
        mFirebaseAuthInstance = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuthInstance.getCurrentUser();
        mStorageInstance = FirebaseStorage.getInstance();
        imgStorage = mStorageInstance.getReference();
        mFirestoreInstance = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestoreInstance.setFirestoreSettings(settings);
    }

    public FirebaseStorage getStorageInstance() {
        return mStorageInstance;
    }

    public StorageReference getImgStorage() {
        return imgStorage;
    }

    public FirebaseFirestore getFirestoreInstance() {
        return mFirestoreInstance;
    }

    public FirebaseAuth getFirebaseAuthInstance() {
        return mFirebaseAuthInstance;
    }

    public FirebaseUser getCurrentUser() {
        return mCurrentUser;
    }

    public CollectionReference getTextData() {
        return textData;
    }

    public CollectionReference getPicData() {
        return picData;
    }

    public DocumentReference getUserRef() {
        return userRef;
    }

    public int getGuideNum() {
        return guideNum;
    }

    public void initializeGuideReferences(final String mode){
        userRef = mFirestoreInstance.document("Users/" + mFirebaseAuthInstance.getUid());

        //gets the number of guides the user has
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                guideNum = documentSnapshot.getLong("numGuides").intValue();

                if (mode.equals("CREATE"))guideNum++;// increments guideNum by 1 because we are making a new guide so there is 1 more than before

                //sets these 2 collections to point to the folders for the guide data of the new guide. for later uploading
                textData = mFirestoreInstance.collection("Users/" + mFirebaseAuthInstance.getUid() +"/guides/"+guideNum+"/textData");
                picData = mFirestoreInstance.collection("Users/" + mFirebaseAuthInstance.getUid() +"/guides/"+guideNum+"/imageData");

                //editorSetup(bundle);
            }

        });
    }

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
    }



    private void uploadStep(final GuideData data) {
        //if the current step has not had an object representing it stored in the db
        //then we will make one here before saving the text
        final DocumentReference step = mFirestoreInstance.document(
                "Users/" + mFirebaseAuthInstance.getUid() +"/guides/"+guideNum+"/stepData/step" + data.getStepNumber());
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

    /**
     * Uploads an image reference to firebase storage and the firestore database
     * @param img object to be uploaded
     * @param picUri the uri string of the picture being uploaded to storage
     */
    public void uploadImage(final PictureData img, String picUri, final Context context, final Activity activity){

        //Check to see if the current step has an object saved in the db
        uploadStep(img);
        //Uri newUri = Uri.fromFile(new File(picUri));
        Uri newUri = Uri.parse(picUri);

        Glide.with(context)
                .asBitmap()
                .load(newUri)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        //create a byte array output stream to prepare the image bitmap for upload
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //resource bitmap is compressed and stored into baos
                        resource.compress(Bitmap.CompressFormat.PNG,100,baos);
                        byte[] data = baos.toByteArray();//outstream is converted into byte array for upload

                        if (img.getId() == null || img.getId().equals("")){
                            img.setId(UUID.randomUUID().toString());
                        }

                        //the path of the image in firebase storage, is set as the imgPath for our PictureData obj for retrieval purposes
                        String path = "guideimages/users/" + mFirebaseAuthInstance.getUid() + "/guide"+guideNum+"/" + img.getId() + ".png";
                        img.setImgPath(path);
                        StorageReference imgRef = mStorageInstance.getReference(path);

                        //metadata is set for the image to be uploaded
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setCustomMetadata(mFirebaseAuthInstance.getUid(),"guide"+guideNum+"/imgBlock-" + img.getId())
                                .build();

                        //image byte array is uploaded with our metadata
                        UploadTask uploadTask = imgRef.putBytes(data,metadata);

                        //on success, do something, otherwise go to the error page and tell us what went wrong
                        uploadTask.addOnSuccessListener(activity,new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //Can create a hashmap to upload but instead we use custom objects
                                //DocumentReference imgBlockRef = picData.document("imgBlock" + imgBlockNum);
                                //imgBlockRef.set(img);
                                //imgBlockNum++;
                            }
                        }).addOnFailureListener(activity, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Intent intent = new Intent(CreateNewGuide.this, ErrorActivity.class);
                                //intent.putExtra("ERRORS", e.getLocalizedMessage());
                                //startActivity(intent);
                                Toast.makeText(context,"Failure Saving",Toast.LENGTH_LONG).show();
                            }
                        }).addOnProgressListener(activity, new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //display the progress to the user
                                long progress = taskSnapshot.getBytesTransferred();
                            }
                        });
                    }
                });

    }
}

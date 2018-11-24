package com.wew.azizchr.guidezprototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class contains all the methods that interact with firebase
 * Created by Jeffrey on 2018-09-23.
 */

public class FirebaseConnection {
    private static String DEBUG_TAG = "BORBOT";

    //Firebase Instance Variables
    private FirebaseStorage mStorageInstance;
    private StorageReference imgStorage;
    private FirebaseFirestore mFirestoreInstance;
    private FirebaseAuth mFirebaseAuthInstance;
    private FirebaseUser mCurrentUser;

//    private CollectionReference textData;
//    private CollectionReference picData;
//    private DocumentReference userRef;
//    private int guideNum = 0;//the current guide index thing

    public FirebaseConnection() {
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

 /*   public CollectionReference getTextData() {
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

    public void initializeGuideReferences(final String mode) {
        userRef = mFirestoreInstance.document("Users/" + mFirebaseAuthInstance.getUid());

        //gets the number of guides the user has
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                guideNum = documentSnapshot.getLong("numGuides").intValue();

                if (mode.equals("CREATE"))
                    guideNum++;// increments guideNum by 1 because we are making a new guide so there is 1 more than before

                //sets these 2 collections to point to the folders for the guide data of the new guide. for later uploading
                textData = mFirestoreInstance.collection("Users/" + mFirebaseAuthInstance.getUid() + "/guides/" + guideNum + "/textData");
                picData = mFirestoreInstance.collection("Users/" + mFirebaseAuthInstance.getUid() + "/guides/" + guideNum + "/imageData");

                //editorSetup(bundle);
            }

        });
    }*/

    /**
     * Uploads a text block to the firestore database
     *
     * @param text object to be uploaded
     * @param textData CollectionReference that we are uploading the text to
     */
    public void uploadText(TextData text,CollectionReference textData,int guideNum) {
        if (text.getStringFromBlob().isEmpty()) {
            return;
        }

        //Check to see if the current step has an object saved in the db
        uploadStep(text,guideNum);

        //if the text object does not have an id, that means that its new and hasn't been saved into the db
        //give it an id so that in future saves we won't create a new doc in db and will instead overwrite old save
        if (text.getId() == null || text.getId().equals("")) {
            text.setId(UUID.randomUUID().toString());
        }
        DocumentReference textBlockRef = textData.document("textBlock-" + text.getId());
        textBlockRef.set(text);
    }


    private void uploadStep(final GuideData data, int guideNum) {
        //if the current step has not had an object representing it stored in the db
        //then we will make one here before saving the text
        final DocumentReference step = mFirestoreInstance.document(
                "Users/" + mFirebaseAuthInstance.getUid() + "/guides/" + guideNum + "/stepData/step" + data.getStepNumber());
        step.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Map<String, Object> dataToSave = new HashMap<>();
                    dataToSave.put("stepNumber", data.getStepNumber());
                    dataToSave.put("stepTitle", data.getStepTitle());
                    if (!documentSnapshot.exists()) {
                        //step doesn't exist so we create it
                        step.set(dataToSave);
                    } else {
                        step.update(dataToSave);
                    }
                }
            }
        });
    }

    /**
     * Uploads all pictures in the guide to firestore and stores bitmaps in storage
     *
     * @param images list of data objects representing the pictures in the guide
     */
    public void uploadImages(final Context context, int guideNum,final CollectionReference picData, final ArrayList<PictureData> images) {
        //arraylists to hold bitmaps and their paths in the firebase storage
        final ArrayList<Bitmap> bitmapsToUpload = new ArrayList<>();
        final ArrayList<String> paths = new ArrayList<>();

        //get each PictureData obj in guide and extract bitmaps/file paths for async uploading;then store the data in firestore
        for (PictureData image : images) {
            //Check to see if the current step has an object saved in the db
            uploadStep(image,guideNum);
            //Uri imageUri = Uri.parse(image.getUri());
            //RequestOptions ro = new RequestOptions()
            //        .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

            final String path = "guideimages/users/" + mFirebaseAuthInstance.getUid() + "/guide" + guideNum + "/" + image.getId() + ".png";
            image.setImgPath(path);

            Glide.with(context)
                    .asBitmap()
                    .load(image.getUri())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            //Log.d(DEBUG_TAG,""+path);
                            bitmapsToUpload.add(resource);
                            paths.add(path);
                            //Log.d(DEBUG_TAG,""+mBitmaps.size());
                            //if uploadlist is the same size as the images list we passed in
                            //then all bitmaps have been processed and we are ready to upload
                            if (bitmapsToUpload.size() == images.size()) {
                                Log.d(DEBUG_TAG, "Uploading images asynchronously");
                                //send all the bitmaps to the async task
                                FirebaseConnection.ImageUploadAsyncTask imageUploader = new FirebaseConnection.ImageUploadAsyncTask(paths);
                                imageUploader.execute(bitmapsToUpload);
                            }
                        }
                    });

            //Can create a hashmap to upload but instead we use custom objects
            DocumentReference imgBlockRef = picData.document("imgBlock-" + image.getId());
            imgBlockRef.set(image);
        }
    }

    private static class ImageUploadAsyncTask extends AsyncTask<ArrayList<Bitmap>, Void, Long> {
        //private Activity sActivity;
        private static ArrayList<String> storagePaths;
        //private static String storagePath;
        private final FirebaseStorage mStorageReference;

        private ImageUploadAsyncTask(ArrayList<String> paths) {
            //sActivity = activity;
            storagePaths = paths;
            //storagePath = path;
            mStorageReference = FirebaseStorage.getInstance();
        }

        @Override
        protected Long doInBackground(ArrayList<Bitmap>[] bitmaps) {

            int storageindex = 0;
            for (Bitmap bmap : bitmaps[0]) {
                //create a byte array output stream to prepare the image bitmap for upload
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();//outstream is converted into byte array for upload
                String path = storagePaths.get(storageindex);//get the path for the image
                Log.d("BORBOT PATHs", path + " / " + bmap);
                StorageReference imgRef = mStorageReference.getReference(path);

                //image byte array is uploaded with our metadata
                UploadTask uploadTask = imgRef.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("ASYNCIMAGEUPLOADA", "Upload Success: " + taskSnapshot.getUploadSessionUri());
                        StorageMetadata storageMetadata = taskSnapshot.getMetadata();
                        Log.d("BORBOT", "Image upload successful");
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

            Log.d("BORBOT", "Uploading Complete");
            return null;
        }
    }
}
package com.wew.azizchr.guidezprototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.util.List;

public class MainPage extends AppCompatActivity {

    LinearLayout layoutFeed;
    int index;

    private ImagePicker imgPicker;
    private CameraImagePicker camera;
    private static final int SELECT_FILE =0;
    private static final int WRITE_TEXT =0;
    private String outputPath;
    private String newText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

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
    public boolean addImage(Uri imageUri){
        try{
            ImageView newImgView = new ImageView(MainPage.this);
            newImgView.setImageURI(imageUri);
            layoutFeed.addView(newImgView, index);
            index++ ;
        }catch(Exception ex){
            ex.getMessage();
            return false;
        }
        return true;
    }

    public boolean addText(String text){
        try{
            TextView textView = new TextView(MainPage.this);
            textView.setText(text);
            layoutFeed.addView(textView, index);
            index++;
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

package com.wew.azizchr.guidezprototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toolbar;

import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.util.ArrayList;
import java.util.List;

public class MainPage extends AppCompatActivity {

    LinearLayout layoutFeed;
    int index;

    private ImagePicker imgPicker;
    private CameraImagePicker camera;
    private static final int SELECT_FILE =0;
    private static final int WRITE_TEXT =1;
    private String outputPath;
    private String newText;
    private List<Spinner> mSpinners = new ArrayList<Spinner>();

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
            LinearLayout newPicBlock = new LinearLayout(MainPage.this);
            final Spinner spinner = new Spinner(MainPage.this);
            setSpinnerListeners(spinner);

            ImageView newImgView = new ImageView(MainPage.this);
            newImgView.setImageURI(imageUri);

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
        }catch(Exception ex){
            ex.getMessage();
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

            //modifies the texts size, color and padding
            textView.setTextSize(18);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(5,10,5,10);

            mSpinners.add(spinner);
            newTextBlock.addView(spinner);
            newTextBlock.addView(textView);
            layoutFeed.addView(newTextBlock, index);
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

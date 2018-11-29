package com.wew.azizchr.guidezprototype;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import ly.img.android.pesdk.assets.filter.basic.FilterPackBasic;
import ly.img.android.pesdk.assets.font.basic.FontPackBasic;
import ly.img.android.pesdk.assets.frame.basic.FramePackBasic;
import ly.img.android.pesdk.assets.overlay.basic.OverlayPackBasic;
import ly.img.android.pesdk.backend.model.constant.Directory;
import ly.img.android.pesdk.backend.model.state.EditorLoadSettings;
import ly.img.android.pesdk.backend.model.state.EditorSaveSettings;
import ly.img.android.pesdk.backend.model.state.manager.SettingsList;
import ly.img.android.pesdk.ui.activity.ImgLyIntent;
import ly.img.android.pesdk.ui.activity.PhotoEditorBuilder;
import ly.img.android.pesdk.ui.model.state.UiConfigFilter;
import ly.img.android.pesdk.ui.model.state.UiConfigFrame;
import ly.img.android.pesdk.ui.model.state.UiConfigOverlay;
import ly.img.android.pesdk.ui.model.state.UiConfigText;
import ly.img.android.pesdk.ui.utils.PermissionRequest;
import ly.img.android.serializer._3._0._0.PESDKFileWriter;

/**
 * Chris created on all of this as well as the matching XML file.
 */

public class EditorActivity extends Activity implements PermissionRequest.Response {

    private Boolean isNewPic;
    public Uri picToEdit;

    public static int GALLERY_RESULT = 0;
    public static int PESDK_RESULT = 1;

    // Important permission request for Android 6.0 and above, don't forget to add this!
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Sets background to black
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        Intent intent = getIntent();
        isNewPic = intent.getBooleanExtra("isNewPic", false);

        //Decide if we are selecting a new pic or if we are just editing an existing one
        if(isNewPic){
            Toast toast = Toast.makeText(getApplicationContext(), "Selecting a new pic?", Toast.LENGTH_SHORT);
            toast.show();
            openSystemGalleryToSelectAnImage();
        } else {
            picToEdit = getIntent().getParcelableExtra("imageUri");
            openEditor(picToEdit);
        }

    }

    @Override
    public void permissionGranted() {
        // Great job
    }

    @Override
    public void permissionDenied() {
        //bad job
        //finish();
    }

    private SettingsList createPesdkSettingsList() {

        // Create a empty new SettingsList and apply the changes on this reference.
        SettingsList settingsList = new SettingsList();

        // Populate the SettingsList
        settingsList.getSettingsModel(UiConfigFilter.class).setFilterList(
                FilterPackBasic.getFilterPack()
        );
        settingsList.getSettingsModel(UiConfigText.class).setFontList(
                FontPackBasic.getFontPack()
        );
        settingsList.getSettingsModel(UiConfigOverlay.class).setOverlayList(
                OverlayPackBasic.getOverlayPack()
        );
        settingsList.getSettingsModel(UiConfigFrame.class).setFrameList(
                FramePackBasic.getFramePack()
        );

        // Set custom editor image export settings
        settingsList.getSettingsModel(EditorSaveSettings.class)
                .setExportDir(Directory.DCIM, "FlashGuides")
                .setExportPrefix("result_")
                .setSavePolicy(EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT);

        return settingsList;
    }
    private void openSystemGalleryToSelectAnImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GALLERY_RESULT);
        } else {
            Toast.makeText(
                    this,
                    "No Gallery APP installed",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openEditor(Uri inputImage) {
        SettingsList settingsList = createPesdkSettingsList();

        // Set input image
        settingsList.getSettingsModel(EditorLoadSettings.class)
                .setImageSource(inputImage);

        new PhotoEditorBuilder(this)
                .setSettingsList(settingsList)
                .startActivityForResult(this, PESDK_RESULT);
        overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == GALLERY_RESULT) {
            // Open Editor with some uri in this case with an image selected from the system gallery.
            Uri selectedImage = data.getData();
            openEditor(selectedImage);

        } else if (resultCode == RESULT_OK && requestCode == PESDK_RESULT) {
            // Editor has saved an Image.
            Uri resultURI = data.getParcelableExtra(ImgLyIntent.RESULT_IMAGE_URI);
            Uri sourceURI = data.getParcelableExtra(ImgLyIntent.SOURCE_IMAGE_URI);

            // Scan result uri to show it up in the Gallery
            if (resultURI != null) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(resultURI));
            }

            // Scan source uri to show it up in the Gallery
            if (sourceURI != null) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(sourceURI));
            }

            //Return back to create guide with new image
            Intent returnIntent = new Intent();
            returnIntent.putExtra("resultURI", resultURI.toString());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
            overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);

            /*// OPTIONAL: read the latest state to save it as a serialisation
            SettingsList lastState = data.getParcelableExtra(ImgLyIntent.SETTINGS_LIST);
            try {
                new PESDKFileWriter(lastState).writeJson(new File(
                        Environment.getExternalStorageDirectory(),
                        "serialisationReadyToReadWithPESDKFileReader.json"
                ));
            } catch (IOException e) { e.printStackTrace(); }

            */

        } else if (resultCode == RESULT_CANCELED && requestCode == PESDK_RESULT) {
            // Editor was canceled
            finish();
            overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
        } else if (resultCode == RESULT_CANCELED && requestCode == GALLERY_RESULT) {
            // Gallery was canceled
            finish();
            overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
        }
    }
}

package com.wew.azizchr.guidezprototype;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import ly.img.android.pesdk.assets.filter.basic.FilterPackBasic;
import ly.img.android.pesdk.assets.font.basic.FontPackBasic;
import ly.img.android.pesdk.assets.overlay.basic.OverlayPackBasic;
import ly.img.android.pesdk.backend.model.constant.Directory;
import ly.img.android.pesdk.backend.model.state.CameraSettings;
import ly.img.android.pesdk.backend.model.state.EditorSaveSettings;
import ly.img.android.pesdk.backend.model.state.manager.SettingsList;
import ly.img.android.pesdk.ui.activity.CameraPreviewActivity;
import ly.img.android.pesdk.ui.activity.CameraPreviewBuilder;
import ly.img.android.pesdk.ui.activity.ImgLyIntent;
import ly.img.android.pesdk.ui.model.state.UiConfigFilter;
import ly.img.android.pesdk.ui.model.state.UiConfigOverlay;
import ly.img.android.pesdk.ui.model.state.UiConfigText;
import ly.img.android.pesdk.ui.utils.PermissionRequest;

public class CameraViewActivity extends Activity implements PermissionRequest.Response {

    public CameraPreviewActivity camera;

    // Important permission request for Android 6.0 and above, don't forget to add this!
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void permissionGranted() {
        Toast.makeText(CameraViewActivity.this,"Permission Granted",Toast.LENGTH_LONG).show();
    }

    @Override
    public void permissionDenied() {
        /* TODO: The Permission was rejected by the user. The Editor was not opened,
         * Show a hint to the user and try again. */
        Toast.makeText(CameraViewActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
    }

    public static int PESDK_RESULT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
        openCamera();
    }

    private SettingsList createPesdkSettingsList() {

        // Create a empty new SettingsList and apply the changes on this referance.
        SettingsList settingsList = new SettingsList();

        // If you include our asset Packs and you use our UI you also need to add them to the UI,
        // otherwise they are only available for the backend
        // See the specific feature sections of our guides if you want to know how to add our own Assets.

        settingsList.getSettingsModel(UiConfigFilter.class).setFilterList(
                FilterPackBasic.getFilterPack()
        );

        settingsList.getSettingsModel(UiConfigText.class).setFontList(
                FontPackBasic.getFontPack()
        );

        settingsList.getSettingsModel(UiConfigOverlay.class).setOverlayList(
                OverlayPackBasic.getOverlayPack()
        );

        // Set custom camera image export settings
        settingsList.getSettingsModel(CameraSettings.class)
                .setExportDir(Directory.DCIM, "GuidePics")
                .setExportPrefix("camera_");

        // Set custom editor image export settings
        settingsList.getSettingsModel(EditorSaveSettings.class)
                .setExportDir(Directory.DCIM, "GuidePics")
                .setExportPrefix("result_")
                .setSavePolicy(EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT);

        return settingsList;
    }

    //problem must be here somewhere
    private void openCamera() {
        SettingsList settingsList = createPesdkSettingsList();
        //second one runs, settings list is not null even though error says we use a null object
        if (settingsList == null){
            Log.i("SettingsCheck", " its null nigga");
        }else{
            Log.i("SettingsCheck", " its cool");
        }
        new CameraPreviewBuilder(CameraViewActivity.this)
                .setSettingsList(settingsList)//tried commenting this out, got runtime exception: editor started without intent
                .startActivityForResult(CameraViewActivity.this, PESDK_RESULT);

    }

    /*TODO: The app crashes when the user takes a picture or picks one from the gallery, need fixin
      Get error "try to use a virtual method on a null object reference" i think it has something to do with the settings list */
//Code never returns a result so it never gets to here
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PESDK_RESULT) {
            // Editor has saved an Image.
            Uri resultURI = data.getParcelableExtra(ImgLyIntent.RESULT_IMAGE_URI);
            Uri sourceURI = data.getParcelableExtra(ImgLyIntent.SOURCE_IMAGE_URI);

            // Scan result uri to show it up in the Gallery
            //tried commenting these 2 out, doesnt effect it
            if (resultURI != null) {
                Log.i("SettingsCheck","good result");
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(resultURI));
            }

            // Scan source uri to show it up in the Gallery
            if (sourceURI != null) {
                Log.i("SettingsCheck","good source");
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(sourceURI));
            }
            Log.i("SettingsCheck","result: " + resultURI.toString() + " / source: " + sourceURI.toString());
            // Send the image back to the guide editor, taking this out doesnt change the crashing
            Intent intent = new Intent(CameraViewActivity.this,CreateNewGuide.class);
            intent.putExtra("URI",resultURI);
            setResult(RESULT_OK,intent);


        } else if (resultCode == RESULT_CANCELED && requestCode == PESDK_RESULT) {
            Log.i("SettingsCheck","Result Cancelled");
            Uri sourceURI = data.getParcelableExtra(ImgLyIntent.SOURCE_IMAGE_URI);
        }
    }

    public static Uri getSourceUri(Intent data){
        return Uri.parse(data.getStringExtra("URI"));
    }
}

package com.wew.azizchr.guidezprototype;

import android.graphics.Bitmap;
//import java.io.ByteArrayOutputStream;

/**
 * Created by Jeffrey on 2018-06-01.
 *
 * Image Bitmap will be taken from here instead of being done in the uploadImage method
 *
 */

public class PictureData extends GuideData {

    private String imgPath;
    private Bitmap img;

    public PictureData() {
    }

    public PictureData(String id, String type, int placement, String guideId, Object data) {
        super(id, type, placement, guideId, data);
    }

    /*public byte[] getImageData(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG,100,baos);
        return baos.toByteArray();
    }*/

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }
}

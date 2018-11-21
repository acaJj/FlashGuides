package com.wew.azizchr.guidezprototype;

import com.google.firebase.firestore.GeoPoint;

/**
 * Created by Jeffrey on 2018-06-01.
 *
 * Image Bitmap will be taken from here instead of being done in the uploadImage method
 *
 */

public class PictureData extends GuideData {

    private String imgPath;
    private String uri;

    public GeoPoint coordinates;

    public PictureData() {
    }

    public PictureData(String id, String type, int placement, String guideId) {
        super(id, type, placement, guideId);
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public GeoPoint getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(GeoPoint coordinates) {
        this.coordinates = coordinates;
    }
}
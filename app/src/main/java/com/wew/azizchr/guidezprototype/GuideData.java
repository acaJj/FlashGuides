package com.wew.azizchr.guidezprototype;

/**
 * Created by Jeffrey on 2018-05-19.
 */


/**
 * This class isnt used for anything right now but in the future it will be used to model
 * the data that we dump into the db
 */

public class GuideData {
    private String id;
    private String type;
    private String placement;
    private String guideId;
    private String data;

    public GuideData(){}

    public GuideData(String id, String type, String placement, String guideId, String data) {
        this.id = id;
        this.type = type;
        this.placement = placement;
        this.guideId = guideId;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlacement() {
        return placement;
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    public String getGuideId() {
        return guideId;
    }

    public void setGuideId(String guideId) {
        this.guideId = guideId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

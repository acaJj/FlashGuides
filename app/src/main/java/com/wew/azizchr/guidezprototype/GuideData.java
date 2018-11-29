package com.wew.azizchr.guidezprototype;

/**
 * Created by Jeffrey on 2018-05-19.
 * Models the data that make up a guide
 */

public class GuideData {
    private String id;
    private String type;
    private int placement;
    private String guideId;
    private String stepTitle;
    private int stepNumber;

    public GuideData(){}

    public GuideData(String id, String type, int placement, String guideId) {
        this.id = id;
        this.type = type;
        this.placement = placement;
        this.guideId = guideId;
    }

    public GuideData(String type, int placement, String guideId, String stepTitle, int stepNumber) {
        this.type = type;
        this.placement = placement;
        this.guideId = guideId;
        this.stepTitle = stepTitle;
        this.stepNumber = stepNumber;
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

    public int getPlacement() {
        return placement;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public String getGuideId() {
        return guideId;
    }

    public void setGuideId(String guideId) {
        this.guideId = guideId;
    }

    public String getStepTitle() {
        return stepTitle;
    }

    public void setStepTitle(String stepTitle) {
        this.stepTitle = stepTitle;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public void setStep(int stepNumber, String stepTitle){
        this.stepNumber = stepNumber;
        this.stepTitle = stepTitle;
    }
}

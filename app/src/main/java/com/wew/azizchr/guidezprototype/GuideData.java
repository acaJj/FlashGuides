package com.wew.azizchr.guidezprototype;

/**
 * Created by Jeffrey on 2018-05-19.
 * Models the data that make up a guide
 */

public class GuideData {
    private final static String PIC_TYPE = "Picture";
    private final static String TEXT_TYPE = "Text";

    private String id;
    private String type;
    private int placement;
    private String guideId;
    private String stepTitle;
    private int stepNumber;

    public GuideData(String id, String type, int placement, String guideId) {
        if (id == null || type == null || guideId == null)return;

        if (!id.equals("") && !type.equals("") && !guideId.equals("") && placement >= 0){
            this.id = id;
            this.type = type;
            this.placement = placement;
            this.guideId = guideId;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null)return;

        if (!id.equals("")){
            this.id = id;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null)return;

        if (type.equals(PIC_TYPE) || type.equals(TEXT_TYPE)){
            this.type = type;
        }
    }

    public int getPlacement() {
        return placement;
    }

    public void setPlacement(int placement) {
        if (placement <0)return;//should never be a negative

        this.placement = placement;
    }

    public String getGuideId() {
        return guideId;
    }

    public void setGuideId(String guideId) {
        if (guideId == null)return;

        if (!guideId.equals("")){
            this.guideId = guideId;
        }
    }

    public String getStepTitle() {
        return stepTitle;
    }

    public void setStepTitle(String stepTitle) {
        if (stepTitle == null)return;

        if (!stepTitle.equals("")){
            this.stepTitle = stepTitle;
        }
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        if (stepNumber >= 0){
            this.stepNumber = stepNumber;
        }
    }

    public void setStep(int stepNumber, String stepTitle){
        if (stepTitle == null)return;

        if (!stepTitle.equals("")){
            this.stepTitle = stepTitle;
        }
        if (stepNumber >= 0){
            this.stepNumber = stepNumber;
        }
    }
}

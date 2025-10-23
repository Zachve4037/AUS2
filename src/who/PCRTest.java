package who;

import java.time.LocalDateTime;

public class PCRTest {
    private LocalDateTime timeOfTest_;
    private String personID_;
    private int pcrID_;
    private int workplaceID_;
    private int regionID_;
    private int districtID_;
    private boolean result_;
    private double testValue_;
    private String note_;

    public LocalDateTime getTimeOfTest_() {
        return timeOfTest_;
    }

    public void setTimeOfTest_(LocalDateTime timeOfTest_) {
        this.timeOfTest_ = timeOfTest_;
    }

    public String getPersonID_() {
        return personID_;
    }

    public void setPersonID_(String personID_) {
        this.personID_ = personID_;
    }

    public int getPcrID_() {
        return pcrID_;
    }

    public void setPcrID_(int pcrID_) {
        this.pcrID_ = pcrID_;
    }

    public int getWorkplaceID_() {
        return workplaceID_;
    }

    public void setWorkplaceID_(int workplaceID_) {
        this.workplaceID_ = workplaceID_;
    }

    public int getRegionID_() {
        return regionID_;
    }

    public void setRegionID_(int regionID_) {
        this.regionID_ = regionID_;
    }

    public int getDistrictID_() {
        return districtID_;
    }

    public void setDistrictID_(int districtID_) {
        this.districtID_ = districtID_;
    }

    public boolean isResult_() {
        return result_;
    }

    public void setResult_(boolean result_) {
        this.result_ = result_;
    }

    public double getTestValue_() {
        return testValue_;
    }

    public void setTestValue_(double testValue_) {
        this.testValue_ = testValue_;
    }

    public String getNote_() {
        return note_;
    }

    public void setNote_(String note_) {
        this.note_ = note_;
    }
}

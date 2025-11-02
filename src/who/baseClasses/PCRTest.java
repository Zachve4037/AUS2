package who.baseClasses;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PCRTest implements Comparable<PCRTest>, Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDateTime timeOfTest_;
    private String personID_;
    private Person person_;
    private int pcrID_;
    private int workplaceID_;
    private int regionID_;
    private int districtID_;
    private boolean result_;
    private double testValue_;
    private String note_;

    public PCRTest(LocalDateTime time,
            String id,
            int pcrId,
            int workplaceId,
            int regionId,
            int districtId,
            boolean res,
            double testVal,
            String note)
    {
        this.timeOfTest_ = time;
        this.personID_ = id;
        this.pcrID_ = pcrId;
        this.workplaceID_ = workplaceId;
        this.regionID_ = regionId;
        this.districtID_ = districtId;
        this.result_ = res;
        this.testValue_ = testVal;
        this.note_ = note;
    }

    public LocalDateTime getTimeOfTest() {
        return timeOfTest_;
    }

    public void setTimeOfTest(LocalDateTime timeOfTest_) {
        this.timeOfTest_ = timeOfTest_;
    }

    public String getPersonID() {
        return personID_;
    }

    public void setPersonID(String personID_) {
        this.personID_ = personID_;
    }

    public int getPcrID() {
        return pcrID_;
    }

    public void setPcrID(int pcrID_) {
        this.pcrID_ = pcrID_;
    }

    public int getWorkplaceID() {
        return workplaceID_;
    }

    public void setWorkplaceID(int workplaceID_) {
        this.workplaceID_ = workplaceID_;
    }

    public int getRegionID() {
        return regionID_;
    }

    public void setRegionID(int regionID_) {
        this.regionID_ = regionID_;
    }

    public int getDistrictID() {
        return districtID_;
    }

    public void setDistrictID(int districtID_) {
        this.districtID_ = districtID_;
    }

    public boolean getTestResult() {
        return result_;
    }

    public void setTestResult(boolean result_) {
        this.result_ = result_;
    }

    public double getTestValue() {
        return testValue_;
    }

    public void setTestValue(double testValue_) {
        this.testValue_ = testValue_;
    }

    public String getNote() {
        return note_;
    }

    public void setNote(String note_) {
        this.note_ = note_;
    }

    @Override
    public int compareTo(PCRTest o) {
        if (o == null) return 1;
        return Integer.compare(this.pcrID_, o.pcrID_);
    }

    public Person getPerson() {
        return person_;
    }

    public void setPerson(Person preson_) {
        this.person_ = preson_;
    }

    public static PCRTest keyToTest(int id) {
        return new PCRTest(null, "", id, 0, 0, 0, false, 0.0, "");
    }
}

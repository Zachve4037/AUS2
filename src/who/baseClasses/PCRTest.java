package who.baseClasses;

import who.Exportable;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PCRTest implements Comparable<PCRTest>, Serializable, Exportable {
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

    public void printTestInfo() {
        System.out.println("PCR Test ID: " + this.pcrID_);
        System.out.println("Person ID: " + this.personID_);
        System.out.println("Time of Test: " + this.timeOfTest_);
        System.out.println("Workplace ID: " + this.workplaceID_);
        System.out.println("Region ID: " + this.regionID_);
        System.out.println("District ID: " + this.districtID_);
        System.out.println("Test Result: " + (this.result_ ? "Positive" : "Negative"));
        System.out.println("Test Value: " + this.testValue_);
        System.out.println("Note: " + this.note_);
        this.person_.printPersonInfo();
    }

    public LocalDateTime getTimeOfTest() {
        return timeOfTest_;
    }

    public void setTimeOfTest(LocalDateTime timeOfTest_) {
        this.timeOfTest_ = timeOfTest_;
    }

    @Override
    public String serialize() {
        return this.timeOfTest_.toString() + ";" +
                this.personID_ + ";" +
                this.pcrID_ + ";" +
                this.workplaceID_ + ";" +
                this.regionID_ + ";" +
                this.districtID_ + ";" +
                this.result_ + ";" +
                this.testValue_ + ";" +
                this.note_;
    }

    @Override
    public void deserialize(String data) {
        String[] parts = data.split(";");
        if (parts.length == 9) {
            this.timeOfTest_ = LocalDateTime.parse(parts[0]);
            this.personID_ = parts[1];
            this.pcrID_ = Integer.parseInt(parts[2]);
            this.workplaceID_ = Integer.parseInt(parts[3]);
            this.regionID_ = Integer.parseInt(parts[4]);
            this.districtID_ = Integer.parseInt(parts[5]);
            this.result_ = Boolean.parseBoolean(parts[6]);
            this.testValue_ = Double.parseDouble(parts[7]);
            this.note_ = parts[8];
        }
    }

    @Override
    public int compareTo(PCRTest o) {
        if (o == null) return 1;
        return Integer.compare(this.pcrID_, o.pcrID_);
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

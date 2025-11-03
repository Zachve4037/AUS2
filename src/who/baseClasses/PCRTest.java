package who.baseClasses;

import who.DBSystem.DBServicesANDInterface.Exportable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;

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

    public PCRTest(
            LocalDateTime time,
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TestID=").append(this.pcrID_)
                .append(" PersonID=").append(this.personID_)
                .append(" Time=").append(this.timeOfTest_)
                .append(" Result=").append(this.result_ ? "Positive" : "Negative")
                .append(" WorkplaceID=").append(this.workplaceID_)
                .append(" RegionID=").append(this.regionID_)
                .append(" DistrictID=").append(this.districtID_)
                .append(" Value=").append(this.testValue_)
                .append(" Note=").append(this.note_)
                .append(" !!!END OF TESTS INFO!!! ");
        if (this.person_ != null) {
            sb.append(" [Person: ID=").append(this.person_.getID())
                    .append(" Name=").append(this.person_.getName())
                    .append(" Surname=").append(this.person_.getSurname())
                    .append(" Birth=").append(this.person_.getBirthDate())
                    .append("]");
        }
        return sb.toString();
    }

    public LocalDateTime getTimeOfTest() {
        return timeOfTest_;
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
        String[] parts = data.split(";", -1);
        if (parts.length >= 9) {
            try {
                this.timeOfTest_ = parts[0].isEmpty() ? null : LocalDateTime.parse(parts[0]);
            } catch (Exception e) {
                this.timeOfTest_ = null;
            }

            this.personID_ = parts[1];

            try {
                this.pcrID_ = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                this.pcrID_ = 0;
            }

            try {
                this.workplaceID_ = Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                this.workplaceID_ = 0;
            }

            try {
                this.regionID_ = Integer.parseInt(parts[4]);
            } catch (NumberFormatException e) {
                this.regionID_ = 0;
            }

            try {
                this.districtID_ = Integer.parseInt(parts[5]);
            } catch (NumberFormatException e) {
                this.districtID_ = 0;
            }

            this.result_ = Boolean.parseBoolean(parts[6]);

            try {
                this.testValue_ = Double.parseDouble(parts[7]);
            } catch (NumberFormatException e) {
                this.testValue_ = 0.0;
            }

            if (parts.length == 9) {
                this.note_ = parts[8];
            } else {
                this.note_ = String.join(";", Arrays.copyOfRange(parts, 8, parts.length));
            }
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

    public int getRegionID() {
        return regionID_;
    }

    public int getDistrictID() {
        return districtID_;
    }

    public boolean getTestResult() {
        return result_;
    }

    public double getTestValue() {
        return testValue_;
    }

    public String getNote() {
        return note_;
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

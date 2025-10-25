package who;

import java.util.Date;

public class Person implements Comparable<Person> {
    private String name_;
    private String surname_;
    private Date birthDate_;
    private String ID_;

    public Person(String id, String name, String surname, Date birthDate) {
        this.ID_ = id;
        this.name_ = name;
        this.surname_ = surname;
        this.birthDate_ = birthDate;
    }

    @Override
    public int compareTo(Person p) {
        if (p == null) return 1;
        return this.ID_.compareTo(p.ID_);
    }

    public String getName_() {
        return name_;
    }

    public void setName_(String name_) {
        this.name_ = name_;
    }

    public String getSurname_() {
        return surname_;
    }

    public void setSurname_(String surname_) {
        this.surname_ = surname_;
    }

    public Date getBirthDate_() {
        return birthDate_;
    }

    public void setBirthDate_(Date birthDate_) {
        this.birthDate_ = birthDate_;
    }

    public String getID_() {
        return ID_;
    }

    public void setID_(String ID_) {
        this.ID_ = ID_;
    }
}

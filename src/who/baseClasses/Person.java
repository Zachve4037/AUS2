package who.baseClasses;

import trees.avl.AVLTree;
import who.wrappers.PCRT_DateTime;

import java.util.Date;

import static who.baseClasses.PCRTest.keyToTest;

public class Person implements Comparable<Person> {
    private String name_;
    private String surname_;
    private Date birthDate_;
    private String ID_;
    private AVLTree<PCRT_DateTime> pcrTestsByDate_;
    private AVLTree<PCRTest> pcrTestsById_;

    public Person(String id, String name, String surname, Date birthDate) {
        this.ID_ = id;
        this.name_ = name;
        this.surname_ = surname;
        this.birthDate_ = birthDate;
        this.pcrTestsByDate_ = new AVLTree<>();
        this.pcrTestsById_ = new AVLTree<>();
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

    public AVLTree<PCRT_DateTime> getPcrTestsByDate_() {
        return this.pcrTestsByDate_;
    }

    public AVLTree<PCRTest> getPcrTestsById_() {
        return pcrTestsById_;
    }

    public void addTest(PCRTest test) {
        this.pcrTestsByDate_.insert(new PCRT_DateTime(test));
        this.pcrTestsById_.insert(test);
    }

    public void deleteTest(PCRTest test) {
        this.pcrTestsByDate_.delete(new PCRT_DateTime(test));
        this.pcrTestsById_.delete(test);
    }


    public PCRTest getTestByID(int testId) {
        return this.pcrTestsById_.search(keyToTest(testId));
    }
}

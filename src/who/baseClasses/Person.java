package who.baseClasses;

import trees.avl.AVLTree;
import who.Exportable;
import who.wrappers.PCRT_DateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static who.baseClasses.PCRTest.keyToTest;

public class Person implements Comparable<Person>, Serializable, Exportable {
    private static final long serialVersionUID = 1L;
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

    @Override
    public String serialize() {
        return this.ID_ + ";" + this.name_ + ";" + this.surname_ + ";" + this.birthDate_.getTime();
    }

    @Override
    public void deserialize(String data) {
        String[] parts = data.split(";");
        if (parts.length == 4) {
            this.ID_ = parts[0];
            this.name_ = parts[1];
            this.surname_ = parts[2];
            try {
                long time = Long.parseLong(parts[3]);
                this.birthDate_ = new Date(time);
            } catch (NumberFormatException e) {
                this.birthDate_ = null;
            }
        }
    }

    public String getName() {
        return name_;
    }

    public String getSurname() {
        return surname_;
    }

    public Date getBirthDate() {
        return birthDate_;
    }

    public String getID() {
        return ID_;
    }

    public AVLTree<PCRTest> getPcrTestsById_() {
        return pcrTestsById_;
    }

    public void addTest(PCRTest test) {
        this.pcrTestsByDate_.insert(new PCRT_DateTime(test));
        this.pcrTestsById_.insert(test);
        test.setPerson(this);
        test.setPersonID(this.ID_);
    }

    public void deleteTest(PCRTest test) {
        if (test == null) return;
        this.pcrTestsByDate_.delete(new PCRT_DateTime(test));
        this.pcrTestsById_.delete(test);

        if (test.getPerson() == this) {
            test.setPerson(null);
            test.setPersonID("");
        }
    }

    public PCRTest getTestByID(int testId) {
        return this.pcrTestsById_.search(keyToTest(testId));
    }

    public List<PCRT_DateTime> getAllTestsByDate() {
        return this.pcrTestsByDate_.inOrder(false);
    }
}

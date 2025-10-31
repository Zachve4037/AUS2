package who.DBSystem;

import trees.avl.AVLTree;
import who.administrativeUnits.District;
import who.administrativeUnits.Region;
import who.administrativeUnits.Workplace;
import who.baseClasses.Person;
import who.wrappers.PCRT_DateTime;

public class Database {
    private AVLTree<PCRT_DateTime> tests_;
    private AVLTree<Person> people_;
    private AVLTree<Region> regions_;
    private AVLTree<District> districts_;
    private AVLTree<Workplace> workplaces_;

}

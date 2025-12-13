package S2;

import S2.db.DBViewer;
import S2.tester.BTester;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        BTester tester = new BTester();
//        tester.testLinearHashingManual();
        tester.testLinearHashing();
//        tester.testHeapFile();
//        tester.testLeftmostInsertion();
//        tester.testLastBlockEmpty();
//        SwingUtilities.invokeLater(() -> {
//            DBViewer frame = new DBViewer();
//            frame.setVisible(true);
//        });
    }
}

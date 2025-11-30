package S2;

import S2.db.DBViewer;

import javax.swing.*;

public class Main {
    static void main(String[] args) {
//        BTester tester = new BTester();
//        tester.testHeapFile();
//        tester.testLeftmostInsertion();
//        tester.testLastBlockEmpty();
        SwingUtilities.invokeLater(() -> {
            DBViewer frame = new DBViewer();
            frame.setVisible(true);
        });
    }
}

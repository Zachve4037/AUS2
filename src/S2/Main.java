package S2;

import S2.DB.DBViewer;
import S2.Tester.BTester;

import javax.swing.*;

public class Main {
    static void main() {
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

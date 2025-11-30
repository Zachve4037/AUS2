package S2.db;

import S2.tester.BTester;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class DBViewer extends JFrame {
    private File dataFile;
    private File metaFile;
    private final JButton runTestButton;
    private final JButton loadHeapButton;
    private final JButton openViewerButton;
    private final JLabel statusLabel;

    public DBViewer() {
        super("HeapFile GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        runTestButton = new JButton("Run Test");
        loadHeapButton = new JButton("Load HeapFile...");
        openViewerButton = new JButton("Open Viewer");

        runTestButton.addActionListener(this::onRunTest);
        loadHeapButton.addActionListener(this::onLoadHeap);
        openViewerButton.addActionListener(this::onOpenViewer);

        topPanel.add(runTestButton);
        topPanel.add(loadHeapButton);
        topPanel.add(openViewerButton);
        add(topPanel, BorderLayout.NORTH);

        statusLabel = new JLabel("No heap loaded");
        add(statusLabel, BorderLayout.SOUTH);

        JTextArea hint = new JTextArea(
                """
                        Use Run Test to generate test heap files.
                        Then use Load HeapFile to pick data/meta files and Open Viewer to inspect.
                        Viewer will display blocks and each record's fields and raw bytes."""
        );
        hint.setEditable(false);
        add(new JScrollPane(hint), BorderLayout.CENTER);
    }

    private void onRunTest(ActionEvent e) {
        runTestButton.setEnabled(false);
        statusLabel.setText("Running test (background)...");
        new Thread(() -> {
            try {
                BTester tester = new BTester();
                tester.testHeapFile();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Test finished. Use Load HeapFile to open files."));
            } catch (Throwable ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Test failed: " + ex.getMessage()));
            } finally {
                SwingUtilities.invokeLater(() -> runTestButton.setEnabled(true));
            }
        }, "RunTestThread").start();
    }

    private void onLoadHeap(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select data file (e.g. test_heap_1.dat)");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        dataFile = chooser.getSelectedFile();

        chooser.setDialogTitle("Select metadata file (e.g. test_heap_1.meta)");
        res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        metaFile = chooser.getSelectedFile();

        statusLabel.setText("Loaded: " + dataFile.getName() + " + " + metaFile.getName());
    }

    private void onOpenViewer(ActionEvent e) {
        if (dataFile == null || metaFile == null) {
            JOptionPane.showMessageDialog(this, "No heap files loaded. Use Load HeapFile first.", "No files", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HeapDumpDialog dialog = new HeapDumpDialog(this, dataFile, metaFile);
        dialog.setVisible(true);
    }
}

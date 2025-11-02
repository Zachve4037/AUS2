package who.gui;

import who.DBSystem.Database;
import who.wrappers.PCRT_DateTime;
import who.baseClasses.Person;
import who.baseClasses.PCRTest;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class DatabaseGUI extends JFrame {
    private final Database db_;
    private JTextArea outputArea;

    public DatabaseGUI() {
        this.db_ = Database.getInstance();
        setTitle("WHO Database System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Tabs ---
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.add("Osoby", createPersonPanel());
        tabPane.add("Testy", createTestPanel());
        tabPane.add("Výpisy", createQueryPanel());

        // --- Výstupná plocha ---
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Ensure sensible minimum sizes so input panels stay visible
        tabPane.setMinimumSize(new Dimension(300, 200));
        scrollPane.setMinimumSize(new Dimension(300, 100));
        scrollPane.setPreferredSize(new Dimension(900, 180));

        // Use a vertical split pane so tabs (top) are not covered by output (bottom)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabPane, scrollPane);
        splitPane.setResizeWeight(0.75); // give more space to the tabs area
        splitPane.setDividerSize(6);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    // ------------------- OSOBY -------------------
    private JPanel createPersonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        JPanel addPanel = new JPanel(new FlowLayout());
        JPanel deletePanel = new JPanel(new FlowLayout());

        JTextField personID = new JTextField(10);
        JTextField name = new JTextField(10);
        JTextField surname = new JTextField(10);
        JTextField birthDate = new JTextField(10); // yyyy-MM-dd
        JButton addBtn = new JButton("Pridať osobu");

        addPanel.add(new JLabel("Rodné číslo:"));
        addPanel.add(personID);
        addPanel.add(new JLabel("Meno:"));
        addPanel.add(name);
        addPanel.add(new JLabel("Priezvisko:"));
        addPanel.add(surname);
        addPanel.add(new JLabel("Birthdate (yyyy-MM-dd):"));
        addPanel.add(birthDate);
        addPanel.add(addBtn);

        addBtn.addActionListener(e -> {
            String id = personID.getText().trim();
            if (id.isEmpty()) {
                output("Person ID is required");
                return;
            }
            Date bd = null;
            String bdStr = birthDate.getText().trim();
            if (!bdStr.isEmpty()) {
                try {
                    LocalDate ld = LocalDate.parse(bdStr);
                    bd = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
                } catch (DateTimeParseException ex) {
                    output("Invalid birthdate format, expected yyyy-MM-dd");
                    return;
                }
            }
            Person p = new Person(id, name.getText().trim(), surname.getText().trim(), bd);
            Person res = this.db_.insertPerson(p);
            if (res != null) {
                output("Inserted person: " + id);
            } else {
                output("Person insertion failed (maybe duplicate)");
            }
        });

        JTextField deleteID = new JTextField(10);
        JButton deleteBtn = new JButton("Vymazať osobu");
        deletePanel.add(new JLabel("Rodné číslo:"));
        deletePanel.add(deleteID);
        deletePanel.add(deleteBtn);

        deleteBtn.addActionListener(e -> {
            String id = deleteID.getText().trim();
            if (id.isEmpty()) {
                output("Person ID required for deletion");
                return;
            }
            this.db_.deletePerson(id);
            output("Requested deletion of person: " + id);
        });

        panel.add(addPanel);
        panel.add(deletePanel);
        return panel;
    }

    // ------------------- TESTY -------------------
    private JPanel createTestPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));

        // Add test
        JPanel addPanel = new JPanel(new FlowLayout());
        JTextField personID = new JTextField(10);
        JTextField timeField = new JTextField(16);
        JTextField workplaceField = new JTextField(6);
        JTextField regionField = new JTextField(6);
        JTextField districtField = new JTextField(6);
        JCheckBox resultBox = new JCheckBox("Positive");
        JTextField valueField = new JTextField(6);
        JTextField noteField = new JTextField(12);
        JButton addBtn = new JButton("Pridať test");

        addPanel.add(new JLabel("Person ID:"));
        addPanel.add(personID);
        addPanel.add(new JLabel("Time (ISO, empty = now):"));
        addPanel.add(timeField);
        addPanel.add(new JLabel("Workplace ID:"));
        addPanel.add(workplaceField);
        addPanel.add(new JLabel("Region ID:"));
        addPanel.add(regionField);
        addPanel.add(new JLabel("District ID:"));
        addPanel.add(districtField);
        addPanel.add(resultBox);
        addPanel.add(new JLabel("Value:"));
        addPanel.add(valueField);
        addPanel.add(new JLabel("Note:"));
        addPanel.add(noteField);
        addPanel.add(addBtn);

        addBtn.addActionListener(e -> {
            String pid = personID.getText().trim();
            LocalDateTime time;
            if (timeField.getText().trim().isEmpty()) {
                time = LocalDateTime.now();
            } else {
                try {
                    time = LocalDateTime.parse(timeField.getText().trim());
                } catch (DateTimeParseException ex) {
                    output("Invalid time format, using now");
                    time = LocalDateTime.now();
                }
            }
            int wp = parseIntSafe(workplaceField.getText().trim(), 0);
            int reg = parseIntSafe(regionField.getText().trim(), 0);
            int dis = parseIntSafe(districtField.getText().trim(), 0);
            boolean res = resultBox.isSelected();
            double val = parseDoubleSafe(valueField.getText().trim(), 0.0);
            String note = noteField.getText().trim();

            PCRTest test = new PCRTest(time, pid, 0, wp, reg, dis, res, val, note);
            PCRTest inserted = this.db_.insertTest(test);
            if (inserted != null) {
                output("Inserted test: " + inserted.getPcrID());
            } else {
                output("Test insertion failed (person may not exist)");
            }
        });

        // Delete test
        JPanel deletePanel = new JPanel(new FlowLayout());
        JTextField testIDField = new JTextField(8);
        JButton deleteBtn = new JButton("Vymazať test");
        deletePanel.add(new JLabel("Test ID:"));
        deletePanel.add(testIDField);
        deletePanel.add(deleteBtn);

        deleteBtn.addActionListener(e -> {
            int id = parseIntSafe(testIDField.getText().trim(), -1);
            if (id <= 0) {
                output("Invalid test ID");
                return;
            }
            this.db_.deleteTest(id);
            output("Requested deletion of test: " + id);
        });

        // Quick actions: clear output
        JPanel miscPanel = new JPanel(new FlowLayout());
        JButton clearBtn = new JButton("Clear Output");
        miscPanel.add(clearBtn);
        clearBtn.addActionListener(e -> outputArea.setText(""));

        panel.add(addPanel);
        panel.add(deletePanel);
        panel.add(miscPanel);
        return panel;
    }

    // ------------------- VÝPISY -------------------
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));

        // Query by person
        JPanel byPerson = new JPanel(new FlowLayout());
        JTextField personQueryField = new JTextField(12);
        JButton showPersonTests = new JButton("Show tests for person");
        byPerson.add(new JLabel("Person ID:"));
        byPerson.add(personQueryField);
        byPerson.add(showPersonTests);

        showPersonTests.addActionListener(e -> {
            String id = personQueryField.getText().trim();
            if (id.isEmpty()) {
                output("Person ID required");
                return;
            }
            List<PCRT_DateTime> tests = this.db_.getAllTestsForPerson(id);
            if (tests == null || tests.isEmpty()) {
                output("No tests found for person: " + id);
                return;
            }
            output("Tests for person " + id + ":");
            for (PCRT_DateTime w : tests) {
                output(formatTest(w.getTest()));
            }
        });

        // Query by test ID
        JPanel byTest = new JPanel(new FlowLayout());
        JTextField testQueryField = new JTextField(8);
        JButton showTest = new JButton("Show test by ID");
        byTest.add(new JLabel("Test ID:"));
        byTest.add(testQueryField);
        byTest.add(showTest);

        showTest.addActionListener(e -> {
            int id = parseIntSafe(testQueryField.getText().trim(), -1);
            if (id <= 0) {
                output("Invalid test ID");
                return;
            }
            PCRTest t = this.db_.getPCRTestByID(id);
            if (t == null) {
                output("Test not found: " + id);
            } else {
                output(formatTest(t));
            }
        });

        // Export / Import
        JPanel ioPanel = new JPanel(new FlowLayout());
        JButton exportBtn = new JButton("Export to files");
        JButton importBtn = new JButton("Import from files");
        ioPanel.add(exportBtn);
        ioPanel.add(importBtn);

        exportBtn.addActionListener(e -> {
            File peopleFile = chooseFile("Choose people export file (will overwrite)", true);
            if (peopleFile == null) return;
            File testsFile = chooseFile("Choose tests export file (will overwrite)", true);
            if (testsFile == null) return;
            try {
                this.db_.exportToFiles(peopleFile.getAbsolutePath(), testsFile.getAbsolutePath());
                output("Exported to files");
            } catch (Exception ex) {
                output("Export failed: " + ex.getMessage());
            }
        });

        importBtn.addActionListener(e -> {
            File peopleFile = chooseFile("Choose people import file", false);
            if (peopleFile == null) return;
            File testsFile = chooseFile("Choose tests import file", false);
            if (testsFile == null) return;
            try {
                this.db_.importFromFiles(peopleFile.getAbsolutePath(), testsFile.getAbsolutePath());
                output("Imported from files");
            } catch (Exception ex) {
                output("Import failed: " + ex.getMessage());
            }
        });

        panel.add(byPerson);
        panel.add(byTest);
        panel.add(ioPanel);
        return panel;
    }

    // ------------------- Pomocné -------------------
    private void output(String msg) {
        outputArea.append(msg + "\n");
    }

    private int parseIntSafe(String s, int def) {
        if (s == null || s.isEmpty()) return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            output("Invalid number: " + s);
            return def;
        }
    }

    private double parseDoubleSafe(String s, double def) {
        if (s == null || s.isEmpty()) return def;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            output("Invalid number: " + s);
            return def;
        }
    }

    private String formatTest(PCRTest t) {
        if (t == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("TestID=").append(t.getPcrID())
                .append(" PersonID=").append(t.getPersonID())
                .append(" Time=").append(t.getTimeOfTest())
                .append(" Result=").append(t.getTestResult() ? "Positive" : "Negative")
                .append(" Value=").append(t.getTestValue())
                .append(" Note=").append(t.getNote());
        Person p = t.getPerson();
        if (p != null) {
            sb.append(" [Person: ID=").append(p.getID())
                    .append(" Name=").append(p.getName())
                    .append(" Surname=").append(p.getSurname())
                    .append(" Birth=").append(p.getBirthDate())
                    .append("]");
        }
        return sb.toString();
    }

    private File chooseFile(String title, boolean forSave) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        int res = forSave ? chooser.showSaveDialog(this) : chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
}
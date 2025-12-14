package S2.db;

import S2.baseClasses.PCRTest;
import S2.baseClasses.PersonBinary;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

public class DBViewer extends JFrame {
    private DB database;
    private JTextArea resultArea;
    private String currentDataDirectory;
    private int peopleBucketSizeMain;
    private int peopleBucketSizeOverflow;
    private int testBucketSizeMain;
    private int testBucketSizeOverflow;

    public DBViewer() {
        setTitle("Database Viewer");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Operations", createOperationsPanel());
        tabbedPane.addTab("View Files", createViewFilesPanel());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tabbedPane, new JScrollPane(resultArea));
        splitPane.setDividerLocation(500);

        add(splitPane);
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton initBtn = new JButton("Initialize DB");
        JButton closeBtn = new JButton("Close DB");
        JButton fillBtn = new JButton("Fill Database");
        JButton insertPersonBtn = new JButton("Insert Person");
        JButton insertTestBtn = new JButton("Insert Test");
        JButton findPersonBtn = new JButton("Find Person");
        JButton findTestBtn = new JButton("Find Test");
        JButton updatePersonBtn = new JButton("Update Person");
        JButton updateTestBtn = new JButton("Update Test");

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(initBtn, gbc);

        gbc.gridy = 1;
        panel.add(closeBtn, gbc);

        gbc.gridy = 2;
        panel.add(fillBtn, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(insertPersonBtn, gbc);
        gbc.gridx = 1;
        panel.add(insertTestBtn, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(findPersonBtn, gbc);
        gbc.gridx = 1;
        panel.add(findTestBtn, gbc);

        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(updatePersonBtn, gbc);
        gbc.gridx = 1;
        panel.add(updateTestBtn, gbc);

        initBtn.addActionListener(e -> initializeDatabase());
        closeBtn.addActionListener(e -> closeDatabase());
        fillBtn.addActionListener(e -> fillDatabase());
        insertPersonBtn.addActionListener(e -> insertPerson());
        insertTestBtn.addActionListener(e -> insertTest());
        findPersonBtn.addActionListener(e -> findPerson());
        findTestBtn.addActionListener(e -> findTest());
        updatePersonBtn.addActionListener(e -> updatePerson());
        updateTestBtn.addActionListener(e -> updateTest());

        return panel;
    }

    private JPanel createViewFilesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton viewPeopleMainBtn = new JButton("View People Main");
        JButton viewPeopleOverflowBtn = new JButton("View People Overflow");
        JButton viewTestsMainBtn = new JButton("View Tests Main");
        JButton viewTestsOverflowBtn = new JButton("View Tests Overflow");

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(viewPeopleMainBtn, gbc);
        gbc.gridy = 1;
        panel.add(viewPeopleOverflowBtn, gbc);
        gbc.gridy = 2;
        panel.add(viewTestsMainBtn, gbc);
        gbc.gridy = 3;
        panel.add(viewTestsOverflowBtn, gbc);

        viewPeopleMainBtn.addActionListener(e -> viewHeapFile("people_main"));
        viewPeopleOverflowBtn.addActionListener(e -> viewHeapFile("people_overflow"));
        viewTestsMainBtn.addActionListener(e -> viewHeapFile("tests_main"));
        viewTestsOverflowBtn.addActionListener(e -> viewHeapFile("tests_overflow"));

        return panel;
    }

    private void initializeDatabase() {
        JPanel configPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        JTextField peopleMainField = new JTextField("1922");
        JTextField peopleOverflowField = new JTextField("1922");
        JTextField testsMainField = new JTextField("276");
        JTextField testsOverflowField = new JTextField("276");
        JTextField directoryField = new JTextField("data");

        configPanel.add(new JLabel("People Main Block Size (bytes):"));
        configPanel.add(peopleMainField);
        configPanel.add(new JLabel("People Overflow Block Size (bytes):"));
        configPanel.add(peopleOverflowField);
        configPanel.add(new JLabel("Test Main Block Size (bytes):"));
        configPanel.add(testsMainField);
        configPanel.add(new JLabel("Test Overflow Block Size (bytes):"));
        configPanel.add(testsOverflowField);
        configPanel.add(new JLabel("Data Directory:"));
        configPanel.add(directoryField);

        int result = JOptionPane.showConfirmDialog(this, configPanel,
                "Database Configuration", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                peopleBucketSizeMain = Integer.parseInt(peopleMainField.getText());
                peopleBucketSizeOverflow = Integer.parseInt(peopleOverflowField.getText());
                testBucketSizeMain = Integer.parseInt(testsMainField.getText());
                testBucketSizeOverflow = Integer.parseInt(testsOverflowField.getText());
                currentDataDirectory = directoryField.getText();

                if (peopleBucketSizeMain <= 0 || peopleBucketSizeOverflow <= 0 ||
                        testBucketSizeMain <= 0 || testBucketSizeOverflow <= 0) {
                    showResult("Error: Block sizes must be positive integers");
                    return;
                }

                database = new DB(currentDataDirectory, peopleBucketSizeMain, peopleBucketSizeOverflow,
                        testBucketSizeMain, testBucketSizeOverflow);

                showResult("Database initialized successfully!\n" +
                        "Directory: " + currentDataDirectory + "\n" +
                        "People Main: " + peopleBucketSizeMain + " bytes long block\n" +
                        "People Overflow: " + peopleBucketSizeOverflow + " bytes long block\n" +
                        "Tests Main: " + testBucketSizeMain + " bytes long block\n" +
                        "Tests Overflow: " + testBucketSizeOverflow + " bytes long block");

            } catch (NumberFormatException ex) {
                showResult("Error: Invalid number format");
            } catch (Exception ex) {
                showResult("Error initializing database: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void closeDatabase() {
        if (database == null) {
            showResult("No database is currently open");
            return;
        }
        database = null;
        currentDataDirectory = null;
        showResult("Database closed successfully");
    }

    private void fillDatabase() {
        if (database == null) {
            showResult("Database not initialized. Click 'Initialize DB' first.");
            return;
        }

        JPanel fillPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField personCountField = new JTextField("50");
        JTextField testsPerPersonField = new JTextField("3");

        fillPanel.add(new JLabel("Number of People:"));
        fillPanel.add(personCountField);
        fillPanel.add(new JLabel("Tests per Person (max 6):"));
        fillPanel.add(testsPerPersonField);

        int result = JOptionPane.showConfirmDialog(this, fillPanel,
                "Fill Database", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int personCount = Integer.parseInt(personCountField.getText());
                int testsPerPerson = Math.min(6, Integer.parseInt(testsPerPersonField.getText()));

                Random rng = new Random();
                int peopleInserted = 0;
                int testsInserted = 0;

                for (int i = 0; i < personCount; i++) {
                    PersonBinary person = generateRandomPerson(rng);
                    database.insertPerson(person);
                    peopleInserted++;

                    for (int j = 0; j < testsPerPerson; j++) {
                        PCRTest test = generateRandomTest(rng, person.getId());
                        database.insertTest(test);
                        testsInserted++;
                    }
                }

                showResult("Database filled successfully!\n" +
                        "Inserted " + peopleInserted + " people\n" +
                        "Inserted " + testsInserted + " tests");

            } catch (NumberFormatException ex) {
                showResult("Error: Invalid number format");
            } catch (Exception ex) {
                showResult("Error filling database: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private PersonBinary generateRandomPerson(Random rng) {
        String[] firstNames = {"John", "Jane", "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};

        String name = firstNames[rng.nextInt(firstNames.length)];
        String surname = lastNames[rng.nextInt(lastNames.length)];
        Date birthDate = new Date(System.currentTimeMillis() - (long) rng.nextInt(100) * 365 * 24 * 60 * 60 * 1000L);

        return new PersonBinary(name, surname, birthDate, "");
    }

    private PCRTest generateRandomTest(Random rng, String patientId) {
        LocalDateTime testTime = LocalDateTime.now().minusDays(rng.nextInt(365));
        boolean result = rng.nextBoolean();
        double value = rng.nextDouble() * 100;
        String note = result ? "Positive" : "Negative";

        return new PCRTest(testTime, patientId, 0, result, value, note);
    }

    private void insertPerson() {
        if (database == null) {
            showResult("Database not initialized.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField surnameField = new JTextField();
        JTextField birthField = new JTextField("2000-01-01");

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Surname:"));
        panel.add(surnameField);
        panel.add(new JLabel("Birth Date (yyyy-MM-dd):"));
        panel.add(birthField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Insert Person",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                Date birthDate = sdf.parse(birthField.getText());
                PersonBinary person = new PersonBinary(nameField.getText(), surnameField.getText(), birthDate, "");
                database.insertPerson(person);
                showResult("Person inserted: " + person.toString());
            } catch (Exception ex) {
                showResult("Error: " + ex.getMessage());
            }
        }
    }

    private void insertTest() {
        if (database == null) {
            showResult("Database not initialized.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField patientIdField = new JTextField();
        JTextField resultField = new JTextField("true");
        JTextField valueField = new JTextField("50.0");
        JTextField noteField = new JTextField("Test note");

        panel.add(new JLabel("Patient ID:"));
        panel.add(patientIdField);
        panel.add(new JLabel("Result (true/false):"));
        panel.add(resultField);
        panel.add(new JLabel("Value:"));
        panel.add(valueField);
        panel.add(new JLabel("Note:"));
        panel.add(noteField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Insert Test",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                PCRTest test = new PCRTest(LocalDateTime.now(), patientIdField.getText(), 0,
                        Boolean.parseBoolean(resultField.getText()),
                        Double.parseDouble(valueField.getText()),
                        noteField.getText());
                database.insertTest(test);
                showResult("Test inserted: " + test.toString());
            } catch (Exception ex) {
                showResult("Error: " + ex.getMessage());
            }
        }
    }

    private void findPerson() {
        if (database == null) {
            showResult("Database not initialized.");
            return;
        }

        String id = JOptionPane.showInputDialog(this, "Enter Person ID:");
        if (id != null && !id.trim().isEmpty()) {
            try {
                PersonBinary person = database.findPersonById(id);
                if (person != null) {
                    showResult("Person found:\n" + person.toString());
                } else {
                    showResult("Person not found with ID: " + id);
                }
            } catch (Exception ex) {
                showResult("Error: " + ex.getMessage());
            }
        }
    }

    private void findTest() {
        if (database == null) {
            showResult("Database not initialized.");
            return;
        }

        String idStr = JOptionPane.showInputDialog(this, "Enter Test ID:");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int testId = Integer.parseInt(idStr);
                PCRTest test = database.findTestById(testId);
                if (test != null) {
                    showResult("Test found:\n" + test.toString());
                } else {
                    showResult("Test not found with ID: " + testId);
                }
            } catch (NumberFormatException ex) {
                showResult("Error: Invalid test ID format");
            } catch (Exception ex) {
                showResult("Error: " + ex.getMessage());
            }
        }
    }

    private void updatePerson() {
        if (database == null) {
            showResult("Database not initialized.");
            return;
        }

        String id = JOptionPane.showInputDialog(this, "Enter Person ID to update:");
        if (id != null && !id.trim().isEmpty()) {
            try {
                PersonBinary existing = database.findPersonById(id);
                if (existing == null) {
                    showResult("Person not found with ID: " + id);
                    return;
                }

                JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

                // Extract current values
                String currentData = existing.toString();
                String[] parts = currentData.split(", ");
                String currentName = parts.length > 0 ? parts[0].split(": ")[1] : "";
                String currentSurname = parts.length > 1 ? parts[1].split(": ")[1] : "";
                String currentBirth = parts.length > 2 ? parts[2].split(": ")[1] : "2000-01-01";

                JTextField nameField = new JTextField(currentName, 15);
                JTextField surnameField = new JTextField(currentSurname, 15);
                JTextField birthField = new JTextField(currentBirth, 15);

                panel.add(new JLabel("Name:"));
                panel.add(nameField);
                panel.add(new JLabel("Surname:"));
                panel.add(surnameField);
                panel.add(new JLabel("Birth Date (yyyy-MM-dd):"));
                panel.add(birthField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Person",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    Date birthDate = sdf.parse(birthField.getText());
                    PersonBinary updated = new PersonBinary(nameField.getText(), surnameField.getText(), birthDate, id);
                    database.updatePerson(id, updated);
                    showResult("Person updated successfully:\n" + updated.toString());
                }
            } catch (Exception ex) {
                showResult("Error: " + ex.getMessage());
            }
        }
    }

    private void updateTest() {
        if (database == null) {
            showResult("Database not initialized.");
            return;
        }

        String idStr = JOptionPane.showInputDialog(this, "Enter Test ID to update:");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int testId = Integer.parseInt(idStr);
                PCRTest existing = database.findTestById(testId);
                if (existing == null) {
                    showResult("Test not found with ID: " + testId);
                    return;
                }

                JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

                // Extract current values (adjust based on your PCRTest.toString() format)
                String currentData = existing.toString();

                JTextField timeField = new JTextField("2024-01-01T12:00", 15);
                JComboBox<String> resultField = new JComboBox<>(new String[]{"true", "false"});
                JTextField valueField = new JTextField("0.0", 10);
                JTextField noteField = new JTextField("", 20);

                panel.add(new JLabel("Test Time (yyyy-MM-ddTHH:mm):"));
                panel.add(timeField);
                panel.add(new JLabel("Result:"));
                panel.add(resultField);
                panel.add(new JLabel("Value:"));
                panel.add(valueField);
                panel.add(new JLabel("Note:"));
                panel.add(noteField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Test",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    LocalDateTime testTime = LocalDateTime.parse(timeField.getText());
                    PCRTest updated = new PCRTest(
                            testTime,
                            existing.getpatientId(),
                            testId,
                            Boolean.parseBoolean((String) resultField.getSelectedItem()),
                            Double.parseDouble(valueField.getText()),
                            noteField.getText()
                    );
                    database.updateTest(testId, updated);
                    showResult("Test updated successfully:\n" + updated.toString());
                }
            } catch (NumberFormatException ex) {
                showResult("Error: Invalid test ID format");
            } catch (Exception ex) {
                showResult("Error: " + ex.getMessage());
            }
        }
    }


    private void viewHeapFile(String fileName) {
        if (currentDataDirectory == null) {
            showResult("No database directory set");
            return;
        }

        File dataFile = new File(currentDataDirectory, fileName + ".dat");
        File metaFile = new File(currentDataDirectory, fileName + ".meta");

        if (!dataFile.exists() || !metaFile.exists()) {
            showResult("File not found: " + fileName);
            return;
        }

        HeapDumpDialog dialog = new HeapDumpDialog(this, dataFile, metaFile);
        dialog.setVisible(true);
    }

    private void showResult(String message) {
        resultArea.setText(message);
    }
}

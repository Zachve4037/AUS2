package who.gui;

import who.DBSystem.DBServicesANDInterface.DatabaseServiceLayer;
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
import java.util.function.Function;

public class DatabaseGUI extends JFrame {
    private final DatabaseServiceLayer db_;
    private JTextArea outputArea;

    public DatabaseGUI() {
        this.db_ = DatabaseServiceLayer.getInstance();
        setTitle("WHO Database System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabPane = new JTabbedPane();
        tabPane.add("Osoby", createPersonPanel());
        tabPane.add("Testy", createTestPanel());

        JPanel queryPanel = createQueryPanel();
        queryPanel.setPreferredSize(new Dimension(900, 900));
        JScrollPane queryScroll = new JScrollPane(queryPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tabPane.add("Výpisy", queryScroll);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        tabPane.setMinimumSize(new Dimension(300, 200));
        scrollPane.setMinimumSize(new Dimension(300, 100));
        scrollPane.setPreferredSize(new Dimension(900, 220));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabPane, scrollPane);
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(6);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    // OSOBY
    private JPanel createPersonPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        JPanel addPanel = new JPanel(new FlowLayout());
        JPanel deletePanel = new JPanel(new FlowLayout());
        JPanel ioPanel = new JPanel(new FlowLayout());

        //19
        JTextField personID = new JTextField(10);
        JTextField name = new JTextField(10);
        JTextField surname = new JTextField(10);
        JTextField birthDate = new JTextField(10);
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
                    if (bdStr.contains("-")) {
                        LocalDate d = LocalDate.parse(bdStr);
                        bd = Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    } else {
                        long millis = Long.parseLong(bdStr);
                        bd = new Date(millis);
                    }
                } catch (DateTimeParseException | NumberFormatException ex) {
                    output("Invalid birthdate format");
                    return;
                }
            }
            Person p = new Person(id, name.getText().trim(), surname.getText().trim(), bd);
            Person res = this.db_.addPerson(p);
            if (res != null) {
                output("Added person: " + res.getID() + " " + res.getName() + " " + res.getSurname());
            } else {
                output("Add person failed");
            }
        });

        //21
        JTextField deleteID = new JTextField(10);
        JButton deleteBtn = new JButton("Vymazať osobu");
        deletePanel.add(new JLabel("Rodné číslo:"));
        deletePanel.add(deleteID);
        deletePanel.add(deleteBtn);

        deleteBtn.addActionListener(e -> {
            String id = deleteID.getText().trim();
            if (id.isEmpty()) {
                output("Person ID required");
                return;
            }
            this.db_.deletePerson(id);
            output("Requested deletion of person: " + id);
        });

        JButton exportAllBtn = new JButton("Export All (people+tests)");
        JButton importAllBtn = new JButton("Import All (people+tests)");
        ioPanel.add(exportAllBtn);
        ioPanel.add(importAllBtn);

        exportAllBtn.addActionListener(e -> {
            File peopleFile = chooseFile("Export people to file", true);
            if (peopleFile == null) { output("Export cancelled"); return; }

            File testsFile = chooseFile("Export tests to file", true);
            if (testsFile == null) { output("Export cancelled"); return; }

            try {
                this.db_.exportToFile(peopleFile.getPath(), testsFile.getPath());
                output("Exported people to: " + peopleFile.getPath());
                output("Exported tests to: " + testsFile.getPath());
            } catch (Exception ex) {
                output("Export failed: " + ex.getMessage());
            }
        });

        importAllBtn.addActionListener(e -> {
            File peopleFile = chooseFile("Import people from file", false);
            if (peopleFile == null) { output("Import cancelled"); return; }

            File testsFile = chooseFile("Import tests from file", false);
            if (testsFile == null) { output("Import cancelled"); return; }

            try {
                this.db_.importFromFile(peopleFile.getPath(), testsFile.getPath());
                output("Imported people from: " + peopleFile.getPath());
                output("Imported tests from: " + testsFile.getPath());
            } catch (Exception ex) {
                output("Import failed: " + ex.getMessage());
            }
        });

        JButton genDataBtn = new JButton("Generate Data");
        ioPanel.add(genDataBtn);
        genDataBtn.addActionListener(e -> {
            try {
                this.db_.generateTestData();
                output("Generated people and tests");
            } catch (Exception ex) {
                output("Generate failed: " + ex.getMessage());
            }
        });

        panel.add(addPanel);
        panel.add(deletePanel);
        panel.add(ioPanel);
        return panel;
    }


    // TESTY
    private JPanel createTestPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));

        //1
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
            if (pid.isEmpty()) { output("Person ID is required"); return; }
            String wpStr = workplaceField.getText().trim();
            if (wpStr.isEmpty()) { output("Workplace ID is required"); return; }
            String regStr = regionField.getText().trim();
            if (regStr.isEmpty()) { output("Region ID is required"); return; }
            String disStr = districtField.getText().trim();
            if (disStr.isEmpty()) { output("District ID is required"); return; }
            String valStr = valueField.getText().trim();
            if (valStr.isEmpty()) { output("Value is required"); return; }

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

            int wp, reg, dis;
            double val;
            try { wp = Integer.parseInt(wpStr); } catch (NumberFormatException ex) { output("Invalid Workplace ID: " + wpStr); return; }
            try { reg = Integer.parseInt(regStr); } catch (NumberFormatException ex) { output("Invalid Region ID: " + regStr); return; }
            try { dis = Integer.parseInt(disStr); } catch (NumberFormatException ex) { output("Invalid District ID: " + disStr); return; }
            try { val = Double.parseDouble(valStr); } catch (NumberFormatException ex) { output("Invalid Value: " + valStr); return; }

            boolean res = resultBox.isSelected();
            String note = noteField.getText().trim();

            PCRTest test = new PCRTest(time, pid, 0, wp, reg, dis, res, val, note);
            PCRTest inserted = this.db_.addTest(test);
            if (inserted != null) {
                output("Inserted test: " + inserted.getPcrID());
            } else {
                output("Test insertion failed (person may not exist or other error)");
            }
        });

        //20
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

        JPanel miscPanel = new JPanel(new FlowLayout());
        JButton clearBtn = new JButton("Clear Output");
        miscPanel.add(clearBtn);
        clearBtn.addActionListener(e -> outputArea.setText(""));

        panel.add(addPanel);
        panel.add(deletePanel);
        panel.add(miscPanel);
        return panel;
    }

    // VÝPISY
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Function<String, JPanel> createSection = (title) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.setBorder(BorderFactory.createTitledBorder(title));
            return p;
        };

        //3
        JPanel byPerson = createSection.apply("Get tests for person");
        JTextField personQueryField = new JTextField(10);
        JButton showPersonTests = new JButton("Show");
        byPerson.add(new JLabel("Person ID:"));
        byPerson.add(personQueryField);
        byPerson.add(showPersonTests);
        showPersonTests.addActionListener(e -> {
            String id = personQueryField.getText().trim();
            if (id.isEmpty()) { output("Person ID required"); return; }
            List<PCRT_DateTime> tests = this.db_.getPersonTestsForPerson(id);
            if (tests == null || tests.isEmpty()) { output("No tests found for " + id); outputCount(tests); return; }
            output("Tests for person " + id + ":");
            tests.forEach(w -> output(formatTest(w.getTest())));
            outputCount(tests);
        });

        //18
        JPanel byTest = createSection.apply("Get test by ID");
        JTextField testQueryField = new JTextField(8);
        JButton showTest = new JButton("Show");
        byTest.add(new JLabel("Test ID:"));
        byTest.add(testQueryField);
        byTest.add(showTest);
        showTest.addActionListener(e -> {
            int id = parseIntSafe(testQueryField.getText().trim(), -1);
            if (id <= 0) { output("Invalid test ID"); return; }
            PCRTest t = this.db_.getPCRTestByID(id);
            output(t == null ? "Test not found: " + id : formatTest(t));
            outputCount(t);
        });

        //2
        JPanel byPersonTest = createSection.apply("Get test by person + ID");
        JTextField personIdForTest = new JTextField(10);
        JTextField testIdForPerson = new JTextField(6);
        JButton showTestForPerson = new JButton("Show");
        byPersonTest.add(new JLabel("Person ID:"));
        byPersonTest.add(personIdForTest);
        byPersonTest.add(new JLabel("Test ID:"));
        byPersonTest.add(testIdForPerson);
        byPersonTest.add(showTestForPerson);
        showTestForPerson.addActionListener(e -> {
            String pid = personIdForTest.getText().trim();
            int tid = parseIntSafe(testIdForPerson.getText().trim(), -1);
            if (pid.isEmpty() || tid <= 0) { output("Invalid input"); return; }
            PCRTest t = this.db_.getTestByPersonByID(pid, tid);
            output(t == null ? "Not found" : formatTest(t));
            outputCount(t);
        });

        //4, 5
        JPanel intervalDistrict = createSection.apply("Tests in interval (District)");
        JTextField districtId = new JTextField(5);
        JTextField fromD = new JTextField(12);
        JTextField toD = new JTextField(12);
        JCheckBox posD = new JCheckBox("Only positive");
        JButton queryDistrict = new JButton("Run");
        intervalDistrict.add(new JLabel("District ID:"));
        intervalDistrict.add(districtId);
        intervalDistrict.add(new JLabel("From (ISO):"));
        intervalDistrict.add(fromD);
        intervalDistrict.add(new JLabel("To (ISO):"));
        intervalDistrict.add(toD);
        intervalDistrict.add(posD);
        intervalDistrict.add(queryDistrict);
        queryDistrict.addActionListener(e -> {
            int did = parseIntSafe(districtId.getText(), -1);
            LocalDateTime f = parseDateTimeOrNull(fromD.getText());
            LocalDateTime t = parseDateTimeOrNull(toD.getText());
            if (did < 0 || f == null || t == null) { output("Invalid input"); return; }
            printTestList(this.db_.getTestsInIntervalForDistrict(did, f, t, posD.isSelected()),
                    "District " + did + " tests:");
        });

        //6, 7
        JPanel intervalRegion = createSection.apply("Tests in interval (Region)");
        JTextField regionId = new JTextField(5);
        JTextField fromR = new JTextField(12);
        JTextField toR = new JTextField(12);
        JCheckBox posR = new JCheckBox("Only positive");
        JButton queryRegion = new JButton("Run");
        intervalRegion.add(new JLabel("Region ID:"));
        intervalRegion.add(regionId);
        intervalRegion.add(new JLabel("From (ISO):"));
        intervalRegion.add(fromR);
        intervalRegion.add(new JLabel("To (ISO):"));
        intervalRegion.add(toR);
        intervalRegion.add(posR);
        intervalRegion.add(queryRegion);
        queryRegion.addActionListener(e -> {
            int rid = parseIntSafe(regionId.getText(), -1);
            LocalDateTime f = parseDateTimeOrNull(fromR.getText());
            LocalDateTime t = parseDateTimeOrNull(toR.getText());
            if (rid < 0 || f == null || t == null) { output("Invalid input"); return; }
            printTestList(this.db_.getTestsInIntervalForRegion(rid, f, t, posR.isSelected()),
                    "Region " + rid + " tests:");
        });

        //17
        JPanel intervalWorkplace = createSection.apply("Tests in interval (Workplace)");
        JTextField workplaceId = new JTextField(5);
        JTextField fromW = new JTextField(12);
        JTextField toW = new JTextField(12);
        JCheckBox posW = new JCheckBox("Only positive");
        JButton queryWorkplace = new JButton("Run");
        intervalWorkplace.add(new JLabel("Workplace ID:"));
        intervalWorkplace.add(workplaceId);
        intervalWorkplace.add(new JLabel("From (ISO):"));
        intervalWorkplace.add(fromW);
        intervalWorkplace.add(new JLabel("To (ISO):"));
        intervalWorkplace.add(toW);
        intervalWorkplace.add(posW);
        intervalWorkplace.add(queryWorkplace);
        queryWorkplace.addActionListener(e -> {
            int rid = parseIntSafe(workplaceId.getText(), -1);
            LocalDateTime f = parseDateTimeOrNull(fromW.getText());
            LocalDateTime t = parseDateTimeOrNull(toW.getText());
            if (rid < 0 || f == null || t == null) { output("Invalid input"); return; }
            printTestList(this.db_.getTestsInIntervalForWorkplace(rid, f, t, posW.isSelected()),
                    "Workplace " + rid + " tests:");
        });

        //8, 9
        JPanel intervalAll = createSection.apply("Tests in interval ALL");
        JTextField fromA = new JTextField(12);
        JTextField toA = new JTextField(12);
        JCheckBox posA = new JCheckBox("Only positive");
        JButton queryAll = new JButton("Run");
        intervalAll.add(new JLabel("From:"));
        intervalAll.add(fromA);
        intervalAll.add(new JLabel("To:"));
        intervalAll.add(toA);
        intervalAll.add(posA);
        intervalAll.add(queryAll);
        queryAll.addActionListener(e -> {
            LocalDateTime f = parseDateTimeOrNull(fromA.getText());
            LocalDateTime t = parseDateTimeOrNull(toA.getText());
            printTestList(this.db_.getTestsInInterval(f, t, posA.isSelected()),
                    "All tests:");
        });

        //10, 11
        JPanel sickDistrict = createSection.apply("Sick people (District)");
        JTextField sdDistrict = new JTextField(5);
        JTextField sdDate = new JTextField(12);
        JTextField sdDays = new JTextField(4);
        JCheckBox sdSort = new JCheckBox("Sort by test value");
        JButton sdBtn = new JButton("Show");
        sickDistrict.add(new JLabel("District ID:"));
        sickDistrict.add(sdDistrict);
        sickDistrict.add(new JLabel("Date:"));
        sickDistrict.add(sdDate);
        sickDistrict.add(new JLabel("Days:"));
        sickDistrict.add(sdDays);
        sickDistrict.add(sdSort);
        sickDistrict.add(sdBtn);
        sdBtn.addActionListener(e -> {
            int did = parseIntSafe(sdDistrict.getText(), -1);
            LocalDateTime date = parseDateTimeOrNull(sdDate.getText());
            int days = parseIntSafe(sdDays.getText(), 0);
            if (did < 0 || date == null) { output("Invalid input"); return; }
            List<Person> res = this.db_.getSickPeopleFromDisctrict(did, date, days, sdSort.isSelected());
            if (res == null || res.isEmpty()) output("No sick people");
            else res.forEach(p -> output(p.getName() + " " + p.getSurname()));
            outputCount(res);
        });

        //12
        JPanel sickRegion = createSection.apply("Sick people (Region)");
        JTextField srRegion = new JTextField(5);
        JTextField srDate = new JTextField(12);
        JTextField srDays = new JTextField(4);
        JCheckBox srSort = new JCheckBox("Sort by test value");
        JButton srBtn = new JButton("Show");
        sickRegion.add(new JLabel("Region ID:"));
        sickRegion.add(srRegion);
        sickRegion.add(new JLabel("Date:"));
        sickRegion.add(srDate);
        sickRegion.add(new JLabel("Days:"));
        sickRegion.add(srDays);
        sickRegion.add(srSort);
        sickRegion.add(srBtn);
        srBtn.addActionListener(e -> {
            int rid = parseIntSafe(sdDistrict.getText(), -1);
            LocalDateTime date = parseDateTimeOrNull(sdDate.getText());
            int days = parseIntSafe(sdDays.getText(), 0);
            if (rid < 0 || date == null) { output("Invalid input"); return; }
            List<Person> res = this.db_.getSickPeopleFromRegion(rid, date, days, sdSort.isSelected());
            if (res == null || res.isEmpty()) output("No sick people");
            else res.forEach(p -> output(p.getName() + " " + p.getSurname()));
            outputCount(res);
        });

        //13
        JPanel sickAll = createSection.apply("Sick people (ALL)");
        JTextField saDate = new JTextField(12);
        JTextField saDays = new JTextField(4);
        JCheckBox saSort = new JCheckBox("Sort by test value");
        JButton saBtn = new JButton("Show");
        sickAll.add(new JLabel("Date:"));
        sickAll.add(saDate);
        sickAll.add(new JLabel("Days:"));
        sickAll.add(saDays);
        sickAll.add(saSort);
        sickAll.add(saBtn);
        saBtn.addActionListener(e -> {
            LocalDateTime date = parseDateTimeOrNull(saDate.getText());
            int days = parseIntSafe(saDays.getText(), 0);
            List<Person> res = this.db_.getSickPeopleFromAll(date, days, saSort.isSelected());
            if (res == null || res.isEmpty()) output("No sick people");
            else res.forEach(p -> output(p.getName() + " " + p.getSurname()));
            outputCount(res);
        });

        //15
        JPanel districtsSorted = createSection.apply("Districts sorted by sick people");
        JTextField dsDate = new JTextField(12);
        JTextField dsDays = new JTextField(4);
        JButton dsBtn = new JButton("Show");
        districtsSorted.add(new JLabel("Date:"));
        districtsSorted.add(dsDate);
        districtsSorted.add(new JLabel("Days:"));
        districtsSorted.add(dsDays);
        districtsSorted.add(dsBtn);
        dsBtn.addActionListener(e -> {
            LocalDateTime d = parseDateTimeOrNull(dsDate.getText());
            int days = parseIntSafe(dsDays.getText(), 0);
            List<?> res = this.db_.getDistrictsSortedBySickPeople(d, days);
            output("Districts sorted by sick people:");
            if (res == null || res.isEmpty()) output("No results");
            else res.forEach(o -> output(o.toString()));
            outputCount(res);
        });

        //16
        JPanel regionsSorted = createSection.apply("Regions sorted by sick people");
        JTextField rsDate = new JTextField(12);
        JTextField rsDays = new JTextField(4);
        JButton rsBtn = new JButton("Show");
        regionsSorted.add(new JLabel("Date:"));
        regionsSorted.add(rsDate);
        regionsSorted.add(new JLabel("Days:"));
        regionsSorted.add(rsDays);
        regionsSorted.add(rsBtn);
        rsBtn.addActionListener(e -> {
            LocalDateTime d = parseDateTimeOrNull(rsDate.getText());
            int days = parseIntSafe(rsDays.getText(), 0);
            List<?> res = this.db_.getRegionsSortedBySickPeople(d, days);
            output("Regions sorted by sick people:");
            if (res == null || res.isEmpty()) output("No results");
            else res.forEach(o -> output(o.toString()));
            outputCount(res);
        });

        //14
        JPanel highestValueSection = createSection.apply("Person(s) by highest test value");
        JTextField hvDate = new JTextField(12);
        JTextField hvDays = new JTextField(4);
        JButton hvBtn = new JButton("Show");
        highestValueSection.add(new JLabel("Date:"));
        highestValueSection.add(hvDate);
        highestValueSection.add(new JLabel("Days:"));
        highestValueSection.add(hvDays);
        highestValueSection.add(hvBtn);
        hvBtn.addActionListener(e -> {
            LocalDateTime d = parseDateTimeOrNull(hvDate.getText());
            int days = parseIntSafe(hvDays.getText(), 0);
            if (d == null) { output("Invalid date"); return; }
            List<Person> res = this.db_.getPersonByHighestTestValue(d, days);
            if (res == null || res.isEmpty()) {
                output("No results");
                outputCount(res);
                return;
            }
            output("Person(s) with highest test value:");
            for (Person p : res) {
                if (p == null) {
                    output("null person");
                    continue;
                }
                output(p.getName() + " " + p.getSurname() + " ID: " + p.getID());
            }
            outputCount(res);
        });

        panel.add(byPerson);
        panel.add(byTest);
        panel.add(byPersonTest);
        panel.add(intervalDistrict);
        panel.add(intervalRegion);
        panel.add(intervalWorkplace);
        panel.add(intervalAll);
        panel.add(sickDistrict);
        panel.add(sickRegion);
        panel.add(sickAll);
        panel.add(districtsSorted);
        panel.add(regionsSorted);
        panel.add(highestValueSection);

        return panel;
    }

    // POMOCNE
    private void printTestList(List<PCRT_DateTime> res, String header) {
        if (res == null || res.isEmpty()) {
            output("No tests found");
            outputCount(res);
            return;
        }
        output(header);
        for (PCRT_DateTime w : res) output(formatTest(w.getTest()));
        outputCount(res);
    }

    private LocalDateTime parseDateTimeOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

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

    private String formatTest(PCRTest t) {
        return t == null ? "null" : t.toString();
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

    // Row counting helpers
    private int countValid(List<?> list) {
        if (list == null) return 0;
        int c = 0;
        for (Object o : list) if (o != null) c++;
        return c;
    }

    private void outputCount(List<?> list) {
        output("Rows: " + countValid(list));
    }

    private void outputCount(Object o) {
        output("Rows: " + (o == null ? 0 : 1));
    }
}
